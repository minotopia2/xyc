/*
 * Copyright (c) 2013 - 2015 xxyy (Philipp Nowak; devnull@nowak-at.net). All rights reserved.
 *
 * Any usage, including, but not limited to, compiling, running, redistributing, printing,
 *  copying and reverse-engineering is strictly prohibited without explicit written permission
 *  from the original author and may result in legal steps being taken.
 *
 * See the included LICENSE file (core/src/main/resources) or email xxyy98+xyclicense@gmail.com for details.
 */

package li.l1t.common.inventory.gui.element;

import li.l1t.common.inventory.gui.InventoryMenu;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Abstract base class for menu items. Offers a no-op click handler.
 *
 * @param <M> the kind of menu this element can be used in
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-06-24
 */
public abstract class NoopMenuItem<M extends InventoryMenu> implements MenuElement<M> {
    @Override
    public void handleMenuClick(InventoryClickEvent evt, M menu) {
        //no-op
    }
}
