package com.bananasandwich.bananaclaims.permission;

import com.bananasandwich.bananaclaims.Bananaclaims;
import net.minecraft.commands.CommandSourceStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Optional, dependency-free bridge to lucko's Fabric Permissions API v0.
 *
 * <p>The bridge resolves the API once and caches the exact check method. This
 * lets Banana Claims integrate with LuckPerms when the API is installed while
 * retaining a vanilla command-level fallback on servers that do not use it.</p>
 */
final class FabricPermissionsBridge {

    private static final String PERMISSIONS_CLASS =
            "me.lucko.fabric.api.permissions.v0.Permissions";

    private final Method commandSourceCheckMethod;
    private final AtomicBoolean invocationFailureLogged =
            new AtomicBoolean();

    FabricPermissionsBridge() {
        commandSourceCheckMethod =
                findCommandSourceCheckMethod();

        if (commandSourceCheckMethod != null) {
            Bananaclaims.LOGGER.info(
                    "Fabric Permissions API detected. Banana Claims permission nodes are active."
            );
        } else {
            Bananaclaims.LOGGER.info(
                    "Fabric Permissions API was not detected. Banana Claims will use configured vanilla command-level fallbacks."
            );
        }
    }

    boolean isAvailable() {
        return commandSourceCheckMethod != null;
    }

    Boolean check(
            CommandSourceStack source,
            String permissionNode,
            int fallbackLevel
    ) {
        if (commandSourceCheckMethod == null
                || source == null) {
            return null;
        }

        try {
            Object result =
                    commandSourceCheckMethod.invoke(
                            null,
                            source,
                            permissionNode,
                            fallbackLevel
                    );

            return result instanceof Boolean value
                    ? value
                    : null;
        } catch (IllegalAccessException
                 | InvocationTargetException
                 | RuntimeException exception) {
            if (invocationFailureLogged.compareAndSet(
                    false,
                    true
            )) {
                Bananaclaims.LOGGER.error(
                        "Fabric Permissions API check failed. Banana Claims will use vanilla fallbacks for this session.",
                        exception
                );
            }

            return null;
        }
    }

    private static Method findCommandSourceCheckMethod() {
        try {
            Class<?> permissionsClass =
                    Class.forName(
                            PERMISSIONS_CLASS,
                            false,
                            FabricPermissionsBridge.class
                                    .getClassLoader()
                    );

            for (Method method : permissionsClass.getMethods()) {
                Class<?>[] parameterTypes =
                        method.getParameterTypes();

                if (!method.getName().equals("check")
                        || !Modifier.isStatic(method.getModifiers())
                        || method.getReturnType() != boolean.class
                        || parameterTypes.length != 3
                        || !parameterTypes[0]
                        .isAssignableFrom(CommandSourceStack.class)
                        || parameterTypes[1] != String.class
                        || parameterTypes[2] != int.class) {
                    continue;
                }

                return method;
            }
        } catch (ClassNotFoundException
                 | LinkageError exception) {
            return null;
        }

        Bananaclaims.LOGGER.warn(
                "Fabric Permissions API was present, but a compatible CommandSourceStack check method was not found."
        );

        return null;
    }
}
