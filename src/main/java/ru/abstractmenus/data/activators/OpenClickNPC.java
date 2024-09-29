package ru.abstractmenus.data.activators;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.extractors.NPCExtractor;

import java.util.List;

public class OpenClickNPC extends Activator {

    private final List<Integer> ids;

    private OpenClickNPC(List<Integer> ids){
        this.ids = ids;
    }

    @EventHandler
    public void onNpcClick(NPCRightClickEvent event){
        checkClicked(event, event.getClicker());
    }

    @EventHandler
    public void onNpcClick(NPCLeftClickEvent event){
        checkClicked(event, event.getClicker());
    }

    private void checkClicked(NPCClickEvent event, Player clicker) {
        if (ids.contains(event.getNPC().getId())) openMenu(event.getNPC(), clicker);
    }

    @Override
    public ValueExtractor getValueExtractor() {
        return NPCExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<OpenClickNPC> {

        @Override
        public OpenClickNPC deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenClickNPC(node.getList(Integer.class));
        }

    }
}
