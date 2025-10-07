package com.naufal90.listeners;

import com.naufal90.managers.DailyRewardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class DailyRewardListener implements Listener {
    
    private final DailyRewardManager manager;
    
    public DailyRewardListener(DailyRewardManager manager) {
        this.manager = manager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Remind player about daily reward
        if (manager.canClaim(event.getPlayer())) {
            event.getPlayer().sendMessage("§6§lDaily Reward Available! §eUse §6/daily §eto claim your reward!");
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§6Daily Rewards")) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() == null) return;
            
            // Handle claim button
            if (event.getSlot() == 53) {
                if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                    org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
                    if (manager.canClaim(player)) {
                        manager.claimReward(player);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cYou have already claimed your daily reward today!");
                    }
                }
            }
        }
    }
}