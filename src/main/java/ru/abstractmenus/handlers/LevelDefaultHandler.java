package ru.abstractmenus.handlers;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.handler.LevelHandler;

public class LevelDefaultHandler implements LevelHandler {

    @Override
    public int getLevel(Player player) {
        return player.getLevel();
    }

    @Override
    public int getXp(Player player) {
        return player.getTotalExperience();
    }

    @Override
    public void giveXp(Player player, int xp) {
        player.giveExp(xp);
    }

    @Override
    public void takeXp(Player player, int xp) {
        int value = Math.max(0, getXp(player) - xp);
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        player.giveExp(value);
    }

    @Override
    public void giveLevel(Player player, int level) {
        player.setLevel(player.getLevel() + level);
    }

    @Override
    public void takeLevel(Player player, int level) {
        player.setLevel(Math.max(0, player.getLevel() - level));
    }
}
