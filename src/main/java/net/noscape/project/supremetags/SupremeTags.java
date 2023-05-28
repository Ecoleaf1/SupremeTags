package net.noscape.project.supremetags;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.noscape.project.supremetags.api.SupremeTagsAPI;
import net.noscape.project.supremetags.checkers.*;
import net.noscape.project.supremetags.commands.*;
import net.noscape.project.supremetags.guis.tageditor.EditorListener;
import net.noscape.project.supremetags.handlers.Editor;
import net.noscape.project.supremetags.handlers.SetupTag;
import net.noscape.project.supremetags.handlers.hooks.*;
import net.noscape.project.supremetags.handlers.menu.*;
import net.noscape.project.supremetags.listeners.*;
import net.noscape.project.supremetags.managers.*;
import net.noscape.project.supremetags.storage.*;
import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.*;

public final class SupremeTags extends JavaPlugin {

    private static SupremeTags instance;
    private TagManager tagManager;
    private CategoryManager categoryManager;
    private MergeManager mergeManager;

    private static SupremeTagsAPI api;

    private static Economy econ = null;
    private static Permission perms = null;

    private static MySQL mysql;
    private static H2Database h2;
    private final H2UserData h2user = new H2UserData();
    private static String connectionURL;
    private final MySQLUserData user = new MySQLUserData();

    private static final HashMap<Player, MenuUtil> menuUtilMap = new HashMap<>();
    private final HashMap<Player, Editor> editorList = new HashMap<>();
    private final HashMap<Player, SetupTag> setupList = new HashMap<>();

    private boolean legacy_format;
    private boolean cmi_hex;
    private boolean disabledWorldsTag;

    private PlayerManager playerManager;
    private PlayerConfig playerConfig;

    public static File latestConfigFile;
    public static FileConfiguration latestConfigConfig;

    private final String host = getConfig().getString("data.address");
    private final int port = getConfig().getInt("data.port");
    private final String database = getConfig().getString("data.database");
    private final String username = getConfig().getString("data.username");
    private final String password = getConfig().getString("data.password");
    private final String options = getConfig().getString("data.options");

    @Override
    public void onEnable() {
        init();
    }

    @Override
    public void onDisable() {
        tagManager.unloadTags();
        editorList.clear();
        //setupList.clear();

        if (isMySQL()) {
            mysql.disconnected();
        }
    }

