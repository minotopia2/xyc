/*
 * MIT License
 *
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy) and contributors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package li.l1t.common.games.maps;

import li.l1t.common.util.FileHelper;
import li.l1t.common.xyplugin.SqlXyGamePlugin;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class that loads maps.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 */
public abstract class AbstractMapLoader {

    protected File mapsFolder;
    protected List<String> availableMaps = new ArrayList<>();
    protected SqlXyGamePlugin plugin;
    /**
     * Map that is currently loaded or being loaded by this {@link AbstractMapLoader}.
     */
    protected String mapName = "NONE"; //TODO default should be null

    /**
     * Makes a new {@link AbstractMapLoader} and initialises the map list. Also sets an error on plug if any directory is invalid.
     *
     * @param plugin Plugin that owns this {@link AbstractMapLoader}.
     */
    public AbstractMapLoader(SqlXyGamePlugin plugin) {
        this.plugin = plugin;
        String path = this.getMapsLocation();
        try {
            this.mapsFolder = new File(path);
            String[] availableMapsArray = this.mapsFolder.list();
            if (availableMapsArray == null) {
                System.out.println(this.getClass().getSimpleName() + "|mapsFolder: " + path);
                plugin.setError(this.getClass().getSimpleName() + "§cmapsFolder is not a valid directory (or an I/O error occurred)! Please check your options.",
                        "AbstractMapLoader.init#2");
                return;
            }
            this.availableMaps = Arrays.asList(availableMapsArray);
            Bukkit.getConsoleSender().sendMessage(
                    this.getClass().getSimpleName() + "§eFound maps: " + this.availableMaps + " at " + this.mapsFolder.
                            getAbsolutePath());
        } catch (Exception e) {
            System.out.println(this.getClass().getSimpleName() + "|mapsFolder: " + path);
            plugin.setError(this.getClass().getSimpleName() + "§cCould not load map location or no valid path specified!", "AbstractMapLoader.init#1");
        }
    }

    /**
     * Deletes any existing world currently in {@link Bukkit#getWorldContainer()} by name of {@link AbstractMapLoader#getWorldName()}.
     *
     * @return {@code true} if the world was cleared successfully or {@code false} if an exception occurred. (Also sets error on plugin)
     */
    public boolean clearWorld() {
        Path toDelete = Paths.get(new File(Bukkit.getWorldContainer(), this.getWorldName()).getAbsolutePath());
        if (!toDelete.toFile().exists()) {
            return true;
        }
        try {
            FileHelper.deleteWithException(toDelete);
        } catch (IOException e) {
            e.printStackTrace();
            this.plugin.setError(this.getClass().getSimpleName() + "§cCould not clear world!", "AbstractMapLoader.clearWorld#1");
            return false;
        }
        return true;
    }

    /**
     * Copies the map by name this.mapName from this.mapsFolder to {@link Bukkit#getWorldContainer()}.
     *
     * @return Whether the map was copied successfully.
     */
    public boolean copyMap() {
        if (Bukkit.getWorld(this.getWorldName()) != null) {
            Bukkit.getConsoleSender().sendMessage(this.getClass().getSimpleName() + "§cWARNING! Tried to copy a map when there was already such map!");
            return false;
        }
        File sourceDir = new File(this.mapsFolder, this.mapName);
        File destDir = new File(Bukkit.getWorldContainer(), this.getWorldName());
        Bukkit.getConsoleSender().sendMessage(this.getClass().getSimpleName() + "§bNOW copying a map '" + this.mapName + "' from " + sourceDir.
                getAbsolutePath() + " to " + destDir.getAbsolutePath() + "!");
        try {
            FileHelper.copyFolder(sourceDir, destDir);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(this.getClass().getSimpleName() + "§cException while copying!");
            e.printStackTrace();
            return false;
        }
        Bukkit.getConsoleSender().sendMessage(this.getClass().getSimpleName() + "§bSuccessfully copied!");
        return true;
    }

    /**
     * @return Returns an unmodifiable List of all maps accessible by this {@link AbstractMapLoader}.
     */
    public List<String> getAvailableMaps() {
        return Collections.unmodifiableList(this.availableMaps);
    }

    /**
     * Returns a YamlConfiguration for a map available in this {@link AbstractMapLoader}.
     *
     * @param mapName The map to use.
     * @return A YamlConfiguration that is in the mapsFolder named "tdmap.meta.yml" (backwards compatibility).
     * @deprecated relies on the name of the file being "tdmap.meta.yml"
     */
    @Deprecated
    public YamlConfiguration getMapMetaYaml(String mapName) {
        return getMapMetaYaml(mapName, "tdmap.meta.yml");
    }

    /**
     * Returns a YamlConfiguration for a map available in this {@link AbstractMapLoader}.
     *
     * @param mapName  The map to use.
     * @param fileName the name of the meta file, relative to the map folder
     * @return A YamlConfiguration in the folder of the chosen map, with the given file name.
     */
    public YamlConfiguration getMapMetaYaml(String mapName, String fileName) {
        File location = new File(this.mapsFolder.getAbsolutePath() + File.separatorChar + mapName, fileName);
        System.out.println("MapMeta location: " + location.getAbsolutePath());
        return YamlConfiguration.loadConfiguration(location);
    }

    /**
     * Checks if a map by this name exists (for example for use with /votemap)
     *
     * @param name Name of the map to check.
     * @return If this map exists.
     */
    public boolean isMapAvailable(String name) {
        return this.availableMaps.contains(name);
    }

