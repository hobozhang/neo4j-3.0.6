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
package org.neo4j.kernel.guard;

import java.time.Clock;

import org.neo4j.kernel.impl.api.KernelStatement;
import org.neo4j.kernel.impl.api.KernelTransactionImplementation;
import org.neo4j.logging.Log;

/**
 * Guard that checks kernel transaction for timeout.
 * As soon as transaction timeout time reached {@link GuardTimeoutException } will be thrown.
 */
public class TimeoutGuard implements Guard
{
    private final Log log;
    private Clock clock;

    public TimeoutGuard( Clock clock, final Log log )
    {
        this.log = log;
        this.clock = clock;
    }

    @Override
    public void check( KernelStatement statement )
    {
        check( statement.getTransaction() );
    }

    private void check (KernelTransactionImplementation transaction)
    {
        check( getMaxTransactionCompletionTime( transaction ), "Transaction timeout." );
    }

    private void check( long maxCompletionTime, String timeoutDescription )
    {
        long now = clock.millis();
        if ( maxCompletionTime < now )
        {
            final long overtime = now - maxCompletionTime;
            String message = timeoutDescription + " (Overtime: " + overtime + " ms).";
            log.warn( message );
            throw new GuardTimeoutException(message, overtime );
        }
    }

    private static long getMaxTransactionCompletionTime( KernelTransactionImplementation transaction )
    {
        return transaction.startTime() + transaction.timeout();
    }
}
