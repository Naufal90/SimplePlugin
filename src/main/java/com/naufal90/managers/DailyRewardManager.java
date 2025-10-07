package com.naufal90.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class DailyRewardManager {
    
    private final JavaPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, PlayerData> playerData;
    private final List<DailyReward> rewards;
    private boolean useServerTime;
    
    public DailyRewardManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "dailyrewards.yml");
        this.playerData = new HashMap<>();
        this.rewards = new ArrayList<>();
        this.useServerTime = plugin.getConfig().getBoolean("daily-reward.use-server-time", true);
        loadRewards();
        loadPlayerData();
    }
    
    public void loadRewards() {
        if (!dataFile.exists()) {
            plugin.saveResource("dailyrewards.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        rewards.clear();
        
        if (config.contains("rewards")) {
            for (String dayStr : config.getConfigurationSection("rewards").getKeys(false)) {
                try {
                    int day = Integer.parseInt(dayStr);
                    String path = "rewards." + dayStr;
                    
                    String name = config.getString(path + ".name", "Day " + day);
                    List<String> commands = config.getStringList(path + ".commands");
                    List<ItemStack> items = loadItems(config, path + ".items");
                    
                    DailyReward reward = new DailyReward(day, name, items, commands);
                    rewards.add(reward);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid day number in daily rewards: " + dayStr);
                }
            }
        }
        
        // Sort rewards by day
        rewards.sort(Comparator.comparingInt(DailyReward::getDay));
    }
    
    private List<ItemStack> loadItems(FileConfiguration config, String path) {
        List<ItemStack> items = new ArrayList<>();
        
        if (config.contains(path)) {
            for (String itemKey : config.getConfigurationSection(path).getKeys(false)) {
                String itemPath = path + "." + itemKey;
                
                Material material = Material.getMaterial(config.getString(itemPath + ".material", "STONE"));
                if (material == null) continue;
                
                int amount = config.getInt(itemPath + ".amount", 1);
                items.add(new ItemStack(material, amount));
            }
        }
        
        return items;
    }
    
    public void loadPlayerData() {
        File playerFile = new File(plugin.getDataFolder(), "players.yml");
        if (!playerFile.exists()) {
            return;
        }
        
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        playerData.clear();
        
        if (playerConfig.contains("players")) {
            for (String playerId : playerConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(playerId);
                String path = "players." + playerId + ".daily-reward";
                
                int streak = playerConfig.getInt(path + ".streak", 0);
                long lastClaim = playerConfig.getLong(path + ".last-claim", 0);
                int totalClaims = playerConfig.getInt(path + ".total-claims", 0);
                String lastClaimDate = playerConfig.getString(path + ".last-claim-date", "");
                
                PlayerData data = new PlayerData(streak, lastClaim, totalClaims, lastClaimDate);
                playerData.put(uuid, data);
            }
        }
    }
    
    public void savePlayerData() {
        File playerFile = new File(plugin.getDataFolder(), "players.yml");
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        
        playerConfig.set("players", null);
        
        for (Map.Entry<UUID, PlayerData> entry : playerData.entrySet()) {
            String path = "players." + entry.getKey().toString() + ".daily-reward";
            PlayerData data = entry.getValue();
            
            playerConfig.set(path + ".streak", data.getStreak());
            playerConfig.set(path + ".last-claim", data.getLastClaim());
            playerConfig.set(path + ".total-claims", data.getTotalClaims());
            playerConfig.set(path + ".last-claim-date", data.getLastClaimDate());
        }
        
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save players.yml: " + e.getMessage());
        }
    }
    
    public boolean canClaim(Player player) {
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return true;
        
        if (useServerTime) {
            // Use server date-based system
            return !isSameDay(data.getLastClaimDate(), getCurrentDate());
        } else {
        // Check if 24 hours have passed since last claim
        return System.currentTimeMillis() - data.getLastClaim() >= 86400000; // 24 hours
    }
  }
    
    public void claimReward(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerData data = playerData.get(playerId);
        String currentDate = getCurrentDate();
        
       if (data == null) {
            data = new PlayerData(1, System.currentTimeMillis(), 1, currentDate);
            playerData.put(playerId, data);
        } else {
            if (useServerTime) {
                // Server date-based streak system
                if (isConsecutiveDay(data.getLastClaimDate(), currentDate)) {
                    // Consecutive day - increase streak
                    data.setStreak(data.getStreak() + 1);
                } else if (!isSameDay(data.getLastClaimDate(), currentDate)) {
                    // Different day but not consecutive - reset streak
                    data.setStreak(1);
                }
                // Same day - don't change streak (shouldn't happen due to canClaim check)
            } else {
        // Check streak
        long timeSinceLastClaim = System.currentTimeMillis() - data.getLastClaim();
        if (timeSinceLastClaim > 172800000) { // More than 48 hours - reset streak
            data.setStreak(1);
        } else if (timeSinceLastClaim >= 86400000) { // 24-48 hours - continue streak
            data.setStreak(data.getStreak() + 1);
        }
     }
     
            data.setLastClaim(System.currentTimeMillis());
            data.setLastClaimDate(currentDate);
            data.setTotalClaims(data.getTotalClaims() + 1);
        }
        // Less than 24 hours - cannot claim yet
        
        int currentStreak = data.getStreak();
        DailyReward reward = getRewardForDay(currentStreak);
        
        if (reward != null) {
            // Give items
            for (ItemStack item : reward.getItems()) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
                for (ItemStack left : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), left);
                }
            }
            
            // Execute commands
            for (String command : reward.getCommands()) {
                String formattedCommand = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
            }
            
            player.sendMessage("§aYou have claimed your daily reward for day " + currentStreak + "!");
        } else {
            // Default reward if no specific reward for this day
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
            player.sendMessage("§aYou have claimed your daily reward for day " + currentStreak + "!");
        }
        
        data.setLastClaim(System.currentTimeMillis());
        data.setTotalClaims(data.getTotalClaims() + 1);
        savePlayerData();
    }
    
    private String getCurrentDate() {
        if (useServerTime) {
            return LocalDate.now(ZoneId.systemDefault()).toString();
        } else {
            return String.valueOf(System.currentTimeMillis());
        }
    }
    
    private boolean isSameDay(String lastDate, String currentDate) {
        return lastDate != null && lastDate.equals(currentDate);
    }
    
    private boolean isConsecutiveDay(String lastDate, String currentDate) {
        if (lastDate == null || lastDate.isEmpty()) return false;
        
        try {
            LocalDate last = LocalDate.parse(lastDate);
            LocalDate current = LocalDate.parse(currentDate);
            return last.plusDays(1).equals(current);
        } catch (Exception e) {
            return false;
        }
    }
    
    private DailyReward getRewardForDay(int day) {
        for (DailyReward reward : rewards) {
            if (reward.getDay() == day) {
                return reward;
            }
        }
        return null;
    }
    
    public PlayerData getPlayerData(UUID playerId) {
        return playerData.get(playerId);
    }
    
    public List<DailyReward> getRewards() {
        return new ArrayList<>(rewards);
    }
    
    public static class PlayerData {
        private int streak;
        private long lastClaim;
        private int totalClaims;
        private String lastClaimDate;
        
        public PlayerData(int streak, long lastClaim, int totalClaims, String lastClaimDate) {
            this.streak = streak;
            this.lastClaim = lastClaim;
            this.totalClaims = totalClaims;
            this.lastClaimDate = lastClaimDate;
        }
        
        public int getStreak() { return streak; }
        public long getLastClaim() { return lastClaim; }
        public int getTotalClaims() { return totalClaims; }
        public String getLastClaimDate() { return lastClaimDate; }
        
        public void setStreak(int streak) { this.streak = streak; }
        public void setLastClaim(long lastClaim) { this.lastClaim = lastClaim; }
        public void setTotalClaims(int totalClaims) { this.totalClaims = totalClaims; }
        public void setLastClaimDate(String lastClaimDate) { this.lastClaimDate = lastClaimDate; }
    }
    
    public static class DailyReward {
        private final int day;
        private final String name;
        private final List<ItemStack> items;
        private final List<String> commands;
        
        public DailyReward(int day, String name, List<ItemStack> items, List<String> commands) {
            this.day = day;
            this.name = name;
            this.items = items;
            this.commands = commands;
        }
        
        public int getDay() { return day; }
        public String getName() { return name; }
        public List<ItemStack> getItems() { return items; }
        public List<String> getCommands() { return commands; }
    }
}