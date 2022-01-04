package com.codenjoy.clientrunner;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2012 - 2022 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.testng.Assert.*;

public class ExceptionAssert {

    public static void expectThrows(Class clazz, Runnable runnable) {
        expectThrows(clazz, runnable);
    }

    public static void expectThrows(Class clazz, String message, Runnable runnable) {
        try {
            runnable.run();
            fail("Exception expected");
        } catch (Throwable throwable) {
            if (throwable instanceof AssertionError) {
                throw throwable;
            }
            assertEquals(throwable.getClass(), clazz);

            if (message != null) {
                try {
                    assertEquals(throwable.getMessage(), message);
                } catch (AssertionError error) {
                    assertTrue(throwable.getMessage().contains(message),
                            String.format("Expected message contains '%s' but was '%s'",
                                    message, throwable.getMessage()));
                }
            }
        }
    }
}
