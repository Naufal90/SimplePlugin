package com.naufal90.commands;

import com.naufal90.managers.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabCompleter {
    
    private final WarpManager warpManager;
    
    public WarpCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "warp":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be executed by players.");
                    return true;
                }
                
                Player player = (Player) sender;
                
                if (args.length == 0) {
                    List<String> accessibleWarps = warpManager.getAccessibleWarps(player);
                    if (accessibleWarps.isEmpty()) {
                        player.sendMessage("§cNo warps available or you don't have permission to access any warps.");
                    } else {
                        player.sendMessage("§aAvailable warps: §e" + String.join("§7, §e", accessibleWarps));
                    }
                    return true;
                }
                
                String warpName = args[0];
                if (!warpManager.teleportToWarp(player, warpName)) {
                    player.sendMessage("§cWarp '" + warpName + "' not found or you don't have permission!");
                }
                break;
                
            case "setwarp":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be executed by players.");
                    return true;
                }
                
                Player setter = (Player) sender;
                
                if (args.length == 0) {
                    setter.sendMessage("§cUsage: /setwarp <name>");
                    return true;
                }
                
                String newWarpName = args[0];
                if (warpManager.setWarp(setter, newWarpName, setter.getLocation())) {
                    setter.sendMessage("§aWarp '" + newWarpName + "' has been set!");
                }
                break;
                
            case "delwarp":
                if (args.length == 0) {
                    sender.sendMessage("§cUsage: /delwarp <name>");
                    return true;
                }
                
                Player deleter = (Player) sender;
                
                if (args.length == 0) {
                    deleter.sendMessage("§cUsage: /delwarp <name>");
                    return true;
                }
                
                String deleteWarpName = args[0];
                if (warpManager.deleteWarp(deleter, deleteWarpName)) {
                    deleter.sendMessage("§aWarp '" + deleteWarpName + "' has been deleted!");
                } else {
                    deleter.sendMessage("§cWarp '" + deleteWarpName + "' not found or you don't have permission!");
                }
                break;
                
            case "warps":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be executed by players.");
                    return true;
                }
                
                Player lister = (Player) sender;
                List<String> accessibleWarps = warpManager.getAccessibleWarps(lister);
                if (accessibleWarps.isEmpty()) {
                    lister.sendMessage("§cNo warps available or you don't have permission to access any warps.");
                } else {
                    lister.sendMessage("§aAvailable warps: §e" + String.join("§7, §e", accessibleWarps));
                }
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            switch (command.getName().toLowerCase()) {
                case "warp":
                case "delwarp":
                    if (args.length == 1) {
                        List<String> accessibleWarps = warpManager.getAccessibleWarps(player);
                        for (String warpName : accessibleWarps) {
                            if (warpName.toLowerCase().startsWith(args[0].toLowerCase())) {
                                completions.add(warpName);
                            }
                        }
                    }
                    break;
            }
        }
        
        return completions;
    }
}