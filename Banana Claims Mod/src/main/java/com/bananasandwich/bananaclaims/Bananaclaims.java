package com.bananasandwich.bananaclaims;

import com.bananasandwich.bananaclaims.claim.ClaimManager;
import com.bananasandwich.bananaclaims.command.ClaimCommand;
import com.bananasandwich.bananaclaims.integration.bluemap.BlueMapIntegration;
import com.bananasandwich.bananaclaims.notification.ClaimNotificationManager;
import com.bananasandwich.bananaclaims.previewv2.DisplayPreviewV2Manager;
import com.bananasandwich.bananaclaims.previewv2.PreviewV2ConfigManager;
import com.bananasandwich.bananaclaims.protection.ClaimProtectionManager;
import com.bananasandwich.bananaclaims.protection.ClaimProtectionService;
import com.bananasandwich.bananaclaims.selection.SelectionManager;
import com.bananasandwich.bananaclaims.storage.ClaimStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bananaclaims implements ModInitializer {

	public static final String MOD_ID =
			"bananaclaims";

	public static final Logger LOGGER =
			LoggerFactory.getLogger(MOD_ID);

	public static final ClaimManager CLAIM_MANAGER =
			new ClaimManager();

	public static final ClaimStorage CLAIM_STORAGE =
			new ClaimStorage();

	public static final SelectionManager SELECTION_MANAGER =
			new SelectionManager();

	public static final PreviewV2ConfigManager PREVIEW_V2_CONFIG_MANAGER =
			new PreviewV2ConfigManager();

	public static final DisplayPreviewV2Manager DISPLAY_PREVIEW_V2_MANAGER =
			new DisplayPreviewV2Manager(
					PREVIEW_V2_CONFIG_MANAGER
			);

	public static final ClaimProtectionService CLAIM_PROTECTION_SERVICE =
			new ClaimProtectionService(
					CLAIM_MANAGER
			);

	public static final ClaimProtectionManager CLAIM_PROTECTION_MANAGER =
			new ClaimProtectionManager(
					CLAIM_PROTECTION_SERVICE
			);

	@Override
	public void onInitialize() {
		LOGGER.info("Banana Claims loaded!");

		PREVIEW_V2_CONFIG_MANAGER.load();

		CLAIM_MANAGER.setStorage(CLAIM_STORAGE);
		CLAIM_MANAGER.loadClaims(
				CLAIM_STORAGE.loadClaims()
		);

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) ->
						ClaimCommand.register(dispatcher)
		);

		DISPLAY_PREVIEW_V2_MANAGER.register();
		ClaimNotificationManager.register();
		CLAIM_PROTECTION_MANAGER.register();
		BlueMapIntegration.register();
	}

	public static Identifier id(
			String path
	) {
		return Identifier.fromNamespaceAndPath(
				MOD_ID,
				path
		);
	}
}


