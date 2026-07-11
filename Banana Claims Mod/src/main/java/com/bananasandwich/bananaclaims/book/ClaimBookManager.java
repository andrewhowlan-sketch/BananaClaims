package com.bananasandwich.bananaclaims.book;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import com.bananasandwich.bananaclaims.claim.ClaimBlueMapStyle;
import com.bananasandwich.bananaclaims.claim.ClaimFlagDefinition;
import com.bananasandwich.bananaclaims.claim.ClaimMember;
import com.bananasandwich.bananaclaims.claim.ClaimMutationResult;
import com.bananasandwich.bananaclaims.claim.ClaimRole;
import com.bananasandwich.bananaclaims.claim.ClaimSubOwner;
import com.bananasandwich.bananaclaims.claim.PopupDisplayMode;
import com.bananasandwich.bananaclaims.invitation.ClaimInvitation;
import com.bananasandwich.bananaclaims.invitation.InvitationResult;
import com.bananasandwich.bananaclaims.notification.PopupRenderer;
import com.bananasandwich.bananaclaims.permission.ClaimPermission;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side written-book management interface.
 *
 * <p>The book is optional: every action remains available through commands.
 * Clickable controls either execute the same domain services as commands or
 * prefill the corresponding command when vanilla books cannot provide a text
 * field.</p>
 */
public final class ClaimBookManager {

    private static final long SESSION_DURATION_TICKS = 20L * 60L * 10L;
    private static final int MAX_LIST_ENTRIES = 7;

    private final Map<UUID, BookSession> sessions = new HashMap<>();
    private boolean registered;

    public void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    public boolean open(ServerPlayer player) {
        return open(player, null);
    }

    public boolean open(ServerPlayer player, Claim requestedClaim) {
        if (player == null) {
            return false;
        }

        List<Claim> accessibleClaims = accessibleClaims(player);
        Claim selected = selectClaim(player, requestedClaim, accessibleClaims);
        String token = createToken();
        long expiry = player.level().getServer().getTickCount() + SESSION_DURATION_TICKS;

        BookSession session = new BookSession(
                token,
                player.getUUID(),
                selected == null ? null : selected.getClaimId(),
                expiry
        );
        sessions.put(player.getUUID(), session);

        BookPacketSender.open(
                player,
                selected == null ? "Banana Claims" : selected.getName(),
                buildPages(player, session, selected, accessibleClaims)
        );
        return true;
    }

    public int handleAction(
            ServerPlayer player,
            String token,
            String action,
            String argument
    ) {
        BookSession session = sessions.get(player.getUUID());
        if (session == null
                || !session.token().equals(token)
                || session.expiryTick() < player.level().getServer().getTickCount()) {
            player.sendSystemMessage(Component.literal(
                    "That Banana Claims book session expired. Run /claim book again."
            ).withStyle(ChatFormatting.RED));
            sessions.remove(player.getUUID());
            return 0;
        }

        Claim claim = session.selectedClaimId() == null
                ? null
                : Bananaclaims.CLAIM_MANAGER
                .getClaimById(session.selectedClaimId())
                .orElse(null);

        return switch (action) {
            case "select" -> select(player, argument);
            case "preview" -> preview(player, claim);
            case "preview_stop" -> stopPreview(player, claim);
            case "toggle_flag" -> toggleFlag(player, claim, argument);
            case "cycle_popup" -> cyclePopup(player, claim);
            case "popup_enter" -> previewPopup(player, claim, true);
            case "popup_leave" -> previewPopup(player, claim, false);
            case "bluemap_preset" -> blueMapPreset(player, claim, argument);
            case "bluemap_reset" -> resetBlueMap(player, claim);
            case "accept_invite" -> acceptInvite(player, argument);
            case "deny_invite" -> denyInvite(player, argument);
            case "confirm_remove_member" -> confirmMemberAction(player, session, claim, argument, true);
            case "remove_member" -> removeMember(player, claim, argument);
            case "confirm_promote" -> confirmPromote(player, session, claim, argument);
            case "promote" -> promote(player, claim, argument);
            case "confirm_demote" -> confirmDemote(player, session, claim, argument);
            case "demote" -> demote(player, claim, argument);
            case "confirm_leave" -> confirmClaimAction(player, session, claim, "Leave claim?", "leave");
            case "leave" -> leave(player, claim);
            case "confirm_delete" -> confirmClaimAction(player, session, claim, "Delete claim?", "delete");
            case "delete" -> delete(player, claim);
            default -> 0;
        };
    }

