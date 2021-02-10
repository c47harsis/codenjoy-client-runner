package com.codenjoy.clientrunner.component;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope("prototype")
public class IdGenerator {
    private final AtomicInteger counter = new AtomicInteger(0);

    public int next() {
        return counter.incrementAndGet();
    }
}
