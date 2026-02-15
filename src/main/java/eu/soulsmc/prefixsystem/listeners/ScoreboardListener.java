package eu.soulsmc.prefixsystem.listeners;

import eu.soulsmc.prefixsystem.PrefixSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardListener implements Listener {

    private static final Map<Character, NamedTextColor> COLOR_MAP = Map.ofEntries(
            Map.entry('0', NamedTextColor.BLACK),
            Map.entry('1', NamedTextColor.DARK_BLUE),
            Map.entry('2', NamedTextColor.DARK_GREEN),
            Map.entry('3', NamedTextColor.DARK_AQUA),
            Map.entry('4', NamedTextColor.DARK_RED),
            Map.entry('5', NamedTextColor.DARK_PURPLE),
            Map.entry('6', NamedTextColor.GOLD),
            Map.entry('7', NamedTextColor.GRAY),
            Map.entry('8', NamedTextColor.DARK_GRAY),
            Map.entry('9', NamedTextColor.BLUE),
            Map.entry('a', NamedTextColor.GREEN),
            Map.entry('b', NamedTextColor.AQUA),
            Map.entry('c', NamedTextColor.RED),
            Map.entry('d', NamedTextColor.LIGHT_PURPLE),
            Map.entry('e', NamedTextColor.YELLOW),
            Map.entry('f', NamedTextColor.WHITE)
    );

    private static final Pattern COLOR_PATTERN = Pattern.compile("(§x(§[0-9a-fA-F]){6})|(§[0-9a-fA-Fk-orK-OR])");

    private final PrefixSystem prefixSystem;
    private final LegacyComponentSerializer legacySerializer;

    public ScoreboardListener(@NotNull PrefixSystem prefixSystem) {
        this.prefixSystem = prefixSystem;
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(prefixSystem, () -> {
            updateNametag(event.getPlayer());
            Bukkit.getOnlinePlayers().forEach(this::updateNametag);
        }, 5L);
    }

    public void updateNametag(@NotNull Player player) {
        String prefix = legacySerializer.serialize(prefixSystem.getPrefix(player));
        String suffix = legacySerializer.serialize(prefixSystem.getSuffix(player));
        NamedTextColor nameColor = extractLastColor(prefix);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            setTeam(viewer, player, prefix, suffix, nameColor);
        }
    }

    private void setTeam(@NotNull Player viewer, @NotNull Player target,
                         @NotNull String prefix, @NotNull String suffix, @NotNull NamedTextColor color) {
        Scoreboard scoreboard = getOrCreateScoreboard(viewer);
        String teamName = generateTeamName(target);
        Team team = getOrCreateTeam(scoreboard, teamName);

        team.color(color);
        team.prefix(prefix.isEmpty() ? Component.empty() : legacySerializer.deserialize(prefix));
        team.suffix(suffix.isEmpty() ? Component.empty() : legacySerializer.deserialize(suffix));

        if (!team.hasEntry(target.getName())) {
            team.addEntry(target.getName());
        }
    }

    @NotNull
    private Scoreboard getOrCreateScoreboard(@NotNull Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }
        return scoreboard;
    }

    @NotNull
    private String generateTeamName(@NotNull Player player) {
        String name = "nt_" + player.getName();
        return name.length() > 16 ? name.substring(0, 16) : name;
    }

    @NotNull
    private Team getOrCreateTeam(@NotNull Scoreboard scoreboard, @NotNull String teamName) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        return team;
    }

    @NotNull
    private NamedTextColor extractLastColor(@NotNull String text) {
        if (text.isEmpty()) {
            return NamedTextColor.WHITE;
        }

        Matcher matcher = COLOR_PATTERN.matcher(text);
        String lastColorCode = null;

        while (matcher.find()) {
            lastColorCode = matcher.group();
        }

        if (lastColorCode != null && lastColorCode.length() >= 2) {
            char code = Character.toLowerCase(lastColorCode.charAt(1));
            return COLOR_MAP.getOrDefault(code, NamedTextColor.WHITE);
        }

        return NamedTextColor.WHITE;
    }
}
