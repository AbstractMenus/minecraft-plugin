package ru.abstractmenus.data.actions;

import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.datatype.TypeDouble;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;

/**
 * Shared deserializer for HOCON shapes used by money-aware actions and
 * rules ({@code takeMoney}, {@code giveMoney}, {@code hasMoney}).
 *
 * <p>Accepts both forms:
 *
 * <pre>{@code
 * takeMoney: 100                                    # scalar - default provider
 * takeMoney { amount: 100, provider: "vault" }      # map - explicit provider
 * }</pre>
 *
 * <p>Validates the optional {@code provider} field at parse time so menus
 * fail fast if they reference an unregistered economy provider, with an
 * error that lists the actually-registered ids ({@code "vault"},
 * {@code "playerpoints"}, etc.) instead of impl class names. Three
 * call sites (TakeMoney, GiveMoney, RuleMoney) used to duplicate this
 * 18-line block; consolidated here.
 */
public final class MoneyAmountSpec {

    public final TypeDouble amount;
    public final String provider;

    private MoneyAmountSpec(TypeDouble amount, String provider) {
        this.amount = amount;
        this.provider = provider;
    }

    /**
     * Parse a {@code TypeDouble amount} plus optional {@code provider} from
     * either the scalar form or the map form. Throws on unknown provider
     * with a helpful message.
     */
    public static MoneyAmountSpec parse(ConfigNode node) throws NodeSerializeException {
        TypeDouble amount;
        String provider = null;

        if (node.isMap()) {
            amount = node.node("amount").getValue(TypeDouble.class);
            provider = node.node("provider").getString(null);
        } else {
            amount = node.getValue(TypeDouble.class);
        }

        if (provider != null && !AbstractMenusApi.get().providers().economy().has(provider)) {
            String known = String.join(", ", AbstractMenusApi.get().providers().economy().ids());
            throw new NodeSerializeException(node,
                    "Unknown economy provider '" + provider + "'. Registered: ["
                            + known + "]. Omit the 'provider' field for default selection.");
        }

        return new MoneyAmountSpec(amount, provider);
    }
}
