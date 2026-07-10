package com.bananasandwich.bananaclaims.integration.bluemap;

import com.bananasandwich.bananaclaims.Bananaclaims;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class BlueMapIntegration {

    private static final String BRIDGE_CLASS_NAME =
            "com.bananasandwich.bananaclaims.integration.bluemap.BlueMapBridge";

    private static boolean registered = false;

    private BlueMapIntegration() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        if (!FabricLoader.getInstance().isModLoaded("bluemap")) {
            Bananaclaims.LOGGER.info(
                    "BlueMap is not installed. BlueMap integration is disabled."
            );
            return;
        }

        try {
            Class<?> bridgeClass = Class.forName(BRIDGE_CLASS_NAME);
            Method registerMethod = bridgeClass.getMethod("register");
            registerMethod.invoke(null);
        } catch (ClassNotFoundException exception) {
            Bananaclaims.LOGGER.error(
                    "BlueMap was detected, but the Banana Claims BlueMap bridge could not be found.",
                    exception
            );
        } catch (NoSuchMethodException exception) {
            Bananaclaims.LOGGER.error(
                    "The Banana Claims BlueMap bridge is missing its register method.",
                    exception
            );
        } catch (IllegalAccessException exception) {
            Bananaclaims.LOGGER.error(
                    "Banana Claims could not access the BlueMap bridge.",
                    exception
            );
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause() == null
                    ? exception
                    : exception.getCause();

            Bananaclaims.LOGGER.error(
                    "Banana Claims could not initialize BlueMap integration.",
                    cause
            );
        } catch (LinkageError error) {
            Bananaclaims.LOGGER.error(
                    "BlueMap was detected, but its API could not be loaded. BlueMap integration is disabled.",
                    error
            );
        }
    }
}