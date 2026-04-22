package ru.abstractmenus.variables;

import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.api.variables.VarBuilder;

public class VarImpl implements Var {

    private final String name;
    private final String value;
    private final long expiry;

    public VarImpl(String key, String value, long expiry) {
        this.name = key;
        this.value = value;
        this.expiry = expiry;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public long expiry() {
        return expiry;
    }

    @Override
    public boolean hasExpiry() {
        return expiry > 0;
    }

    @Override
    public boolean isExpired() {
        return hasExpiry() && System.currentTimeMillis() >= expiry;
    }

    @Override
    public boolean boolValue() {
        String lower = value.toLowerCase();
        return lower.equals("true") || lower.equals("1");
    }

    @Override
    public int intValue() throws NumberFormatException {
        return Integer.parseInt(value);
    }

    @Override
    public long longValue() throws NumberFormatException {
        return Long.parseLong(value);
    }

    @Override
    public float floatValue() throws NumberFormatException {
        return Float.parseFloat(value);
    }

    @Override
    public double doubleValue() throws NumberFormatException {
        return Double.parseDouble(value);
    }

    @Override
    public VarBuilder toBuilder() {
        return new VarBuilderImpl(this);
    }
}