    /**
     * Loads the map at mapName.
     *
     * @return If loading succeeded.
     */
    public boolean loadMap() {
        if (Bukkit.getWorld(this.getWorldName()) != null) {
            Bukkit.getConsoleSender().sendMessage(this.getClass().getSimpleName() + "§cWARNING! Tried to load a map when there was already such map!");
            return false;
        }
        Bukkit.getConsoleSender().sendMessage(this.getClass().getSimpleName() + "§bNow loading map '" + this.mapName + "' ...");

        File uidDat = new File(Bukkit.getWorldContainer(), this.getWorldName() + "/uid.dat");
        if (uidDat.exists()) {
            try {
                Files.delete(uidDat.toPath());
            } catch (IOException e) {
                throw new IllegalStateException("Could not delete uid.dat", e);
            }
        }

        WorldCreator creator = WorldCreator.name(this.getWorldName()).environment(Environment.NORMAL).type(WorldType.NORMAL);
        World result = Bukkit.createWorld(creator);
        if (result == null) {
            this.plugin.setError(this.getClass().getSimpleName() + "§cCould not load map: world is null!", "AbstractMapLoader.loadMap#1");
            return false;
        }

        result.setAmbientSpawnLimit(0);
        result.setAnimalSpawnLimit(0);
        result.setAutoSave(false);
        result.setMonsterSpawnLimit(0);
        result.setDifficulty(Difficulty.PEACEFUL);
        result.setGameRuleValue("doDaylightCycle", "false");
        result.setGameRuleValue("doMobSpawning", "false");
        result.setGameRuleValue("doFireTick", "false");
        result.setGameRuleValue("doMobLoot", "false");
        result.setGameRuleValue("mobGriefing", "false");
        result.setGameRuleValue("keepInventory", "true");
        result.setGameRuleValue("naturalRegeneration", "true");
        result.setKeepSpawnInMemory(false);
        result.setFullTime(6000);
        result.setPVP(true);
        result.setStorm(false);
        result.setThundering(false);
        result.setWaterAnimalSpawnLimit(0);

        this.teleport(result);

        Bukkit.getConsoleSender().sendMessage(
                this.getClass().getSimpleName() + "§eWorld " + this.mapName + " loaded to '" + this.getWorldName() + "'.");

        return true;
    }

    /**
     * Loads a random map from a List of map names. If you don't use /votemap, try using {@link AbstractMapLoader#loadRandomMap()}.
     *
     * @param maps Maps to choose from
     * @see AbstractMapLoader#loadRandom(String...)
     */
    public void loadRandom(List<String> maps) {
        Validate.notEmpty(maps);
        this.mapName = (maps.size() == 1) ? maps.get(0) : this.chooseNext(1, maps);

        this.loadMyMap();
    }

    /**
     * Loads a random map from all maps specified of map names. If you don't use /votemap, try using {@link AbstractMapLoader#loadRandomMap()}.
     *
     * @param maps Maps to choose from
     * @see AbstractMapLoader#loadRandom(List)
     */
    public void loadRandom(String... maps) {
        Validate.notEmpty(maps);
        this.mapName = (maps.length == 1) ? maps[0] : this.chooseNext(1, Arrays.asList(maps));

        this.loadMyMap();
    }

    /**
     * Loads a random available map.
     */
    public void loadRandomMap() {
        this.mapName = this.chooseNext(1, this.availableMaps);

        this.loadMyMap();
    }

    /**
     * Loads a map denoted by this name. Loads a random map if no map by this name is available.
     *
     * @param mapName Map to load.
     * @see AbstractMapLoader#isMapAvailable(String)
     * @see AbstractMapLoader#loadRandomMap()
     */
    public void loadThisMap(String mapName) {
        if (!new File(this.mapsFolder, mapName).exists()) {
            Bukkit.getConsoleSender().sendMessage("Could not find map '" + mapName + "'! Loading random map.");
            this.loadRandomMap();
            return;
        }
        this.mapName = mapName;

        this.loadMyMap();
    }

    /**
     * @return The last map used by this {@link AbstractMapLoader}, so that it won't get loaded twice in a row. {@code null} if there's none or this should be ignored.
     */
    protected abstract String getLastMapName();

    /**
     * @return The directory where all maps are located.
     */
    protected abstract String getMapsLocation();

    /**
     * @return The name of the world folder to copy the map to.
     */
    protected abstract String getWorldName();

    /**
     * Clears the world at {@link AbstractMapLoader#getWorldName()} and then copies this.mapName to that location. After doing that, it loads it to
     * memory. Implementations can call this on their own randomizing algorithms.
     */
    protected void loadMyMap() {
        this.clearWorld();

        if (!this.copyMap()) {
            System.out.println(this.getClass().getSimpleName() + "|Could not copy map '" + this.mapName + "'.");
        }

        if (!this.loadMap()) {
            System.out.println(this.getClass().getSimpleName() + "|Could not load map '" + this.mapName + "'.");
        }
    }

    /**
     * This will be called when the map has been loaded and is primarily to teleport players there. The map will be loaded at the world name
     * {@link AbstractMapLoader#getWorldName()}.
     *
     * @param worldSoFar The loaded world will be passed here, modify if needed, then return it.
     * @return worldSoFar, with implementation modifications.
     */
    protected abstract World teleport(World worldSoFar);

    private String chooseNext(int iteration, List<String> maps) {
        int mapCount = maps.size();
        int theChosenOne = RandomUtils.nextInt(mapCount);
        String mapName = maps.get(theChosenOne);
        if (mapCount > 1 && mapName.equals(this.getLastMapName()) && iteration <= 10) {
            return this.chooseNext(iteration + 1, maps);
        }
        return mapName;
    }

    public File getMapsFolder() {
        return this.mapsFolder;
    }

    public SqlXyGamePlugin getPlugin() {
        return this.plugin;
    }

    public String getMapName() {
        return this.mapName;
    }
}
