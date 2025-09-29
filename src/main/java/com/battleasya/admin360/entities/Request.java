package com.battleasya.admin360.entities;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.battleasya.admin360.handler.Config;

public class Request {

    public static int completedToday = 0;

    /** Collection of Pending Requests */
    public static Queue<Request> requestPending = new LinkedList<>();

    /**
     * Collection of Attending Requests
     * Key: UUID of Admin
     * Value: Request
     */
    public static HashMap<UUID, Request> requestAttending = new HashMap<>();

    /**
     * Collection of Completing Requests (awaits user feedback)
     * Key: UUID of Player
     * Value: Request
     */
    public static HashMap<UUID, Request> requestCompleting = new HashMap<>();

    /** UUID of Creator of Request */
    private final UUID playerID;

    /** Name of Creator of Request */
    private final String playerName;

    /** Comment of Request */
    private final String comment;

    /** Creation Timestamp */
    private final long timestamp;

    /** UUID of Attendants of Request */
    private UUID adminID;

    /** Name of Attendants of Request */
    private String adminName;

    /* Create a new request */
    public Request(UUID playerID, String playerName, String comment){
        this.playerID = playerID;
        this.playerName = playerName;
        this.comment = comment;
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    /* Gets the player's name who create the request */
    public UUID getPlayerID() {
        return playerID;
    }

    public String getPlayerName() {
        return playerName;
    }

    /* Gets the reason the player set for this request */
    public String getComment() {
        return comment;
    }

    /* Gets the Epoch timestamp this request was created*/
    public Long getTimestamp() {
        return timestamp;
    }

    /* Gets the admin who handled this request */
    public UUID getHandledByID() {
        return adminID;
    }

    public String getHandledByName() {
        return adminName;
    }

    /* Sets the admin who handled this request */
    public void setHandledBy(UUID adminID) {
        this.adminID = adminID;
    }

    public void setHandledBy(String adminName) {
        this.adminName = adminName;
    }


    /**
     * 0: No request present
     * 1: Request is currently queued
     * 2: Request is being honored by admins
     * 3: Request is completed and awaiting review
     */
    public static int getStatus(UUID playerID) {

        // check keys in requestCompleting
        if (inCptLst(playerID)) {
            return 3;
        }

        // check elements in requestPending
        for (Request request : requestPending) {
            if (request.getPlayerID().equals(playerID))
                return 1;
        }

        // check values in requestAttending
        for (Map.Entry<UUID, Request> entry : requestAttending.entrySet()) {
            if (entry.getValue().getPlayerID().equals(playerID)) {
                return 2;
            }
        }

        // return 0 if no open ticket
        return 0;

    }


    /* Removes any requests associated with the specified player */
    public static void removePlayer(UUID playerID){

        // Remove from requestPending
        removeFromPndLst(playerID);

        // Remove from requestAttending
        for (Map.Entry<UUID, Request> entry : requestAttending.entrySet()) {
            if (entry.getValue().getPlayerID().equals(playerID)){
                UUID adminID = entry.getKey();

                // Get admin player once
                Player admin = Bukkit.getPlayer(adminID);
                if (admin != null) {
                    // Un-invincibilize admin
                    if (Config.attend_invincibility) {
                        User.messagePlayer(admin, Config.invulnerable_off);
                        admin.setInvulnerable(false);
                    }

                    // Reset flight mode
                    admin.setAllowFlight(false);
                    admin.setFlying(false);
                }
                
                removeFromAtdLst(adminID);
                break; // prevent concurrent modification error
            }
        }

        // Remove from requestAwaiting
        removeFromCptLst(playerID);

    }

    public static void removeAdmin(UUID adminID) {
        removeFromAtdLst(adminID);
    }


    public static boolean inAtdLst(UUID adminID) {
        return requestAttending.containsKey(adminID);
    }

    public static boolean inCptLst(UUID playerID) {
        return requestCompleting.containsKey(playerID);
    }


    public static Request getPndRequest(UUID playerID) {

        for (Request request : requestPending) {
            if (request.getPlayerID().equals(playerID)) {
                return request;
            }
        }

        return null;

    }

    public static Request getAtdRequest(UUID adminID) {
        return requestAttending.get(adminID);
    }


    public static int getPndLstSize() {
        return requestPending.size();
    }

    public static int getAtdLstSize() {
        return requestAttending.size();
    }

    public static int getCptLstSize() {
        return requestCompleting.size();
    }


    public static void addToPndLst(Request request) {
        requestPending.add(request);
    }

    public static void addToAtdLst(UUID adminID, Request request) {
        requestAttending.put(adminID, request);
    }

    public static void addToCptLst(UUID playerID, Request request) {
        requestCompleting.put(playerID, request);
    }


    public static Request getHeadOfPndLst() {
        return requestPending.poll();
    }

    public static void removeFromPndLst(UUID playerID){
        requestPending.removeIf(request -> request.getPlayerID().equals(playerID));
    }

    public static void removeFromPndLst(Request request) {
        requestPending.remove(request);
    }

    public static Request removeFromAtdLst(UUID adminID) {
        return requestAttending.remove(adminID);
    }

    public static Request removeFromCptLst(UUID playerID) {
        return requestCompleting.remove(playerID);
    }


    public static void clearPndLst() {
        requestPending.clear();
    }

    public static void clearAtdLst() {
        requestAttending.clear();
    }

    public static void clearCptLst() {
        requestCompleting.clear();
    }


    public static int getCompletedToday() {
        return completedToday;
    }

    public static void addCompletedToday() {
        completedToday += 1;
    }


    public static boolean isPndLstEmpty() {
        return requestPending.isEmpty();
    }

    public static int getPosInPndLst(UUID playerID){

        int i = 1;

        for (Request request : requestPending) {
            if (request.getPlayerID().equals(playerID)) {
                return i;
            }
            i++;
        }

        return -1;

    }

}
