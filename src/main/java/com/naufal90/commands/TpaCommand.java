package com.naufal90.commands;

import com.naufal90.managers.TpaManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCommand implements CommandExecutor {
    
    private final TpaManager tpaManager;
    
    public TpaCommand(TpaManager tpaManager) {
        this.tpaManager = tpaManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        switch (command.getName().toLowerCase()) {
            case "tpa":
                if (args.length != 1) {
                    player.sendMessage("§cUsage: /tpa <player>");
                    return true;
                }
                
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found or offline.");
                    return true;
                }
                
                if (target == player) {
                    player.sendMessage("§cYou cannot teleport to yourself.");
                    return true;
                }
                
                tpaManager.sendTpaRequest(player, target, false);
                break;
                
            case "tpahere":
                if (args.length != 1) {
                    player.sendMessage("§cUsage: /tpahere <player>");
                    return true;
                }
                
                Player targetHere = Bukkit.getPlayer(args[0]);
                if (targetHere == null) {
                    player.sendMessage("§cPlayer not found or offline.");
                    return true;
                }
                
                if (targetHere == player) {
                    player.sendMessage("§cYou cannot teleport yourself to yourself.");
                    return true;
                }
                
                tpaManager.sendTpaRequest(player, targetHere, true);
                break;
                
            case "tpaccept":
                tpaManager.acceptTpaRequest(player);
                break;
                
            case "tpdeny":
                tpaManager.denyTpaRequest(player);
                break;
        }
        
        return true;
    }
}