    private int select(ServerPlayer player, String claimIdText) {
        try {
            UUID claimId = UUID.fromString(claimIdText);
            Optional<Claim> claim = Bananaclaims.CLAIM_MANAGER.getClaimById(claimId)
                    .filter(value -> value.hasAccess(player.getUUID()));
            if (claim.isEmpty()) {
                return fail(player, "You no longer have access to that claim.");
            }
            open(player, claim.get());
            return 1;
        } catch (IllegalArgumentException exception) {
            return fail(player, "Invalid claim selection.");
        }
    }

    private int preview(ServerPlayer player, Claim claim) {
        if (!allowed(player, ClaimPermission.PREVIEW) || !participates(player, claim)) {
            return fail(player, "You cannot preview that claim.");
        }
        boolean shown = Bananaclaims.DISPLAY_PREVIEW_V2_MANAGER.showClaimDisplay(player, claim);
        if (!shown) {
            return fail(player, "Unable to show that claim preview.");
        }
        player.sendSystemMessage(Component.literal("Showing preview for " + claim.getName() + "."));
        open(player, claim);
        return 1;
    }

    private int stopPreview(ServerPlayer player, Claim claim) {
        Bananaclaims.DISPLAY_PREVIEW_V2_MANAGER.stop(player.getUUID());
        open(player, claim);
        return 1;
    }

    private int toggleFlag(ServerPlayer player, Claim claim, String flagName) {
        if (!allowed(player, ClaimPermission.FLAG)
                || claim == null
                || !claim.canEditFlags(player.getUUID())) {
            return fail(player, "You cannot edit protection settings for this claim.");
        }
        Optional<ClaimFlagDefinition> definition = ClaimFlagDefinition.findCommandFlag(flagName);
        if (definition.isEmpty()) {
            return fail(player, "Unknown claim protection setting.");
        }
        ClaimFlagDefinition flag = definition.get();
        flag.setValue(claim.getFlags(), !flag.getValue(claim.getFlags()));
        Bananaclaims.CLAIM_MANAGER.markClaimUpdated(claim);
        open(player, claim);
        return 1;
    }

    private int cyclePopup(ServerPlayer player, Claim claim) {
        if (!allowed(player, ClaimPermission.POPUP)
                || claim == null
                || !claim.canEditPopup(player.getUUID())) {
            return fail(player, "You cannot edit notifications for this claim.");
        }
        PopupDisplayMode current = claim.getPopupSettings().getDisplayMode();
        PopupDisplayMode next = switch (current) {
            case ACTIONBAR -> PopupDisplayMode.TITLE;
            case TITLE -> PopupDisplayMode.CHAT;
            case CHAT -> PopupDisplayMode.ACTIONBAR;
        };
        claim.getPopupSettings().setDisplayMode(next);
        Bananaclaims.CLAIM_MANAGER.markClaimUpdated(claim);
        open(player, claim);
        return 1;
    }

    private int previewPopup(ServerPlayer player, Claim claim, boolean enter) {
        if (claim == null || !participates(player, claim)) {
            return fail(player, "You cannot preview that notification.");
        }
        if (enter) {
            PopupRenderer.showEnter(player, claim);
        } else {
            PopupRenderer.showLeave(player, claim);
        }
        open(player, claim);
        return 1;
    }

    private int blueMapPreset(ServerPlayer player, Claim claim, String preset) {
        if (!allowed(player, ClaimPermission.BLUEMAP)
                || claim == null
                || !claim.canEditAppearance(player.getUUID())) {
            return fail(player, "You cannot edit BlueMap appearance for this claim.");
        }

        String color = switch (preset.toLowerCase()) {
            case "purple" -> "#987CEF";
            case "green" -> "#4CAF50";
            case "gold" -> "#FFB300";
            case "red" -> "#E05252";
            case "cyan" -> "#36C9C6";
            default -> "#2378FF";
        };
        claim.getBlueMapStyle().setFillColor(color);
        claim.getBlueMapStyle().setLineColor(color);
        Bananaclaims.CLAIM_MANAGER.markClaimUpdated(claim);
        open(player, claim);
        return 1;
    }

    private int resetBlueMap(ServerPlayer player, Claim claim) {
        if (!allowed(player, ClaimPermission.BLUEMAP)
                || claim == null
                || !claim.canEditAppearance(player.getUUID())) {
            return fail(player, "You cannot edit BlueMap appearance for this claim.");
        }
        claim.getBlueMapStyle().reset();
        Bananaclaims.CLAIM_MANAGER.markClaimUpdated(claim);
        open(player, claim);
        return 1;
    }

