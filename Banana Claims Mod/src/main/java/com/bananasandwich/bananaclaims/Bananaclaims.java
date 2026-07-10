package com.bananasandwich.bananaclaims;

import com.bananasandwich.bananaclaims.claim.ClaimManager;
import com.bananasandwich.bananaclaims.command.ClaimCommand;
import com.bananasandwich.bananaclaims.integration.bluemap.BlueMapIntegration;
import com.bananasandwich.bananaclaims.notification.ClaimNotificationManager;
import com.bananasandwich.bananaclaims.preview.BoundaryPreviewManager;
import com.bananasandwich.bananaclaims.previewv2.DisplayPreviewV2Manager;
import com.bananasandwich.bananaclaims.protection.ClaimProtectionManager;
import com.bananasandwich.bananaclaims.selection.SelectionManager;
import com.bananasandwich.bananaclaims.storage.ClaimStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bananaclaims implements ModInitializer {

	public static final String MOD_ID = "bananaclaims";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ClaimManager CLAIM_MANAGER = new ClaimManager();
	public static final ClaimStorage CLAIM_STORAGE = new ClaimStorage();
	public static final SelectionManager SELECTION_MANAGER =
			new SelectionManager();
	public static final BoundaryPreviewManager BOUNDARY_PREVIEW_MANAGER =
			new BoundaryPreviewManager();
	public static final DisplayPreviewV2Manager DISPLAY_PREVIEW_V2_MANAGER =
			new DisplayPreviewV2Manager();

	@Override
	public void onInitialize() {
		LOGGER.info("Banana Claims loaded!");

		CLAIM_MANAGER.setStorage(CLAIM_STORAGE);
		CLAIM_MANAGER.loadClaims(CLAIM_STORAGE.loadClaims());

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) ->
						ClaimCommand.register(dispatcher)
		);

		BOUNDARY_PREVIEW_MANAGER.register();
		DISPLAY_PREVIEW_V2_MANAGER.register();
		ClaimNotificationManager.register();
		ClaimProtectionManager.register();
		BlueMapIntegration.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}

