package ru.abstractmenus.data.activators;

import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.extractors.BlockExtractor;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.util.List;

public class OpenSign extends Activator {

    private final List<String> text;

    private OpenSign(List<String> text) {
        this.text = text;
    }

    @EventHandler
    public void onTableClick(PlayerInteractEvent event) {
        if (!ActivatorUtil.checkHand(event)) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || !Tag.SIGNS.isTagged(block.getType())) return;
        if (!(block.getState() instanceof Sign sign)) return;

        Player player = event.getPlayer();
        // Sign has at most 4 lines; clamp to whichever side is shorter so a
        // mis-configured `text` list with >4 entries can't crash the listener.
        String[] lines = sign.getLines();
        int compareLines = Math.min(text.size(), lines.length);
        for (int i = 0; i < compareLines; i++) {
            String line = Handlers.getPlaceholderHandler().replace(player, text.get(i));
            if (!line.equalsIgnoreCase(lines[i])) return;
        }

        openMenu(block, player);
    }


    @Override
    public ValueExtractor getValueExtractor() {
        return BlockExtractor.INSTANCE;
    }

    public static class Serializer implements NodeSerializer<OpenSign> {

        @Override
        public OpenSign deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new OpenSign(Colors.ofList(node.getList(String.class)));
        }

    }
}