    private int acceptInvite(ServerPlayer player, String invitationIdText) {
        if (!allowed(player, ClaimPermission.INVITE)) {
            return fail(player, "You cannot use claim invitations.");
        }
        Optional<ClaimInvitation> invitation = invitationById(invitationIdText)
                .filter(value -> value.inviteeUuid().equals(player.getUUID()));
        if (invitation.isEmpty()) {
            return fail(player, "That invitation is no longer available.");
        }
        Optional<Claim> claim = Bananaclaims.CLAIM_MANAGER.getClaimById(invitation.get().claimId());
        if (claim.isEmpty()) {
            return fail(player, "That claim no longer exists.");
        }
        InvitationResult result = Bananaclaims.INVITATION_MANAGER.accept(player, claim.get());
        if (result != InvitationResult.ACCEPTED) {
            return fail(player, "The invitation could not be accepted.");
        }
        open(player, claim.get());
        return 1;
    }

    private int denyInvite(ServerPlayer player, String invitationIdText) {
        Optional<ClaimInvitation> invitation = invitationById(invitationIdText)
                .filter(value -> value.inviteeUuid().equals(player.getUUID()));
        if (invitation.isEmpty()) {
            return fail(player, "That invitation is no longer available.");
        }
        Optional<Claim> claim = Bananaclaims.CLAIM_MANAGER.getClaimById(invitation.get().claimId());
        if (claim.isEmpty()) {
            return fail(player, "That claim no longer exists.");
        }
        Bananaclaims.INVITATION_MANAGER.deny(player, claim.get());
        open(player);
        return 1;
    }

    private int confirmMemberAction(
            ServerPlayer player,
            BookSession session,
            Claim claim,
            String playerUuidText,
            boolean removal
    ) {
        Optional<ClaimMember> member = memberById(claim, playerUuidText);
        if (member.isEmpty()) {
            return fail(player, "That member is no longer in the claim.");
        }
        return openConfirmation(
                player,
                session,
                removal ? "Remove member?" : "Change member?",
                member.get().getName(),
                "remove_member",
                member.get().getUuid().toString()
        );
    }

    private int removeMember(ServerPlayer player, Claim claim, String playerUuidText) {
        if (!allowed(player, ClaimPermission.MEMBER)
                || claim == null
                || !claim.canEditMembers(player.getUUID())) {
            return fail(player, "You cannot remove members from this claim.");
        }
        Optional<ClaimMember> member = memberById(claim, playerUuidText);
        if (member.isEmpty()) {
            return fail(player, "That member is no longer in the claim.");
        }
        ClaimMutationResult result = Bananaclaims.CLAIM_MANAGER.removeMember(
                claim,
                player.getUUID(),
                member.get().getUuid()
        );
        if (result != ClaimMutationResult.MEMBER_REMOVED) {
            return fail(player, "The member could not be removed.");
        }
        open(player, claim);
        return 1;
    }

    private int confirmPromote(
            ServerPlayer player,
            BookSession session,
            Claim claim,
            String playerUuidText
    ) {
        Optional<ClaimMember> member = memberById(claim, playerUuidText);
        if (member.isEmpty()) {
            return fail(player, "That member is no longer in the claim.");
        }
        return openConfirmation(
                player,
                session,
                "Promote to subowner?",
                member.get().getName(),
                "promote",
                member.get().getUuid().toString()
        );
    }

    private int promote(ServerPlayer player, Claim claim, String playerUuidText) {
        if (!allowed(player, ClaimPermission.SUBOWNER)
                || claim == null
                || !claim.canEditSubOwners(player.getUUID())) {
            return fail(player, "Only the owner can promote subowners.");
        }
        Optional<ClaimMember> member = memberById(claim, playerUuidText);
        if (member.isEmpty()) {
            return fail(player, "That member is no longer in the claim.");
        }
        ClaimMutationResult result = Bananaclaims.CLAIM_MANAGER.addSubOwner(
                claim,
                player.getUUID(),
                member.get().getUuid(),
                member.get().getName()
        );
        if (!result.isSuccess()) {
            return fail(player, "The member could not be promoted.");
        }
        open(player, claim);
        return 1;
    }

    private int confirmDemote(
            ServerPlayer player,
            BookSession session,
            Claim claim,
            String playerUuidText
    ) {
        Optional<ClaimSubOwner> subOwner = subOwnerById(claim, playerUuidText);
        if (subOwner.isEmpty()) {
            return fail(player, "That subowner is no longer in the claim.");
        }
        return openConfirmation(
                player,
                session,
                "Demote subowner?",
                subOwner.get().getName(),
                "demote",
                subOwner.get().getUuid().toString()
        );
    }

