package com.naufal90.commands;

import com.naufal90.managers.AuthManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthCommand implements CommandExecutor {
    
    private final AuthManager authManager;
    
    public AuthCommand(AuthManager authManager) {
        this.authManager = authManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by players.");
            return true;
        }
        
        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();
        
        switch (cmd) {
            case "register":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /register <password> <confirm>");
                    return true;
                }
                
                if (authManager.isPlayerRegistered(player.getUniqueId())) {
                    player.sendMessage("§cYou are already registered! Use /login instead.");
                    return true;
                }
                
                if (!args[0].equals(args[1])) {
                    player.sendMessage("§cPasswords do not match!");
                    return true;
                }
                
                if (args[0].length() < 4) {
                    player.sendMessage("§cPassword must be at least 4 characters long!");
                    return true;
                }
                
                if (authManager.registerPlayer(player, args[0])) {
                    player.sendMessage("§aRegistration successful! You are now logged in.");
                } else {
                    player.sendMessage("§cRegistration failed!");
                }
                break;
                
            case "login":
                if (args.length < 1) {
                    player.sendMessage("§cUsage: /login <password>");
                    return true;
                }
                
                if (!authManager.isPlayerRegistered(player.getUniqueId())) {
                    player.sendMessage("§cYou are not registered! Use /register first.");
                    return true;
                }
                
                if (authManager.loginPlayer(player, args[0])) {
                    player.sendMessage("§aLogin successful!");
                } else {
                    player.sendMessage("§cIncorrect password!");
                }
                break;
        }
        
        return true;
    }
}