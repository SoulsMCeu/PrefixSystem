package eu.soulsmc.prefixsystem.listeners;

import eu.soulsmc.prefixsystem.PrefixSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TablistListener implements Listener {

    private final PrefixSystem prefixSystem;
    private final LegacyComponentSerializer legacySerializer;

    public TablistListener(@NotNull PrefixSystem prefixSystem) {
        this.prefixSystem = prefixSystem;
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();

        // Register LuckPerms event listener for permission updates
        prefixSystem.getLuckPerms().getEventBus().subscribe(
                prefixSystem,
                UserDataRecalculateEvent.class,
                this::onUserDataRecalculate
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updateTablist(player);
    }

    /**
     * Called when LuckPerms recalculates user data (prefix/suffix changes)
     * @param event The LuckPerms event
     */
    private void onUserDataRecalculate(@NotNull UserDataRecalculateEvent event) {
        UUID uuid = event.getUser().getUniqueId();
        Player player = Bukkit.getPlayer(uuid);

        if (player != null && player.isOnline()) {
            updateTablist(player);
        }
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
        Component displayName = legacySerializer.deserialize(prefixString + player.getName());

        player.playerListName(displayName.append(suffix));
    }
}