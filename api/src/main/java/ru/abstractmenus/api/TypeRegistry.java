package ru.abstractmenus.api;

import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.Set;

/**
 * Typed registry of menu type classes keyed by a HOCON-visible name.
 *
 * <p>AbstractMenus ships five of these, accessible via
 * {@link AbstractMenusApi#actions()}, {@link AbstractMenusApi#rules()},
 * {@link AbstractMenusApi#activators()},
 * {@link AbstractMenusApi#itemProperties()}, and
 * {@link AbstractMenusApi#catalogs()} &mdash; one per extension surface.
 *
 * <p>Registration takes a {@link MenuExtension} "owner" so
 * {@link #unregisterAll(MenuExtension)} can wipe every entry an addon
 * contributed when that addon is disabled or reloaded.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * public class MyAddon implements MenuExtension {
 *     @Override
 *     public void onEnable(AbstractMenusApi api) {
 *         api.actions().register(
 *             "sendWebhook",
 *             SendWebhookAction.class,
 *             new SendWebhookAction.Serializer(),
 *             this);
 *     }
 * }
 * }</pre>
 *
 * <h2>Threading</h2>
 *
 * Implementations are safe for concurrent reads but registration/unregistration
 * is expected to happen on the main server thread during plugin / extension
 * {@code onEnable} / {@code onDisable} hooks.
 *
 * @param <T> the registry element base type (e.g. {@code Action},
 *            {@code Rule}, {@code ItemProperty})
 *
 * @see AbstractMenusApi
 * @see MenuExtension
 */
public interface TypeRegistry<T> {

    /**
     * Register a subtype under {@code key}.
     *
     * <p>Keys are compared case-insensitively (lowercased internally). If the
     * key is already registered, this method overwrites the previous entry
     * and logs a warning &mdash; prefer unique keys with a vendor prefix.
     *
     * <p>The {@code serializer} is also registered against the shared
     * {@link ru.abstractmenus.hocon.api.serialize.NodeSerializer NodeSerializer}
     * collection so HOCON parsing can deserialize instances of this type.
     *
     * @param <S>        the concrete subtype
     * @param key        HOCON-visible name (case-insensitive)
     * @param type       the class token; must be assignable to {@code T}
     * @param serializer HOCON serializer for {@code type}
     * @param owner      the registering extension; used for later
     *                   {@link #unregisterAll(MenuExtension)} cleanup. Pass
     *                   the core extension for core registrations.
     */
    <S extends T> void register(String key,
                                Class<S> type,
                                NodeSerializer<S> serializer,
                                MenuExtension owner);

    /**
     * Look up a registered subtype by key.
     *
     * @param key HOCON-visible name (case-insensitive)
     * @return the registered class, or {@code null} if not found
     */
    Class<? extends T> get(String key);

    /**
     * Reverse lookup &mdash; the HOCON name a given class is registered under.
     *
     * @param type the class to look up
     * @return the registered key, or {@code null} if not registered
     */
    String name(Class<? extends T> type);

    /**
     * All registered keys. Returned set is an unmodifiable snapshot.
     *
     * @return all registered keys (lowercased)
     */
    Set<String> keys();

    /**
     * Remove every entry registered by {@code owner}. Invoked by the addon
     * manager when an extension is disabled or reloaded.
     *
     * @param owner the extension whose entries should be wiped
     */
    void unregisterAll(MenuExtension owner);
}
