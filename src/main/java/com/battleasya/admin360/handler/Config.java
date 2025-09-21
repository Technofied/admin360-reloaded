package com.battleasya.admin360.handler;

import com.battleasya.admin360.Admin360;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class Config {

    private final Admin360 plugin;

    /* Config Constructor */
    public Config(Admin360 plugin) {
        this.plugin = plugin;
    }

    public static boolean version_set;
    public static boolean version_latest;

    public static String version;

    public static String no_permission;
    public static String incorrect_syntax;
    public static String reload_config;

    public static boolean ds_mysql;
    public static String ds_host;
    public static String ds_port;
    public static String ds_database;
    public static String ds_username;
    public static String ds_password;
    public static String ds_options;

    public static List<String> player_command_list;
    public static List<String> staff_command_list;

    public static boolean create_cooldown_enable;
    public static int create_cooldown_interval;
    public static String create_cooldown_message;

    public static boolean create_require_staff;

    public static String create_failed_no_staff;
    public static String create_failed_pending;
    public static String create_failed_attending;
    public static String create_failed_completing;
    public static String create_failed_restricted;

    public static List<String> create_passed_notify_player;
    public static List<String> create_passed_notify_staff;

    public static boolean create_passed_trigger_enable;
    public static List<String> create_passed_trigger_command;

    public static String cancel_failed_no_ticket;
    public static String cancel_failed_attending;
    public static String cancel_failed_restricted;
    public static String cancel_failed_completing;

    public static String cancel_passed_notify_player;

    public static String status_no_ticket;
    public static String status_pending;
    public static String status_attending;
    public static String status_completing;

    public static String status_staff_attending;
    public static String status_staff_not_attending;

    public static List<String> stats_message;

    public static List<String> list_header;
    public static String list_body;
    public static List<String> list_footer;

    public static boolean message_pending_on_join;

    public static boolean attend_auto_teleport;
    public static boolean attend_invincibility;

    public static String attend_failed_no_ticket;
    public static String attend_failed_not_online;
    public static String attend_failed_not_pending;
    public static String attend_failed_attending;

    public static List<String> attend_passed_notify_player;
    public static List<String> attend_passed_notify_staff;

    public static boolean attend_passed_trigger_enable;
    public static List<String> attend_passed_trigger_command;

    public static String teleport_failed;
    public static String teleport_passed;

    public static String invulnerable_on;
    public static String invulnerable_off;

    public static String info_failed;
    public static List<String> info_passed;

    public static String transfer_failed;
    public static String transfer_passed;

    public static String drop_failed;

    public static String drop_passed_notify_handler;
    public static List<String> drop_passed_notify_player;

    public static String close_failed;
    public static String close_passed;

    public static boolean close_passed_trigger_enable;
    public static List<String> close_passed_trigger_command;

    public static boolean review_reminder_enable;
    public static int review_reminder_interval;

    public static List<String> review_prompt;

    public static boolean review_prompt_trigger_enable;
    public static List<String> review_prompt_trigger_command;

    public static String review_failed;

    public static String review_received;
    public static String review_upvote_notify_handler;
    public static String review_downvote_notify_handler;

    public static boolean review_received_trigger_enable;
    public static List<String> review_received_trigger_command;

    public static List<String> purge_message;

    public static String remove_passed;
    public static String remove_failed;

    public static List<String> hpstats_message;

    public static int hptop_default_limit;

    public static List<String> hptop_header;
    public static String hptop_body;
    public static List<String> hptop_footer;

    public static String hpreset_failed;
    public static String hpreset_passed;

    public static int history_default_limit;

    public static String history_upvote_indicator;
    public static String history_downvote_indicator;

    public static List<String> history_header;
    public static String history_body;
    public static List<String> history_footer;

    public static String pending_tickets;

    /* Init checking */
    public void initConfig() {

        FileConfiguration config = plugin.getConfig();

        version_set = config.isSet("version");
        version_latest = plugin.getDescription().getVersion().equalsIgnoreCase(config.getString("version"));

    }

    public void checkConfig() {

        if (!version_set || !version_latest) {

            File configFile = new File(plugin.getDataFolder(), "config.yml");

            boolean rename = configFile.renameTo(new File(plugin.getDataFolder(), "config_old.yml"));

            if (rename) {
                plugin.getLogger().info("[Admin360-Reloaded] Renamed the old config file to config_old.yml.");
            } else {
                plugin.getLogger().severe("[Admin360-Reloaded] Failed to rename the old config file to config_old.yml.");
            }

            plugin.saveDefaultConfig();

        } else {
            plugin.getLogger().info("[Admin360-Reloaded] config.yml is at the latest version.");
        }

    }

    /* Load config into memory */
    public void fetchConfig() {

        FileConfiguration config = plugin.getConfig();

        version = config.getString("version");

        no_permission = config.getString("general.message.no-permission");
        incorrect_syntax = config.getString("general.message.incorrect-syntax");
        reload_config = config.getString("general.message.reload-config");

        ds_mysql = config.getBoolean("datasource.mysql");
        ds_host = config.getString("datasource.host");
        ds_port = config.getString("datasource.port");
        ds_database = config.getString("datasource.database");
        ds_username = config.getString("datasource.username");
        ds_password = config.getString("datasource.password");
        ds_options = config.getString("datasource.options");

        player_command_list = config.getStringList("help.message.player-commands");
        staff_command_list = config.getStringList("help.message.staff-commands");

        create_cooldown_enable = config.getBoolean("create.cooldown.enable");
        create_cooldown_interval = config.getInt("create.cooldown.interval");
        create_cooldown_message = config.getString("create.cooldown.message");

        create_require_staff = config.getBoolean("create.require-staff");

        create_failed_no_staff = config.getString("create.failed.message.no-staff");
        create_failed_pending = config.getString("create.failed.message.pending");
        create_failed_attending = config.getString("create.failed.message.attending");
        create_failed_completing = config.getString("create.failed.message.completing");
        create_failed_restricted = config.getString("create.failed.message.restricted");

        create_passed_notify_player = config.getStringList("create.passed.message.notify-player");
        create_passed_notify_staff = config.getStringList("create.passed.message.notify-staff");

        create_passed_trigger_enable = config.getBoolean("create.passed.trigger.enable");
        create_passed_trigger_command = config.getStringList("create.passed.trigger.command");

        cancel_failed_no_ticket = config.getString("cancel.failed.message.no-ticket");
        cancel_failed_attending = config.getString("cancel.failed.message.attending");
        cancel_failed_completing = config.getString("cancel.failed.message.completing");
        cancel_failed_restricted = config.getString("cancel.failed.message.restricted");

        cancel_passed_notify_player = config.getString("cancel.passed.message");

        status_no_ticket = config.getString("status.player.message.no-ticket");
        status_pending = config.getString("status.player.message.pending");
        status_attending = config.getString("status.player.message.attending");
        status_completing = config.getString("status.player.message.completing");

        status_staff_attending = config.getString("status.staff.message.attending");
        status_staff_not_attending = config.getString("status.staff.message.not-attending");

        stats_message = config.getStringList("stats.message");

        list_header = config.getStringList("list.message.header");
        list_body = config.getString("list.message.body");
        list_footer = config.getStringList("list.message.footer");

        message_pending_on_join = config.getBoolean("general.send-pending-message-to-admins-on-join");

        attend_auto_teleport = config.getBoolean("attend.auto-teleport");
        attend_invincibility = config.getBoolean("attend.ticket-invincibility");

        attend_failed_no_ticket = config.getString("attend.failed.message.no-ticket");
        attend_failed_not_online = config.getString("attend.failed.message.not-online");
        attend_failed_not_pending = config.getString("attend.failed.message.not-pending");
        attend_failed_attending = config.getString("attend.failed.message.attending");

        attend_passed_notify_player = config.getStringList("attend.passed.message.notify-player");
        attend_passed_notify_staff = config.getStringList("attend.passed.message.notify-staff");

        attend_passed_trigger_enable = config.getBoolean("attend.passed.trigger.enable");
        attend_passed_trigger_command = config.getStringList("attend.passed.trigger.command");

        teleport_failed = config.getString("teleport.failed.message");
        teleport_passed = config.getString("teleport.passed.message");

        invulnerable_on = config.getString("invulnerable.enabled");
        invulnerable_off = config.getString("invulnerable.disabled");

        pending_tickets = config.getString("general.message.pending-tickets");

        info_failed = config.getString("info.failed.message");
        info_passed = config.getStringList("info.passed.message");

        transfer_failed = config.getString("transfer.failed.message");
        transfer_passed = config.getString("transfer.passed.message");

        drop_failed = config.getString("drop.failed.message");
        drop_passed_notify_handler = config.getString("drop.passed.message.notify-handler");
        drop_passed_notify_player = config.getStringList("drop.passed.message.notify-player");

        close_failed = config.getString("close.failed.message");
        close_passed = config.getString("close.passed.message");

        close_passed_trigger_enable = config.getBoolean("close.passed.trigger.enable");
        close_passed_trigger_command = config.getStringList("close.passed.trigger.command");

        review_reminder_enable = config.getBoolean("review.reminder.enable");
        review_reminder_interval = config.getInt("review.reminder.interval");

        review_prompt = config.getStringList("review.prompt.message");

        review_prompt_trigger_enable = config.getBoolean("review.prompt.trigger.enable");
        review_prompt_trigger_command = config.getStringList("review.prompt.trigger.command");

        review_failed = config.getString("review.failed.message");

        review_received = config.getString("review.passed.message.received");
        review_upvote_notify_handler = config.getString("review.passed.message.upvote-notify");
        review_downvote_notify_handler = config.getString("review.passed.message.downvote-notify");

        review_received_trigger_enable = config.getBoolean("review.passed.trigger.enable");
        review_received_trigger_command = config.getStringList("review.passed.trigger.command");

        purge_message = config.getStringList("purge.message");

        remove_failed = config.getString("remove.failed.message");
        remove_passed = config.getString("remove.passed.message");

        hpstats_message = config.getStringList("hpstats.message");

        hptop_default_limit = config.getInt("hptop.default-limit");

        hptop_header = config.getStringList("hptop.message.header");
        hptop_body = config.getString("hptop.message.body");
        hptop_footer = config.getStringList("hptop.message.footer");

        hpreset_failed = config.getString("hpreset.failed.message");
        hpreset_passed = config.getString("hpreset.passed.message");

        history_default_limit = config.getInt("history.default-limit");

        history_upvote_indicator = config.getString("history.upvote-indicator");
        history_downvote_indicator = config.getString("history.downvote-indicator");

        history_header = config.getStringList("history.message.header");
        history_body = config.getString("history.message.body");
        history_footer = config.getStringList("history.message.footer");

    }

}
