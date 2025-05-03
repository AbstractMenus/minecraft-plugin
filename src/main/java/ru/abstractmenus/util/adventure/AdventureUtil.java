package ru.abstractmenus.util.adventure;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * Utility class for handling messages and titles in the game.
 * This class provides methods to send messages, action bars, and titles to players using MiniMessage.
 */
@UtilityClass
public class AdventureUtil {

    private static final Duration DEFAULT_FADE_IN = Duration.ofMillis(500);
    private static final Duration DEFAULT_STAY = Duration.ofSeconds(2);
    private static final Duration DEFAULT_FADE_OUT = Duration.ofMillis(500);

    private final MiniMessage customComponentSerializer = MiniMessage.builder()
            .preProcessor(new LegacyMiniMessagePreProcessor())
            .postProcessor(new LegacyMiniMessagePostProcessor())
            .tags(
                    TagResolver.resolver(
                            StandardTags.decorations(),
                            StandardTags.color(),
                            StandardTags.gradient(),
                            StandardTags.rainbow(),
                            StandardTags.transition(),
                            StandardTags.reset(),
                            StandardTags.newline()
                    )
            )
            .build();

    /**
     * Sends a message to the specified player.
     *
     * @param player     The player to whom the message will be sent.
     * @param message The message
     * @param resolvers  Additional tag resolvers for processing.
     */
    public void sendMessage(Player player, String message, TagResolver... resolvers) {
        Component component = customComponentSerializer.deserialize(message, resolvers);
        player.sendMessage(component);
    }

    /**
     * Sends an action bar message to the specified player.
     *
     * @param player     The player to whom the action bar message will be sent.
     * @param message The action bar message
     * @param resolvers  Additional tag resolvers for processing.
     */
    public void sendActionbar(Player player, String message, TagResolver... resolvers) {
        Component component = customComponentSerializer.deserialize(message, resolvers);
        player.sendActionBar(component);
    }

    /**
     * Sends a title to the specified player with custom fade in, stay, and fade out durations.
     *
     * @param player     The player to whom the title will be sent.
     * @param title      The title text
     * @param subtitle   The subtitle text
     * @param fadeIn    The duration for the title to fade in.
     * @param stay      The duration for the title to stay on screen.
     * @param fadeOut   The duration for the title to fade out.
     * @param tagResolvers Additional tag resolvers for processing.
     */
    public static void sendTitle(
            Player player,
            String title,
            String subtitle,
            Duration fadeIn,
            Duration stay,
            Duration fadeOut,
            TagResolver... tagResolvers
    ) {
        Component titleComponent = customComponentSerializer.deserialize(title, tagResolvers);
        Component subtitleComponent = customComponentSerializer.deserialize(subtitle, tagResolvers);

        Title fulltitle = Title.title(
                titleComponent,
                subtitleComponent,
                Title.Times.times(fadeIn, stay, fadeOut)
        );

        player.showTitle(fulltitle);
    }

    /**
     * Sends a title to the specified player with default fade in, stay, and fade out durations.
     *
     * @param player     The player to whom the title will be sent.
     * @param title      The title text
     * @param subtitle   The subtitle text
     * @param tagResolvers Additional tag resolvers for processing.
     */
    public static void sendTitle(
            Player player,
            String title,
            String subtitle,
            TagResolver... tagResolvers
    ) {
        sendTitle(player, title, subtitle, DEFAULT_FADE_IN, DEFAULT_STAY, DEFAULT_FADE_OUT, tagResolvers);
    }
}
