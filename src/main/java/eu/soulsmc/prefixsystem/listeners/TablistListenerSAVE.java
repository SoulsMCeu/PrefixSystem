package eu.soulsmc.prefixsystem.listeners;

import eu.soulsmc.prefixsystem.PrefixSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TablistListenerSAVE implements Listener {

    private final PrefixSystem prefixSystem;
    private final LegacyComponentSerializer legacySerializer;
    private final Map<Character, NamedTextColor> colorMap;

    public TablistListenerSAVE(@NotNull PrefixSystem prefixSystem) {
        this.prefixSystem = prefixSystem;
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
        this.colorMap = initColorMap();

        prefixSystem.getLuckPerms().getEventBus().subscribe(
                prefixSystem,
                UserDataRecalculateEvent.class,
                this::onUserDataRecalculate
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(prefixSystem, () -> {
            updatePlayer(event.getPlayer());
            Bukkit.getOnlinePlayers().forEach(this::updateNametag);
        }, 5L);
    }

    private void onUserDataRecalculate(@NotNull UserDataRecalculateEvent event) {
        UUID uuid = event.getUser().getUniqueId();
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            Bukkit.getScheduler().runTask(prefixSystem, () -> updatePlayer(player));
        }
    }

    private void updatePlayer(@NotNull Player player) {
        updateTablist(player);
        updateNametag(player);
    }

    private void updateTablist(@NotNull Player player) {
        Component prefix = prefixSystem.getPrefix(player);
        Component suffix = prefixSystem.getSuffix(player);
        String prefixString = legacySerializer.serialize(prefix);
        Component displayName = legacySerializer.deserialize(prefixString + player.getName()).append(suffix);

        player.playerListName(displayName);
        player.displayName(displayName);
    }

    private void updateNametag(@NotNull Player player) {
        String prefix = legacySerializer.serialize(prefixSystem.getPrefix(player));
        String suffix = legacySerializer.serialize(prefixSystem.getSuffix(player));
        NamedTextColor nameColor = extractLastColor(prefix);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            setTeam(viewer, player, prefix, suffix, nameColor);
        }
    }

    private void setTeam(@NotNull Player viewer, @NotNull Player target,
                         @NotNull String prefix, @NotNull String suffix, NamedTextColor color) {
        Scoreboard scoreboard = viewer.getScoreboard();
        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            viewer.setScoreboard(scoreboard);
        }

        String teamName = "nt_" + target.getName();
        teamName = teamName.length() > 16 ? teamName.substring(0, 16) : teamName;

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.color(color != null ? color : NamedTextColor.WHITE);
        team.prefix(prefix.isEmpty() ? Component.empty() : legacySerializer.deserialize(prefix + " "));
        team.suffix(suffix.isEmpty() ? Component.empty() : legacySerializer.deserialize(" " + suffix));

        if (!team.hasEntry(target.getName())) {
            team.addEntry(target.getName());
        }
    }

    private NamedTextColor extractLastColor(@NotNull String text) {
        NamedTextColor lastColor = null;
        for (int i = 0; i < text.length() - 1; i++) {
            char current = text.charAt(i);
            if (current == '&' || current == '§') {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (colorMap.containsKey(code)) {
                    lastColor = colorMap.get(code);
                }
            }
        }
        return lastColor;
    }

    private Map<Character, NamedTextColor> initColorMap() {
        Map<Character, NamedTextColor> map = new HashMap<>();
        map.put('0', NamedTextColor.BLACK);
        map.put('1', NamedTextColor.DARK_BLUE);
        map.put('2', NamedTextColor.DARK_GREEN);
        map.put('3', NamedTextColor.DARK_AQUA);
        map.put('4', NamedTextColor.DARK_RED);
        map.put('5', NamedTextColor.DARK_PURPLE);
        map.put('6', NamedTextColor.GOLD);
        map.put('7', NamedTextColor.GRAY);
        map.put('8', NamedTextColor.DARK_GRAY);
        map.put('9', NamedTextColor.BLUE);
        map.put('a', NamedTextColor.GREEN);
        map.put('b', NamedTextColor.AQUA);
        map.put('c', NamedTextColor.RED);
        map.put('d', NamedTextColor.LIGHT_PURPLE);
        map.put('e', NamedTextColor.YELLOW);
        map.put('f', NamedTextColor.WHITE);
        return map;
    }
}