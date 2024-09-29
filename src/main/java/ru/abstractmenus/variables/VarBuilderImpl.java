package ru.abstractmenus.variables;

import com.google.common.base.Preconditions;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.api.variables.VarBuilder;
import ru.abstractmenus.util.NumberUtil;

public class VarBuilderImpl implements VarBuilder {

    private String name;
    private String value;
    private long expiry;

    VarBuilderImpl() { }

    VarBuilderImpl(Var var) {
        this.name = var.name();
        this.value = var.value();
        this.expiry = var.expiry();
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
    public VarBuilderImpl name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public VarBuilderImpl value(String value) {
        this.value = value;
        return this;
    }

    @Override
    public VarBuilderImpl expiry(long expiry) {
        this.expiry = expiry;
        return this;
    }

    @Override
    public Var build() {
        Preconditions.checkNotNull(name, "Variable name cannot be null");
        Preconditions.checkNotNull(value, "Variable value cannot be null");
        return new VarImpl(name, NumberUtil.tryToInt(value), expiry);
    }

}
