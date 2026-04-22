package ru.abstractmenus.menu.animated;

import lombok.Setter;
import org.bukkit.entity.Player;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.menu.SimpleMenu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.util.TimeUtil;

import java.util.*;

public class AnimatedMenu extends SimpleMenu {

    @Setter
    private Actions animStartActions;
    @Setter
    private Actions animEndActions;

    @Setter
    private List<Frame> frames;

    private int currentFrame;
    private long lastPlayTime;
    private boolean endActionsActivated = false;
    @Setter
    private boolean loop;

    public AnimatedMenu(String title, int size) {
        super(title, size);
    }

    @Override
    public void refresh(Player player) {
        if (animStartActions != null)
            animStartActions.activate(player, this, null);

        currentFrame = 0;
        lastPlayTime = TimeUtil.currentTimeTicks();

        showedItems.clear();
        inventory.clear();

        placeItems(player);
    }

    @Override
    public void update(Player player) {
        if (player == null || !player.isOnline()) return;

        if (currentFrame >= frames.size()) {
            if (!loop) {
                if (!endActionsActivated && animEndActions != null) {
                    animEndActions.activate(player, this, null);
                    endActionsActivated = true;
                }
                return;
            }

            currentFrame = 0;
        }

        Frame frame = frames.get(currentFrame);

        if (TimeUtil.currentTimeTicks() >= lastPlayTime + frame.getDelay()) {
            Map<Integer, Frame.PlayedSlot> played = frame.play(player, this);

            if (played != null) {
                if (frame.getStartActions() != null)
                    frame.getStartActions().activate(player, this, null);

                if (updateActions != null)
                    updateActions.activate(player, this, null);

                if (frame.isClear()) {
                    showedItems.clear();
                    inventory.clear();
                    placeItems(player);
                }

                int size = inventory.getSize();
                for (Map.Entry<Integer, Frame.PlayedSlot> entry : played.entrySet()) {
                    int slot = entry.getKey();
                    Frame.PlayedSlot ps = entry.getValue();
                    showedItems.put(slot, ps.item());
                    if (slot >= 0 && slot < size) {
                        // Frame.play already built the stack — no second build here.
                        inventory.setItem(slot, ps.stack());
                    }
                }

                if (frame.getEndActions() != null)
                    frame.getEndActions().activate(player, this, null);
            }

            currentFrame++;
            lastPlayTime = TimeUtil.currentTimeTicks();
        }
    }

    @Override
    public Collection<Item> getItems() {
        Frame frame = frames.get(currentFrame);
        return frame == null ? null : frame.getItems();
    }

}
