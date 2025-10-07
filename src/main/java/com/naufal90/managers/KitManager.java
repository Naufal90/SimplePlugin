package com.naufal90.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitManager {
    
    private final JavaPlugin plugin;
    private final File kitsFile;
    private FileConfiguration kitsConfig;
    private final Map<String, Kit> kits;
    private final Map<UUID, Map<String, Long>> playerCooldowns;
    
    public KitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        this.kits = new HashMap<>();
        this.playerCooldowns = new HashMap<>();
        loadKits();
    }
    
    public void loadKits() {
        if (!kitsFile.exists()) {
            plugin.saveResource("kits.yml", false);
        }
        
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        kits.clear();
        
        if (kitsConfig.contains("kits")) {
            for (String kitName : kitsConfig.getConfigurationSection("kits").getKeys(false)) {
                String path = "kits." + kitName;
                
                String displayName = kitsConfig.getString(path + ".display-name", kitName);
                int cooldown = kitsConfig.getInt(path + ".cooldown", 86400); // Default 24 jam
                boolean giveOnFirstJoin = kitsConfig.getBoolean(path + ".give-on-first-join", false);
                
                List<ItemStack> items = new ArrayList<>();
                if (kitsConfig.contains(path + ".items")) {
                    for (String itemKey : kitsConfig.getConfigurationSection(path + ".items").getKeys(false)) {
                        String itemPath = path + ".items." + itemKey;
                        
                        Material material = Material.getMaterial(kitsConfig.getString(itemPath + ".material", "STONE"));
                        if (material == null) continue;
                        
                        int amount = kitsConfig.getInt(itemPath + ".amount", 1);
                        String name = kitsConfig.getString(itemPath + ".name");
                        List<String> lore = kitsConfig.getStringList(itemPath + ".lore");
                        
                        ItemStack item = new ItemStack(material, amount);
                        ItemMeta meta = item.getItemMeta();
                        
                        if (name != null) {
                            meta.setDisplayName(name.replace('&', '§'));
                        }
                        
                        if (lore != null && !lore.isEmpty()) {
                            List<String> coloredLore = new ArrayList<>();
                            for (String line : lore) {
                                coloredLore.add(line.replace('&', '§'));
                            }
                            meta.setLore(coloredLore);
                        }
                        
                        // Enchantments
                        if (kitsConfig.contains(itemPath + ".enchantments")) {
                            for (String enchantKey : kitsConfig.getConfigurationSection(itemPath + ".enchantments").getKeys(false)) {
                                Enchantment enchantment = Enchantment.getByName(enchantKey);
                                if (enchantment != null) {
                                    int level = kitsConfig.getInt(itemPath + ".enchantments." + enchantKey);
                                    meta.addEnchant(enchantment, level, true);
                                }
                            }
                        }
                        
                        item.setItemMeta(meta);
                        items.add(item);
                    }
                }
                
                Kit kit = new Kit(kitName, displayName, items, cooldown, giveOnFirstJoin);
                kits.put(kitName.toLowerCase(), kit);
            }
        }
        
        // Default starter kit jika tidak ada kit
        if (kits.isEmpty()) {
            createDefaultStarterKit();
        }
    }
    
    private void createDefaultStarterKit() {
        List<ItemStack> items = Arrays.asList(
            new ItemStack(Material.STONE_SWORD, 1),
            new ItemStack(Material.STONE_PICKAXE, 1),
            new ItemStack(Material.STONE_AXE, 1),
            new ItemStack(Material.STONE_SHOVEL, 1),
            new ItemStack(Material.BREAD, 16)
        );
        
        Kit starterKit = new Kit("starter", "&aStarter Kit", items, 86400, true);
        kits.put("starter", starterKit);
    }
    
    public boolean giveKit(Player player, String kitName) {
        Kit kit = kits.get(kitName.toLowerCase());
        if (kit == null) {
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Check cooldown
        if (hasCooldown(playerId, kitName)) {
            long remaining = getCooldownRemaining(playerId, kitName);
            player.sendMessage("§cYou must wait " + formatTime(remaining) + " before using this kit again.");
            return false;
        }
        
        // Give items
        for (ItemStack item : kit.getItems()) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
            // Drop leftover items if inventory is full
            for (ItemStack left : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
        }
        
        // Set cooldown
        setCooldown(playerId, kitName, kit.getCooldown());
        
        player.sendMessage("§aYou have received the " + kit.getDisplayName() + "§a!");
        return true;
    }
    
    public void giveFirstJoinKits(Player player) {
        for (Kit kit : kits.values()) {
            if (kit.isGiveOnFirstJoin()) {
                giveKit(player, kit.getName());
            }
        }
    }
    
    public List<String> getAvailableKits(Player player) {
        List<String> available = new ArrayList<>();
        UUID playerId = player.getUniqueId();
        
        for (Kit kit : kits.values()) {
            if (!hasCooldown(playerId, kit.getName())) {
                available.add(kit.getName());
            }
        }
        
        return available;
    }
    
    public Map<String, Kit> getKits() {
        return new HashMap<>(kits);
    }
    
    private boolean hasCooldown(UUID playerId, String kitName) {
        Map<String, Long> playerKits = playerCooldowns.get(playerId);
        if (playerKits == null) return false;
        
        Long cooldownEnd = playerKits.get(kitName);
        return cooldownEnd != null && cooldownEnd > System.currentTimeMillis();
    }
    
    private long getCooldownRemaining(UUID playerId, String kitName) {
        Map<String, Long> playerKits = playerCooldowns.get(playerId);
        if (playerKits == null) return 0;
        
        Long cooldownEnd = playerKits.get(kitName);
        return cooldownEnd != null ? (cooldownEnd - System.currentTimeMillis()) / 1000 : 0;
    }
    
    private void setCooldown(UUID playerId, String kitName, long cooldownSeconds) {
        Map<String, Long> playerKits = playerCooldowns.computeIfAbsent(playerId, k -> new HashMap<>());
        playerKits.put(kitName, System.currentTimeMillis() + (cooldownSeconds * 1000));
    }
    
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes";
        } else {
            return (seconds / 3600) + " hours";
        }
    }
    
    public static class Kit {
        private final String name;
        private final String displayName;
        private final List<ItemStack> items;
        private final long cooldown;
        private final boolean giveOnFirstJoin;
        
        public Kit(String name, String displayName, List<ItemStack> items, long cooldown, boolean giveOnFirstJoin) {
            this.name = name;
            this.displayName = displayName;
            this.items = items;
            this.cooldown = cooldown;
            this.giveOnFirstJoin = giveOnFirstJoin;
        }
        
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public List<ItemStack> getItems() { return items; }
        public long getCooldown() { return cooldown; }
        public boolean isGiveOnFirstJoin() { return giveOnFirstJoin; }
    }
}