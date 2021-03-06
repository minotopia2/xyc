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

package li.l1t.common.inventory.gui;

import com.google.common.base.Preconditions;
import li.l1t.common.inventory.gui.element.MenuElement;
import li.l1t.common.inventory.gui.exception.IllegalPositionException;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A menu with a top row for control buttons and the rest of the canvas filled with arbitrary items.
 * Items need not implement {@link MenuElement} and the super implementations does not know about
 * them. This implementation does not permit null elements.
 *
 * <p>The items are divided into pages of {@link #CANVAS_SIZE}, with the first page index being
 * zero. Provides methods to change to specific pages.</p>
 *
 * <p>Behaves like {@link TopRowMenu} otherwise.</p>
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-08-16
 */
public abstract class PagingListMenu<V> extends TopRowMenu implements PageableListMenu<V> {
    public static int CANVAS_SIZE = INVENTORY_SIZE - ROW_SIZE;
    private final List<V> items = new ArrayList<>();
    private int currentItemStart;

    public PagingListMenu(Plugin plugin, Player player) {
        super(plugin, player);
    }

    /**
     * Handles a click on an item.
     *
     * @param item the item that was clicked
     * @param evt  the event that caused the click
     */
    protected abstract void handleValueClick(V item, InventoryClickEvent evt);

    /**
     * Creates an item stack to represent given item in the canvas.
     *
     * @param toDraw the item to draw
     * @return the item stack to represent given item
     */
    protected abstract ItemStack drawItem(V toDraw);

    /**
     * Returns the formatted inventory title for this menu in its current state.
     *
     * @param currentPage the current page
     * @param pageCount   the amount of pages in total
     * @return the base title for this menu, excluding the page count
     */
    protected abstract String formatTitle(int currentPage, int pageCount);

    @Override
    public void selectPageNum(int pageNum) {
        Preconditions.checkArgument(pageNum > 0, "pageNum %s must be greater than zero", pageNum);
        Preconditions.checkArgument(pageNum <= getPageCount(), "pageNum %s must be less than or equal to page count %s", pageNum, getPageCount());
        this.currentItemStart = getItemStartOfPageNum(pageNum);
    }

    private int getItemStartOfPageNum(int pageNum) {
        int pageIndex = pageNum - 1;
        return pageIndex * CANVAS_SIZE;
    }

    @Override
    public String getInventoryTitle() {
        return formatTitle(getCurrentPageNum(), getPageCount());
    }

    @Override
    public int getPageCount() {
        return getPageNumOf(items.size());
    }

    @Override
    public int getCurrentPageNum() {
        return getPageNumOf(currentItemStart);
    }

    private int getPageNumOf(int itemStart) {
        return getPageIndexOf(itemStart) + 1;
    }

    private int getPageIndexOf(int itemStart) {
        return Math.floorDiv(itemStart, CANVAS_SIZE);
    }

    @Override
    public boolean handleClick(InventoryClickEvent evt) {
        if (evt.getSlot() < ROW_SIZE) {
            return super.handleClick(evt);
        }
        return handleCanvasClick(evt);
    }

    private boolean handleCanvasClick(InventoryClickEvent evt) {
        V slotValue = getItemForCanvasSlot(toCanvasId(evt.getSlot()));
        if (slotValue != null) {
            handleValueClick(slotValue, evt);
        }
        return true;
    }

    private int toCanvasId(int slotId) {
        Preconditions.checkArgument(!isTopBarSlotId(slotId), "must be canvas slot: %s", slotId);
        return slotId - ROW_SIZE;
    }

    @Override
    public void redraw() {
        resetCanvas();
        super.redraw();
        drawCanvas();
    }

    private void resetCanvas() {
        fillCanvasWithPlaceholders();
    }

    private void fillCanvasWithPlaceholders() {
        for (int slotId = ROW_SIZE; slotId < INVENTORY_SIZE; slotId++) {
            super.addPlaceholder(slotId);
        }
    }

    private void drawCanvas() {
        int numberOfItemsToDisplay = items.size() - currentItemStart;
        int numberOfSlotsToFill = Math.min(numberOfItemsToDisplay, CANVAS_SIZE);
        for (int canvasId = 0; canvasId < numberOfSlotsToFill; canvasId++) {
            drawCanvasItem(canvasId);
        }
    }

    private void drawCanvasItem(int canvasId) {
        V item = getItemForCanvasSlot(canvasId);
        ItemStack stack = drawItem(item);
        setCanvasStack(canvasId, stack);
    }

    private V getItemForCanvasSlot(int canvasId) {
        int index = currentItemStart + canvasId;
        if (index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    private void setCanvasStack(int canvasId, ItemStack stack) {
        getInventory().setItem(canvasId + ROW_SIZE, stack);
    }

    @Override
    public List<V> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void setItems(Collection<V> newItems) {
        clearItems();
        addItemsInternal(newItems);
    }

    @Override
    public void clearItems() {
        items.clear();
    }

    @Override
    public void addItems(Collection<V> newItems) {
        addItemsInternal(newItems);
    }

    private void addItemsInternal(Collection<V> newItems) {
        Preconditions.checkArgument(newItems.stream().allMatch(Objects::nonNull), "PagingListMenu does not permit null elements!");
        items.addAll(newItems);
    }

    @Override
    public void addElement(int slotId, MenuElement element) {
        if (!isTopBarSlotId(slotId)) {
            throw new IllegalPositionException(String.format(
                    "cannot draw into canvas manually (top bar only!) - at %s: %s",
                    slotId, element));
        }
        super.addElement(slotId, element);
    }
}
