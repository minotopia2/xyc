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

import li.l1t.common.inventory.gui.element.Placeholder;
import li.l1t.common.test.util.MockHelper;
import li.l1t.common.test.util.mokkit.MockServer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests some parts of SimpleInventoryMenu.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-08-14
 */
public class SimpleInventoryMenuTest {
    @Test
    public void redraw__singleItem() throws Exception {
        //given
        MockServer server = MockHelper.mockServer();
        Player player = MockHelper.mockPlayer(UUID.randomUUID(), "Hans");
        SimpleInventoryMenu menu = new SimpleInventoryMenu(MockHelper.mockPlugin(server), "lel", player);
        ItemStack stack = new ItemStack(Material.MELON);
        menu.addElement(5, new Placeholder(stack));
        //when
        menu.redraw();
        //then
        assertThat("item must be drawn correctly", menu.getInventory().getItem(5), is(stack));
    }

}
