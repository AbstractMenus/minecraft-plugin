package ru.abstractmenus.data.properties;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import ru.abstractmenus.hocon.api.*;
import ru.abstractmenus.hocon.api.source.ConfigSources;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class TestAttributeModifier {
 @BeforeEach void setup(){MockBukkit.mock();}
 @AfterEach void done(){MockBukkit.unmock();}
 private ConfigNode node(String text) throws Exception {
  return ConfigurationLoader.builder().source(ConfigSources.inputStream("audit",new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)))).build().load().node("attributeModifier");
 }
 @Test void legacyKeyResolves() throws Exception {
  var n=node("attributeModifier: [{type: \"generic.attack_damage\", amount: 3, slot: HAND}]".replace("\\\"","\""));
  var prop=new PropAttributeModifier.Serializer().deserialize(PropAttributeModifier.class,n);
  var item=new ItemStack(Material.DIAMOND_SWORD);var meta=item.getItemMeta();
  prop.apply(item,meta,null,null);
  assertEquals(3,meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE).iterator().next().getAmount());
 }
 @Test void repeatedAttributeKeepsBothSlots() throws Exception {
  var n=node("attributeModifier: [{type: attack_damage, amount: 3, slot: HAND}, {type: attack_damage, amount: 7, slot: OFF_HAND}]");
  var prop=new PropAttributeModifier.Serializer().deserialize(PropAttributeModifier.class,n);
  var item=new ItemStack(Material.DIAMOND_SWORD);var meta=item.getItemMeta();
  prop.apply(item,meta,null,null);
  var modifiers=meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE);
  assertEquals(2,modifiers.size());
  assertEquals(java.util.Set.of(3.0,7.0),modifiers.stream().map(org.bukkit.attribute.AttributeModifier::getAmount).collect(java.util.stream.Collectors.toSet()));
 }
 @Test void baseAndBindingKeepDistinctStableModifiers() throws Exception {
  ru.abstractmenus.api.Logger.set(java.util.logging.Logger.getLogger("audit"));
  var first=new PropAttributeModifier.Serializer().deserialize(PropAttributeModifier.class,node("attributeModifier: [{type: attack_damage, amount: 3, slot: HAND}]"));
  var second=new PropAttributeModifier.Serializer().deserialize(PropAttributeModifier.class,node("attributeModifier: [{type: attack_damage, amount: 7, slot: OFF_HAND}]"));
  var group=new PropBindings.BindGroup(java.util.List.of(second),(p,m,i)->true);
  var ctor=PropBindings.class.getDeclaredConstructor(java.util.List.class);ctor.setAccessible(true);
  var binding=ctor.newInstance(java.util.List.of(group));
  var item=new ru.abstractmenus.menu.item.SimpleItem();
  item.addProperty("attributeModifier",first);item.addProperty("bindings",binding);
  var built=item.build(null,null);
  assertNotNull(built);
  assertEquals(2,built.getItemMeta().getAttributeModifiers(Attribute.ATTACK_DAMAGE).size());
  assertTrue(built.isSimilar(item.build(null,null)));
 }
 @Test void unknownAttributeReportsNode() throws Exception {
  var n=node("attributeModifier: [{type: does_not_exist}]");
  var ex=assertThrows(NodeSerializeException.class,()->new PropAttributeModifier.Serializer().deserialize(PropAttributeModifier.class,n));
  assertTrue(ex.getMessage().contains("does_not_exist"));
  assertTrue(ex.getMessage().contains("attributeModifier"));
 }
}
