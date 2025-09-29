package com.battleasya.admin360.commands;

import com.battleasya.admin360.Admin360;
import com.battleasya.admin360.handler.Config;
import com.battleasya.admin360.handler.Permission;
import com.battleasya.admin360.entities.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/* This class handles everything about the /ticket command. */

@SuppressWarnings("NullableProblems")
public class A3 implements CommandExecutor {

    private final Admin360 plugin;

    /* A3 Constructor */
    public A3 (Admin360 plugin) {
        this.plugin = plugin;
    }

    public void printCommandList(CommandSender sender) {
        for (String message : Config.player_command_list) {
            User.messagePlayer(sender, message);
        }
        if (User.hasPermission(sender, Permission.ATTEND_TICKET, false)) {
            for (String message : Config.staff_command_list) {
                User.messagePlayer(sender, message);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            User.messagePlayer(sender, "This is a player-only command!");
            return true;
        }

        if (args.length == 0) {
            printCommandList(sender);
            return true;
        }

        if (args.length == 1) {

            switch (args[0].toLowerCase()) {

                case "help":
                    printCommandList(sender);
                    return true;

                case "create":
                    break;

                case "cancel":
                    if (User.hasPermission(sender, Permission.ATTEND_TICKET, false)) {
                        User.messagePlayer(sender, Config.cancel_failed_restricted);
                        return true;
                    }
                    if (User.hasPermission(sender, Permission.CREATE_TICKET, true)) {
                        plugin.getRequestHandler().cancelTicket(sender);
                    }
                    return true;

                case "status":
                    if (User.hasPermission(sender, Permission.VIEW_STATUS, true)) {
                        plugin.getRequestHandler().printTicketStatus(sender);
                    }
                    return true;

                case "stats":
                    if (User.hasPermission(sender, Permission.VIEW_STATS, true)) {
                        plugin.getRequestHandler().printTicketStats(sender);
                    }
                    return true;

                case "list":
                    if (User.hasPermission(sender, Permission.ATTEND_TICKET, true)) {
                        plugin.getRequestHandler().printPendingList(sender);
                    }
                    return true;

                case "next":
                    if (User.hasPermission(sender, Permission.ATTEND_TICKET, true)) {
                        plugin.getRequestHandler().attendTicket(sender, null);
                    }
                    return true;

                case "tp":
                    if (User.hasPermission(sender, Permission.TELEPORT, true)) {
                        plugin.getRequestHandler().teleportToPlayer(sender);
                    }
                    return true;

                case "info":
                    if (User.hasPermission(sender, Permission.VIEW_INFO, true)) {
                        plugin.getRequestHandler().printTicketInfo(sender);
                    }
                    return true;

                case "drop":
                    if (User.hasPermission(sender, Permission.DROP_TICKET, true)) {
                        plugin.getRequestHandler().dropTicket(sender);
                    }
                    return true;

                case "close":
                    if (User.hasPermission(sender, Permission.ATTEND_TICKET, true)) {
                        plugin.getRequestHandler().closeTicket(sender);
                    }
                    return true;

                case "yes":
                    if (User.hasPermission(sender, Permission.CREATE_TICKET, true)) {
                        plugin.getRequestHandler().giveFeedback(sender, true);
                    }
                    return true;

                case "no":
                    if (User.hasPermission(sender, Permission.CREATE_TICKET, true)) {
                        plugin.getRequestHandler().giveFeedback(sender, false);
                    }
                    return true;

                case "purge":
                    if (User.hasPermission(sender, Permission.PURGE_TICKET, true)) {
                        plugin.getRequestHandler().purgeTicket(sender, "pending");
                    }
                    return true;

                case "hpstats":
                    if (User.hasPermission(sender, Permission.VIEW_HP_STATS, true)) {
                        plugin.getRequestHandler().printHonorStats(sender, sender.getName());
                    }
                    return true;

                case "hptop":
                    if (User.hasPermission(sender, Permission.VIEW_HP_TOP, true)) {
                        plugin.getRequestHandler().printHonorTop(sender, Config.hptop_default_limit);
                    }
                    return true;

                case "history":
                    if (User.hasPermission(sender, Permission.VIEW_HISTORY, true)) {
                        plugin.getRequestHandler().printHonorHistory(sender, Config.history_default_limit);
                    }
                    return true;

                case "fly":
                    if (User.hasPermission(sender, Permission.FLY, true)) {
                        plugin.getRequestHandler().toggleFly(sender);
                    }
                    return true;

            }

        }

        if (args.length == 2) {

            switch (args[0].toLowerCase()) {

                case "create":
                    break;

                case "select":
                    if (User.hasPermission(sender, Permission.ATTEND_TICKET, true)) {
                        plugin.getRequestHandler().attendTicket(sender, args[1]);
                    }
                    return true;

                case "transfer":
                    if (User.hasPermission(sender, Permission.TRANSFER_TICKET, true)) {
                        plugin.getRequestHandler().transferTicket(sender, args[1]);
                    }
                    return true;

                case "purge":
                    if (User.hasPermission(sender, Permission.PURGE_TICKET, true)) {
                        plugin.getRequestHandler().purgeTicket(sender, args[1]);
                    }
                    return true;

                case "remove":
                    if (User.hasPermission(sender, Permission.REMOVE_TICKET, true)) {
                        plugin.getRequestHandler().removeTicket(sender, args[1]);
                    }
                    return true;

                case "hpreset":
                    if (User.hasPermission(sender, Permission.RESET_HP_STATS, true)) {
                        plugin.getRequestHandler().resetHonor(sender, args[1]);
                    }
                    return true;

                case "hpstats":
                    if (User.hasPermission(sender, Permission.VIEW_HP_STATS, true)) {
                        plugin.getRequestHandler().printHonorStats(sender, args[1]);
                    }
                    return true;

                case "hptop":
                    if (User.hasPermission(sender, Permission.VIEW_HP_TOP, true)) {
                        try {
                            plugin.getRequestHandler().printHonorTop(sender, Integer.parseInt(args[1]));
                        } catch (Exception e) {
                            User.messagePlayer(sender, Config.incorrect_syntax);
                        }
                    }
                    return true;

                case "history":
                    if (User.hasPermission(sender, Permission.VIEW_HISTORY, true)) {
                        try {
                            plugin.getRequestHandler().printHonorHistory(sender, Integer.parseInt(args[1]));
                        } catch (Exception e) {
                            User.messagePlayer(sender, Config.incorrect_syntax);
                        }
                    }
                    return true;

            }

        }

        if (args[0].equalsIgnoreCase("create")) {

            // admin restriction
            if (User.hasPermission(sender, Permission.ATTEND_TICKET, false)) {
                User.messagePlayer(sender, Config.create_failed_restricted);
                return true;
            }

            if (User.hasPermission(sender, Permission.CREATE_TICKET, true)) {

                StringBuilder comment;

                if (args.length == 1) {
                    comment = new StringBuilder();
                } else {
                    comment = new StringBuilder(args[1]);
                    for (int i = 2; i < args.length; i++) {
                        comment.append(" ").append(args[i]);
                    }
                }

                plugin.getRequestHandler().createTicket(sender, comment.toString());

            }

            return true;

        }

        User.messagePlayer(sender, Config.incorrect_syntax);
        return true;

    }

}
