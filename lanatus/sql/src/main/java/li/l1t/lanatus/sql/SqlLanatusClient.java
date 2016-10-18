/*
 * Copyright (c) 2013 - 2015 xxyy (Philipp Nowak; devnull@nowak-at.net). All rights reserved.
 *
 * Any usage, including, but not limited to, compiling, running, redistributing, printing,
 *  copying and reverse-engineering is strictly prohibited without explicit written permission
 *  from the original author and may result in legal steps being taken.
 *
 * See the included LICENSE file (core/src/main/resources) or email xxyy98+xyclicense@gmail.com for details.
 */

package li.l1t.lanatus.sql;

import li.l1t.common.sql.sane.AbstractSqlConnected;
import li.l1t.common.sql.sane.SaneSql;
import li.l1t.lanatus.api.LanatusClient;
import li.l1t.lanatus.api.builder.melons.CreditMelonsBuilder;
import li.l1t.lanatus.sql.account.SqlAccountRepository;
import li.l1t.lanatus.sql.builder.melons.SqlCreditMelonsBuilder;
import li.l1t.lanatus.sql.position.SqlPositionRepository;
import li.l1t.lanatus.sql.product.SqlProductRepository;
import li.l1t.lanatus.sql.purchase.SqlPurchaseBuilder;
import li.l1t.lanatus.sql.purchase.SqlPurchaseRepository;

import java.util.UUID;

/**
 * An implementation of a Lanatus client using a SQL database as backend.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-09-28
 */
public class SqlLanatusClient extends AbstractSqlConnected implements LanatusClient {
    private final String module;
    private SqlAccountRepository accountRepository = new SqlAccountRepository(this);
    private SqlProductRepository productRepository = new SqlProductRepository(this);
    private SqlPurchaseRepository purchaseRepository = new SqlPurchaseRepository(this);
    private SqlPositionRepository positionRepository = new SqlPositionRepository(this);

    /**
     * Constructs a new SQL Lanatus client.
     *
     * @param sql    the database connection to use
     * @param module the name of the module using this client
     */
    public SqlLanatusClient(SaneSql sql, String module) {
        super(sql);
        this.module = module;
    }

    @Override
    public String getModuleName() {
        return module;
    }

    @Override
    public SqlAccountRepository accounts() {
        return accountRepository;
    }

    @Override
    public SqlPositionRepository positions() {
        return positionRepository;
    }

    @Override
    public SqlPurchaseRepository purchases() {
        return purchaseRepository;
    }

    @Override
    public SqlProductRepository products() {
        return productRepository;
    }

    @Override
    public SqlPurchaseBuilder startPurchase(UUID playerId) {
        return new SqlPurchaseBuilder(playerId, this);
    }

    @Override
    public CreditMelonsBuilder creditMelons(UUID playerId) {
        return new SqlCreditMelonsBuilder(playerId, this);
    }
}
