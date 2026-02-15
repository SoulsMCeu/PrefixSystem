package eu.soulsmc.prefixsystem.listeners;

import eu.soulsmc.prefixsystem.PrefixSystem;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LuckPermsListener implements Listener {

    private final PrefixSystem prefixSystem;
    private final TablistListener tablistListener;
    private final ScoreboardListener scoreboardListener;
    private final LegacyComponentSerializer legacySerializer;

    public LuckPermsListener(@NotNull PrefixSystem prefixSystem) {
        this.prefixSystem = prefixSystem;
        this.scoreboardListener = this.prefixSystem.getScoreboardListener();
        this.tablistListener = this.prefixSystem.getTablistListener();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();

        // Register LuckPerms event listener for permission updates
        prefixSystem.getLuckPerms().getEventBus().subscribe(
                prefixSystem,
                UserDataRecalculateEvent.class,
                this::onUserDataRecalculate
        );
    }

    /**
     * Called when LuckPerms recalculates user data (prefix/suffix changes)
     * @param event The LuckPerms event
     */
    private void onUserDataRecalculate(@NotNull UserDataRecalculateEvent event) {
        UUID uuid = event.getUser().getUniqueId();
        Player player = Bukkit.getPlayer(uuid);

        if (player != null && player.isOnline()) {
            tablistListener.updateTablist(player);
            scoreboardListener.updateNametag(player);
        }
    }
}
