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

package li.l1t.common.sql.sane.scoped;

import com.google.common.base.Preconditions;
import li.l1t.common.exception.InternalException;
import li.l1t.common.sql.sane.sanebox.SqlSanebox;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An auto-closeable session instance that keeps the count of scopes currently using it. Intended
 * for use with try...finally statements. <p><b>Important:</b> Before accessing any state of this
 * object, join() must be called. After the current unit of work (ideally a try block) is done,
 * close() must be called. If this is not adhered, the reference count will become invalid and the
 * session might not get closed properly. Note that close() is automatically called by the JVM on
 * resources managed by try-with-resources.</p>
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-10-18
 */
public class JdbcScopedSession implements RawScopedSession {
    private final AtomicInteger refCount = new AtomicInteger(0);
    private final AtomicBoolean transactionOpen = new AtomicBoolean(false);
    private final Connection connection;
    private boolean previousAutoCommit = true;
    private boolean closed = false;

    public JdbcScopedSession(Connection connection) {
        this.connection = Preconditions.checkNotNull(connection, "connection");
    }

    @Override
    public JdbcScopedSession join() {
        checkNotClosed();
        refCount.incrementAndGet();
        return this;
    }

    private void checkNotClosed() {
        if (closed) {
            throw new InternalException("scoped session already closed");
        }
    }

    /**
     * @return the raw connection used by this session
     */
    public Connection connection() {
        return connection;
    }

    @Override
    public JdbcScopedSession tx() {
        join();
        if (transactionOpen.compareAndSet(false, true)) { //note: minor race condition here
            SqlSanebox.run(() -> {
                previousAutoCommit = connection().getAutoCommit();
                if (previousAutoCommit) {
                    connection().setAutoCommit(false);
                }
            });
        }
        return this;
    }

    @Override
    public void commit() {
        checkActive();
        SqlSanebox.run(() -> connection().commit());
        endTransactionAndResetAutoCommit();
    }

    private void checkActive() {
        checkNotClosed();
        if (!transactionOpen.get()) {
            throw new InternalException("transaction already ended or not yet started");
        }
    }

    private void endTransactionAndResetAutoCommit() {
        transactionOpen.set(false);
        if (previousAutoCommit) {
            SqlSanebox.run(() -> connection().setAutoCommit(true));
        }
    }

    @Override
    public void rollbackAndClose() {
        checkActive();
        SqlSanebox.run(() -> connection().rollback());
        endTransactionAndResetAutoCommit();
        forceClose();
    }

    @Override
    public void commitIfLast() {
        if (thisIsTheLastReference()) {
            commit();
        }
    }

    private boolean thisIsTheLastReference() {
        return refCount.get() <= 1;
    }

    @Override
    public void commitIfLastAndChanged() {
        if (hasTransaction()) {
            commitIfLast();
        }
    }


    @Override
    public void close() throws InternalException {
        if (refCount.decrementAndGet() <= 0) {
            forceClose();
        }
    }

    private void forceClose() {
        if (transactionOpen.get()) {
            rollbackAndClose();
            throw new InternalException("Transaction was not completed cleanly in last ref count! Rolled back forcefully.");
        }
        closeInternal();
    }

    private void closeInternal() {
        refCount.set(0);
        transactionOpen.set(false);
        closed = true;
    }

    @Override
    public boolean hasReferences() {
        return refCount.get() != 0;
    }

    @Override
    public boolean acceptsFurtherReferences() {
        return !closed;
    }

    @Override
    public boolean hasTransaction() {
        return transactionOpen.get();
    }
}
