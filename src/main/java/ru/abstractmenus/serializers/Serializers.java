package ru.abstractmenus.serializers;

import com.google.gson.JsonElement;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import ru.abstractmenus.api.Types;
import ru.abstractmenus.command.args.*;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.data.BannerData;
import ru.abstractmenus.data.BookData;
import ru.abstractmenus.data.EntityData;
import ru.abstractmenus.data.comparator.ModernValueComparator;
import ru.abstractmenus.data.properties.PropBindings;
import ru.abstractmenus.data.properties.PropLPMeta;
import ru.abstractmenus.data.rules.logical.RuleOr;
import ru.abstractmenus.datatype.*;
import ru.abstractmenus.hocon.api.serialize.NodeSerializers;
import ru.abstractmenus.serializers.menu.BaseMenuSerializer;
import ru.abstractmenus.command.Command;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.data.comparator.LegacyValueComparator;
import ru.abstractmenus.data.rules.logical.RuleAnd;
import ru.abstractmenus.data.rules.logical.RulesGroup;
import ru.abstractmenus.menu.animated.Frame;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.menu.generated.Matrix;
import ru.abstractmenus.variables.VarData;
import ru.abstractmenus.variables.VarNumData;

public final class Serializers {

    public static NodeSerializers serializers = NodeSerializers.defaults();

    private Serializers() { }

    public static void init(Plugin plugin) {
        NodeSerializers serializers = Types.serializers();
        
        serializers.register(TypeBool.class, new TypeBool.Serializer());
        serializers.register(TypeByte.class, new TypeByte.Serializer());
        serializers.register(TypeDouble.class, new TypeDouble.Serializer());
        serializers.register(TypeEnum.class, new TypeEnum.Serializer());
        serializers.register(TypeFloat.class, new TypeFloat.Serializer());
        serializers.register(TypeInt.class, new TypeInt.Serializer());
        serializers.register(TypeShort.class, new TypeShort.Serializer());
        serializers.register(TypeLocation.class, new TypeLocation.Serializer());
        serializers.register(TypeWorld.class, new TypeWorld.Serializer());
        serializers.register(TypeColor.class, new TypeColor.Serializer());
        serializers.register(TypeSlot.class, new TypeSlot.Serializer());
        serializers.register(TypeSlot.class, new TypeSlot.Serializer());
        serializers.register(TypeMaterial.class, new TypeMaterial.Serializer());

        serializers.register(JsonElement.class, new JsonSerializer());

        try {
            NbtCompoundSerializer.register(serializers);
        } catch (Throwable ignore) { }

        serializers.register(EntityType.class, new EntityTypeSerializer());
        serializers.register(World.class, new WorldSerializer());
        serializers.register(Color.class, new ColorSerializer());
        serializers.register(Item.class, new ItemSerializer());
        serializers.register(Location.class, new LocationSerializer());
        serializers.register(PotionEffect.class, new PotionEffectSerializer());
        serializers.register(LegacyValueComparator.class, new LegacyValueComparator.Serializer());
        serializers.register(LegacyValueComparator.Comparator.class, new LegacyValueComparator.Comparator.Serializer());
        serializers.register(ModernValueComparator.class, new ModernValueComparator.Serializer());
        serializers.register(EntityData.class, new EntityData.Serializer());
        serializers.register(FireworkEffect.class, new FireworkEffectSerializer());
        serializers.register(BookData.class, new BookData.Serializer());
        serializers.register(Pattern.class, new BannerPatternSerializer());
        serializers.register(BannerData.class, new BannerData.Serializer());
        serializers.register(Matrix.class, new Matrix.Serializer());

        serializers.register(RulesGroup.class, new RulesGroup.Serializer());
        serializers.register(RuleAnd.class, new RuleAnd.Serializer());
        serializers.register(RuleOr.class, new RuleOr.Serializer());
        serializers.register(Actions.class, new Actions.Serializer());
        serializers.register(PropBindings.BindGroup.class, new PropBindings.BindGroupSerializer());

        try {
            serializers.register(ShapedRecipe.class, new ShapedRecipeSerializer(plugin));
        } catch (Throwable ignore){ }

        serializers.register(VarData.class, new VarData.Serializer());
        serializers.register(VarNumData.class, new VarNumData.Serializer());

        serializers.register(Command.class, new Command.Serializer());
        serializers.register(Argument.class, new Argument.Serializer());
        serializers.register(StringArgument.class, new StringArgument.Serializer());
        serializers.register(NumberArgument.class, new NumberArgument.Serializer());
        serializers.register(IntegerArgument.class, new IntegerArgument.Serializer());
        serializers.register(ChoiceArgument.class, new ChoiceArgument.Serializer());
        serializers.register(PlayerArgument.class, new PlayerArgument.Serializer());

        serializers.register(Menu.class, new BaseMenuSerializer());
        serializers.register(Frame.class, new Frame.Serializer());
        serializers.register(PropLPMeta.class, new PropLPMeta.Serializer());
    }

}
