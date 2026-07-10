package com.bananasandwich.bananaclaims.claim.event;

@FunctionalInterface
public interface ClaimChangeListener {

    void onClaimsChanged(ClaimChangeEvent event);
}