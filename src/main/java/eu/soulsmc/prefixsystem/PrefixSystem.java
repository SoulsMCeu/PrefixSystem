package eu.soulsmc.prefixsystem;

import eu.soulsmc.prefixsystem.listeners.ChatListener;
import eu.soulsmc.prefixsystem.listeners.LuckPermsListener;
import eu.soulsmc.prefixsystem.listeners.ScoreboardListener;
import eu.soulsmc.prefixsystem.listeners.TablistListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PrefixSystem extends JavaPlugin {

    private TablistListener tablistListener;
    private ScoreboardListener scoreboardListener;
    private LuckPerms luckPerms;
    private LegacyComponentSerializer legacySerializer;

    @Override
    public void onEnable() {
        super.onEnable();

        // LuckPerms detection
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().severe("LuckPerms not found! Disabling plugin.");
            getLogger().severe("Please install LuckPerms to get working prefixes!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        luckPerms = LuckPermsProvider.get();
        legacySerializer = LegacyComponentSerializer.legacyAmpersand();

        getLogger().info("LuckPerms API hooked successfully!");

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new TablistListener(this), this);
        pluginManager.registerEvents(new ScoreboardListener(this), this);
        pluginManager.registerEvents(new LuckPermsListener(this), this);
    }


    @Override
    public void onDisable() {
        super.onDisable();
    }

    @NotNull
    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    @NotNull
    public LegacyComponentSerializer getLegacySerializer() {
        return legacySerializer;
    }

    /**
     * Gets the prefix of a player from LuckPerms
     * @param player The player
     * @return Component with the formatted prefix
     */
    @NotNull
    public Component getPrefix(@NotNull Player player) {
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        String prefix = user.getCachedData().getMetaData().getPrefix();

        if (prefix == null || prefix.isEmpty()) {
            return Component.empty();
        }

        return legacySerializer.deserialize(prefix);
    }

    /**
     * Gets the suffix of a player from LuckPerms
     * @param player The player
     * @return Component with the formatted suffix
     */
    @NotNull
    public Component getSuffix(@NotNull Player player) {
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        String suffix = user.getCachedData().getMetaData().getSuffix();

        if (suffix == null || suffix.isEmpty()) {
            return Component.empty();
        }

        return legacySerializer.deserialize(suffix);
    }

    public TablistListener getTablistListener() {
        return tablistListener;
    }

    public ScoreboardListener getScoreboardListener() {
        return scoreboardListener;
    }
}
