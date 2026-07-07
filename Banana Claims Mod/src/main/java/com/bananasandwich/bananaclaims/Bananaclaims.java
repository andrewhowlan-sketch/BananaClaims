package com.bananasandwich.bananaclaims;

import com.bananasandwich.bananaclaims.claim.ClaimManager;
import com.bananasandwich.bananaclaims.command.ClaimCommand;
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

	@Override
	public void onInitialize() {
		LOGGER.info("Banana Claims loaded!");

		CLAIM_MANAGER.setStorage(CLAIM_STORAGE);
		CLAIM_MANAGER.loadClaims(CLAIM_STORAGE.loadClaims());

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				ClaimCommand.register(dispatcher)
		);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}