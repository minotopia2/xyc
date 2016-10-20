/*
 * Copyright (c) 2013 - 2016 xxyy (Philipp Nowak; xyc@l1t.li). All rights reserved.
 *
 * Any usage, including, but not limited to, compiling, running, redistributing, printing,
 *  copying and reverse-engineering is strictly prohibited without explicit written permission
 *  from the original author and may result in legal steps being taken.
 *
 * See the included LICENSE file (core/src/main/resources) for details.
 */

package li.l1t.lanatus.sql.account;

import li.l1t.lanatus.api.account.AccountSnapshot;
import li.l1t.lanatus.api.account.MutableAccount;
import li.l1t.lanatus.api.exception.AccountConflictException;
import li.l1t.lanatus.sql.AbstractLanatusSqlTest;
import li.l1t.lanatus.sql.SqlLanatusClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

/**
 * Tests the integration of the sql account repository with the database, verifying only using the
 * repository's own methods.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-10-20
 */
public class SqlAccountRepositoryTest extends AbstractLanatusSqlTest {
    private static final UUID PLAYER_ID = UUID.randomUUID();
    private static final String EXPECTED_RANK = "testrank";

    @BeforeClass
    public static void setupSampleData() throws AccountConflictException {
        SqlLanatusClient client = createClient();
        MutableAccount account = client.accounts().findMutable(PLAYER_ID);
        account.setLastRank(EXPECTED_RANK);
        account.setMelonsCount(420);
        client.accounts().save(account);
    }

    @Test
    public void testFind__basic() {
        //given PLAYER_ID
        //when
        AccountSnapshot firstSnapshot = repo().find(PLAYER_ID);
        //then
        assertNotNull("firstSnapshot", firstSnapshot);
        assertThat("rank does not match expected", firstSnapshot.getLastRank(), is(EXPECTED_RANK));
    }

    @Test
    public void testFind__caching() {
        //given PLAYER_ID
        //when
        AccountSnapshot first = repo().find(PLAYER_ID);
        AccountSnapshot second = repo().find(PLAYER_ID);
        //then
        assertSame("cache does not return the same object every time", first, second);
    }

    @Test
    public void testRefresh__notSame() {
        //given
        AccountSnapshot initial = repo().find(PLAYER_ID);
        //when
        AccountSnapshot refreshed = repo().refresh(initial);
        //then
        assertNotSame("refresh does not return new object", initial, refreshed);
        assertNotSame("refresh does not purge cache", initial, repo().find(PLAYER_ID));
        assertSame("refresh does not cache new object", refreshed, repo().find(PLAYER_ID));
    }

    private SqlAccountRepository repo() {
        return client().accounts();
    }
}