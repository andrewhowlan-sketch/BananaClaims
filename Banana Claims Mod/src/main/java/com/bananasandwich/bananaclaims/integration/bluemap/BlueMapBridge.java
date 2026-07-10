package com.bananasandwich.bananaclaims.integration.bluemap;

import com.bananasandwich.bananaclaims.Bananaclaims;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;

import java.util.function.Consumer;

public final class BlueMapBridge {

    public static final String MARKER_SET_ID = "bananaclaims";

    private static boolean registered = false;

    private static final Consumer<BlueMapAPI> ENABLE_LISTENER =
            BlueMapBridge::enable;

    private static final Consumer<BlueMapAPI> DISABLE_LISTENER =
            BlueMapBridge::disable;

    private BlueMapBridge() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        BlueMapAPI.onEnable(ENABLE_LISTENER);
        BlueMapAPI.onDisable(DISABLE_LISTENER);

        Bananaclaims.LOGGER.info(
                "BlueMap detected. Waiting for the BlueMap API."
        );
    }

    private static void enable(BlueMapAPI api) {
        for (BlueMapMap map : api.getMaps()) {
            map.getMarkerSets().put(
                    MARKER_SET_ID,
                    createMarkerSet()
            );
        }

        Bananaclaims.LOGGER.info(
                "Banana Claims BlueMap integration enabled for {} map(s).",
                api.getMaps().size()
        );
    }

    private static void disable(BlueMapAPI api) {
        for (BlueMapMap map : api.getMaps()) {
            map.getMarkerSets().remove(MARKER_SET_ID);
        }

        Bananaclaims.LOGGER.info(
                "Banana Claims BlueMap integration disabled."
        );
    }

    private static MarkerSet createMarkerSet() {
        MarkerSet markerSet = new MarkerSet(
                "Banana Claims",
                true,
                false
        );

        markerSet.setSorting(0);

        return markerSet;
    }
}