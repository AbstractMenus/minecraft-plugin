package ru.abstractmenus.handlers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.handler.EconomyHandler;

public class EconomyVaultHandler implements EconomyHandler {

    private final Economy economy;

    public EconomyVaultHandler(Economy economy){
        this.economy = economy;
    }

    @Override
    public boolean hasBalance(Player player, double balance) {
        return economy.has(player, balance);
    }

    @Override
    public void takeBalance(Player player, double balance) {
        economy.withdrawPlayer(player, balance);
    }

    @Override
    public void giveBalance(Player player, double balance) {
        economy.depositPlayer(player, balance);
    }
}
