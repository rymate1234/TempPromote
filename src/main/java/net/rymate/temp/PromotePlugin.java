package net.rymate.temp;

import net.rymate.temp.Promotion.PromotionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Plugin that temporarily promotes people for a specified amount of time
 *
 * Created by rymate1234 on 27/07/2016.
 */

public class PromotePlugin extends JavaPlugin {
    PromotionStore store;

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(PromotionStore.class);
        ConfigurationSerialization.registerClass(Promotion.class);
        ConfigurationSerialization.registerClass(UuidWrap.class);

        try {
            store = PromotionStore.createInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "Enabled!");
    }

    @Override
    public void onDisable() {
        store.savePromotions();
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "Disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Promotion promotion = new Promotion();

        if (command.getName().startsWith("temp")) {
            if (!checkPermission(sender, "temppromote.promote")) {
                sender.sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "You don't have permission!");
                return false;
            }

            if (args.length < 3) {
                return false;
            }

            OfflinePlayer player;
            if (isUUID(args[0])) {
                player = getServer().getOfflinePlayer(UUID.fromString(args[0]));
            } else {
                player = getServer().getOfflinePlayer(args[0]);
            }

            List<String> worlds = new ArrayList<String>();

            if (args.length == 4) {
                worlds.addAll(Arrays.asList(args[4].split(",")));
            } else for (World world : getServer().getWorlds()) {
                worlds.add(world.getName());
            }

            promotion.setUsername(player.getName());
            promotion.setUuid(player.getUniqueId());
            promotion.setWorlds(worlds);
            promotion.setValue(args[1]);
            promotion.setTime(Utils.parseTime(args[2]));

            if (command.getName().equalsIgnoreCase("temppromote")) {
                promotion.setType(PromotionType.TEMP_PROMOTION);
            } else if (command.getName().equalsIgnoreCase("tempdemote")) {
                promotion.setType(PromotionType.TEMP_DEMOTION);
            } else if (command.getName().equalsIgnoreCase("temppermission")) {
                promotion.setType(PromotionType.TEMP_PERMISSION);
            }

            store.addPromotion(promotion);
            promotion.getType().confirmPromotion(sender, player, promotion);
        } else if (command.getName().equals("promotionslist")) {
            if (!checkPermission(sender, "temppromote.list")) {
                sender.sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "You don't have permission!");
                return false;
            }

            sender.sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "Here's a list of all active promotions");
            for (Promotion p : store.listPromotions()) {
                sender.sendMessage(p.toString());
            }
        } else if (command.getName().equals("promotioncheck")) {
            UUID toCheck;

            if (!checkPermission(sender, "temppromote.check")) {
                sender.sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "You don't have permission!");
                return false;
            }

            if (args.length == 1) {
                if (!checkPermission(sender, "temppromote.check.other")) {
                    sender.sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "You don't have permission to check other players!");
                    return false;
                }

                if (isUUID(args[0])) {
                    toCheck = UUID.fromString(args[0]);
                } else {
                    toCheck = getServer().getOfflinePlayer(args[0]).getUniqueId();
                }
            } else if (sender instanceof Player) {
                toCheck = ((Player) sender).getUniqueId();
            } else {
                sender.sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "Nothing to check! Please specify a username or UUID");
                return false;
            }

            if (store.hasPromotions(toCheck)) {
                OfflinePlayer player = getServer().getOfflinePlayer(toCheck);

                sender.sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "Here's a list of the promotions for " + player.getName());
                for (Promotion p : store.listPromotions(toCheck)) {
                    sender.sendMessage(p.toString());
                }
            } else {
                sender.sendMessage(ChatColor.AQUA + "[TempPromote] " + ChatColor.WHITE + "No active promotions found!" );
            }

        }

        return true;
    }

    public boolean checkPermission(CommandSender sender, String permission) {
        return sender instanceof ConsoleCommandSender || sender.isOp() || sender.hasPermission(permission);
    }

    public boolean isUUID(String string) {
        // use the uuid check from the java source code
        String[] components = string.split("-");
        return components.length == 5;
    }

}
