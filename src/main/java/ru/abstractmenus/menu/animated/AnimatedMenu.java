package ru.abstractmenus.menu.animated;

import org.bukkit.entity.Player;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.menu.SimpleMenu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.util.TimeUtil;

import java.util.*;

public class AnimatedMenu extends SimpleMenu {

    private Actions animStartActions;
    private Actions animEndActions;

    private List<Frame> frames;

    private int currentFrame;
    private long lastPlayTime;
    private boolean endActionsActivated = false;
    private boolean loop;

    public AnimatedMenu(String title, int size) {
        super(title, size);
    }

    @Override
    public void refresh(Player player) {
        if(animStartActions != null)
            animStartActions.activate(player, this, null);

        currentFrame = 0;
        lastPlayTime = TimeUtil.currentTimeTicks();

        showedItems.clear();
        inventory.clear();

        placeItems(player);
    }

    @Override
    public void update(Player player){
        if(player == null || !player.isOnline()) return;

        if(currentFrame >= frames.size()) {
            if(!loop){
                if (!endActionsActivated && animEndActions != null){
                    animEndActions.activate(player, this, null);
                    endActionsActivated = true;
                }
                return;
            }

            currentFrame = 0;
        }

        Frame frame = frames.get(currentFrame);

        if(TimeUtil.currentTimeTicks() >= lastPlayTime + frame.getDelay()){
            Map<Integer, Item> items = frame.play(player, this);

            if(items != null) {
                if(frame.getStartActions() != null)
                    frame.getStartActions().activate(player, this, null);

                if (updateActions != null)
                    updateActions.activate(player, this, null);

                if(frame.isClear()) {
                    showedItems.clear();
                    inventory.clear();
                    placeItems(player);
                }

                showedItems.putAll(items);

                for (Map.Entry<Integer, Item> entry : items.entrySet()){
                    if(entry.getKey() >= 0 && entry.getKey() < inventory.getSize()){
                        inventory.setItem(entry.getKey(), entry.getValue().build(player, this));
                    }
                }

                if(frame.getEndActions() != null)
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

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setAnimStartActions(Actions animStartActions) {
        this.animStartActions = animStartActions;
    }

    public void setAnimEndActions(Actions animEndActions) {
        this.animEndActions = animEndActions;
    }

    public void setFrames(List<Frame> frames){
        this.frames = frames;
    }
}
