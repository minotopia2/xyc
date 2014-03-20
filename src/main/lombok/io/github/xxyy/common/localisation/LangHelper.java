package io.github.xxyy.common.localisation;

import com.google.common.io.Files;
import io.github.xxyy.common.XyHelper;
import io.github.xxyy.common.sql.SafeSql;
import io.github.xxyy.common.util.CommandHelper;
import io.github.xxyy.common.xyplugin.AbstractXyPlugin;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that provides some static methods to deal with localising XyPlugins.
 */
public class LangHelper {

    static HashMap<String, HashMap<String, YamlConfiguration>> langCache = new HashMap<>();
    @SuppressWarnings("unused")
    private static HashMap<String, String> chosenLangCache = new HashMap<>();
    public static final Pattern PATTERN = Pattern.compile("`([^` ]+)`");

    /**
     * Parses a String so that a) {@link ChatColor} is parsed with '&amp;' instead of the paragraph sign b) html entities are replaced with their unicode representations.
     */
    public static String applyCodes(String str) {
        return ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeHtml(str));
    }

    public static boolean changeSenderLang(String senderName) {
        return false; //NYI
    }

    /**
     * Clears the language cache for a plugin, so that language files will be reloaded.
     */
    public static void clearPluginLangCache(String pluginId) {
        if (!LangHelper.langCache.containsKey(pluginId)) {
            return;
        }
        LangHelper.langCache.remove(pluginId);
    }

    private static void updateLanguageFile(FileConfiguration cfg, FileConfiguration defaultCfg, File file, AbstractXyPlugin plug) {
        Map<String, Object> newEntryMap = defaultCfg.getValues(true);
        newEntryMap.keySet().removeAll(cfg.getValues(true).keySet());//changes on key set are reflected to map.

        if (!newEntryMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : newEntryMap.entrySet()) {
                if (!cfg.contains(entry.getKey())) {
                    cfg.set(entry.getKey(), entry.getValue());
                }
            }
            try {
                cfg.save(file);// vv Will not be printed in case of exception
                CommandHelper.sendMessageToOpsAndConsole("§a[" + plug.getName() + "] Your language files were outdated and"
                        + " have automagically been updated for you. You may want to check if the changes reflect your opinion :)");
                System.out.println("Updated Language Nodes: " + CommandHelper.CSCollection(newEntryMap.keySet()));
            } catch (IOException e) {
                System.out.println("Could not save modified language file. That's an error.");
            }
        }
    }

    /**
     * loc &amp; pl are normally the same object. Files will be pulled from /lang/*.lng.yml folder in JAR languages are defined by getShippedLocales()
     *
     * @param loc    The localization object to pull the names from
     * @param plugin The plugin to copy the files from
     */
    public static void copyLangsFromJar(XyLocalizable loc, AbstractXyPlugin plugin) {
        HashMap<String, YamlConfiguration> languages = new HashMap<>();

        for (String language : loc.getShippedLocales()) {
            try {
                File destinationFile = new File(plugin.getDataFolder().getAbsolutePath()+"/lang/", language + ".lng.yml");
                Files.createParentDirs(destinationFile);
                YamlConfiguration loadedCfg = YamlConfiguration.loadConfiguration(destinationFile);

                if (destinationFile.exists()) {
                    InputStream in = plugin.getResource("lang/" + language + ".lng.yml");
                    YamlConfiguration defaultCfg = YamlConfiguration.loadConfiguration(in);
                    SafeSql.tryClose(in);
                    updateLanguageFile(loadedCfg, defaultCfg, destinationFile, plugin);
                    languages.put(language, loadedCfg);
                } else {
                    if(!destinationFile.createNewFile()){
                        throw new IOException("Could not create new language file: "+destinationFile.getAbsolutePath());
                    }
                    InputStream in;
                    try (FileOutputStream out = new FileOutputStream(destinationFile)) {
                        in = plugin.getResource("lang/" + language + ".lng.yml");
                        int read;
                        while ((read = in.read()) != -1) {
                            out.write(read);
                        }
                        out.flush();
                    }
                    in.close();
                    languages.put(language, YamlConfiguration.loadConfiguration(destinationFile));
                }
            } catch (Exception exc) {
                plugin.getLogger().log(Level.WARNING, "Could not copy localization files from JAR: " + plugin.getName() + "/" + language, exc);
            }
        }
        LangHelper.langCache.put(plugin.getName(), languages);
    }

    /**
     * Gets the {@link YamlConfiguration} object that represents a specific language file of a specific plugin.
     *
     * @return {@link YamlConfiguration} or <code>null</code> if not found.
     */
    public static YamlConfiguration getLangFile(String lang, String pluginId) {
        if (!LangHelper.langCache.containsKey(pluginId)) {
            return null;
        }
        return LangHelper.langCache.get(pluginId).get(lang);
    }

    /**
     * Gets an option from the language metadata provided at YAML node "info".
     *
     * @param option   the option to get (i.e. "info.OPTION")
     * @param lang     The language file to use.
     * @param pluginId The plugin of the file.
     *
     * @return The option's value or "XYC-notexists"/"XYC-notexists2"
     */
    public static String getOption(String option, String lang, String pluginId) {
        if (!LangHelper.langCache.containsKey(pluginId)) {
            return "XYC-notloaded";
        }
        YamlConfiguration cfg = LangHelper.langCache.get(pluginId).get(lang);
        if (cfg == null) {
            return "XYC-notexists";
        }
        return cfg.getString("info." + option, "XYC-notexists2");
    }

    /**
     * Gets the lang
     * <code>senderName</code> has chosen for all messages; Only returns {@link XyHelper#defaultLang} for now.
     *
     * @param senderName Name of the {@link CommandSender} whose lang will be fetched.
     */
    public static String getSenderChosenLang(String senderName) {
        return XyHelper.defaultLang;
    }

    /**
     * Returns a localised String.
     *
     * @param locId      Key to look for. (actually, the valuze of "lang.KEY" will be returned)
     * @param senderName Who will receive this message (for choosing languages) or "CONSOLE" to choose {@link XyHelper#defaultLang}.
     * @param pluginId   The plugin providing this file
     *
     * @return A localised string or <code>locId</code> on failure.
     */
    public static String localiseString(String locId, String senderName, String pluginId) {
        String chLang = LangHelper.getSenderChosenLang(senderName);
        HashMap<String, YamlConfiguration> lMap = LangHelper.langCache.get(pluginId);

        if (lMap == null) {
            System.err.println("Error: No such plugin: " + pluginId + " @ LangHelper#localiseString");
            return locId;
        }

        if (!lMap.containsKey(chLang)) {
            chLang = XyHelper.defaultLang;
            if (!lMap.containsKey(chLang)) {
                return locId;
            }
        }

        String result = LangHelper.applyCodes(lMap.get(chLang).getString("lang." + locId, locId));
        Matcher matcher = PATTERN.matcher(result);
        StringBuffer buf = new StringBuffer();

        while(matcher.find()){
            matcher.appendReplacement(buf, localiseString(matcher.group(1), senderName, pluginId));
        }

        matcher.appendTail(buf);

        return buf.toString();
    }

    /**
     * Prints a full dump of the keys loaded for this file and plugin to {@link System#out}.
     */
    public static void printDebug(String pl, String lang) {
        HashMap<String, YamlConfiguration> map = LangHelper.langCache.get(pl);
        System.out.println(map.get(lang).toString());
        for (String key : map.get(lang).getKeys(true)) {
            System.out.println(key);
        }
    }

    /**
     * Reloads a language file for this plugin. Prints a message to {@link System#out} on failure.
     *
     * @param pl  Plugin to use
     * @param lng File to reload
     */
    public static void reloadLang(AbstractXyPlugin pl, String lng) {
        try {
            HashMap<String, YamlConfiguration> hm = LangHelper.langCache.get(pl.getName());
            if (hm == null) {
                CommandHelper.sendMessageToOpsAndConsole("§4[XYC] WARNING: Could not reload " + lng + " from "
                        + pl.getName() + ": No such plugin!");
                return;
            }
            YamlConfiguration cfg = hm.get(lng);
            if (cfg == null) {
                CommandHelper.sendMessageToOpsAndConsole("§4[XYC] WARNING: Could not reload " + lng + " from "
                        + pl.getName() + ": No such language!");
                return;
            }
            cfg.load("plugins/XYC/lang/" + pl.getName() + "/" + lng + ".lng.yml");
        } catch (IOException | InvalidConfigurationException | NullPointerException e) {
            CommandHelper.sendMessageToOpsAndConsole("§4[XYC] WARNING: Could not reload " + lng + " from "
                    + pl.getName());
            e.printStackTrace();
        }
    }

    /**
     * Localises a String and sends it to
     * <code>sender</code>, prefixed with the result of getChatPrefix() for
     * <code>plug</code>, using getName() of
     * <code>plug</code> as plugin key.
     *
     * @see LangHelper#localiseString(String, String, String)
     */
    public static void sendLocalizedStringWithPrefix(String locId, CommandSender sender, AbstractXyPlugin plug) {
        String str = LangHelper.localiseString(locId, sender.getName(), plug.getName());
        if (!(str == null || str.isEmpty())) {
            sender.sendMessage(plug.getChatPrefix() + str);
        }
    }
}