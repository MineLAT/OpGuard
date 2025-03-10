/*
 * OpGuard - Password protected op.
 * Copyright © 2016-2022 OpGuard Contributors (https://github.com/GuardedOperators/OpGuard)
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.guardedoperators.opguard.util;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

public class Cooldown {
    private static final Cooldown COOLDOWN_30_MINUTES = new Cooldown(Duration.ofMinutes(30));

    public static Cooldown of30Minutes() {
        return COOLDOWN_30_MINUTES;
    }

    private final Duration duration;

    public Cooldown(Duration duration) {
        this.duration = duration;
    }

    public Duration duration() {
        return duration;
    }

    public boolean since(@Nullable Instant past) {
        if (past == null) {
            return true;
        }
        return Duration.between(Instant.now(), past).compareTo(duration) >= 0;
    }
}
