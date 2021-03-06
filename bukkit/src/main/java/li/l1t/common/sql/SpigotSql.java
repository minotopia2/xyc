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

package li.l1t.common.sql;

import com.google.common.base.Preconditions;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An extension to {@link SafeSql} providing several Spigot-specific utility methods.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 09/11/14
 */
public class SpigotSql extends SafeSql {
    private final Plugin plugin;

    /**
     * Constructs a new instance.
     *
     * @param connectable {@link SqlConnectable} providing login data
     * @param plugin      the plugin managing this sql connector
     * @throws IllegalArgumentException If any of the arguments are {@code null}.
     */
    public SpigotSql(SqlConnectable connectable, Plugin plugin) {
        super(connectable);
        this.plugin = plugin;
    }

    /**
     * Convenience shorthand for {@link #executeSimpleUpdateAsync(String, Object...)}.
     * Executes an update statement in an asynchronous thread using
     * Bukkit's {@link BukkitScheduler#runTaskAsynchronously(org.bukkit.plugin.Plugin, Runnable) Scheduler API}.
     * The plugin managing this SpigotSql is used to register the task.
     * Please note that any tasks added to the returned Future will be called in an async thread.
     * Any exceptions will be passed to the future on completion.
     *
     * @param query the query string
     * @param args  the arguments to pass to the SQL driver
     * @return a future that will be populated with the return value upon completion of the update
     * @see #safelyExecuteUpdate(String, Object...)
     */
    public CompletableFuture<Integer> asyncUpdate(String query, Object... args) {
        return executeSimpleUpdateAsync(query, args);
    }

    /**
     * Executes an update statement in an asynchronous thread using
     * Bukkit's {@link BukkitScheduler#runTaskAsynchronously(org.bukkit.plugin.Plugin, Runnable) Scheduler API}.
     * The plugin managing this SpigotSql is used to register the task.
     * Please note that any tasks added to the returned Future will be called in an async thread.
     * Any exceptions will be passed to the future on completion and additionally printed to stdout.
     *
     * @param query the query string
     * @param args  the arguments to pass to the SQL driver
     * @return a future that will be populated with the return value upon completion of the update
     * @see #safelyExecuteUpdate(String, Object...)
     */
    public CompletableFuture<Integer> executeSimpleUpdateAsync(String query, Object... args) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> {
                    try (PreparedStatement stmnt = this.prepareStatement(query)) {
                        this.fillStatement(stmnt, args);

                        future.complete(stmnt.executeUpdate());
                    } catch (SQLException e) {
                        e.printStackTrace(); //Not all impls will handle the exception
                        future.completeExceptionally(e);
                    }
                });

        return future;
    }

    /**
     * Executes an update statement in an asynchronous thread using
     * Bukkit's {@link BukkitScheduler#runTaskAsynchronously(org.bukkit.plugin.Plugin, Runnable) Scheduler API}.
     * The plugin managing this SpigotSql is used to register the task.
     * Please note that any tasks added to the returned Future (including {@code consumer}) will be called in an async thread.
     * The consumer is expected to close the passed result. If it does not close the result, memory leaks are to be expected.
     *
     * @param query    the query string
     * @param args     the arguments to pass to the SQL driver
     * @param consumer a consumer which will be processing the update's result.
     * @return a Future which can be used to retrieve the result of the update
     * @see #executeUpdateWithGenKeys(String, Object...)
     */
    public CompletableFuture<UpdateResult> executeUpdateAsyncWithGenKeys(String query, Consumer<UpdateResult> consumer, Object... args) {
        CompletableFuture<UpdateResult> future = new CompletableFuture<>();
        future.thenAccept(consumer);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> {
                    try {
                        future.complete(executeUpdateWithGenKeys(query, args));
                    } catch (SQLException e) {
                        e.printStackTrace(); //Not all impls will handle the exception
                        future.completeExceptionally(e);
                    }
                });

        return future;
    }

    /**
     * Executes a query statement in an asynchronous thread using
     * Bukkit's {@link BukkitScheduler#runTaskAsynchronously(org.bukkit.plugin.Plugin, Runnable) Scheduler API}.
     * The plugin managing this SpigotSql is used to register the task.
     * Please note that any tasks added to the returned Future (including {@code consumer}) will be called in an async thread.
     * The consumer is expected to close the passed result. If it does not close the result, memory leaks are to be expected.
     *
     * @param query    the query string
     * @param args     the arguments to pass to the SQL driver
     * @param consumer a consumer which will be processing the query's result.
     * @return a Future which can be used to retrieve the result of the query
     * @see #executeQueryWithResult(String, Object...) (String, Object...)
     */
    public CompletableFuture<QueryResult> executeQueryAsync(String query, Consumer<QueryResult> consumer, Object... args) {
        CompletableFuture<QueryResult> future = new CompletableFuture<>();
        future.thenAccept(consumer);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> {
                    try {
                        future.complete(executeQueryWithResult(query, args));
                    } catch (SQLException e) {
                        e.printStackTrace(); //Not all impls will handle the exception
                        future.completeExceptionally(e);
                    }
                });

        return future;
    }

    /**
     * Executes a set of updates for a given object type in a batch. Note that this can only operate on same objects and
     * same SQL statements. The batch is executed in an async task using the Bukkit Scheduler API.
     *
     * @param sql             the SQL update or insert statement to fill with the parameters for each batch element
     * @param data            a collection of objects representing the data to be written to the database
     * @param parameterMapper a mapper function mapping an object to the {@code sql} parameters representing it, in
     *                        declaration order.
     * @param <T>             the type of object to be written to the database
     * @return a future that is completed exceptionally when an exception occurs during execution of the batch
     */
    public <T> CompletableFuture<Void> asyncBatch(String sql, Collection<T> data, Function<T, Object[]> parameterMapper) {
        Preconditions.checkNotNull(data, "data");
        CompletableFuture<Void> future = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> {
                    try {
                        executeBatchUpdate(sql, data, parameterMapper);
                        future.complete(null);
                    } catch (SQLException e) {
                        future.completeExceptionally(e);
                    }
                }
        );

        return future;
    }
}