    private int demote(ServerPlayer player, Claim claim, String playerUuidText) {
        if (!allowed(player, ClaimPermission.SUBOWNER)
                || claim == null
                || !claim.canEditSubOwners(player.getUUID())) {
            return fail(player, "Only the owner can demote subowners.");
        }
        Optional<ClaimSubOwner> subOwner = subOwnerById(claim, playerUuidText);
        if (subOwner.isEmpty()) {
            return fail(player, "That subowner is no longer in the claim.");
        }
        ClaimMutationResult result = Bananaclaims.CLAIM_MANAGER.demoteSubOwner(
                claim,
                player.getUUID(),
                subOwner.get().getUuid()
        );
        if (!result.isSuccess()) {
            return fail(player, "The subowner could not be demoted.");
        }
        open(player, claim);
        return 1;
    }

    private int confirmClaimAction(
            ServerPlayer player,
            BookSession session,
            Claim claim,
            String heading,
            String action
    ) {
        if (claim == null) {
            return fail(player, "That claim no longer exists.");
        }
        return openConfirmation(player, session, heading, claim.getName(), action, "none");
    }

    private int leave(ServerPlayer player, Claim claim) {
        if (!allowed(player, ClaimPermission.LEAVE) || claim == null) {
            return fail(player, "You cannot leave that claim.");
        }
        ClaimMutationResult result = Bananaclaims.CLAIM_MANAGER.leaveClaim(claim, player.getUUID());
        if (!result.isSuccess()) {
            return fail(player, result == ClaimMutationResult.OWNER_CANNOT_LEAVE
                    ? "Owners must transfer or delete their claim."
                    : "You could not leave that claim.");
        }
        open(player);
        return 1;
    }

    private int delete(ServerPlayer player, Claim claim) {
        if (!allowed(player, ClaimPermission.DELETE)
                || claim == null
                || !claim.canDelete(player.getUUID())) {
            return fail(player, "Only the owner can delete this claim.");
        }
        if (!Bananaclaims.CLAIM_MANAGER.removeClaim(claim)) {
            return fail(player, "The claim could not be deleted.");
        }
        open(player);
        return 1;
    }

    private int openConfirmation(
            ServerPlayer player,
            BookSession session,
            String heading,
            String subject,
            String action,
            String argument
    ) {
        MutableComponent page = BookComponents.page();
        BookComponents.line(page, BookComponents.title(heading));
        BookComponents.blank(page);
        BookComponents.line(page, BookComponents.value(subject));
        BookComponents.blank(page);
        page.append(BookComponents.action(
                "[Confirm]",
                actionCommand(session, action, argument),
                ChatFormatting.RED
        ));
        page.append("  ");
        page.append(BookComponents.action(
                "[Cancel]",
                "/claim book",
                ChatFormatting.DARK_GREEN
        ));
        BookPacketSender.open(player, "Confirm Action", List.of(page));
        return 1;
    }

    private List<Component> buildPages(
            ServerPlayer player,
            BookSession session,
            Claim claim,
            List<Claim> claims
    ) {
        return List.of(
                homePage(player, session, claim, claims),
                overviewPage(player, session, claim),
                peoplePage(player, session, claim),
                protectionPage(player, session, claim),
                notificationPage(player, session, claim),
                appearancePage(player, session, claim),
                toolsPage(player, session, claim),
                ownershipPage(player, session, claim),
                invitationsPage(player, session),
                membersPage(player, session, claim),
                subOwnersPage(player, session, claim)
        );
    }

    private Component homePage(
            ServerPlayer player,
            BookSession session,
            Claim selected,
            List<Claim> claims
    ) {
        MutableComponent page = BookComponents.page();
        BookComponents.line(page, BookComponents.title("Banana Claims"));
        BookComponents.line(page, BookComponents.label("Select a claim:"));

        if (claims.isEmpty()) {
            BookComponents.line(page, BookComponents.value("You are not part of a claim."));
            BookComponents.blank(page);
            BookComponents.line(page, BookComponents.suggest(
                    "[Create Claim]",
                    "/claim create ",
                    ChatFormatting.DARK_GREEN
            ));
            BookComponents.line(page, BookComponents.action(
                    "[Set Pos 1]",
                    "/claim pos1",
                    ChatFormatting.BLUE
            ));
            BookComponents.line(page, BookComponents.action(
                    "[Set Pos 2]",
                    "/claim pos2",
                    ChatFormatting.BLUE
            ));
        } else {
            for (Claim claim : claims.stream().limit(MAX_LIST_ENTRIES).toList()) {
                String marker = selected != null && selected.getClaimId().equals(claim.getClaimId())
                        ? "> "
                        : "  ";
                BookComponents.line(page, BookComponents.action(
                        marker + claim.getName(),
                        actionCommand(session, "select", claim.getClaimId().toString()),
                        selected != null && selected.getClaimId().equals(claim.getClaimId())
                                ? ChatFormatting.DARK_PURPLE
                                : ChatFormatting.BLUE
                ));
            }
        }

        BookComponents.blank(page);
        page.append(BookComponents.pageLink("[Overview]", 2, ChatFormatting.DARK_GREEN));
        page.append(" ");
        page.append(BookComponents.pageLink("[Invites]", 9, ChatFormatting.GOLD));
        return page;
    }

