package eu.soulsmc.prefixsystem.listeners;

import eu.soulsmc.prefixsystem.PrefixSystem;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener {

    private final PrefixSystem prefixSystem;
    private final LegacyComponentSerializer legacySerializer;

    public ChatListener(@NotNull PrefixSystem prefixSystem) {
        this.prefixSystem = prefixSystem;
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(@NotNull AsyncChatEvent event) {
        Player player = event.getPlayer();

        event.renderer((source, displayName, message, viewer) -> {
            Component chatMessage = message.colorIfAbsent(NamedTextColor.GRAY);
            if (player.hasPermission("prefixsystem.chat.color")) {
                String messageText = legacySerializer.serialize(message);
                chatMessage = legacySerializer.deserialize(messageText).colorIfAbsent(NamedTextColor.GRAY);
            }

            // Get prefix as legacy string to preserve color continuation
            String prefixString = legacySerializer.serialize(prefixSystem.getPrefix(source));

            return legacySerializer.deserialize(prefixString + source.getName())
                    .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                    .append(chatMessage);
        });
    }

}
