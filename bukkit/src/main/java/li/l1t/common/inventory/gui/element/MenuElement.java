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

package li.l1t.common.inventory.gui.element;

import li.l1t.common.inventory.gui.InventoryMenu;
import li.l1t.common.inventory.gui.holder.ElementHolder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a clickable element in an inventory menu, shown to the user as an item stack.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-04-17
 */
public interface MenuElement {
    /**
     * @param menu the menu requesting to display this element
     * @return the item stack representing this element, or null if this element is hidden
     */
    ItemStack draw(ElementHolder menu);

    /**
     * Handles a click on this element.
     *
     * @param evt  the Bukkit event that caused the click
     * @param menu the menu associated with the click
     */
    void handleMenuClick(InventoryClickEvent evt, InventoryMenu menu);
}