    private Component overviewPage(ServerPlayer player, BookSession session, Claim claim) {
        MutableComponent page = baseClaimPage("Overview", claim);
        if (claim == null) {
            return page;
        }
        BookComponents.line(page, BookComponents.label("Owner: ")
                .copy().append(BookComponents.value(claim.getOwnerName())));
        BookComponents.line(page, BookComponents.label("Your role: ")
                .copy().append(BookComponents.value(claim.getRole(player.getUUID()).name())));
        BookComponents.line(page, BookComponents.label("Chunks: ")
                .copy().append(BookComponents.value(Integer.toString(claim.getChunks().size()))));
        String description = claim.getDescription().isBlank() ? "No description" : claim.getDescription();
        BookComponents.line(page, BookComponents.label("Description: ")
                .copy().append(BookComponents.value(trim(description, 45))));
        BookComponents.blank(page);
        page.append(BookComponents.action("[Preview]", actionCommand(session, "preview", "none"), ChatFormatting.DARK_GREEN));
        page.append(" ");
        page.append(BookComponents.action("[Stop]", actionCommand(session, "preview_stop", "none"), ChatFormatting.RED));
        BookComponents.line(page, Component.empty());
        if (claim.canManage(player.getUUID())) {
            BookComponents.line(page, BookComponents.suggest("[Rename]", "/claim rename " + claim.getName() + " ", ChatFormatting.BLUE));
            BookComponents.line(page, BookComponents.suggest("[Edit Description]", "/claim description " + claim.getName() + " ", ChatFormatting.BLUE));
        }
        navigation(page);
        return page;
    }

    private Component peoplePage(ServerPlayer player, BookSession session, Claim claim) {
        MutableComponent page = baseClaimPage("People", claim);
        if (claim == null) {
            return page;
        }
        BookComponents.line(page, BookComponents.value("Members: " + claim.getMembers().size()));
        BookComponents.line(page, BookComponents.value("Subowners: " + claim.getSubOwners().size()));
        BookComponents.line(page, BookComponents.value("Pending invites: " + Bananaclaims.INVITATION_MANAGER.getForClaim(claim.getClaimId()).size()));
        BookComponents.blank(page);
        page.append(BookComponents.pageLink("[Members]", 10, ChatFormatting.BLUE));
        page.append(" ");
        page.append(BookComponents.pageLink("[Subowners]", 11, ChatFormatting.DARK_PURPLE));
        BookComponents.line(page, Component.empty());
        page.append(BookComponents.pageLink("[My Invites]", 9, ChatFormatting.GOLD));
        if (claim.canEditMembers(player.getUUID()) && allowed(player, ClaimPermission.INVITE)) {
            BookComponents.blank(page);
            BookComponents.line(page, BookComponents.suggest(
                    "[Invite Player]",
                    "/claim invite for " + claim.getName() + " ",
                    ChatFormatting.DARK_GREEN
            ));
            BookComponents.line(page, BookComponents.suggest(
                    "[Cancel Invite]",
                    "/claim invite cancel-for " + claim.getName() + " ",
                    ChatFormatting.RED
            ));
        }
        navigation(page);
        return page;
    }

    private Component protectionPage(ServerPlayer player, BookSession session, Claim claim) {
        MutableComponent page = baseClaimPage("Protection", claim);
        if (claim == null) {
            return page;
        }
        boolean editable = claim.canEditFlags(player.getUUID()) && allowed(player, ClaimPermission.FLAG);
        for (ClaimFlagDefinition flag : ClaimFlagDefinition.values()) {
            if (!flag.isCommandAvailable()) {
                continue;
            }
            boolean enabled = flag.getValue(claim.getFlags());
            Component status = editable
                    ? BookComponents.action(
                    enabled ? "[ON]" : "[OFF]",
                    actionCommand(session, "toggle_flag", flag.getCanonicalName()),
                    enabled ? ChatFormatting.DARK_GREEN : ChatFormatting.RED
            )
                    : BookComponents.status(enabled);
            BookComponents.line(page, Component.literal(shortFlagName(flag) + ": ").append(status));
        }
        navigation(page);
        return page;
    }

