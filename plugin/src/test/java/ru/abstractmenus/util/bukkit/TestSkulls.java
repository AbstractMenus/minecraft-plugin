package ru.abstractmenus.util.bukkit;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import ru.abstractmenus.listeners.PlayerListener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestSkulls {

    private ServerMock server;

    @BeforeEach
    void startServer() {
        server = MockBukkit.mock(new ServerMock() {
            @Override
            public OfflinePlayer getOfflinePlayer(String name) {
                throw new AssertionError("Head creation must not perform a blocking offline player lookup");
            }
        });
        Skulls.clearCache();
    }

    @AfterEach
    void stopServer() {
        Skulls.clearCache();
        MockBukkit.unmock();
    }

    @Test
    void unknownOfflineOwnerProducesNameOnlyHeadWithoutMojangLookup() {
        try (var mojang = mockStatic(MojangApi.class)) {
            ItemStack head = Skulls.getPlayerSkull("OfflineSkin");

            assertNotNull(head);
            assertEquals(Material.PLAYER_HEAD, head.getType());
            var profile = ((SkullMeta) head.getItemMeta()).getPlayerProfile();
            assertNotNull(profile);
            assertEquals("OfflineSkin", profile.getName());
            assertNull(profile.getId());
            assertTrue(profile.getProperties().isEmpty());
            mojang.verifyNoInteractions();
        }
    }

    @Test
    void joinInvalidatesCaseInsensitiveCacheAndPreservesOnlineSkin() {
        ItemStack offline = Skulls.getPlayerSkull("OfflineSkin");
        offline.setAmount(5);
        assertEquals(1, Skulls.getPlayerSkull("offlineskin").getAmount());

        var player = server.addPlayer("OfflineSkin");
        var profile = player.getPlayerProfile();
        var texture = new ProfileProperty("textures", "custom-skin", "signature");
        profile.setProperty(texture);
        player.setPlayerProfile(profile);
        new PlayerListener().onPlayerJoin(new org.bukkit.event.player.PlayerJoinEvent(player, (net.kyori.adventure.text.Component) null));

        ItemStack online = Skulls.getPlayerSkull("OFFLINESKIN");
        assertNotNull(online);
        var skullProfile = ((SkullMeta) online.getItemMeta()).getPlayerProfile();
        assertNotNull(skullProfile);
        assertEquals(player.getUniqueId(), skullProfile.getId());
        assertTrue(skullProfile.getProperties().contains(texture));
    }

    @Test
    void invalidOwnerKeepsDefaultHeadPath() {
        assertNull(Skulls.getPlayerSkull(null));
        assertNull(Skulls.getPlayerSkull(""));
        assertNull(Skulls.getPlayerSkull("%missing owner%"));
        assertNull(Skulls.getPlayerSkull("longerThanSixteenCharacters"));
    }
}
