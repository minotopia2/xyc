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

package li.l1t.common.util.inventory;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.Validate;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This factory helps with creating {@link org.bukkit.inventory.ItemStack}s. Kept in this package
 * instead of {@link li.l1t.common.inventory} for historic reasons.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30/01/14
 */
@SuppressWarnings("UnusedDeclaration")
public class ItemStackFactory {
    private final ItemStack base;
    private final List<String> lore;
    private MaterialData materialData;
    private ItemMeta meta;

    /**
     * Creates a factory from a base {@link org.bukkit.inventory.ItemStack}.
     *
     * @param source the item stack to use as base for this factory
     */
    public ItemStackFactory(ItemStack source) {
        base = source;
        materialData = source.getData();
        meta = source.getItemMeta(); //returns new meta if unset
        lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
    }

    /**
     * Creates a factory from a {@link org.bukkit.Material}.
     * The resulting stack will have an amount of 1.
     *
     * @param material the material of the product
     */
    public ItemStackFactory(Material material) {
        this(new ItemStack(material));
    }

    /**
     * @param newAmount the new amount of the product
     * @return this factory
     */
    public ItemStackFactory amount(int newAmount) {
        base.setAmount(newAmount);
        return this;
    }

    /**
     * @param displayName the new display name of the product
     * @return this factory
     */
    public ItemStackFactory displayName(String displayName) {
        meta.setDisplayName(displayName);
        return this;
    }

    /**
     * Sets the display name of this factory if the resulting stack would not have a custom display name.
     *
     * @param defaultDisplayName the display name to set
     * @return this factory
     */
    public ItemStackFactory defaultDisplayName(String defaultDisplayName) {
        if (!meta.hasDisplayName()) {
            return displayName(defaultDisplayName);
        }
        return this;
    }

    /**
     * Sets the resulting item stack's lore, overriding any previous values.
     *
     * @param lore the new lore
     * @return this factory
     */
    public ItemStackFactory lore(List<String> lore) {
        this.lore.clear();
        this.lore.addAll(lore);
        return this;
    }

    /**
     * Adds a potion effect to the product. <p><b>Important:</b> This does not set the base data, which means that given
     * effect will be handled as a custom effect and will not modify potion colour or display name!</p>
     *
     * @param effect the effect to add
     * @return this factory
     */
    public ItemStackFactory effect(PotionEffect effect) {
        Preconditions.checkState(meta instanceof PotionMeta, "expected potion meta, got: %s", meta.getClass());
        ((PotionMeta) meta).addCustomEffect(effect, /* overwrite */ true);
        return this;
    }

    /**
     * Sets the base potion effect of the product. This causes Minecraft to change the potion colour and display name.
     *
     * @param data the data to add
     * @return this factory
     */
    public ItemStackFactory potion(PotionData data) {
        Preconditions.checkState(meta instanceof PotionMeta, "expected potion meta, got: %s", meta.getClass());
        ((PotionMeta) meta).setBasePotionData(data);
        return this;
    }

    /**
     * Appends a collection of strings to the resulting item stack's lore, treating every element as a separate line.
     * If this factory was constructed with a template item stack, this method will append to its existing lore, if any.
     *
     * @param loreToAppend the lines to add to the lore
     * @return this factory
     */
    public ItemStackFactory appendLore(Collection<String> loreToAppend) {
        lore.addAll(loreToAppend);
        return this;
    }

    /**
     * Adds a string to the lore of the product. If given a simple string, it will be added as
     * new line. If given a String containing newlines, it will split the input by {@code \n}
     * and add each result String to the lore. If the factory was constructed with a template item
     * stack, this will be appended to its existing lore, if any.
     *
     * @param whatToAdd the input string
     * @return this factory
     */
    public ItemStackFactory lore(String whatToAdd) {
        Collections.addAll(lore, whatToAdd.split("\r?\n"));
        return this;
    }

    /**
     * Appends an array of strings to the resulting item stack's lore, treating every element as a separate line.
     * If this factory was constructed with a template item stack, this method will append to its existing lore, if any.
     *
     * @param whatToAdd the lines to add to the lore
     * @return this factory
     */
    public ItemStackFactory lore(String... whatToAdd) {
        return appendLore(Arrays.asList(whatToAdd));
    }

