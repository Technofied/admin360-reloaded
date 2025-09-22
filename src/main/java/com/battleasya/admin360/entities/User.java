package com.battleasya.admin360.entities;

import com.battleasya.admin360.Admin360;
import com.battleasya.admin360.handler.Config;
import com.battleasya.admin360.handler.Permission;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User {

    public static HashMap<UUID, Long> cooldownList = new HashMap<>();

    public static void messagePlayer(CommandSender player, String message) {
        player.sendMessage(translate(message));
    }

    public static void playSoundPlayer(CommandSender player, String sound) {

        String command = "execute at <PLAYERNAME> run playsound <SOUND> master <PLAYERNAME> ~ ~ ~";

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                .replace("<SOUND>", sound)
                .replace("<PLAYERNAME>", player.getName())); 
    }

    public static String translate(String message) {

        /* HEX code support starts at 1.16 */
        if (Admin360.getServerVersion() >= 16) {

            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                // String color = message.substring(matcher.start(), matcher.end());
                String color = matcher.group(0);
                message = message.replace(color, ChatColor.of(color).toString());
                matcher = pattern.matcher(message);
            }

        }

        return ChatColor.translateAlternateColorCodes('&', message);

    }

    public static boolean hasPermission(CommandSender sender, Permission permission, Boolean sendMessage) {
        if (sender.hasPermission(permission.getString())) {
            return true;
        } else {
            if (sendMessage) {
                messagePlayer(sender, Config.no_permission);
            }
            return false;
        }
    }

    public static long inCooldown(UUID playerID, int duration, Admin360 plugin) {

        if (User.cooldownList.containsKey(playerID)) {

            long secondsLeft = ((cooldownList.get(playerID) / 1000) + duration) - (System.currentTimeMillis() / 1000);

            // Still cooling down
            if (secondsLeft > 0) { // still in cool down
                return secondsLeft;
            }

            return 0;

        }

        cooldownList.put(playerID, System.currentTimeMillis()); // put in list

        (new BukkitRunnable() {
            public void run() {
                cooldownList.remove(playerID);
            }
        }).runTaskLater(plugin, 20L * duration);

        return -1; // not in cooldown

    }

}