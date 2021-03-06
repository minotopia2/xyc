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

package li.l1t.lanatus.sql.product;

import li.l1t.lanatus.api.product.Product;
import li.l1t.lanatus.api.product.ProductQueryBuilder;
import li.l1t.lanatus.sql.AbstractLanatusSqlTest;
import org.junit.Test;

import java.util.Collection;
import java.util.UUID;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.Assert.assertThat;

/**
 * Tests the integration of the sql product query builder with the database, verifying only using
 * the repository's methods.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-10-21
 */
public class SqlProductQueryBuilderTest extends AbstractLanatusSqlTest {

    @Test
    public void testQuery__any() {
        //given
        UUID productId = givenSomeProduct();
        ProductQueryBuilder builder = client().products().query().inAnyModule();
        //when
        Collection<Product> results = builder.execute();
        //then
        thenTheResultHasAnItemWithId(productId, results);
    }

    private UUID givenSomeProduct() {
        UUID productId = UUID.randomUUID();
        client().products().registration(productId).register();
        return productId;
    }

    @Test
    public void testQuery__activeOnlyNaive() {
        //given
        UUID productId = givenSomeProduct();
        ProductQueryBuilder builder = client().products().query().inAnyModule().andActive();
        //when
        Collection<Product> results = builder.execute();
        //then
        thenTheResultHasAnItemWithId(productId, results);
    }

    private void thenTheResultHasAnItemWithId(UUID productId, Collection<Product> results) {
        assertThat(results, hasItem(hasProperty("uniqueId", is(productId))));
    }

    @Test
    public void testQuery__containing__displayName() {
        //given
        String searchTerm = "fox";
        UUID productId = givenAProductWithDisplayNameContaining(searchTerm);
        ProductQueryBuilder query = givenAContainsQuery(searchTerm);
        //when
        Collection<Product> results = query.execute();
        //then
        thenTheResultHasAnItemWithId(productId, results);
        thenAllResultsContain(searchTerm, results);
    }

    private UUID givenAProductWithDisplayNameContaining(String query) {
        return givenAProductWith("any", "the quick brown " + query, "anything");
    }

    private UUID givenAProductWith(String module, String displayName, String description) {
        UUID productId = UUID.randomUUID();
        client().products().registration(productId)
                .inModule(module)
                .withDisplayName(displayName)
                .withDescription(description)
                .register();
        return productId;
    }

    private ProductQueryBuilder givenAContainsQuery(String query) {
        return client().products().query().inAnyModule()
                .containing(query);
    }

    private void thenAllResultsContain(String searchTerm, Collection<Product> results) {
        assertThat(results, everyItem(anyOf(
                hasProperty("module", containsString(searchTerm)),
                hasProperty("displayName", containsString(searchTerm)),
                hasProperty("description", containsString(searchTerm))
        )));
    }

    @Test
    public void testQuery__containing__module() {
        //given
        String searchTerm = "foxdule";
        UUID productId = givenAProductWithModuleNameContaining(searchTerm);
        ProductQueryBuilder query = givenAContainsQuery(searchTerm);
        //when
        Collection<Product> results = query.execute();
        //then
        thenTheResultHasAnItemWithId(productId, results);
        thenAllResultsContain(searchTerm, results);
    }

    private UUID givenAProductWithModuleNameContaining(String query) {
        return givenAProductWith(query, "basically whatever you'd like", "anything");
    }

    @Test
    public void testQuery__containing__description() {
        //given
        String searchTerm = "descriptive";
        UUID productId = givenAProductWithDescriptionContaining(searchTerm);
        ProductQueryBuilder query = givenAContainsQuery(searchTerm);
        //when
        Collection<Product> results = query.execute();
        //then
        thenTheResultHasAnItemWithId(productId, results);
        thenAllResultsContain(searchTerm, results);
    }

    private UUID givenAProductWithDescriptionContaining(String query) {
        return givenAProductWith("any", "basically whatever you'd like", "a very " + query + " description");
    }

    @Test
    public void testQuery__containing__description_multiWord() {
        //given
        String searchTerm = "weird but good";
        UUID productId = givenAProductWithDescriptionContaining(searchTerm);
        ProductQueryBuilder query = givenAContainsQuery(searchTerm);
        //when
        Collection<Product> results = query.execute();
        //then
        thenTheResultHasAnItemWithId(productId, results);
        thenAllResultsContain(searchTerm, results);
    }
}