    /**
     * Adds an enchantment to the product.
     *
     * @param enchantment the enchantment to apply
     * @param level       the level of the enchantment
     * @return this factory
     */
    public ItemStackFactory enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, false);
        return this;
    }

    /**
     * Adds an enchantment to the product, but without checking level and type restrictions.
     *
     * @param enchantment the enchantment to apply
     * @param level       the level of the enchantment
     * @return this factory
     */
    public ItemStackFactory enchantUnsafe(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    /**
     * @param newData the new {@link org.bukkit.material.MaterialData} for the product
     * @return this factory
     */
    public ItemStackFactory materialData(MaterialData newData) {
        materialData = newData;
        return this;
    }

    /**
     * @param newData the future legacy byte data value for the product
     * @return this factory
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public ItemStackFactory legacyData(byte newData) {
        MaterialData data = base.getData();
        data.setData(newData);
        materialData(data);
        return this;
    }

    /**
     * Sets the color of the wool product.
     *
     * @param color the new color of the product
     * @return this factory
     * @throws IllegalArgumentException if the base stack is not of material WOOL
     */
    public ItemStackFactory woolColor(DyeColor color) {
        Preconditions.checkArgument(base.getType() == Material.WOOL,
                "material of base stack must be WOOL (is: %s)", base.getType());
        materialData = new Wool(color);
        base.setDurability(materialData.toItemStack().getDurability());
        return this;
    }

    /**
     * Sets the color of the leather armor product.
     *
     * @param color the new color of the product
     * @return this factory
     * @throws IllegalArgumentException if the base stack is not of type leather armor
     */
    public ItemStackFactory leatherArmorColor(Color color) {
        Preconditions.checkArgument(meta instanceof LeatherArmorMeta,
                "Base stack must be leather armor (is: %s)", meta.getClass());
        ((LeatherArmorMeta) meta).setColor(color);
        return this;
    }

    /**
     * Sets the color of the {@link org.bukkit.material.Colorable} product.
     *
     * @param color the new color of the product
     * @return this factory
     * @throws IllegalArgumentException if the base stack is not colorable
     */
    public ItemStackFactory color(DyeColor color) {
        Preconditions.checkArgument(meta instanceof Colorable,
                "Base stack must be colorable (is: %s)", meta.getClass());
        ((Colorable) meta).setColor(color);
        return this;
    }

    /**
     * Sets the owner of the skull product.
     *
     * @param ownerName the new skull owner name
     * @return this factory
     * @throws IllegalArgumentException if the base stack is not of material SKULL_ITEM
     */
    public ItemStackFactory skullOwner(String ownerName) {
        Validate.isTrue(base.getType() == Material.SKULL_ITEM, "Material of base stack must be SKULL_ITEM (" + base.getType() + ')');
        ((SkullMeta) meta).setOwner(ownerName);
        base.setDurability((short) 3);
        return this;
    }

    /**
     * Adds given item flags to the item meta of the result.
     *
     * @param itemFlags the flags to add
     * @return this factory
     */
    public ItemStackFactory withFlags(ItemFlag... itemFlags) {
        meta.addItemFlags(itemFlags);
        return this;
    }

    /**
     * Marks the product's item meta to hide enchantment information.
     *
     * @return this factory
     */
    public ItemStackFactory hideEnchants() {
        return withFlags(ItemFlag.HIDE_ENCHANTS);
    }

    /**
     * Marks the product's item meta to hide all information normally shown by Minecraft in its
     * lore text.
     *
     * @return this factory
     */
    public ItemStackFactory hideAll() {
        return withFlags(
                ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE
        );
    }

    /**
     * Marks the product's item meta to hide enchantment information and adds a dummy enchantment
     * to make the item glow without enchantment data in the lore text.
     *
     * @return this factory
     */
    public ItemStackFactory glow() {
        enchantUnsafe(Enchantment.WATER_WORKER, 1);
        return withFlags(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemStack produce() {
        final ItemStack product = new ItemStack(base);
        product.setData(materialData);
        meta.setLore(lore);
        product.setItemMeta(meta);
        return product;
    }

    @Nonnull
    public ItemStack getBase() {
        return this.base;
    }

    public MaterialData getMaterialData() {
        return this.materialData;
    }
}
