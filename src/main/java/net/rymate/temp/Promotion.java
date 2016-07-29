package net.rymate.temp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Class that stores a promotion.
 *
 * It stores the user's name, UUID, value that was added, type of promotion, and the time to demote them.
 *
 * Created by Ryan on 27/07/2016.
 */
public class Promotion implements ConfigurationSerializable {
    private String username;
    private List<String> worlds;
    private UuidWrap uuid;
    private String value;
    private PromotionType type;
    private Date time;

    public enum PromotionType {
        TEMP_PROMOTION("%player was promoted to %value"),
        TEMP_DEMOTION("%player was demoted to %value"),
        TEMP_PERMISSION("%player was given the permission %value");

        private final String message;

        PromotionType(String message) {
            this.message = message;
        }

        public void confirmPromotion(CommandSender sender, OfflinePlayer player, Promotion promotion) {
            String msg = replacePlaceholders(promotion, message);

            sender.sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + msg);

            if (player.isOnline()) {
                String replace = player.getName() + " was";
                ((Player) player).sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + msg.replaceAll(replace, "You were"));
            }
        }
    }

    //region Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUuid() {
        return uuid.get();
    }

    public void setUuid(UUID uuid) {
        this.uuid = new UuidWrap(uuid);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public PromotionType getType() {
        return type;
    }

    public void setType(PromotionType type) {
        this.type = type;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public void setWorlds(List<String> worlds) {
        this.worlds = worlds;
    }

    //endregion

    public static String replacePlaceholders(Promotion promotion, String message) {
        return message.replaceAll("%player", promotion.getUsername())
                .replaceAll("%type", promotion.getType().toString())
                .replaceAll("%date", promotion.getTime().toString())
                .replaceAll("%timeleft", promotion.printDifference())
                .replaceAll("%value", promotion.getValue());
    }

    public void logExpirary() {
        String message = ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "The promotion for " + getUsername() + "has expired!";
        Bukkit.getServer().getConsoleSender().sendMessage(message);
        OfflinePlayer player = Bukkit.getOfflinePlayer(getUuid());
        if (player.isOnline()) {
            Bukkit.getPlayer(getUuid()).sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "Your promotion has expired!");
        }
    }

    public Map<String, Object> serialize() {
        final Map<String, Object> serialized = new LinkedHashMap<String, Object>();
        serialized.put("username", username);
        serialized.put("uuid", uuid);
        serialized.put("worlds", worlds);
        serialized.put("value", value);
        serialized.put("type", type.toString());
        serialized.put("time", time);
        return serialized;
    }

    public static Promotion deserialize(final Map<String, Object> args) {
        Promotion promotion = new Promotion();
        if (!args.containsKey("username")) {
            return null;
        }
        promotion.setUsername((String) args.get("username"));
        promotion.setUuid(((UuidWrap) args.get("uuid")).get());
        promotion.setValue((String) args.get("value"));
        promotion.setWorlds((List<String>) args.get("worlds"));
        promotion.setType(PromotionType.valueOf((String) args.get("type")));
        promotion.setTime((Date) args.get("time"));

        return promotion;
    }

    // thanks http://www.mkyong.com/java/java-time-elapsed-in-days-hours-minutes-seconds/
    public String printDifference() {
        Date startDate = new Date();
        Date endDate = getTime();

        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        Formatter formatter = new Formatter();

        return formatter.format(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays,
                elapsedHours, elapsedMinutes, elapsedSeconds).toString();
    }

    @Override
    public String toString() {
        String info = "Promotion for: %player | Type: %type | Given: %value | Until: %date | Time Left: %timeleft";
        return replacePlaceholders(this, info);
    }
}
