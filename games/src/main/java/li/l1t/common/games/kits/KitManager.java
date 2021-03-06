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

package li.l1t.common.games.kits;

import li.l1t.common.games.kits.objective.ParentObjectiveResolver;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages kits. This includes keeping the list of available kits and objectives as well as
 * delegating loading to a {@link KitLoader}.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-06-21
 */
public class KitManager {
    private final KitLoader loader;
    private final ParentObjectiveResolver objectiveResolver = new ParentObjectiveResolver();
    private final List<Kit> kits = new ArrayList<>();
    private final List<Kit> kitsView = Collections.unmodifiableList(kits);
    private final Map<UUID, Kit> playerKits = new HashMap<>();

    /**
     * Creates a new kit manager and loads all kits currently in the kits directory.
     *
     * @param kitDirectory the directory containing the kit files (need not exist yet)
     */
    public KitManager(File kitDirectory) {
        this.loader = new KitLoader(this, kitDirectory);
        loadKits();
    }

    /**
     * Loads all kits currently in the {@link KitLoader#getKitDirectory() kits directory} to the
     * kits list.
     *
     * @see KitLoader#load()
     */
    public void loadKits() {
        this.kits.clear();
        this.kits.addAll(loader.load());
    }

    /**
     * @return an immutable view of all kits managed by this manager
     */
    public List<Kit> getKits() {
        return this.kitsView;
    }

    /**
     * Applies a kit to a player, that is, sets their inventory and armor contents to those
     * specified by the kit. Also notes which kit they have been given for later retrieval.
     *
     * @param kit the kit to apply
     * @param plr the player to apply the kit to
     */
    public void apply(Kit kit, Player plr) {
        Validate.notNull(kit);
        Validate.notNull(plr);
        plr.getInventory().setContents(kit.getContents());
        plr.getInventory().setArmorContents(kit.getArmor());
        this.playerKits.put(plr.getUniqueId(), kit);
    }

    /**
     * Applies a random kit to a player, that is, sets their inventory and armor contents to those
     * specified by the kit. Also notes which kit they have been given for later retrieval. This
     * loads the list of kits if it has not yet been loaded.
     *
     * @param plr the player to apply a random kit to
     */
    public void applyRandomKit(Player plr) {
        int rand = RandomUtils.nextInt(getKits().size());
        this.apply(getKits().get(rand), plr);
    }

    /**
     * Retrieves the kit that was most recently applied to a player. If no kit has ever been
     * applied to them, returns null.
     *
     * @param uuid the unique id of the player to query
     * @return the most recently applied kit for that player
     */
    public Kit getPlayerKit(UUID uuid) {
        return this.playerKits.get(uuid);
    }

    /**
     * Deletes a kit from memory and disk, i.e. removes it from the list of kits and deletes it
     * file.
     *
     * @param kit the kit to delete
     * @throws IOException if an error occurs while deleting
     */
    public void delete(Kit kit) throws IOException {
        kits.remove(kit);
        loader.delete(kit);
    }

    /**
     * Registers a new kit info with this manager and adds it to the list of kits.
     *
     * @param kit the kit info to register
     */
    public void register(Kit kit) {
        kits.add(kit);
    }


    /**
     * @return the kit loader associated with this manager
     */
    public KitLoader getLoader() {
        return loader;
    }

    /**
     * Returns the objective resolver for this manager. Use this to add custom objective resolvers.
     *
     * @return the objective resolver associated with this manager
     */
    public ParentObjectiveResolver getObjectiveResolver() {
        return objectiveResolver;
    }
}