    private Component notificationPage(ServerPlayer player, BookSession session, Claim claim) {
        MutableComponent page = baseClaimPage("Notifications", claim);
        if (claim == null) {
            return page;
        }
        BookComponents.line(page, BookComponents.value("Mode: " + claim.getPopupSettings().getDisplayMode().name()));
        if (claim.canEditPopup(player.getUUID()) && allowed(player, ClaimPermission.POPUP)) {
            BookComponents.line(page, BookComponents.action("[Cycle Mode]", actionCommand(session, "cycle_popup", "none"), ChatFormatting.BLUE));
            BookComponents.line(page, BookComponents.suggest("[Enter Title]", "/claim popup " + claim.getName() + " set enterTitle ", ChatFormatting.DARK_GREEN));
            BookComponents.line(page, BookComponents.suggest("[Enter Subtitle]", "/claim popup " + claim.getName() + " set enterSubtitle ", ChatFormatting.DARK_GREEN));
            BookComponents.line(page, BookComponents.suggest("[Leave Title]", "/claim popup " + claim.getName() + " set leaveTitle ", ChatFormatting.GOLD));
            BookComponents.line(page, BookComponents.suggest("[Leave Subtitle]", "/claim popup " + claim.getName() + " set leaveSubtitle ", ChatFormatting.GOLD));
            BookComponents.line(page, BookComponents.suggest("[Enter Sound]", "/claim popup " + claim.getName() + " set enterSound ", ChatFormatting.BLUE));
            BookComponents.line(page, BookComponents.suggest("[Leave Sound]", "/claim popup " + claim.getName() + " set leaveSound ", ChatFormatting.BLUE));
        }
        page.append(BookComponents.action("[Test Enter]", actionCommand(session, "popup_enter", "none"), ChatFormatting.DARK_GREEN));
        page.append(" ");
        page.append(BookComponents.action("[Test Leave]", actionCommand(session, "popup_leave", "none"), ChatFormatting.RED));
        navigation(page);
        return page;
    }

    private Component appearancePage(ServerPlayer player, BookSession session, Claim claim) {
        MutableComponent page = baseClaimPage("BlueMap Style", claim);
        if (claim == null) {
            return page;
        }
        ClaimBlueMapStyle style = claim.getBlueMapStyle();
        BookComponents.line(page, BookComponents.value("Fill: " + style.getFillColor() + " / " + style.getFillOpacity()));
        BookComponents.line(page, BookComponents.value("Line: " + style.getLineColor() + " / " + style.getLineOpacity()));
        BookComponents.line(page, BookComponents.value("Width: " + style.getLineWidth()));
        if (claim.canEditAppearance(player.getUUID()) && allowed(player, ClaimPermission.BLUEMAP)) {
            BookComponents.blank(page);
            page.append(BookComponents.action("[Blue]", actionCommand(session, "bluemap_preset", "blue"), ChatFormatting.BLUE));
            page.append(" ");
            page.append(BookComponents.action("[Purple]", actionCommand(session, "bluemap_preset", "purple"), ChatFormatting.DARK_PURPLE));
            page.append(" ");
            page.append(BookComponents.action("[Green]", actionCommand(session, "bluemap_preset", "green"), ChatFormatting.DARK_GREEN));
            BookComponents.line(page, Component.empty());
            page.append(BookComponents.action("[Gold]", actionCommand(session, "bluemap_preset", "gold"), ChatFormatting.GOLD));
            page.append(" ");
            page.append(BookComponents.action("[Red]", actionCommand(session, "bluemap_preset", "red"), ChatFormatting.RED));
            page.append(" ");
            page.append(BookComponents.action("[Cyan]", actionCommand(session, "bluemap_preset", "cyan"), ChatFormatting.AQUA));
            BookComponents.line(page, Component.empty());
            BookComponents.line(page, BookComponents.suggest("[Custom Fill]", "/claim bluemap " + claim.getName() + " fill #", ChatFormatting.BLUE));
            BookComponents.line(page, BookComponents.suggest("[Custom Line]", "/claim bluemap " + claim.getName() + " line #", ChatFormatting.BLUE));
            BookComponents.line(page, BookComponents.suggest("[Opacity]", "/claim bluemap " + claim.getName() + " fillopacity ", ChatFormatting.BLUE));
            BookComponents.line(page, BookComponents.action("[Reset]", actionCommand(session, "bluemap_reset", "none"), ChatFormatting.RED));
        }
        navigation(page);
        return page;
    }

