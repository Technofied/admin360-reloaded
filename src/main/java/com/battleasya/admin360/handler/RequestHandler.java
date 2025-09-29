package com.battleasya.admin360.handler;

import com.battleasya.admin360.Admin360;
import com.battleasya.admin360.entities.Admin;
import com.battleasya.admin360.entities.Request;
import com.battleasya.admin360.entities.Review;
import com.battleasya.admin360.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class RequestHandler {

    private final Admin360 plugin;

    /** RequestHandler Constructor */
    public RequestHandler(Admin360 plugin) {
        this.plugin = plugin;
    }

    /** Adds Request to Queue */
    public void createTicket(CommandSender sender, String comment) {

        String playerName = sender.getName();
        UUID playerID = ((Player) sender).getUniqueId();

        // check staff availability
        if (Config.create_require_staff && !Admin.isAvailable()) {
            User.messagePlayer(sender, Config.create_failed_no_staff);
            return;
        }

        // check user status
        switch (Request.getStatus(playerID)) {
            case 1:
                User.messagePlayer(sender, Config.create_failed_pending);
                return;
            case 2:
                User.messagePlayer(sender, Config.create_failed_attending);
                return;
            case 3:
                User.messagePlayer(sender, Config.create_failed_completing);
                return;
        }

        // check cooldown status
        if (Config.create_cooldown_enable){
            long secondsLeft = User.inCooldown(playerID, Config.create_cooldown_interval, plugin);
            if (secondsLeft != -1) { // still in cooldown
                String secondsLeft2S = String.valueOf(secondsLeft);
                User.messagePlayer(sender, Config.create_cooldown_message
                        .replace("<SECONDS>", secondsLeft2S));
                return;
            }
        }

        // create request
        Request request = new Request(playerID, playerName, comment);
        Request.addToPndLst(request); // add to end of queue

        String posInPndLst = Integer.toString(Request.getPndLstSize());

        // notify user
        for (String message : Config.create_passed_notify_player) {
            User.messagePlayer(sender, message
                    .replace("<POSITION>", posInPndLst)
                    .replace("<DETAILS>", comment));
        }

        // notify staff
        for (String message : Config.create_passed_notify_staff) {
            Admin.messageAdmins(message
                    .replace("<PLAYERNAME>", playerName)
                    .replace("<DETAILS>", comment)
                    .replace("<AMOUNT>", posInPndLst));
        }

        // trigger custom commands
        if (Config.create_passed_trigger_enable) {
            for (String command : Config.create_passed_trigger_command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                        .replace("<PLAYERNAME>", playerName)
                        .replace("<POSITION>", posInPndLst)
                        .replace("<DETAILS>", comment));
            }
        }

        // play sound for online staff
        if (Config.create_passed_sound_effect_enable) {
            Admin.playSoundAdmins(Config.create_passed_sound_effect_sound);
        }

    }


    public void printPendingList(CommandSender admin) {

        // print header
        for (String message : Config.list_header) {
            User.messagePlayer(admin, message);
        }

        int i = 1;

        // print body
        // loop through each Request in Pending List
        for (Request request : Request.requestPending) {

            String index = Integer.toString(i);
            String playerName = request.getPlayerName();
            String comment = request.getComment();
            String datetime = new SimpleDateFormat("MM/dd/yy HH:mm")
                    .format(new Date(request.getTimestamp() * 1000));

            User.messagePlayer(admin, Config.list_body
                    .replace("<INDEX>", index)
                    .replace("<PLAYERNAME>", playerName)
                    .replace("<DETAILS>", comment)
                    .replace("<DATETIME>", datetime));

            i++;

        }

        // print footer
        for (String message : Config.list_footer) {
            User.messagePlayer(admin, message);
        }

    }


    public void attendTicket(CommandSender admin, String playerName) {

        String adminName = admin.getName();
        UUID adminID = ((Player) admin).getUniqueId();

        Player player;

        Request request;

        // this must go first to avoid problems
        if (Request.inAtdLst(adminID)) {
            User.messagePlayer(admin, Config.attend_failed_attending);
            return;
        }

        if (Request.isPndLstEmpty()) {
            User.messagePlayer(admin, Config.attend_failed_no_ticket);
            return;
        }

        // next
        if (playerName == null) {

            // removes from head of queue
            request = Request.getHeadOfPndLst();

            // player should exist because request exists
            player = Bukkit.getPlayer(request.getPlayerID());

        // select
        } else {

            // check if player is online
            player = Bukkit.getPlayer(playerName);

            if (player == null) {
                User.messagePlayer(admin, Config.attend_failed_not_online
                        .replace("<PLAYERNAME>", playerName));
                return;
            }

            // check if request exists
            request = Request.getPndRequest(player.getUniqueId());

            if (request == null) {
                User.messagePlayer(admin, Config.attend_failed_not_pending
                        .replace("<PLAYERNAME>", player.getName()));
                return;
            }

            Request.removeFromPndLst(request);

        }

        playerName = request.getPlayerName(); // get real name

        // set the staff who honored the request
        request.setHandledBy(adminName);
        request.setHandledBy(adminID);

        // migrate Request to Attending List
        Request.addToAtdLst(adminID, request);

        // teleport admin to player
        if (Config.attend_auto_teleport) {
            try {
                ((Player) admin).teleport(player);
                User.messagePlayer(admin, Config.teleport_passed
                        .replace("<PLAYERNAME>", playerName));
            } catch (Exception e) {
                User.messagePlayer(admin, Config.teleport_failed);
            }
        }

        // make admin invulnerable
        if (Config.attend_invincibility) {
            User.messagePlayer(admin, Config.invulnerable_on);
            ((Player) admin).setInvulnerable(true);
        }

        String ticketsRemain = Integer.toString(Request.getPndLstSize());

        // notify user
        for (String message : Config.attend_passed_notify_player) {
            User.messagePlayer(player, message
                    .replace("<ADMINNAME>", adminName));
        }

        // notify staff
        for (String message : Config.attend_passed_notify_staff) {
            Admin.messageAdmins(message
                    .replace("<ADMINNAME>", adminName)
                    .replace("<PLAYERNAME>", playerName)
                    .replace("<AMOUNT>", ticketsRemain));
        }

        // trigger custom commands
        if (Config.attend_passed_trigger_enable) {
            for (String command : Config.attend_passed_trigger_command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                        .replace("<PLAYERNAME>", playerName)
                        .replace("<ADMINNAME>", adminName));
            }
        }

    }


    public void teleportToPlayer(CommandSender admin) {

        UUID adminID = ((Player) admin).getUniqueId();

        // check if a staff is already handling a player
        if (!Request.inAtdLst(adminID)) {
            User.messagePlayer(admin, Config.teleport_failed);
            return;
        }

        Request request = Request.getAtdRequest(adminID);

        String playerName = request.getPlayerName();
        UUID playerID = request.getPlayerID();

        try {
            ((Player) admin).teleport(Bukkit.getPlayer(playerID));
            User.messagePlayer(admin, Config.teleport_passed
                    .replace("<PLAYERNAME>", playerName));
        } catch (Exception e) {
            User.messagePlayer(admin, Config.teleport_failed);
        }

    }


    public void printTicketInfo(CommandSender admin) {

        UUID adminID = ((Player) admin).getUniqueId();

        if (!Request.inAtdLst(adminID)) {
            User.messagePlayer(admin, Config.info_failed);
            return;
        }

        Request request = Request.getAtdRequest(adminID);

        String playerName = request.getPlayerName();
        String comment = request.getComment();
        String datetime = new SimpleDateFormat("MM/dd/yy HH:mm")
                .format(new Date(request.getTimestamp() * 1000));

        for (String message : Config.info_passed) {
            User.messagePlayer(admin, message
                    .replace("<PLAYERNAME>", playerName)
                    .replace("<DETAILS>", comment)
                    .replace("<DATETIME>", datetime));
        }

    }


    public void dropTicket(CommandSender admin) {

        String adminName = admin.getName();
        UUID adminID = ((Player) admin).getUniqueId();

        if (!Request.inAtdLst(adminID)) {
            User.messagePlayer(admin, Config.drop_failed);
            return;
        }

        Request request = Request.removeFromAtdLst(adminID);

        String playerName = request.getPlayerName();
        UUID playerID = request.getPlayerID();
        Player player = Bukkit.getPlayer(playerID);

        User.messagePlayer(admin, Config.drop_passed_notify_handler
                .replace("<PLAYERNAME>", playerName));

        for (String message : Config.drop_passed_notify_player) {
            User.messagePlayer(player, message
                    .replace("<ADMINNAME>", adminName));
        }

        // Un-invincibilize admin
        if (Config.attend_invincibility) {
            User.messagePlayer(admin, Config.invulnerable_off);
            ((Player) admin).setInvulnerable(false);
        }

        // Reset flight mode
        ((Player) admin).setAllowFlight(false);
        ((Player) admin).setFlying(false);

    }


    public void transferTicket(CommandSender admin1, String admin2Name) {

        UUID admin1ID = ((Player) admin1).getUniqueId();

        // check admin1 status
        if (!Request.inAtdLst(admin1ID)) {
            User.messagePlayer(admin1, Config.transfer_failed
                    .replace("<ADMINNAME>",admin2Name));
            return;
        }

        // check if admin2 is online
        Player admin2 = Bukkit.getPlayer(admin2Name);

        if (admin2 == null) {
            User.messagePlayer(admin1, Config.transfer_failed
                    .replace("<ADMINNAME>",admin2Name));
            return;
        }

        admin2Name = admin2.getName(); // get real name
        UUID admin2ID = admin2.getUniqueId();

        // check if admin2 is admin or if admin2 is attending
        if (!Admin.isAdmin(admin2ID) || Request.inAtdLst(admin2ID)) {
            User.messagePlayer(admin1, Config.transfer_failed
                    .replace("<ADMINNAME>",admin2Name));
            return;
        }

        Request request = Request.removeFromAtdLst(admin1ID);

        request.setHandledBy(admin2Name);
        request.setHandledBy(admin2ID);

        Request.addToAtdLst(admin2ID, request);

        String playerName = request.getPlayerName();
        UUID playerID = request.getPlayerID();
        Player player = Bukkit.getPlayer(playerID);

        String ticketsRemain = Integer.toString(Request.getPndLstSize());

        User.messagePlayer(admin1, Config.transfer_passed
                .replace("<ADMINNAME>",admin2Name));

        for (String message : Config.attend_passed_notify_player) {
            User.messagePlayer(player, message
                    .replace("<ADMINNAME>", admin2Name));
        }

        for (String message : Config.attend_passed_notify_staff) {
            Admin.messageAdmins(message
                    .replace("<ADMINNAME>", admin2Name)
                    .replace("<PLAYERNAME>", playerName)
                    .replace("<AMOUNT>", ticketsRemain));
        }

        if (Config.attend_passed_trigger_enable) {
            for (String command : Config.attend_passed_trigger_command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                        .replace("<PLAYERNAME>", playerName)
                        .replace("<ADMINNAME>", admin2Name));
            }
        }

    }


    /**
     * Called when an admin attempt to set a request as solved.
     * The user is then asked if the help was satisfactory
     */

    public void closeTicket(CommandSender admin) {

        UUID adminID = ((Player) admin).getUniqueId();
        String adminName = admin.getName();

        // Make sure admin is not attending other requests
        if (!Request.inAtdLst(adminID)) {
            User.messagePlayer(admin, Config.close_failed);
            return;
        }

        // Get and remove Request from Attending List
        Request request = Request.removeFromAtdLst(adminID);

        // Get player uuid
        UUID playerID = request.getPlayerID();
        String playerName = request.getPlayerName();

        // Add Request to Awaiting List
        Request.addToCptLst(playerID, request);

        // Notify admin of a successful operation
        User.messagePlayer(admin, Config.close_passed
                .replace("<PLAYERNAME>", playerName));

        // Set review scheduler
        if (Config.review_reminder_enable) {
            BukkitTask reminder = new Review(plugin, playerID, adminName).runReminder();
            Review.addToRmdLst(playerID, reminder);
        } else {
            promptFeedback(playerID, adminName);
        }

        // Un-invincibilize admin
        if (Config.attend_invincibility) {
            User.messagePlayer(admin, Config.invulnerable_off);
            ((Player) admin).setInvulnerable(false);
        }

        // Reset flight mode
        ((Player) admin).setAllowFlight(false);
        ((Player) admin).setFlying(false);

        // trigger custom commands
        if (Config.close_passed_trigger_enable) {
            for (String command : Config.close_passed_trigger_command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                        .replace("<PLAYERNAME>", playerName)
                        .replace("<ADMINNAME>", adminName));
            }
        }
    }


    /**
     * Closes a request and disposes it. It also gives an admin an honor point based on
     * what the player rated the help received
     */

    public void giveFeedback(CommandSender sender, Boolean isSatisfactory) {

        String playerName = sender.getName();
        UUID playerID = ((Player) sender).getUniqueId();

        // check if a player has a request in the completed list awaiting a rating
        if (!Request.inCptLst(playerID)) {
            User.messagePlayer(sender, Config.review_failed);
            return;
        }

        // remove request from completed request list
        Request request = Request.removeFromCptLst(playerID);

        String adminName = request.getHandledByName();
        UUID adminID = request.getHandledByID();

        // stop the reminders
        Review.removePlayer(playerID);

        // increment counter
        Request.addCompletedToday();

        // add to database
        (new BukkitRunnable() {
            public void run() {
                plugin.getDataSource().addRecord(request, isSatisfactory);
            }
        }).runTaskAsynchronously(plugin);

        // send player a message
        User.messagePlayer(sender, Config.review_received);

        // trigger command here so that even admin is offline it is executed
        if (Config.review_received_trigger_enable) {
            for (String command : Config.review_received_trigger_command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                        .replace("<PLAYERNAME>", playerName)
                        .replace("<ADMINNAME>", adminName));
            }
        }

        // check if admin is online
        // this must be kept because we did not remove from Completing List after admin log out
        Player admin = Bukkit.getPlayer(adminID);

        if (admin == null) {
            return;
        }

        // give admin honor point based on ans
        if (isSatisfactory) {

            User.messagePlayer(admin, Config.review_upvote_notify_handler
                    .replace("<PLAYERNAME>", playerName));

            // fireworks
            Firework fw = admin.getWorld().spawn(admin.getLocation(), Firework.class);
            FireworkMeta fwm = fw.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().trail(true).withColor(Color.LIME).with(FireworkEffect.Type.CREEPER).build();
            fwm.addEffect(effect);
            fwm.setPower(1);
            fw.setFireworkMeta(fwm);

        } else {

            User.messagePlayer(admin, Config.review_downvote_notify_handler
                    .replace("<PLAYERNAME>", playerName));

        }

    }


    /**
     * Prompts a player to rate a request after an admin has handled it
     */

    public boolean promptFeedback(UUID playerID, String adminName) {

        Player player = Bukkit.getPlayer(playerID);

        if (player == null) {
            return false;
        }

        String playerName = player.getName();

        for (String message : Config.review_prompt) {
            User.messagePlayer(player, message
                    .replace("<ADMINNAME>", adminName));
        }

        if (Config.review_prompt_trigger_enable) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (String command : Config.review_prompt_trigger_command) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("<PLAYERNAME>", playerName)
                            .replace("<ADMINNAME>", adminName));
                }
            });
        }

        return true;

    }


    /** Removes Requests from Lists */

    public void purgeTicket(CommandSender sender, String option) {

        String amountPurged;
        option = option.toLowerCase();

        switch (option) {

            case "pending":
                amountPurged = Integer.toString(Request.getPndLstSize());
                Request.clearPndLst();
                break;
            case "attending":
                amountPurged = Integer.toString(Request.getAtdLstSize());
                Request.clearAtdLst();
                break;
            case "completing":
                amountPurged = Integer.toString(Request.getCptLstSize());
                Request.clearCptLst();
                Review.clearRmdLst();
                break;
            case "all":
                amountPurged = Integer.toString(Request.getPndLstSize()
                        + Request.getAtdLstSize() + Request.getCptLstSize());
                Request.clearPndLst();
                Request.clearAtdLst();
                Request.clearCptLst();
                Review.clearRmdLst();
                break;
            default:
                User.messagePlayer(sender, Config.incorrect_syntax);
                return;

        }

        for (String message : Config.purge_message) {
            User.messagePlayer(sender, message
                    .replace("<AMOUNT>", amountPurged)
                    .replace("<TYPE>", option));
        }

    }


    public void removeTicket(CommandSender sender, String playerName) {

        // Validate playerName
        Player player = Bukkit.getPlayer(playerName);
        UUID playerID;

        if (player == null) {
            User.messagePlayer(sender, Config.remove_failed
                    .replace("<PLAYERNAME>", playerName));
            return;
        }

        playerName = player.getName(); // get the correct name
        playerID = player.getUniqueId();

        Request.removePlayer(playerID);
        Review.removePlayer(playerID);

        User.messagePlayer(sender, Config.remove_passed
                .replace("<PLAYERNAME>", playerName));

    }


    /**
     * Attempts to cancel a request made my a player
     */

    public void cancelTicket(CommandSender sender) {

        UUID playerID = ((Player) sender).getUniqueId();

        switch (Request.getStatus(playerID)) {

            case 0:
                User.messagePlayer(sender, Config.cancel_failed_no_ticket);
                return;
            case 1:
                Request.removeFromPndLst(playerID);
                User.messagePlayer(sender, Config.cancel_passed_notify_player);
                return;
            case 2:
                User.messagePlayer(sender, Config.cancel_failed_attending);
                return;
            case 3:
                User.messagePlayer(sender, Config.cancel_failed_completing);

        }

    }


    /**
     * Queries and tells the player the position/status of his request
     */

    public void printTicketStatus(CommandSender sender) {

        UUID playerID = ((Player) sender).getUniqueId();

        if (Admin.isAdmin(playerID)) {
            if (Request.inAtdLst(playerID)) {
                User.messagePlayer(sender, Config.status_staff_attending);
            } else {
                User.messagePlayer(sender, Config.status_staff_not_attending);
            }
            return;
        }

        switch (Request.getStatus(playerID)) {

            case 0:
                User.messagePlayer(sender, Config.status_no_ticket);
                return;
            case 1:
                String posInPndLst = Integer.toString(Request.getPosInPndLst(playerID));
                User.messagePlayer(sender, Config.status_pending
                        .replace("<POSITION>", posInPndLst));
                return;
            case 2:
                User.messagePlayer(sender, Config.status_attending);
                return;
            case 3:
                User.messagePlayer(sender, Config.status_completing);

        }
    }


    /**
     * Get all requests count. Group by statuses
     * Sends the message to the specified player
     */

    public void printTicketStats(CommandSender sender) {

        (new BukkitRunnable() {
            public void run() {

        int total = plugin.getDataSource().getTotalTicketCount(1);
        int upvote = plugin.getDataSource().getTotalTicketCount(2);

        (new BukkitRunnable() {
            public void run() {

        int upvotePercent;

        if (total == 0 || upvote == 0) {
            upvotePercent = 0;
        } else {
            upvotePercent = upvote * 100 / total;
        }

        String pending = Integer.toString(Request.getPndLstSize());
        String attending = Integer.toString(Request.getAtdLstSize());
        String completing = Integer.toString(Request.getCptLstSize());
        String completed = Integer.toString(Request.getCompletedToday());

        String total2S = Integer.toString(total);
        String upVotePercent2S = Integer.toString(upvotePercent);

        for (String message : Config.stats_message) {
            User.messagePlayer(sender, message
                    .replace("<PENDING>", pending)
                    .replace("<ATTENDING>", attending)
                    .replace("<COMPLETING>", completing)
                    .replace("<COMPLETED>", completed)
                    .replace("<TOTAL>", total2S)
                    .replace("<UPVOTE_PERCENT>", upVotePercent2S));
        }

            }
        }).runTask(plugin);

            }
        }).runTaskAsynchronously(plugin);

    }


    public void resetHonor(CommandSender sender, String target) {

        (new BukkitRunnable() {
            public void run() {

        if (plugin.getDataSource().resetAdminsHonor(target)) {
            User.messagePlayer(sender, Config.hpreset_passed
                    .replace("<ADMINNAME>", target));
        } else {
            User.messagePlayer(sender, Config.hpreset_failed
                    .replace("<ADMINNAME>", target));
        }

            }
        }).runTaskAsynchronously(plugin);

    }


    // print hptop module
    public void printHonorTop(CommandSender sender, int limit) {

        (new BukkitRunnable() {
            public void run() {

        String[][] honors = plugin.getDataSource().getTopHonors(limit);

        (new BukkitRunnable() {
            public void run() {

        for (String message : Config.hptop_header) {
            User.messagePlayer(sender, message);
        }

        for (int i = 0; i < limit; i++) {

            if (honors[i][0] == null) {
                // continue;
                break;
            }

            User.messagePlayer(sender, Config.hptop_body
                    .replace("<ADMINNAME>", honors[i][0])
                    .replace("<UPVOTE>", honors[i][1])
                    .replace("<DOWNVOTE>", honors[i][2])
                    .replace("<TOTAL>", honors[i][3])
                    .replace("<UPVOTE_PERCENT>", honors[i][4]));

        }

        for (String message : Config.hptop_footer) {
            User.messagePlayer(sender, message);
        }

            }
        }).runTask(plugin);

            }
        }).runTaskAsynchronously(plugin);

    }


    public void printHonorStats(CommandSender sender, String adminName) {

        (new BukkitRunnable() {
            public void run() {

        int upvote = plugin.getDataSource().getAdminTicketCount(adminName, 1);
        int downvote = plugin.getDataSource().getAdminTicketCount(adminName, 2);

        (new BukkitRunnable() {
            public void run() {

        int total = upvote + downvote;
        int upvotePercent;

        if (total == 0 || upvote == 0) {
            upvotePercent = 0;
        } else {
            upvotePercent = (upvote * 100 / total);
        }

        String upvote2S = Integer.toString(upvote);
        String downvote2S = Integer.toString(downvote);
        String total2S = Integer.toString(total);
        String upvotePercent2S = Integer.toString(upvotePercent);

        for (String message : Config.hpstats_message) {
            User.messagePlayer(sender, message
                    .replace("<UPVOTE>", upvote2S)
                    .replace("<DOWNVOTE>", downvote2S)
                    .replace("<TOTAL>", total2S)
                    .replace("<UPVOTE_PERCENT>", upvotePercent2S)
                    .replace("<ADMINNAME>", adminName));
        }

            }
        }).runTask(plugin);

            }
        }).runTaskAsynchronously(plugin);

    }


    // print history module
    public void printHonorHistory(CommandSender sender, int limit) {

        (new BukkitRunnable() {
            public void run() {

        String[][] history = plugin.getDataSource().getHistory(limit);

        (new BukkitRunnable() {
            public void run() {

        String rating;

        for (String message : Config.history_header) {
            User.messagePlayer(sender, message);
        }

        for (int i = 0; i < limit; i++) {

            if (history[i][0] == null) {
                // continue;
                break;
            }

            if (Long.parseLong(history[i][4]) == 0) {
                rating = Config.history_downvote_indicator;
            } else {
                rating = Config.history_upvote_indicator;
            }

            String datetime = new SimpleDateFormat("MM/dd/yy HH:mm")
                    .format(new Date(Long.parseLong(history[i][3]) * 1000));

            User.messagePlayer(sender, Config.history_body
                    .replace("<PLAYERNAME>", history[i][0])
                    .replace("<ADMINNAME>", history[i][1])
                    .replace("<DETAILS>", history[i][2])
                    .replace("<DATETIME>", datetime)
                    .replace("<RATING>", rating));

        }

        for (String message : Config.history_footer) {
            User.messagePlayer(sender, message);
        }

            }
        }).runTask(plugin);

            }
        }).runTaskAsynchronously(plugin);

    }


    public void toggleFly(CommandSender sender) {

        Player player = (Player) sender;
        
        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            User.messagePlayer(sender, Config.fly_disabled);
        } else {
            player.setAllowFlight(true);
            player.setFlying(true);
            User.messagePlayer(sender, Config.fly_enabled);
        }

    }


}