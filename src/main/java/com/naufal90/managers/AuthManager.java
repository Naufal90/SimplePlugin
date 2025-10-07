package com.naufal90.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AuthManager {
    
    private final JavaPlugin plugin;
    private final File authFile;
    private FileConfiguration authConfig;
    private final Set<UUID> loggedInPlayers;
    private final Set<UUID> registeredPlayers;
    private final Map<UUID, String> pendingRegistrations;
    private final Map<UUID, Long> loginTimeouts;
    
    public AuthManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.authFile = new File(plugin.getDataFolder(), "auth.yml");
        this.loggedInPlayers = new HashSet<>();
        this.registeredPlayers = new HashSet<>();
        this.pendingRegistrations = new HashMap<>();
        this.loginTimeouts = new HashMap<>();
        loadAuthData();
    }
    
    public void loadAuthData() {
        if (!authFile.exists()) {
            try {
                authFile.getParentFile().mkdirs();
                authFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create auth.yml: " + e.getMessage());
            }
        }
        
        authConfig = YamlConfiguration.loadConfiguration(authFile);
        registeredPlayers.clear();
        
        if (authConfig.contains("players")) {
            for (String playerId : authConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(playerId);
                registeredPlayers.add(uuid);
            }
        }
    }
    
    public void saveAuthData() {
        try {
            authConfig.save(authFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save auth.yml: " + e.getMessage());
        }
    }
    
    public void saveAllData() {
        saveAuthData();
    }
    
    public boolean isPlayerRegistered(UUID playerId) {
        return registeredPlayers.contains(playerId);
    }
    
    public boolean isPlayerLoggedIn(UUID playerId) {
        return loggedInPlayers.contains(playerId);
    }
    
    public boolean registerPlayer(Player player, String password) {
        UUID playerId = player.getUniqueId();
        
        if (isPlayerRegistered(playerId)) {
            return false;
        }
        
        String hashedPassword = hashPassword(password);
        String path = "players." + playerId.toString();
        
        authConfig.set(path + ".password", hashedPassword);
        authConfig.set(path + ".first-join", System.currentTimeMillis());
        authConfig.set(path + ".last-ip", player.getAddress().getAddress().getHostAddress());
        
        registeredPlayers.add(playerId);
        saveAuthData();
        
        // Auto login after registration
        loggedInPlayers.add(playerId);
        removeRestrictions(player);
        
        return true;
    }
    
    public boolean loginPlayer(Player player, String password) {
        UUID playerId = player.getUniqueId();
        
        if (!isPlayerRegistered(playerId)) {
            return false;
        }
        
        String storedPassword = authConfig.getString("players." + playerId.toString() + ".password");
        String inputPassword = hashPassword(password);
        
        if (storedPassword != null && storedPassword.equals(inputPassword)) {
            loggedInPlayers.add(playerId);
            loginTimeouts.remove(playerId);
            removeRestrictions(player);
            
            // Update last login info
            authConfig.set("players." + playerId.toString() + ".last-login", System.currentTimeMillis());
            authConfig.set("players." + playerId.toString() + ".last-ip", player.getAddress().getAddress().getHostAddress());
            saveAuthData();
            
            return true;
        }
        
        return false;
    }
    
    public void startLoginTimeout(Player player) {
        UUID playerId = player.getUniqueId();
        loginTimeouts.put(playerId, System.currentTimeMillis() + 60000); // 60 second timeout
        
        // Schedule timeout check
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isPlayerLoggedIn(playerId) && loginTimeouts.containsKey(playerId)) {
                    if (System.currentTimeMillis() > loginTimeouts.get(playerId)) {
                        player.kickPlayer("§cLogin timeout! Please try again.");
                        loginTimeouts.remove(playerId);
                    }
                }
            }
        }.runTaskLater(plugin, 20 * 60); // 60 seconds
    }
    
    public void applyRestrictions(Player player) {
        // Freeze player
        player.setWalkSpeed(0.0f);
        player.setFlySpeed(0.0f);
        player.setInvulnerable(true);
        
        // Blindness effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
        
        // Clear inventory and prevent interaction
        player.getInventory().clear();
        
        // Send login/register message
        if (!isPlayerRegistered(player.getUniqueId())) {
            player.sendMessage("§6==========================================");
            player.sendMessage("§e          Welcome to the Server!");
            player.sendMessage("");
            player.sendMessage("§7You need to §aregister §7first.");
            player.sendMessage("§7Use: §e/register <password> <confirm>");
            player.sendMessage("");
            player.sendMessage("§6==========================================");
        } else {
            player.sendMessage("§6==========================================");
            player.sendMessage("§e          Welcome Back!");
            player.sendMessage("");
            player.sendMessage("§7Please §alogin §7to continue playing.");
            player.sendMessage("§7Use: §e/login <password>");
            player.sendMessage("");
            player.sendMessage("§6==========================================");
        }
    }
    
    public void removeRestrictions(Player player) {
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.setInvulnerable(false);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        
        player.sendMessage("§aAuthentication successful! Welcome to the server!");
        
        // Give starter kits if first time
        if (!isPlayerRegistered(player.getUniqueId())) {
            KitManager kitManager = ((com.naufal90.SimplePlugin) plugin).getKitManager();
            kitManager.giveFirstJoinKits(player);
        }
    }
    
    public void logoutPlayer(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            plugin.getLogger().severe("Could not hash password: " + e.getMessage());
            return password; // Fallback to plain text (not recommended for production)
        }
    }
    
    // GeyserMC compatibility - handle Bedrock players
    public boolean isBedrockPlayer(Player player) {
        // Check if player is from GeyserMC (common methods)
        try {
            // Method 1: Check if player has bedrock prefix in name
            String playerName = player.getName();
            if (playerName.startsWith(".") || !playerName.matches("[a-zA-Z0-9_]+")) {
                return true;
            }
            
            // Method 2: Check via metadata (if available)
            if (player.hasMetadata("geyser")) {
                return true;
            }
            
            // Method 3: Check client brand (if available)
            String clientBrand = getClientBrand(player);
            if (clientBrand != null && clientBrand.toLowerCase().contains("geyser")) {
                return true;
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking Bedrock player: " + e.getMessage());
        }
        
        return false;
    }
    
    private String getClientBrand(Player player) {
        try {
            // This might require ProtocolLib or similar for older versions
            // For newer versions, you might need different approaches
            return player.getClientBrandName();
        } catch (Exception e) {
            return null;
        }
    }
    
    public void handleBedrockPlayerJoin(Player player) {
        if (isBedrockPlayer(player)) {
            // Additional handling for Bedrock players if needed
            player.sendMessage("§bBedrock player detected! Welcome!");
        }
    }
}