    private Component toolsPage(ServerPlayer player, BookSession session, Claim claim) {
        MutableComponent page = baseClaimPage("Claim Tools", claim);
        BookComponents.line(page, BookComponents.action("[Set Pos 1]", "/claim pos1", ChatFormatting.BLUE));
        BookComponents.line(page, BookComponents.action("[Set Pos 2]", "/claim pos2", ChatFormatting.BLUE));
        BookComponents.line(page, BookComponents.suggest("[Create Area]", "/claim createarea ", ChatFormatting.DARK_GREEN));
        BookComponents.line(page, BookComponents.suggest("[Create Here]", "/claim create ", ChatFormatting.DARK_GREEN));
        if (claim != null && claim.canResize(player.getUUID())) {
            BookComponents.blank(page);
            BookComponents.line(page, BookComponents.action("[Expand Here]", "/claim expand " + claim.getName(), ChatFormatting.GOLD));
            BookComponents.line(page, BookComponents.action("[Shrink Here]", "/claim shrink " + claim.getName(), ChatFormatting.RED));
        }
        BookComponents.blank(page);
        BookComponents.line(page, BookComponents.action("[Preview]", actionCommand(session, "preview", "none"), ChatFormatting.DARK_PURPLE));
        navigation(page);
        return page;
    }

    private Component ownershipPage(ServerPlayer player, BookSession session, Claim claim) {
        MutableComponent page = baseClaimPage("Ownership", claim);
        if (claim == null) {
            return page;
        }
        ClaimRole role = claim.getRole(player.getUUID());
        BookComponents.line(page, BookComponents.value("Role: " + role.name()));
        if (role == ClaimRole.OWNER) {
            BookComponents.line(page, BookComponents.suggest("[Transfer]", "/claim transfer " + claim.getName() + " ", ChatFormatting.GOLD));
            BookComponents.blank(page);
            BookComponents.line(page, BookComponents.action("[Delete Claim]", actionCommand(session, "confirm_delete", "none"), ChatFormatting.RED));
        } else if (role == ClaimRole.MEMBER || role == ClaimRole.SUBOWNER) {
            BookComponents.line(page, BookComponents.action("[Leave Claim]", actionCommand(session, "confirm_leave", "none"), ChatFormatting.RED));
            if (role == ClaimRole.SUBOWNER) {
                BookComponents.line(page, BookComponents.label("First click steps down to member."));
            }
        }
        navigation(page);
        return page;
    }

    private Component invitationsPage(ServerPlayer player, BookSession session) {
        MutableComponent page = BookComponents.page();
        BookComponents.line(page, BookComponents.title("My Invitations"));
        List<ClaimInvitation> invitations = Bananaclaims.INVITATION_MANAGER.getIncoming(player.getUUID());
        if (invitations.isEmpty()) {
            BookComponents.line(page, BookComponents.value("No pending invitations."));
        } else {
            for (ClaimInvitation invitation : invitations.stream().limit(4).toList()) {
                BookComponents.line(page, BookComponents.section(invitation.claimName()));
                BookComponents.line(page, BookComponents.label("From " + invitation.inviterName()));
                page.append(BookComponents.action("[Accept]", actionCommand(session, "accept_invite", invitation.invitationId().toString()), ChatFormatting.DARK_GREEN));
                page.append(" ");
                page.append(BookComponents.action("[Deny]", actionCommand(session, "deny_invite", invitation.invitationId().toString()), ChatFormatting.RED));
                page.append("\n");
            }
        }
        BookComponents.blank(page);
        page.append(BookComponents.pageLink("[Home]", 1, ChatFormatting.BLUE));
        return page;
    }