    private void init() {
        instance = this;

        Logger logger = Bukkit.getLogger();

        this.saveDefaultConfig();
        this.callMetrics();

        sendConsoleLog();

        if (isH2()) {
            connectionURL = "jdbc:h2:" + getDataFolder().getAbsolutePath() + "/database";
            h2 = new H2Database(connectionURL);
        }

        if (isMySQL()) {
            mysql = new MySQL(host, port, database, username, password, options);
        }

        tagManager = new TagManager(getConfig().getBoolean("settings.cost-system"));
        categoryManager = new CategoryManager();
        playerManager = new PlayerManager();
        mergeManager = new MergeManager();
        //playerConfig = new PlayerConfig();

        Objects.requireNonNull(getCommand("tags")).setExecutor(new Tags());

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        getServer().getPluginManager().registerEvents(new EditorListener(), this);
        getServer().getPluginManager().registerEvents(new UpdateChecker(this), this);
        getServer().getPluginManager().registerEvents(new SetupListener(), this);

        legacy_format = getConfig().getBoolean("settings.legacy-hex-format");
        cmi_hex = getConfig().getBoolean("settings.cmi-color-support");
        disabledWorldsTag = getConfig().getBoolean("settings.tag-command-in-disabled-worlds");

        merge(logger);

        if (isPlaceholderAPI()) {
            logger.info(ChatColor.YELLOW + "> PlaceholderAPI: Found");
            new PAPI(this).register();
        } else {
            logger.info(ChatColor.RED + "> PlaceholderAPI: Not Found!");
        }


        tagManager.loadTags();

        categoryManager.loadCategories();
        categoryManager.loadCategoriesTags();
        tagManager.getDataItem().clear();

        deleteCurrentLatestConfig();

        latestConfigFile = new File(getDataFolder(), "DEFAULT-CONFIG-LATEST.yml");

        if (!latestConfigFile.exists()) {
            saveResource("DEFAULT-CONFIG-LATEST.yml", true);
        }

        latestConfigConfig = new YamlConfiguration();
        try {
            latestConfigConfig.load(latestConfigFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }

        api = new SupremeTagsAPI();

        if (tagManager.getTags().size() == 0) {
            tagManager.loadTags();
        }
    }

    public static SupremeTags getInstance() { return instance; }

    public TagManager getTagManager() { return tagManager; }

    public CategoryManager getCategoryManager() { return categoryManager; }

    public static MenuUtil getMenuUtil(Player player) {
        MenuUtil menuUtil;

        if (menuUtilMap.containsKey(player)) {
            return menuUtilMap.get(player);
        } else {
            menuUtil = new MenuUtil(player, UserData.getActive(player.getUniqueId()));
            menuUtilMap.put(player, menuUtil);
        }

        return menuUtil;
    }

    public static MenuUtil getMenuUtilIdentifier(Player player, String identifier) {
        MenuUtil menuUtil;

        if (menuUtilMap.containsKey(player)) {
            return menuUtilMap.get(player);
        } else {
            menuUtil = new MenuUtil(player, identifier);
            menuUtilMap.put(player, menuUtil);
        }

        return menuUtil;
    }
 
    public static MenuUtil getMenuUtil(Player player, String category) {
        MenuUtil menuUtil;

        if (menuUtilMap.containsKey(player)) {
            return menuUtilMap.get(player);
        } else {
            menuUtil = new MenuUtil(player, UserData.getActive(player.getUniqueId()), category);
            menuUtilMap.put(player, menuUtil);
        }

        return menuUtil;
    }

    public HashMap<Player, MenuUtil> getMenuUtil() {
        return menuUtilMap;
    }

    public static String getConnectionURL() {
        return connectionURL;
    }

    public H2UserData getUserData() { return h2user; }

    public static H2Database getDatabase() { return h2; }

    public MySQLUserData getUser() {
        return instance.user;
    }

    public static MySQL getMysql() {
        return mysql;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        legacy_format = getConfig().getBoolean("settings.legacy-hex-format");
        cmi_hex = getConfig().getBoolean("settings.cmi-color-support");
        disabledWorldsTag = getConfig().getBoolean("settings.tag-command-in-disabled-worlds");
    }

    public boolean isLegacyFormat() {
        return legacy_format;
    }

    public void merge(Logger log) {
        mergeManager.merge(log);
    }

    private void sendConsoleLog() {
        Logger logger = Bukkit.getLogger();

        logger.info("");
        logger.info(ChatColor.GREEN + "  ____  _   _ ____  ____  _____ __  __ _____ _____  _    ____ ____  ");
        logger.info(ChatColor.GREEN + " / ___|| | | |  _ \\|  _ \\| ____|  \\/  | ____|_   _|/ \\  / ___/ ___| ");
        logger.info(ChatColor.GREEN + " \\___ \\| | | | |_) | |_) |  _| | |\\/| |  _|   | | / _ \\| |  _\\___ \\ ");
        logger.info(ChatColor.GREEN + "  ___) | |_| |  __/|  _ <| |___| |  | | |___  | |/ ___ \\ |_| |___) |");
        logger.info(ChatColor.GREEN + " |____/ \\___/|_|   |_| \\_\\_____|_|  |_|_____| |_/_/   \\_\\____|____/ ");
        logger.info(ChatColor.GRAY + " Allow players to show off their supreme tags!");
        logger.info("");
        logger.info(ChatColor.YELLOW + "> Version: " + getDescription().getVersion());
        logger.info(ChatColor.YELLOW + "> Author: DevScape");

        if (getServer().getPluginManager().getPlugin("NBTAPI") == null) {
            logger.warning(ChatColor.RED + "> NBTAPI: Supremetags requires NBTAPI to run, disabling plugin....");
            getServer().getPluginManager().disablePlugin(this);
        } else {
            logger.info(ChatColor.YELLOW + "> NBTAPI: Found!");
        }

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
            setupPermissions();
            logger.info(ChatColor.YELLOW + "> Vault: Found!");
        } else {
            logger.info(ChatColor.RED + "> Vault: Not Found!");
        }

        if (isH2()) {
            logger.info(ChatColor.YELLOW + "> Database: H2!");
        } else if (isMySQL()) {
            logger.info(ChatColor.YELLOW + "> Database: MySQL!");
        }

        if (getConfig().getBoolean("settings.update-check")) {
            UpdateChecker updater = new UpdateChecker(this);
            updater.fetch();
            if (updater.hasUpdateAvailable()) {
                logger.info(ChatColor.AQUA + "> An update is available! " + updater.getSpigotVersion());
                logger.info(ChatColor.AQUA + "Download at https://www.spigotmc.org/resources/%E2%9C%85-supremetags-%E2%9C%85-1-8-1-19-placeholderapi-support-unlimited-tags-%E2%9C%85.103140/");
            } else {
                logger.info(ChatColor.YELLOW + "> Plugin up to date!");
            }
        }
    }

    public Boolean isH2() {
        return Objects.requireNonNull(getConfig().getString("data.type")).equalsIgnoreCase("H2");
    }

    public Boolean isMySQL() {
        return Objects.requireNonNull(getConfig().getString("data.type")).equalsIgnoreCase("MYSQL");
    }

    private void callMetrics() {
        int pluginId = 18038;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(new Metrics.SimplePie("used_language", () -> getConfig().getString("language", "en")));

        metrics.addCustomChart(new Metrics.DrilldownPie("java_version", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            String javaVersion = System.getProperty("java.version");
            Map<String, Integer> entry = new HashMap<>();
            entry.put(javaVersion, 1);
            if (javaVersion.startsWith("1.7")) {
                map.put("Java 1.7", entry);
            } else if (javaVersion.startsWith("1.8")) {
                map.put("Java 1.8", entry);
            } else if (javaVersion.startsWith("1.9")) {
                map.put("Java 1.9", entry);
            } else {
                map.put("Other", entry);
            }
            return map;
        }));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static SupremeTagsAPI getTagAPI() {
        return api;
    }

    private void deleteCurrentLatestConfig() {
        latestConfigFile = new File(getDataFolder(), "DEFAULT-CONFIG-LATEST.yml");

        if (latestConfigFile.exists()) {
            latestConfigFile.delete();
        }
    }

    public HashMap<Player, Editor> getEditorList() {
        return editorList;
    }

    public boolean isCMIHex() {
        return cmi_hex;
    }

    public boolean isDisabledWorldsTag() {
        return disabledWorldsTag;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    public MergeManager getMergeManager() {
        return mergeManager;
    }

    public PlayerConfig getPlayerConfig() {
        return playerConfig;
    }

    public HashMap<Player, SetupTag> getSetupList() {
        return setupList;
    }

    public boolean isPlaceholderAPI() {
        return getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }
}
