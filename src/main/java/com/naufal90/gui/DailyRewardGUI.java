package com.naufal90.gui;

import com.naufal90.managers.DailyRewardManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DailyRewardGUI {
    
    private final DailyRewardManager manager;
    
    public DailyRewardGUI(DailyRewardManager manager) {
        this.manager = manager;
    }
    
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6Daily Rewards");
        
        // Player info
        gui.setItem(4, createPlayerInfoItem(player));
        
        // Current streak info
        DailyRewardManager.PlayerData data = manager.getPlayerData(player.getUniqueId());
        int currentStreak = data != null ? data.getStreak() : 0;
        gui.setItem(49, createStreakItem(currentStreak));
        
        // Reward items
        List<DailyRewardManager.DailyReward> rewards = manager.getRewards();
        for (DailyRewardManager.DailyReward reward : rewards) {
            int slot = reward.getDay() - 1;
            if (slot < 45) { // Only show first 45 days
                boolean canClaim = reward.getDay() == currentStreak + 1;
                boolean claimed = reward.getDay() <= currentStreak;
                
                gui.setItem(slot, createRewardItem(reward, claimed, canClaim));
            }
        }
        
        // Navigation and action items
        gui.setItem(48, createPreviousPageItem());
        gui.setItem(50, createNextPageItem());
        gui.setItem(53, createClaimItem(manager.canClaim(player)));
        
        player.openInventory(gui);
    }
    
    private ItemStack createPlayerInfoItem(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§e" + player.getName() + "'s Daily Rewards");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Total Claims: §a" + getTotalClaims(player));
        lore.add("§7Current Streak: §a" + getCurrentStreak(player));
        lore.add("");
        lore.add("§eClaim your daily reward every day");
        lore.add("§eto maintain your streak!");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createStreakItem(int streak) {
        ItemStack item = new ItemStack(Material.TARGET);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§6Current Streak: §e" + streak + " days");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Don't break your streak!");
        lore.add("§7Claim your reward every day.");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createRewardItem(DailyRewardManager.DailyReward reward, boolean claimed, boolean canClaim) {
        Material material = claimed ? Material.LIME_STAINED_GLASS_PANE : 
                            canClaim ? Material.YELLOW_STAINED_GLASS_PANE : 
                            Material.GRAY_STAINED_GLASS_PANE;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = claimed ? "§aDay " + reward.getDay() + " ✓" : 
                            canClaim ? "§eDay " + reward.getDay() + " §6(CLAIM NOW!)" : 
                            "§7Day " + reward.getDay();
        
        meta.setDisplayName(displayName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7" + reward.getName());
        lore.add("");
        
        if (!reward.getItems().isEmpty()) {
            lore.add("§6Items:");
            for (ItemStack rewardItem : reward.getItems()) {
                lore.add("§7- " + rewardItem.getAmount() + "x " + 
                        rewardItem.getType().toString().toLowerCase().replace('_', ' '));
            }
        }
        
        if (!reward.getCommands().isEmpty()) {
            lore.add("§6Commands:");
            for (String command : reward.getCommands()) {
                lore.add("§7- " + command);
            }
        }
        
        lore.add("");
        if (claimed) {
            lore.add("§a✓ Already claimed!");
        } else if (canClaim) {
            lore.add("§eClick to claim!");
        } else {
            lore.add("§cNot available yet");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createClaimItem(boolean canClaim) {
        Material material = canClaim ? Material.GOLD_INGOT : Material.BARRIER;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(canClaim ? "§6§lCLAIM DAILY REWARD" : "§cAlready Claimed Today");
        
        List<String> lore = new ArrayList<>();
        if (canClaim) {
            lore.add("§7Click to claim your daily reward!");
        } else {
            lore.add("§7You have already claimed your reward today.");
            lore.add("§7Come back tomorrow!");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private ItemStack createPreviousPageItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Previous Page");
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createNextPageItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Next Page");
        item.setItemMeta(meta);
        return item;
    }
    
    private int getTotalClaims(Player player) {
        DailyRewardManager.PlayerData data = manager.getPlayerData(player.getUniqueId());
        return data != null ? data.getTotalClaims() : 0;
    }
    
    private int getCurrentStreak(Player player) {
        DailyRewardManager.PlayerData data = manager.getPlayerData(player.getUniqueId());
        return data != null ? data.getStreak() : 0;
    }
}