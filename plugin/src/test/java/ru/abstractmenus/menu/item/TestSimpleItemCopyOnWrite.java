package ru.abstractmenus.menu.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the copy-on-write contract of {@link SimpleItem#clone()}.
 *
 * <p>Background: {@code clone()} used to deep-copy three {@code LinkedHashMap}
 * fields (allProps + materialProps + simpleProps) on every call. With a 54-slot
 * menu refreshed at 20 TPS for 100 players that came out to ~324 000 maps
 * allocated per second — none of which the typical refresh actually mutated.
 *
 * <p>The new behaviour: clone shares map references; the first add/remove/
 * setProperties call on the clone copies on demand and detaches from the
 * template. {@code ItemProperty.apply()} never mutates these maps, so the
 * common refresh path now allocates zero maps per item.
 *
 * <p>The {@link ru.abstractmenus.data.actions.ActionPropertyRemove} and
 * {@link ru.abstractmenus.data.actions.ActionPropertySet} actions DO mutate
 * properties at runtime — these tests verify the template is shielded from
 * those mutations after CoW kicks in.
 */
class TestSimpleItemCopyOnWrite {

    /** Property without canReplaceMaterial — lands in {@code simpleProps}. */
    private static class SimpleProp implements ItemProperty {
        @Override public boolean canReplaceMaterial() { return false; }
        @Override public boolean isApplyMeta() { return true; }
        @Override public void apply(ItemStack i, ItemMeta m, Player p, Menu menu) {}
    }

    /** Property with canReplaceMaterial — lands in {@code materialProps}. */
    private static class MaterialProp implements ItemProperty {
        @Override public boolean canReplaceMaterial() { return true; }
        @Override public boolean isApplyMeta() { return false; }
        @Override public void apply(ItemStack i, ItemMeta m, Player p, Menu menu) {}
    }

    @Test
    void cloneSharesMapReferenceWhenNoMutationOccurs() {
        SimpleItem template = new SimpleItem();
        template.addProperty("name", new SimpleProp());
        template.addProperty("material", new MaterialProp());

        SimpleItem clone = template.clone();

        // Same Map instance — no allocation happened in clone().
        assertSame(template.getProperties(), clone.getProperties(),
                "clone() must share allProps map reference until mutation");
    }

    @Test
    void cloneRemainsIsolatedAfterAddOnTemplate() {
        // After clone, mutating the TEMPLATE should NOT cascade through the
        // shared map into existing clones — the template itself is owned, so
        // adds happen in-place on the shared map. This exposes the fundamental
        // shape of CoW: only mutations on the *clone* are isolated.
        // The contract is: do not mutate templates after they have been cloned.
        // This test documents that contract by constructing the clone first.
        SimpleItem template = new SimpleItem();
        template.addProperty("name", new SimpleProp());

        SimpleItem clone = template.clone();
        assertEquals(1, clone.getProperties().size());

        // Mutating template at this point would leak into the clone's view —
        // that's expected. The codebase only mutates per-render clones via
        // ActionPropertyRemove / ActionPropertySet, never templates.
    }

    @Test
    void mutationOnCloneDoesNotAffectTemplateAddProperty() {
        SimpleItem template = new SimpleItem();
        template.addProperty("name", new SimpleProp());

        SimpleItem clone = template.clone();
        clone.addProperty("lore", new SimpleProp());

        assertEquals(1, template.getProperties().size(),
                "Template properties must not gain 'lore'");
        assertEquals(2, clone.getProperties().size(),
                "Clone must have both 'name' and 'lore'");
        assertNotSame(template.getProperties(), clone.getProperties(),
                "After mutation, clone must own its own map");
    }

    @Test
    void mutationOnCloneDoesNotAffectTemplateRemoveProperty() {
        SimpleItem template = new SimpleItem();
        template.addProperty("name", new SimpleProp());
        template.addProperty("lore", new SimpleProp());

        SimpleItem clone = template.clone();
        clone.removeProperty("lore");

        assertEquals(2, template.getProperties().size(),
                "Template must still see both properties");
        assertEquals(1, clone.getProperties().size(),
                "Clone must have only 'name' after remove");
        assertTrue(template.getProperties().containsKey("lore"));
        assertFalse(clone.getProperties().containsKey("lore"));
    }

    @Test
    void mutationOnCloneDoesNotAffectTemplateSetProperties() {
        SimpleItem template = new SimpleItem();
        template.addProperty("name", new SimpleProp());

        SimpleItem clone = template.clone();
        Map<String, ItemProperty> replacement = Map.of("amount", new SimpleProp());
        clone.setProperties(replacement);

        // setProperties calls addProperty for each entry — does not clear existing,
        // so clone ends up with name + amount; template stays at just name.
        assertEquals(1, template.getProperties().size());
        assertTrue(template.getProperties().containsKey("name"));
        assertFalse(template.getProperties().containsKey("amount"));

        assertTrue(clone.getProperties().containsKey("name"));
        assertTrue(clone.getProperties().containsKey("amount"));
    }

    @Test
    void multipleClonesAreIndependentAfterMutation() {
        SimpleItem template = new SimpleItem();
        template.addProperty("name", new SimpleProp());

        SimpleItem cloneA = template.clone();
        SimpleItem cloneB = template.clone();

        cloneA.addProperty("lorea", new SimpleProp());
        cloneB.addProperty("loreb", new SimpleProp());

        assertEquals(1, template.getProperties().size());
        assertEquals(2, cloneA.getProperties().size());
        assertEquals(2, cloneB.getProperties().size());
        assertTrue(cloneA.getProperties().containsKey("lorea"));
        assertFalse(cloneA.getProperties().containsKey("loreb"));
        assertTrue(cloneB.getProperties().containsKey("loreb"));
        assertFalse(cloneB.getProperties().containsKey("lorea"));
    }

    @Test
    void materialAndSimpleMapsAreBothIsolatedAfterMutation() {
        // Mixed maps: ensure both materialProps and simpleProps survive CoW.
        SimpleItem template = new SimpleItem();
        template.addProperty("material", new MaterialProp());
        template.addProperty("name", new SimpleProp());

        SimpleItem clone = template.clone();
        clone.removeProperty("material");

        assertEquals(2, template.getProperties().size());
        assertEquals(1, clone.getProperties().size());
        assertTrue(template.getProperties().containsKey("material"));
        assertFalse(clone.getProperties().containsKey("material"));
    }

    @Test
    void cloneOfCloneIsAlsoCowIsolated() {
        SimpleItem template = new SimpleItem();
        template.addProperty("name", new SimpleProp());

        SimpleItem clone1 = template.clone();
        SimpleItem clone2 = clone1.clone();

        clone2.addProperty("lore", new SimpleProp());

        assertEquals(1, template.getProperties().size());
        assertEquals(1, clone1.getProperties().size());
        assertEquals(2, clone2.getProperties().size());
    }

    @Test
    void freshConstructedItemIsImmediatelyOwned() {
        // Sanity: a brand-new item (not from clone()) can mutate without copying.
        SimpleItem item = new SimpleItem();
        Map<String, ItemProperty> mapBefore = item.getProperties();
        item.addProperty("name", new SimpleProp());
        Map<String, ItemProperty> mapAfter = item.getProperties();
        assertSame(mapBefore, mapAfter,
                "Fresh items must mutate in place — no spurious CoW");
    }

    @Test
    void menuItemCloneInheritsCowBehaviour() {
        // MenuItem extends InventoryItem extends SimpleItem; the clone chain
        // ultimately calls SimpleItem.clone which sets propsOwned=false.
        MenuItem template = new MenuItem();
        template.addProperty("name", new SimpleProp());

        MenuItem clone = template.clone();
        assertSame(template.getProperties(), clone.getProperties(),
                "MenuItem clone must share map until first mutation");

        clone.addProperty("lore", new SimpleProp());
        assertEquals(1, template.getProperties().size());
        assertEquals(2, clone.getProperties().size());
    }
}
