package ru.abstractmenus.nms.title;

import org.bukkit.entity.Player;
import ru.abstractmenus.util.NMS;

import java.lang.reflect.Constructor;

/**
 * Title sender for Spigot < 1.10
 */
public class SenderLegacy implements TitleSender {

    @Override
    public void send(Player player, Title title) {
        Class<?> typeClass, chatClass, titleClass;
        Object chatTitle;
        Object chatSubtitle;
        Object titleComp, subtitleComp, timesComp;

        try{
            chatClass = NMS.getNMSClass("IChatBaseComponent");
            titleClass = NMS.getNMSClass("PacketPlayOutTitle");
            typeClass = titleClass.getClasses()[0];
            chatTitle = chatClass.getClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title.getTitle() + "\"}");
            chatSubtitle = chatClass.getClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title.getSubtitle() + "\"}");

            Constructor<?> titleConstr = titleClass.getConstructor(typeClass, chatClass);
            Constructor<?> timesConstr = titleClass.getConstructor(int.class, int.class, int.class);

            titleComp = titleConstr.newInstance(typeClass.getField("TITLE").get(null), chatTitle);
            subtitleComp = titleConstr.newInstance(typeClass.getField("SUBTITLE").get(null), chatSubtitle);
            timesComp = timesConstr.newInstance(title.getFadeIn(), title.getStay(), title.getFadeOut());

            NMS.sendPacket(player, titleComp);
            NMS.sendPacket(player, subtitleComp);
            NMS.sendPacket(player, timesComp);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
