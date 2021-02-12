package com.codenjoy.clientrunner;

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
