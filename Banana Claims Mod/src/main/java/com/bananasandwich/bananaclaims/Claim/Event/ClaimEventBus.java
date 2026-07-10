package com.bananasandwich.bananaclaims.claim.event;

import com.bananasandwich.bananaclaims.Bananaclaims;

import java.util.ArrayList;
import java.util.List;

public class ClaimEventBus {

    private final List<ClaimChangeListener> listeners = new ArrayList<>();

    public void register(ClaimChangeListener listener) {
        if (listener == null || listeners.contains(listener)) {
            return;
        }

        listeners.add(listener);
    }

    public void unregister(ClaimChangeListener listener) {
        listeners.remove(listener);
    }

    public void publish(ClaimChangeEvent event) {
        if (event == null || listeners.isEmpty()) {
            return;
        }

        for (ClaimChangeListener listener : List.copyOf(listeners)) {
            try {
                listener.onClaimsChanged(event);
            } catch (Exception exception) {
                Bananaclaims.LOGGER.error(
                        "A Banana Claims change listener failed.",
                        exception
                );
            }
        }
    }
}