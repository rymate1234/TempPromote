package net.rymate.temp;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class that holds all of the currently active promotions.
 * <p>
 * This class is saved and loaded to yaml files
 * <p>
 * Created by rymate1234 on 27/07/2016.
 */
public class PromotionStore implements ConfigurationSerializable {
    private static PromotionStore instance;
    public File path;

    private List<Promotion> promotions;
    private List<UuidWrap> users;

    private Permission permission = null;
    private YamlConfiguration config;

    private PromotionStore() {
        promotions = new ArrayList<Promotion>();
        users = new ArrayList<UuidWrap>();
    }

    public static PromotionStore getInstance() {
        if (instance != null) {
            return instance;
        }

        return null; // this should never happen
    }

    public static PromotionStore createInstance(PromotePlugin promotePlugin) throws IOException {
        PromotionStore store = null;

        if (!promotePlugin.getDataFolder().exists()) promotePlugin.getDataFolder().mkdir();

        File path = new File(promotePlugin.getDataFolder() + "/promotions.yml");

        if (path.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(path);
            store = (PromotionStore) config.get("promotions");
            store.config = config;
            store.path = path;
        } else {
            boolean created = path.createNewFile();
            store = new PromotionStore();

            if (created) {
                store.config = YamlConfiguration.loadConfiguration(path);
                store.path = path;
                store.config.set("promotions", store);
                store.config.save(path);
            }
        }

        RegisteredServiceProvider<Permission> permissionProvider = promotePlugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            store.permission = permissionProvider.getProvider();
        }

        final PromotionStore finalStore = store;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(promotePlugin, new Runnable() {
            public void run() {
                finalStore.checkPromotions();
            }
        },0, 1200);

        instance = store;
        return store;
    }

    private void checkPromotions() {
        Date now = new Date(); // get the current date
        List<Promotion> toRemove = new ArrayList<Promotion>();

        for (Promotion promotion : promotions) {
            if (now.after(promotion.getTime())) {
                promotion.logExpirary();
                toRemove.add(promotion);
            }
        }

        for (Promotion promotion : toRemove) {
            removePromotion(promotion);
        }
    }

    public Map<String, Object> serialize() {
        final Map<String, Object> serialized = new LinkedHashMap<String, Object>();
        serialized.put("users", users);
        serialized.put("list", promotions);
        return serialized;
    }

    public static PromotionStore deserialize(final Map<String, Object> args) {
        PromotionStore store = new PromotionStore();
        store.promotions = (List<Promotion>) args.get("list");
        store.users = (List<UuidWrap>) args.get("users");

        if (!args.containsKey("list")) {
            store.promotions = new ArrayList<Promotion>();
        }

        if (!args.containsKey("users")) {
            store.users = new ArrayList<UuidWrap>();
        }

        return store;
    }

    void savePromotions() {
        config.set("promotions", this);
        try {
            config.save(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Promotion> listPromotions() {
        return new ArrayList<Promotion>(promotions);
    }

    public List<Promotion> listPromotions(UUID toGet) {
        List<Promotion> toReturn =  new ArrayList<Promotion>();

        for (Promotion p : new ArrayList<Promotion>(promotions)) {
            if (p.getUuid().equals(toGet)) toReturn.add(p);
        }
        return toReturn;
    }


    public boolean hasPromotions(UUID toCheck) {
        return users.contains(new UuidWrap(toCheck));
    }

    public boolean addPromotion(Promotion p) {
        if (promote(p)) {
            promotions.add(p);
            users.add(new UuidWrap(p.getUuid()));

            savePromotions();
            return true;
        }
        return false;
    }

    private boolean removePromotion(Promotion p) {
        if (demote(p)) {
            promotions.remove(p);
            users.remove(new UuidWrap(p.getUuid()));

            savePromotions();
            return true;
        }
        return false;
    }

    private boolean promote(Promotion promotion) {
        Promotion.PromotionType type = promotion.getType();
        OfflinePlayer player = Bukkit.getOfflinePlayer(promotion.getUuid());

        for (String world : promotion.getWorlds()) {
            if (type == Promotion.PromotionType.TEMP_PROMOTION) {
                return permission.playerAddGroup(world, player, promotion.getValue());
            } else if (type == Promotion.PromotionType.TEMP_DEMOTION) {
                return permission.playerRemoveGroup(world, player, promotion.getValue());
            } else if (type == Promotion.PromotionType.TEMP_PERMISSION) {
                return permission.playerAdd(world, player, promotion.getValue());
            }
        }

        return false;
    }

    private boolean demote(Promotion promotion) {
        Promotion.PromotionType type = promotion.getType();
        OfflinePlayer player = Bukkit.getOfflinePlayer(promotion.getUuid());

        for (String world : promotion.getWorlds()) {
            if (type == Promotion.PromotionType.TEMP_PROMOTION) {
                return permission.playerRemoveGroup(world, player, promotion.getValue());
            } else if (type == Promotion.PromotionType.TEMP_DEMOTION) {
                return permission.playerAddGroup(world, player, promotion.getValue());
            } else if (type == Promotion.PromotionType.TEMP_PERMISSION) {
                return permission.playerRemove(world, player, promotion.getValue());
            }
        }

        return false;
    }

}
