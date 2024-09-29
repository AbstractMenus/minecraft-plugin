package ru.abstractmenus.nms.actionbar;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class ActionBar_1_8 extends ActionBar {

    private MethodHandle chatComponentConstructor;
    private MethodHandle packetConstructor;

    public ActionBar_1_8() throws ReflectiveOperationException {
        Class<?> packetClass = getNMSClass("PacketPlayOutChat");
        Class<?> chatComponentTextClass = getNMSClass("ChatComponentText");
        Class<?> iChatBaseComponentClass = getNMSClass("IChatBaseComponent");
        chatComponentConstructor = MethodHandles.lookup()
                .unreflectConstructor(chatComponentTextClass.getConstructor(String.class));
        packetConstructor = MethodHandles.lookup()
                .unreflectConstructor(packetClass.getConstructor(iChatBaseComponentClass, byte.class));
    }

    @Override
    public void send(Player player, String message) {
        try{
            Object chatComponentText = chatComponentConstructor.invoke(message);
            Object packet = packetConstructor.invoke(chatComponentText, (byte) 2);
            sendPacket(player, packet);
        } catch (Throwable e){
            Logger.severe("Cannot send actionbar message: " + e.getMessage());
        }

    }
}
