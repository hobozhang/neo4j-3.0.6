/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.api;

import org.neo4j.kernel.api.KernelTransactionHandle;
import org.neo4j.kernel.api.exceptions.Status;

/**
 * A {@link KernelTransactionHandle} that wraps the given {@link KernelTransactionImplementation}.
 * This handle knows that {@link KernelTransactionImplementation}s can be reused and represents a single logical
 * transaction. This means that methods like {@link #markForTermination(Status)} can only terminate running
 * transaction this handle was created for.
 */
class KernelTransactionImplementationHandle implements KernelTransactionHandle
{
    private final long txReuseCount;
    private final long lastTransactionIdWhenStarted;
    private final long lastTransactionTimestampWhenStarted;
    private final long startTime;
    private final KernelTransactionImplementation tx;

    KernelTransactionImplementationHandle( KernelTransactionImplementation tx )
    {
        this.txReuseCount = tx.getReuseCount();
        this.lastTransactionIdWhenStarted = tx.lastTransactionIdWhenStarted();
        this.lastTransactionTimestampWhenStarted = tx.lastTransactionTimestampWhenStarted();
        this.startTime = tx.startTime();
        this.tx = tx;
    }

    @Override
    public long lastTransactionIdWhenStarted()
    {
        return lastTransactionIdWhenStarted;
    }

    @Override
    public long lastTransactionTimestampWhenStarted()
    {
        return lastTransactionTimestampWhenStarted;
    }

    @Override
    public long startTime()
    {
        return startTime;
    }

    @Override
    public boolean isOpen()
    {
        return tx.isOpen() && txReuseCount == tx.getReuseCount();
    }

    @Override
    public void markForTermination( Status reason )
    {
        tx.markForTermination( txReuseCount, reason );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        KernelTransactionImplementationHandle that = (KernelTransactionImplementationHandle) o;
        return txReuseCount == that.txReuseCount && tx.equals( that.tx );
    }

    @Override
    public int hashCode()
    {
        return 31 * (int) (txReuseCount ^ (txReuseCount >>> 32)) + tx.hashCode();
    }

    @Override
    public String toString()
    {
        return "KernelTransactionImplementationHandle{txReuseCount=" + txReuseCount + ", tx=" + tx + "}";
    }
}
