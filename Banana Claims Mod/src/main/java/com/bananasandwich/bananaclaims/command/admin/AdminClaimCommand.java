package com.bananasandwich.bananaclaims.command.admin;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import com.bananasandwich.bananaclaims.claim.ClaimFlagDefinition;
import com.bananasandwich.bananaclaims.claim.ClaimMember;
import com.bananasandwich.bananaclaims.claim.ClaimMutationResult;
import com.bananasandwich.bananaclaims.claim.ClaimSubOwner;
import com.bananasandwich.bananaclaims.permission.ClaimPermission;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Permission-gated administrative claim tools.
 */
public final class AdminClaimCommand {

    private static final int CLAIMS_PER_PAGE = 8;

    private AdminClaimCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("admin")
                .requires(AdminClaimPermission::canUse)
                .executes(context ->
                        AdminClaimPermission.runIfAllowed(
                                context.getSource(),
                                ClaimPermission.ADMIN_ROOT,
                                () -> showSummary(
                                        context.getSource()
                                )
                        )
                )
                .then(Commands.literal("list")
                        .requires(source ->
                                AdminClaimPermission.canUse(
                                        source,
                                        ClaimPermission.ADMIN_LIST
                                )
                        )
                        .executes(context ->
                                listClaims(
                                        context.getSource(),
                                        1
                                )
                        )
                        .then(Commands.argument(
                                                "page",
                                                IntegerArgumentType.integer(1)
                                        )
                                        .executes(context ->
                                                listClaims(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(
                                                                context,
                                                                "page"
                                                        )
                                                )
                                        )
                        )
                )
                .then(Commands.literal("info")
                        .requires(source ->
                                AdminClaimPermission.canUse(
                                        source,
                                        ClaimPermission.ADMIN_INFO
                                )
                        )
                        .executes(context ->
                                showCurrentClaim(
                                        context.getSource()
                                )
                        )
                        .then(Commands.argument(
                                                "claim",
                                                StringArgumentType.word()
                                        )
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggest(
                                                        AdminClaimSelector.suggestions(),
                                                        builder
                                                )
                                        )
                                        .executes(context ->
                                                showClaimBySelector(
                                                        context.getSource(),
                                                        StringArgumentType.getString(
                                                                context,
                                                                "claim"
                                                        )
                                                )
                                        )
                        )
                )
                .then(Commands.literal("nearest")
                        .requires(source ->
                                AdminClaimPermission.canUse(
                                        source,
                                        ClaimPermission.ADMIN_NEAREST
                                )
                        )
                        .executes(context ->
                                showCurrentClaim(
                                        context.getSource()
                                )
                        )
                )
                .then(Commands.literal("force-transfer")
                        .requires(source ->
                                AdminClaimPermission.canUse(
                                        source,
                                        ClaimPermission.ADMIN_FORCE_TRANSFER
                                )
                        )
                        .then(Commands.argument(
                                                "claim",
                                                StringArgumentType.word()
                                        )
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggest(
                                                        AdminClaimSelector.suggestions(),
                                                        builder
                                                )
                                        )
                                        .then(Commands.argument(
                                                                "player",
                                                                StringArgumentType.word()
                                                        )
                                                        .suggests((context, builder) ->
                                                                SharedSuggestionProvider.suggest(
                                                                        context.getSource()
                                                                                .getServer()
                                                                                .getPlayerList()
                                                                                .getPlayers()
                                                                                .stream()
                                                                                .map(player ->
                                                                                        player.getName()
                                                                                                .getString()
                                                                                )
                                                                                .sorted(
                                                                                        String.CASE_INSENSITIVE_ORDER
                                                                                ),
                                                                        builder
                                                                )
                                                        )
                                                        .executes(context ->
                                                                forceTransfer(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "claim"
                                                                        ),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "player"
                                                                        )
                                                                )
                                                        )
                                        )
                        )
                )
                .then(Commands.literal("force-delete")
                        .requires(source ->
                                AdminClaimPermission.canUse(
                                        source,
                                        ClaimPermission.ADMIN_FORCE_DELETE
                                )
                        )
                        .then(Commands.argument(
                                                "claim",
                                                StringArgumentType.word()
                                        )
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggest(
                                                        AdminClaimSelector.suggestions(),
                                                        builder
                                                )
                                        )
                                        .then(Commands.literal("confirm")
                                                .executes(context ->
                                                        forceDelete(
                                                                context.getSource(),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "claim"
                                                                )
                                                        )
                                                )
                                        )
                        )
                )
                .then(Commands.literal("reload")
                        .requires(source ->
                                AdminClaimPermission.canUseAny(
                                        source,
                                        ClaimPermission.ADMIN_RELOAD_ALL,
                                        ClaimPermission.ADMIN_RELOAD_CONFIG,
                                        ClaimPermission.ADMIN_RELOAD_CLAIMS,
                                        ClaimPermission.ADMIN_RELOAD_PREVIEW
                                )
                        )
                        .executes(context ->
                                AdminClaimPermission.runIfAllowed(
                                        context.getSource(),
                                        ClaimPermission.ADMIN_RELOAD_ALL,
                                        () -> reloadAll(
                                                context.getSource()
                                        )
                                )
                        )
                        .then(Commands.literal("config")
                                .requires(source ->
                                        AdminClaimPermission.canUse(
                                                source,
                                                ClaimPermission.ADMIN_RELOAD_CONFIG
                                        )
                                )
                                .executes(context ->
                                        reloadConfig(
                                                context.getSource()
                                        )
                                )
                        )
                        .then(Commands.literal("claims")
                                .requires(source ->
                                        AdminClaimPermission.canUse(
                                                source,
                                                ClaimPermission.ADMIN_RELOAD_CLAIMS
                                        )
                                )
                                .executes(context ->
                                        reloadClaims(
                                                context.getSource()
                                        )
                                )
                        )
                        .then(Commands.literal("preview")
                                .requires(source ->
                                        AdminClaimPermission.canUse(
                                                source,
                                                ClaimPermission.ADMIN_RELOAD_PREVIEW
                                        )
                                )
                                .executes(context ->
                                        reloadPreview(
                                                context.getSource()
                                        )
                                )
                        )
                )
                .then(Commands.literal("diagnostics")
                        .requires(source ->
                                AdminClaimPermission.canUse(
                                        source,
                                        ClaimPermission.ADMIN_DIAGNOSTICS
                                )
                        )
                        .executes(context ->
                                showGlobalDiagnostics(
                                        context.getSource()
                                )
                        )
                        .then(Commands.argument(
                                                "claim",
                                                StringArgumentType.word()
                                        )
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggest(
                                                        AdminClaimSelector.suggestions(),
                                                        builder
                                                )
                                        )
                                        .executes(context ->
                                                showClaimDiagnostics(
                                                        context.getSource(),
                                                        StringArgumentType.getString(
                                                                context,
                                                                "claim"
                                                        )
                                                )
                                        )
                        )
                );
    }

    private static int showSummary(
            CommandSourceStack source
    ) {
        List<Claim> claims = sortedClaims();
        int chunkCount = claims.stream()
                .mapToInt(claim -> claim.getChunks().size())
                .sum();

        source.sendSuccess(
                () -> Component.literal(
                        "Banana Claims Administration"
                                + "\nClaims: "
                                + claims.size()
                                + " | Chunks: "
                                + chunkCount
                                + " | Revision: "
                                + Bananaclaims.CLAIM_MANAGER.getRevision()
                                + "\nCommands: list, info, nearest, force-transfer, force-delete, reload, diagnostics"
                ),
                false
        );

        return 1;
    }

    private static int listClaims(
            CommandSourceStack source,
            int requestedPage
    ) {
        List<Claim> claims = sortedClaims();

        if (claims.isEmpty()) {
            source.sendSuccess(
                    () -> Component.literal(
                            "No claims are currently loaded."
                    ),
                    false
            );

            return 1;
        }

        int pageCount = Math.max(
                1,
                (claims.size() + CLAIMS_PER_PAGE - 1)
                        / CLAIMS_PER_PAGE
        );
        int page = Math.min(requestedPage, pageCount);
        int startIndex = (page - 1) * CLAIMS_PER_PAGE;
        int endIndex = Math.min(
                startIndex + CLAIMS_PER_PAGE,
                claims.size()
        );

        StringBuilder message = new StringBuilder()
                .append("All Claims — Page ")
                .append(page)
                .append("/")
                .append(pageCount)
                .append("\n");

        for (int index = startIndex;
             index < endIndex;
             index++) {
            Claim claim = claims.get(index);

            message.append("- ")
                    .append(AdminClaimSelector.selectorFor(claim))
                    .append(" | owner=")
                    .append(claim.getOwnerName())
                    .append(" | dimension=")
                    .append(claim.getDimension())
                    .append(" | chunks=")
                    .append(claim.getChunks().size())
                    .append('\n');
        }

        message.append("Total: ")
                .append(claims.size());

        source.sendSuccess(
                () -> Component.literal(message.toString()),
                false
        );

        return 1;
    }

    private static int showCurrentClaim(
            CommandSourceStack source
    ) {
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(
                    Component.literal(
                            "This command requires a player position. Use /claim admin info <claim> from console."
                    )
            );

            return 0;
        }

        Optional<Claim> claim =
                Bananaclaims.CLAIM_MANAGER.getClaimAt(
                        player.level().dimension().toString(),
                        player.chunkPosition().x(),
                        player.chunkPosition().z()
                );

        if (claim.isEmpty()) {
            source.sendFailure(
                    Component.literal(
                            "There is no claim at your current position."
                    )
            );

            return 0;
        }

        sendClaimInformation(
                source,
                claim.get()
        );

        return 1;
    }

    private static int showClaimBySelector(
            CommandSourceStack source,
            String selector
    ) {
        Optional<Claim> claim = resolveClaim(
                source,
                selector
        );

        if (claim.isEmpty()) {
            return 0;
        }

        sendClaimInformation(
                source,
                claim.get()
        );

        return 1;
    }

    private static int forceTransfer(
            CommandSourceStack source,
            String selector,
            String playerName
    ) {
        Optional<Claim> optionalClaim = resolveClaim(
                source,
                selector
        );

        if (optionalClaim.isEmpty()) {
            return 0;
        }

        Optional<NameAndId> optionalProfile =
                source.getServer()
                        .services()
                        .nameToIdCache()
                        .get(playerName);

        if (optionalProfile.isEmpty()) {
            source.sendFailure(
                    Component.literal(
                            "Unable to resolve a player named \""
                                    + playerName
                                    + "\"."
                    )
            );

            return 0;
        }

        Claim claim = optionalClaim.get();
        NameAndId profile = optionalProfile.get();
        String previousOwnerName = claim.getOwnerName();
        UUID previousOwnerUuid = claim.getOwnerUuid();

        ClaimMutationResult result =
                Bananaclaims.CLAIM_MANAGER.forceTransferOwnership(
                        claim,
                        profile.id(),
                        profile.name()
                );

        return switch (result) {
            case OWNERSHIP_TRANSFERRED -> {
                audit(
                        source,
                        "force-transferred claim \""
                                + claim.getName()
                                + "\" ["
                                + claim.getClaimId()
                                + "] from "
                                + previousOwnerName
                                + " to "
                                + profile.name()
                );

                source.sendSuccess(
                        () -> Component.literal(
                                "Force-transferred claim \""
                                        + claim.getName()
                                        + "\" from "
                                        + previousOwnerName
                                        + " to "
                                        + profile.name()
                                        + ". The previous owner is now a member."
                        ),
                        true
                );

                notifyPlayer(
                        source,
                        profile.id(),
                        "An administrator transferred ownership of claim \""
                                + claim.getName()
                                + "\" to you."
                );

                if (previousOwnerUuid != null) {
                    notifyPlayer(
                            source,
                            previousOwnerUuid,
                            "An administrator transferred ownership of claim \""
                                    + claim.getName()
                                    + "\" to "
                                    + profile.name()
                                    + ". You remain a member."
                    );
                }

                yield 1;
            }

            case SAME_OWNER -> {
                source.sendFailure(
                        Component.literal(
                                profile.name()
                                        + " already owns this claim."
                        )
                );
                yield 0;
            }

            case DUPLICATE_OWNER_CLAIM_NAME -> {
                source.sendFailure(
                        Component.literal(
                                profile.name()
                                        + " already owns another claim named \""
                                        + claim.getName()
                                        + "\". Rename one claim before transferring it."
                        )
                );
                yield 0;
            }

            default -> {
                source.sendFailure(
                        Component.literal(
                                "Unable to force-transfer that claim. Result: "
                                        + result
                        )
                );
                yield 0;
            }
        };
    }

    private static int forceDelete(
            CommandSourceStack source,
            String selector
    ) {
        Optional<Claim> optionalClaim = resolveClaim(
                source,
                selector
        );

        if (optionalClaim.isEmpty()) {
            return 0;
        }

        Claim claim = optionalClaim.get();
        String claimName = claim.getName();
        UUID claimId = claim.getClaimId();
        String ownerName = claim.getOwnerName();
        UUID ownerUuid = claim.getOwnerUuid();
        int chunkCount = claim.getChunks().size();

        if (!Bananaclaims.CLAIM_MANAGER.removeClaim(claim)) {
            source.sendFailure(
                    Component.literal(
                            "Unable to force-delete that claim."
                    )
            );

            return 0;
        }

        audit(
                source,
                "force-deleted claim \""
                        + claimName
                        + "\" ["
                        + claimId
                        + "] owned by "
                        + ownerName
                        + " with "
                        + chunkCount
                        + " chunk(s)"
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Force-deleted claim \""
                                + claimName
                                + "\" ["
                                + claimId
                                + "] owned by "
                                + ownerName
                                + "."
                ),
                true
        );

        if (ownerUuid != null) {
            notifyPlayer(
                    source,
                    ownerUuid,
                    "An administrator deleted your claim \""
                            + claimName
                            + "\"."
            );
        }

        return 1;
    }

    private static int reloadAll(
            CommandSourceStack source
    ) {
        boolean configResult =
                Bananaclaims.CONFIG_MANAGER.reload();
        int claimsResult = reloadClaimsInternal(source);
        boolean previewResult =
                Bananaclaims.PREVIEW_V2_CONFIG_MANAGER.reload();

        if (!configResult
                || claimsResult < 0
                || !previewResult) {
            source.sendFailure(
                    Component.literal(
                            "Banana Claims reload completed with errors. Check the server log."
                    )
            );

            return 0;
        }

        audit(
                source,
                "reloaded main configuration, claims, and preview configuration"
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Reloaded Banana Claims configuration, claim data, and preview configuration successfully."
                ),
                true
        );

        return 1;
    }

    private static int reloadConfig(
            CommandSourceStack source
    ) {
        if (!Bananaclaims.CONFIG_MANAGER.reload()) {
            source.sendFailure(
                    Component.literal(
                            "Unable to reload the Banana Claims configuration. The previous settings remain active."
                    )
            );

            return 0;
        }

        audit(
                source,
                "reloaded the main Banana Claims configuration"
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Reloaded Banana Claims configuration from "
                                + Bananaclaims.CONFIG_MANAGER
                                .getConfigPath()
                                + ". New permission and protection-message settings are active immediately."
                ),
                true
        );

        return 1;
    }

    private static int reloadClaims(
            CommandSourceStack source
    ) {
        int result = reloadClaimsInternal(source);

        if (result < 0) {
            return 0;
        }

        audit(
                source,
                "reloaded claim data"
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Reloaded "
                                + result
                                + " claim(s) from disk."
                ),
                true
        );

        return 1;
    }

    private static int reloadPreview(
            CommandSourceStack source
    ) {
        if (!Bananaclaims.PREVIEW_V2_CONFIG_MANAGER.reload()) {
            source.sendFailure(
                    Component.literal(
                            "Unable to reload the preview configuration. The previous settings remain active."
                    )
            );

            return 0;
        }

        audit(
                source,
                "reloaded preview configuration"
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Reloaded preview configuration from "
                                + Bananaclaims.PREVIEW_V2_CONFIG_MANAGER
                                .getConfigPath()
                                + ". Existing previews retain their original settings; new previews use the reload."
                ),
                true
        );

        return 1;
    }

    private static int reloadClaimsInternal(
            CommandSourceStack source
    ) {
        try {
            Optional<List<Claim>> loadedClaims =
                    Bananaclaims.CLAIM_STORAGE.tryLoadClaims();

            if (loadedClaims.isEmpty()) {
                source.sendFailure(
                        Component.literal(
                                "Unable to reload claims. Existing in-memory claim data was preserved. Check the server log."
                        )
                );

                return -1;
            }

            Bananaclaims.CLAIM_MANAGER.loadClaims(
                    loadedClaims.get()
            );

            return Bananaclaims.CLAIM_MANAGER
                    .getAllClaims()
                    .size();
        } catch (RuntimeException exception) {
            Bananaclaims.LOGGER.error(
                    "An administrator failed to reload Banana Claims data.",
                    exception
            );

            source.sendFailure(
                    Component.literal(
                            "Unable to reload claims. The server log contains details."
                    )
            );

            return -1;
        }
    }

    private static int showGlobalDiagnostics(
            CommandSourceStack source
    ) {
        ClaimDiagnostics.GlobalReport report =
                ClaimDiagnostics.analyze(
                        Bananaclaims.CLAIM_MANAGER.getAllClaims()
                );

        StringBuilder message = new StringBuilder()
                .append("Banana Claims Diagnostics")
                .append("\nStatus: ")
                .append(report.healthy() ? "HEALTHY" : "WARNINGS")
                .append("\nClaims: ")
                .append(report.claimCount())
                .append(" | Owners: ")
                .append(report.uniqueOwnerCount())
                .append(" | Chunk assignments: ")
                .append(report.totalChunkAssignments())
                .append(" | Unique chunks: ")
                .append(report.uniqueChunkCount())
                .append("\nRevision: ")
                .append(Bananaclaims.CLAIM_MANAGER.getRevision())
                .append("\nClaims file: ")
                .append(Bananaclaims.CLAIM_STORAGE.getClaimsFile())
                .append("\nMain config: ")
                .append(Bananaclaims.CONFIG_MANAGER.getConfigPath())
                .append("\nPreview config: ")
                .append(Bananaclaims.PREVIEW_V2_CONFIG_MANAGER.getConfigPath())
                .append("\nPermissions API: ")
                .append(
                        Bananaclaims.PERMISSION_SERVICE
                                .isFabricPermissionsApiAvailable()
                                ? "Detected"
                                : "Vanilla fallback"
                )
                .append("\nDimensions: ")
                .append(formatDimensions(report.chunksByDimension()));

        if (report.warnings().isEmpty()) {
            message.append("\nIntegrity warnings: None");
        } else {
            message.append("\nIntegrity warnings (")
                    .append(report.warnings().size())
                    .append("):");

            report.warnings()
                    .stream()
                    .limit(10)
                    .forEach(warning ->
                            message.append("\n- ")
                                    .append(warning)
                    );

            if (report.warnings().size() > 10) {
                message.append("\n- ...and ")
                        .append(report.warnings().size() - 10)
                        .append(" more warning(s). Check individual claims.");
            }
        }

        source.sendSuccess(
                () -> Component.literal(message.toString()),
                false
        );

        return 1;
    }

    private static int showClaimDiagnostics(
            CommandSourceStack source,
            String selector
    ) {
        Optional<Claim> optionalClaim = resolveClaim(
                source,
                selector
        );

        if (optionalClaim.isEmpty()) {
            return 0;
        }

        Claim claim = optionalClaim.get();
        List<String> warnings =
                ClaimDiagnostics.analyzeClaim(claim);

        StringBuilder message = new StringBuilder()
                .append("Diagnostics for claim \"")
                .append(claim.getName())
                .append("\" [")
                .append(claim.getClaimId())
                .append("]")
                .append("\nStatus: ")
                .append(warnings.isEmpty() ? "HEALTHY" : "WARNINGS");

        if (warnings.isEmpty()) {
            message.append("\nIntegrity warnings: None");
        } else {
            for (String warning : warnings) {
                message.append("\n- ")
                        .append(warning);
            }
        }

        source.sendSuccess(
                () -> Component.literal(message.toString()),
                false
        );

        return 1;
    }

    private static void sendClaimInformation(
            CommandSourceStack source,
            Claim claim
    ) {
        List<String> subOwners = claim.getSubOwners()
                .stream()
                .map(ClaimSubOwner::getName)
                .filter(name -> name != null && !name.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        List<String> members = claim.getMembers()
                .stream()
                .map(ClaimMember::getName)
                .filter(name -> name != null && !name.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        String flags = ClaimFlagDefinition.canonicalNames()
                .stream()
                .map(name -> ClaimFlagDefinition.find(name)
                        .map(flag ->
                                name
                                        + "="
                                        + flag.getValue(claim.getFlags())
                        )
                        .orElse(name + "=?")
                )
                .reduce((left, right) -> left + ", " + right)
                .orElse("None");
        List<String> warnings =
                ClaimDiagnostics.analyzeClaim(claim);

        String message = "Claim: "
                + claim.getName()
                + "\nSelector: "
                + AdminClaimSelector.selectorFor(claim)
                + "\nClaim ID: "
                + claim.getClaimId()
                + "\nOwner: "
                + claim.getOwnerName()
                + " ["
                + claim.getOwnerUuid()
                + "]"
                + "\nDimension: "
                + claim.getDimension()
                + "\nAnchor chunk: "
                + claim.getChunkX()
                + ", "
                + claim.getChunkZ()
                + "\nChunks: "
                + claim.getChunks().size()
                + " ("
                + claim.getChunks().size() * 256L
                + " block column area)"
                + "\nSubowners ("
                + subOwners.size()
                + "): "
                + (subOwners.isEmpty() ? "None" : String.join(", ", subOwners))
                + "\nMembers ("
                + members.size()
                + "): "
                + (members.isEmpty() ? "None" : String.join(", ", members))
                + "\nFlags: "
                + flags
                + "\nIntegrity: "
                + (warnings.isEmpty()
                ? "Healthy"
                : warnings.size() + " warning(s)");

        source.sendSuccess(
                () -> Component.literal(message),
                false
        );
    }

    private static Optional<Claim> resolveClaim(
            CommandSourceStack source,
            String selector
    ) {
        AdminClaimSelector.Resolution resolution =
                AdminClaimSelector.resolve(selector);

        if (resolution.found()) {
            return Optional.of(resolution.claim());
        }

        if (resolution.status()
                == AdminClaimSelector.Status.AMBIGUOUS) {
            String options = resolution.matches()
                    .stream()
                    .map(AdminClaimSelector::selectorFor)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("None");

            source.sendFailure(
                    Component.literal(
                            "Claim selector \""
                                    + selector
                                    + "\" is ambiguous. Use one of: "
                                    + options
                                    + "."
                    )
            );
        } else {
            source.sendFailure(
                    Component.literal(
                            "No claim found for selector \""
                                    + selector
                                    + "\"."
                    )
            );
        }

        return Optional.empty();
    }

    private static List<Claim> sortedClaims() {
        return Bananaclaims.CLAIM_MANAGER
                .getAllClaims()
                .stream()
                .sorted(
                        Comparator.comparing(
                                        Claim::getName,
                                        Comparator.nullsLast(
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                )
                                .thenComparing(
                                        Claim::getOwnerName,
                                        Comparator.nullsLast(
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                )
                )
                .toList();
    }

    private static String formatDimensions(
            Map<String, Integer> dimensions
    ) {
        if (dimensions == null || dimensions.isEmpty()) {
            return "None";
        }

        return dimensions.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry ->
                        entry.getKey()
                                + "="
                                + entry.getValue()
                )
                .reduce((left, right) -> left + ", " + right)
                .orElse("None");
    }

    private static void notifyPlayer(
            CommandSourceStack source,
            UUID playerUuid,
            String message
    ) {
        ServerPlayer player = source.getServer()
                .getPlayerList()
                .getPlayer(playerUuid);

        if (player != null) {
            player.sendSystemMessage(
                    Component.literal(message)
            );
        }
    }

    private static void audit(
            CommandSourceStack source,
            String action
    ) {
        Bananaclaims.LOGGER.info(
                "[ADMIN] {} {}.",
                source.getTextName(),
                action
        );
    }
}
