package ru.abstractmenus.data.comparator;

import ru.abstractmenus.datatype.TypeDouble;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.Handlers;

import java.util.List;

public final class LegacyValueComparator implements ValueComparator {

    private final List<Comparator> comparators;

    public LegacyValueComparator(List<Comparator> comparators) {
        this.comparators = comparators;
    }

    @Override
    public boolean compare(Player player, Menu menu) {
        for (Comparator comparator : comparators) {
            if (!comparator.compare(player, menu))
                return false;
        }
        return true;
    }

    public static class Comparator {

        private final String param;
        private List<String> equals;
        private List<String> equalsIgnoreCase;
        private List<String> contains;
        private TypeDouble less = new TypeDouble(Double.MIN_VALUE);
        private TypeDouble more = new TypeDouble(Double.MIN_VALUE);

        public Comparator(String param) {
            this.param = param;
        }

        public String getParam(){
            return param;
        }

        public void setEquals(List<String> equals) {
            this.equals = equals;
        }

        public void setEqualsIgnoreCase(List<String> equalsIgnoreCase) {
            this.equalsIgnoreCase = equalsIgnoreCase;
        }

        public void setContains(List<String> contains) {
            this.contains = contains;
        }

        public void setLess(TypeDouble less) {
            this.less = less;
        }

        public void setMore(TypeDouble more) {
            this.more = more;
        }

        public boolean compare(Player player, Menu menu){
            String param = Handlers.getPlaceholderHandler().replace(player, getParam());

            if(equals != null){
                for(String str : equals){
                    String val = Handlers.getPlaceholderHandler().replace(player, str);

                    try{
                        if(Double.parseDouble(param) == Double.parseDouble(val)) return true;
                    }catch (NumberFormatException e){
                        if(param.equals(val)) return true;
                    }
                }
            }

            if(equalsIgnoreCase != null){
                for(String str : equalsIgnoreCase){
                    String val = Handlers.getPlaceholderHandler().replace(player, str);
                    if(param.equalsIgnoreCase(val)){
                        return true;
                    }
                }
            }

            if(contains != null){
                for(String str : contains){
                    String val = Handlers.getPlaceholderHandler().replace(player, str);
                    if(param.toLowerCase().contains(val.toLowerCase())){
                        return true;
                    }
                }
            }

            double lessVal = less.getDouble(player, menu);
            double moreVal = more.getDouble(player, menu);

            if(lessVal != Double.MIN_VALUE){
                try{
                    return Double.parseDouble(param) < lessVal;
                } catch (NumberFormatException e){
                    return false;
                }
            }

            if(moreVal != Double.MIN_VALUE){
                try{
                    return Double.parseDouble(param) > moreVal;
                } catch (NumberFormatException e){
                    return false;
                }
            }

            return false;
        }

        public static class Serializer implements NodeSerializer<Comparator> {

            @Override
            public Comparator deserialize(Class type, ConfigNode node) throws NodeSerializeException {
                Comparator matcher = new Comparator(node.node("param").getString());
                if(node.node("equals").rawValue() != null){
                    matcher.setEquals(node.node("equals").getList(String.class));
                }
                if(node.node("equalsIgnoreCase").rawValue() != null){
                    matcher.setEqualsIgnoreCase(node.node("equalsIgnoreCase").getList(String.class));
                }
                if(node.node("contains").rawValue() != null){
                    matcher.setContains(node.node("contains").getList(String.class));
                }
                if(node.node("less").rawValue() != null){
                    matcher.setLess(node.node("less").getValue(TypeDouble.class));
                }
                if(node.node("more").rawValue() != null){
                    matcher.setMore(node.node("more").getValue(TypeDouble.class));
                }
                return matcher;
            }
        }
    }

    public static class Serializer implements NodeSerializer<LegacyValueComparator> {

        @Override
        public LegacyValueComparator deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new LegacyValueComparator(node.getList(Comparator.class));
        }

    }
}