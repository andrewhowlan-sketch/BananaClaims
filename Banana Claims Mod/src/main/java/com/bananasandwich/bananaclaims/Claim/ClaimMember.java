package com.bananasandwich.bananaclaims.claim;

import java.util.Objects;
import java.util.UUID;

public class ClaimMember {

    private UUID uuid;
    private String name;

    public ClaimMember() {
    }

    public ClaimMember(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof ClaimMember other)) {
            return false;
        }

        return Objects.equals(uuid, other.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}