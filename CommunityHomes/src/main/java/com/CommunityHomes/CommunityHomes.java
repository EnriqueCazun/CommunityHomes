package com.CommunityHomes;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.*;

public class CommunityHomes extends JavaPlugin {

    private FileConfiguration config;
    private File configFile;
    private HashMap<String, Location> homes;

    @Override
    public void onEnable() {
        // Cargar configuración
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        homes = new HashMap<>();
        
        loadHomes();
        getLogger().info("Plugin de Homes Comunitarios activado!");
    }

    @Override
    public void onDisable() {
        if (config != null && homes != null) {
            saveHomes();
        }
        getLogger().info("Plugin de Homes Comunitarios desactivado!");
    }

    private void loadHomes() {
        if (config.getConfigurationSection("homes") != null) {
            for (String key : config.getConfigurationSection("homes").getKeys(false)) {
                Location loc = config.getLocation("homes." + key);
                if (loc != null && loc.getWorld() != null) {
                    homes.put(key, loc);
                }
            }
        }
    }

    private void saveHomes() {
        config.set("homes", null);
        if (homes != null) {
            homes.forEach((name, loc) -> {
                if (loc != null && loc.getWorld() != null) {
                    config.set("homes." + name, loc);
                }
            });
        }
        try {
            config.save(configFile);
        } catch (Exception e) {
            getLogger().severe("No se pudo guardar la configuración: " + e.getMessage());
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo los jugadores pueden usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()) {
            case "sethome":
                return setHome(player, args);
            case "home":
                return teleportHome(player, args);
            case "homes":
                return listHomes(player);
            case "delhome":
                return deleteHome(player, args);
            case "renamehome":
                return renameHome(player, args);
            default:
                return false;
        }
    }

    private boolean setHome(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Uso: /sethome <nombre>");
            return false;
        }

        String homeName = args[0];
        homes.put(homeName, player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' establecido!");
        return true;
    }

    private boolean teleportHome(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Uso: /home <nombre>");
            return false;
        }

        String homeName = args[0];
        Location loc = homes.get(homeName);

        if (loc == null) {
            player.sendMessage(ChatColor.RED + "El home '" + homeName + "' no existe!");
            return true;
        }

        if (loc.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Error: Mundo no encontrado!");
            return true;
        }

        player.teleport(loc);
        player.sendMessage(ChatColor.GREEN + "Teletransportado al home '" + homeName + "'!");
        return true;
    }

    private boolean listHomes(Player player) {
        if (homes.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No hay homes establecidos.");
            return true;
        }

        StringBuilder list = new StringBuilder(ChatColor.GOLD + "Homes disponibles:\n");
        homes.keySet().forEach(home -> list.append(ChatColor.WHITE).append("- ").append(home).append("\n"));
        player.sendMessage(list.toString());
        return true;
    }

    private boolean deleteHome(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Uso: /delhome <nombre>");
            return false;
        }

        String homeName = args[0];
        if (homes.remove(homeName) == null) {
            player.sendMessage(ChatColor.RED + "El home '" + homeName + "' no existe!");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' eliminado!");
        return true;
    }

    private boolean renameHome(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Uso: /renamehome <nombre-viejo> <nombre-nuevo>");
            return false;
        }

        String oldName = args[0];
        String newName = args[1];

        if (!homes.containsKey(oldName)) {
            player.sendMessage(ChatColor.RED + "El home '" + oldName + "' no existe!");
            return true;
        }

        if (homes.containsKey(newName)) {
            player.sendMessage(ChatColor.RED + "El home '" + newName + "' ya existe!");
            return true;
        }

        Location loc = homes.remove(oldName);
        homes.put(newName, loc);
        player.sendMessage(ChatColor.GREEN + "Home renombrado de '" + oldName + "' a '" + newName + "'!");
        return true;
    }
}