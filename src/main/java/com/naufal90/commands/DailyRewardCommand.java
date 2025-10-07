package com.naufal90.commands;

import com.naufal90.managers.DailyRewardManager;
import com.naufal90.gui.DailyRewardGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DailyRewardCommand implements CommandExecutor {
    
    private final DailyRewardManager manager;
    private final DailyRewardGUI gui;
    
    public DailyRewardCommand(DailyRewardManager manager) {
        this.manager = manager;
        this.gui = new DailyRewardGUI(manager);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length > 0 && args[0].equalsIgnoreCase("claim")) {
            // Direct claim without GUI
            if (manager.canClaim(player)) {
                manager.claimReward(player);
            } else {
                player.sendMessage("§cYou have already claimed your daily reward today!");
            }
        } else {
            // Open GUI
            gui.openGUI(player);
        }
        
        return true;
    }
}