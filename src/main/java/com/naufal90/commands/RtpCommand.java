package com.naufal90.commands;

import com.naufal90.managers.RtpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RtpCommand implements CommandExecutor {
    
    private final RtpManager rtpManager;
    
    public RtpCommand(RtpManager rtpManager) {
        this.rtpManager = rtpManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by players.");
            return true;
        }
        
        Player player = (Player) sender;
        rtpManager.randomTeleport(player);
        return true;
    }
}