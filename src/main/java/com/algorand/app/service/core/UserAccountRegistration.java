package com.algorand.app.service.core;

import java.util.Objects;

public class UserAccountRegistration {
    private String alias;
    private String address;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccountRegistration that = (UserAccountRegistration) o;
        return Objects.equals(alias, that.alias) &&
                Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, address);
    }

    @Override
    public String toString() {
         return   alias;
    }
}