    private Component membersPage(ServerPlayer player, BookSession session, Claim claim) {
        MutableComponent page = baseClaimPage("Members", claim);
        if (claim == null) {
            return page;
        }
        List<ClaimMember> members = claim.getMembers().stream()
                .sorted(Comparator.comparing(ClaimMember::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_LIST_ENTRIES)
                .toList();
        if (members.isEmpty()) {
            BookComponents.line(page, BookComponents.value("No members."));
        }
        for (ClaimMember member : members) {
            BookComponents.line(page, BookComponents.value(member.getName()));
            if (claim.canEditMembers(player.getUUID()) && allowed(player, ClaimPermission.MEMBER)) {
                page.append(BookComponents.action("[Remove]", actionCommand(session, "confirm_remove_member", member.getUuid().toString()), ChatFormatting.RED));
                if (claim.canEditSubOwners(player.getUUID()) && allowed(player, ClaimPermission.SUBOWNER)) {
                    page.append(" ");
                    page.append(BookComponents.action("[Promote]", actionCommand(session, "confirm_promote", member.getUuid().toString()), ChatFormatting.DARK_PURPLE));
                }
                page.append("\n");
            }
        }
        navigation(page);
        return page;
    }

    private Component subOwnersPage(ServerPlayer player, BookSession session, Claim claim) {
        MutableComponent page = baseClaimPage("Subowners", claim);
        if (claim == null) {
            return page;
        }
        List<ClaimSubOwner> subOwners = claim.getSubOwners().stream()
                .sorted(Comparator.comparing(ClaimSubOwner::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_LIST_ENTRIES)
                .toList();
        if (subOwners.isEmpty()) {
            BookComponents.line(page, BookComponents.value("No subowners."));
        }
        for (ClaimSubOwner subOwner : subOwners) {
            BookComponents.line(page, BookComponents.value(subOwner.getName()));
            if (claim.canEditSubOwners(player.getUUID()) && allowed(player, ClaimPermission.SUBOWNER)) {
                BookComponents.line(page, BookComponents.action("[Demote]", actionCommand(session, "confirm_demote", subOwner.getUuid().toString()), ChatFormatting.RED));
            }
        }
        navigation(page);
        return page;
    }

    private MutableComponent baseClaimPage(String heading, Claim claim) {
        MutableComponent page = BookComponents.page();
        BookComponents.line(page, BookComponents.title(heading));
        if (claim == null) {
            BookComponents.line(page, BookComponents.value("Select a claim on page 1."));
        } else {
            BookComponents.line(page, BookComponents.section(claim.getName()));
        }
        return page;
    }

    private void navigation(MutableComponent page) {
        BookComponents.blank(page);
        page.append(BookComponents.pageLink("[Home]", 1, ChatFormatting.BLUE));
        page.append(" ");
        page.append(BookComponents.pageLink("[People]", 3, ChatFormatting.DARK_GREEN));
        page.append(" ");
        page.append(BookComponents.pageLink("[Flags]", 4, ChatFormatting.RED));
        page.append("\n");
        page.append(BookComponents.pageLink("[Notify]", 5, ChatFormatting.GOLD));
        page.append(" ");
        page.append(BookComponents.pageLink("[Map]", 6, ChatFormatting.AQUA));
        page.append(" ");
        page.append(BookComponents.pageLink("[Tools]", 7, ChatFormatting.DARK_PURPLE));
        page.append(" ");
        page.append(BookComponents.pageLink("[Owner]", 8, ChatFormatting.DARK_RED));
    }

    private List<Claim> accessibleClaims(ServerPlayer player) {
        return Bananaclaims.CLAIM_MANAGER.getAllClaims().stream()
                .filter(claim -> claim.hasAccess(player.getUUID()))
                .sorted(Comparator.comparing(Claim::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private Claim selectClaim(ServerPlayer player, Claim requested, List<Claim> claims) {
        if (requested != null && requested.hasAccess(player.getUUID())) {
            return requested;
        }
        Optional<Claim> atPlayer = Bananaclaims.CLAIM_MANAGER.getClaimAt(
                player.level().dimension().toString(),
                player.chunkPosition().x(),
                player.chunkPosition().z()
        ).filter(claim -> claim.hasAccess(player.getUUID()));
        return atPlayer.orElseGet(() -> claims.isEmpty() ? null : claims.getFirst());
    }

    private boolean participates(ServerPlayer player, Claim claim) {
        return claim != null && claim.hasAccess(player.getUUID());
    }

    private boolean allowed(ServerPlayer player, ClaimPermission permission) {
        return Bananaclaims.PERMISSION_SERVICE.has(player, permission);
    }

    private Optional<ClaimInvitation> invitationById(String text) {
        try {
            return Bananaclaims.INVITATION_MANAGER.getById(UUID.fromString(text));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Optional<ClaimMember> memberById(Claim claim, String text) {
        if (claim == null) {
            return Optional.empty();
        }
        try {
            return claim.getMember(UUID.fromString(text));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Optional<ClaimSubOwner> subOwnerById(Claim claim, String text) {
        if (claim == null) {
            return Optional.empty();
        }
        try {
            return claim.getSubOwner(UUID.fromString(text));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String actionCommand(BookSession session, String action, String argument) {
        return "/claim book action " + session.token() + " " + action + " " + argument;
    }

    private String createToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private int fail(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
        return 0;
    }

    private void tick(MinecraftServer server) {
        if (sessions.isEmpty()) {
            return;
        }
        long tick = server.getTickCount();
        sessions.entrySet().removeIf(entry ->
                entry.getValue().expiryTick() < tick
                        || server.getPlayerList().getPlayer(entry.getKey()) == null
        );
    }

    private static String trim(String text, int max) {
        if (text == null || text.length() <= max) {
            return text == null ? "" : text;
        }
        return text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static String shortFlagName(ClaimFlagDefinition flag) {
        return switch (flag) {
            case BREAK_BLOCKS -> "Break";
            case PLACE_BLOCKS -> "Place";
            case INTERACT -> "Interact";
            case CONTAINERS -> "Containers";
            case ENTITIES -> "Entities";
            case PVP -> "PvP";
            case EXPLOSIONS -> "Explosions";
            default -> flag.getCanonicalName();
        };
    }

    private record BookSession(
            String token,
            UUID playerUuid,
            UUID selectedClaimId,
            long expiryTick
    ) {
    }
}
