package eu.soulsmc.prefixsystem.listeners;

import eu.soulsmc.prefixsystem.PrefixSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class TablistListener implements Listener {

    private final PrefixSystem prefixSystem;
    private final LegacyComponentSerializer legacySerializer;

    public TablistListener(@NotNull PrefixSystem prefixSystem) {
        this.prefixSystem = prefixSystem;
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updateTablist(player);
    }

    /**
     * Updates the tablist display for a player
     * @param player The player to update
     */
    public void updateTablist(@NotNull Player player) {
        Component prefix = prefixSystem.getPrefix(player);
        Component suffix = prefixSystem.getSuffix(player);

        // Get prefix as legacy string to preserve color continuation
        String prefixString = legacySerializer.serialize(prefix);
        Component displayName = legacySerializer.deserialize(prefixString + player.getName()).append(suffix);

        player.playerListName(displayName);
    }
}