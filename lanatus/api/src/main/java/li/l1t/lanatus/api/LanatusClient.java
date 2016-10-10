/*
 * Copyright (c) 2013 - 2015 xxyy (Philipp Nowak; devnull@nowak-at.net). All rights reserved.
 *
 * Any usage, including, but not limited to, compiling, running, redistributing, printing,
 *  copying and reverse-engineering is strictly prohibited without explicit written permission
 *  from the original author and may result in legal steps being taken.
 *
 * See the included LICENSE file (core/src/main/resources) or email xxyy98+xyclicense@gmail.com for details.
 */

package li.l1t.lanatus.api;

import li.l1t.lanatus.api.account.AccountRepository;
import li.l1t.lanatus.api.position.PositionRepository;
import li.l1t.lanatus.api.product.ProductRepository;
import li.l1t.lanatus.api.purchase.PurchaseRepository;

/**
 * Represents a client implementation for the Lanatus API.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-09-28
 */
public interface LanatusClient {
    String getModuleName();

    AccountRepository accounts();

    PositionRepository positions();

    PurchaseRepository purchases();

    ProductRepository products();

    //TODO: PurchaseBuilder startPurchase();

    //TODO: CreditMelonsBuilder creditMelons();
}