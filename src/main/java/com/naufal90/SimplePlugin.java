package com.naufal90;

import com.naufal90.managers.*;
import com.naufal90.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public class SimplePlugin extends JavaPlugin {
    
    private static SimplePlugin instance;
    private TpaManager tpaManager;
    private WarpManager warpManager;
    private RtpManager rtpManager;
    private KitManager kitManager;
    private DailyRewardManager dailyRewardManager;
    private AuthManager authManager;
    private PermissionManager permissionManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        initConfigs();
        
        // Initialize managers
        this.permissionManager = new PermissionManager(this);
        this.tpaManager = new TpaManager(this);
        this.warpManager = new WarpManager(this);
        this.rtpManager = new RtpManager(this);
        this.kitManager = new KitManager(this);
        this.dailyRewardManager = new DailyRewardManager(this);
        this.authManager = new AuthManager(this);
        
        registerCommands();
        registerListeners();
        checkPlaceholderAPI();
        
        getLogger().info("Simple Plugin has been enabled!");
    }
    
    private void checkPlaceholderAPI() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PlaceholderAPI detected! Placeholders are available.");
            // Here you can register custom placeholders if needed
        } else {
            getLogger().info("PlaceholderAPI not found. Using internal time system.");
        }
    }
    
    private void initConfigs() {
        // Save default config files if they don't exist
        saveResource("kits.yml", false);
        saveResource("dailyrewards.yml", false);
    }
    
    private void registerCommands() {
        // TPA Commands
        getCommand("tpa").setExecutor(new com.naufal90.commands.TpaCommand(tpaManager));
        getCommand("tpaccept").setExecutor(new com.naufal90.commands.TpaCommand(tpaManager));
        getCommand("tpahere").setExecutor(new com.naufal90.commands.TpaCommand(tpaManager));
        getCommand("tpdeny").setExecutor(new com.naufal90.commands.TpaCommand(tpaManager));
        
        // Warp Commands
        getCommand("warp").setExecutor(new com.naufal90.commands.WarpCommand(warpManager));
        getCommand("setwarp").setExecutor(new com.naufal90.commands.WarpCommand(warpManager));
        getCommand("delwarp").setExecutor(new com.naufal90.commands.WarpCommand(warpManager));
        getCommand("warps").setExecutor(new com.naufal90.commands.WarpCommand(warpManager));
        
        // RTP Command
        getCommand("rtp").setExecutor(new com.naufal90.commands.RtpCommand(rtpManager));
    
        // Kit Commands
        getCommand("kit").setExecutor(new com.naufal90.commands.KitCommand(kitManager));
        getCommand("kits").setExecutor(new com.naufal90.commands.KitCommand(kitManager));
        
        // Daily Reward Command
        getCommand("daily").setExecutor(new com.naufal90.commands.DailyRewardCommand(dailyRewardManager));
        
        // Auth Commands
        getCommand("register").setExecutor(new com.naufal90.commands.AuthCommand(authManager));
        getCommand("login").setExecutor(new com.naufal90.commands.AuthCommand(authManager));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new TpaListener(tpaManager), this);
        getServer().getPluginManager().registerEvents(new KitListener(kitManager), this);
        getServer().getPluginManager().registerEvents(new DailyRewardListener(dailyRewardManager), this);
        getServer().getPluginManager().registerEvents(new AuthListener(authManager), this);
    }
    
    @Override
    public void onDisable() {
        if (authManager != null) {
            authManager.saveAllData();
        }
        getLogger().info("TPA-Warp Plugin has been disabled!");
    }
    
    public static SimplePlugin getInstance() {
        return instance;
    }
    
    public TpaManager getTpaManager() { 
      return tpaManager; 
    }
    
    public WarpManager getWarpManager() { 
      return warpManager; 
    }
    
    public RtpManager getRtpManager() { 
      return rtpManager; 
    }
    
    public KitManager getKitManager() { 
      return kitManager; 
    }
    
    public DailyRewardManager getDailyRewardManager() { 
      return dailyRewardManager; 
    }
    
    public AuthManager getAuthManager() { 
      return authManager; 
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
}