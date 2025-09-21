package com.battleasya.admin360.entities;

import com.battleasya.admin360.handler.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class Admin {

    /* List of admins online */
    public static HashSet<UUID> adminList = new HashSet<>();

    /* Send a message to all the admins online */
    public static void messageAdmins(String message) {

        for (UUID adminID : adminList) {

            Player admin = Bukkit.getPlayer(adminID);

            if (admin != null) {
                User.messagePlayer(admin, message);
            }

        }
    }

    /* Play a sound effect for all the admins online */
    public static void playSoundAdmins(String sound) {

        for (UUID adminID : adminList) {
            
            Player admin = Bukkit.getPlayer(adminID);

            if (admin != null) {
                User.playSoundPlayer(admin, sound);
            }
        }
    }

    /* Refresh all the admins in the list */
    public static void refreshAdmLst() {
        Collection<? extends Player> playerList = Bukkit.getOnlinePlayers();
        for (Player player : playerList) {
            if (User.hasPermission(player, Permission.ATTEND_TICKET, false)) {
                Admin.addAdmin(player.getUniqueId());
            }
        }
    }

    public static void removeAdmin(UUID adminID) {
        adminList.remove(adminID);
    }

    public static void addAdmin(UUID adminID) {
        adminList.add(adminID);
    }

    public static boolean isAvailable() {
        return !adminList.isEmpty();
    }

    public static boolean isAdmin(UUID adminID) {
        return adminList.contains(adminID);
    }

}
