package com.naufal90.commands;

import com.naufal90.managers.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KitCommand implements CommandExecutor, TabCompleter {
    
    private final KitManager kitManager;
    
    public KitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (command.getName().equalsIgnoreCase("kits")) {
            // List available kits
            List<String> availableKits = kitManager.getAvailableKits(player);
            if (availableKits.isEmpty()) {
                player.sendMessage("§cNo kits available at the moment.");
            } else {
                player.sendMessage("§aAvailable kits: §e" + String.join("§7, §e", availableKits));
            }
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage("§cUsage: /kit <name>");
            player.sendMessage("§cUse /kits to see available kits.");
            return true;
        }
        
        String kitName = args[0];
        if (kitManager.giveKit(player, kitName)) {
            player.sendMessage("§aYou have received the " + kitName + " kit!");
        } else {
            player.sendMessage("§cKit not found or on cooldown. Use /kits to see available kits.");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (command.getName().equalsIgnoreCase("kit") && args.length == 1) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                for (String kitName : kitManager.getAvailableKits(player)) {
                    if (kitName.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(kitName);
                    }
                }
            }
        }
        
        return completions;
    }
}