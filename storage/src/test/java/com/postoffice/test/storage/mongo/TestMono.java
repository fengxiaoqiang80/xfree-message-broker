package com.postoffice.test.storage.mongo;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TestMono {

    @Test
    public void testMonoOnNext() {

        Mono.just(new AtomicInteger(1))
                .doOnNext(c -> c.getAndAdd(2))
                .map(c -> new AtomicLong(c.get()))
                .doOnNext(c -> c.addAndGet(1L))
                .doOnNext(c -> c.addAndGet(1))
                .subscribe(System.out::println);


    }

    @Test
    public void testFluxZipToMono() {

        AtomicInteger atomicInteger = new AtomicInteger(0);
        Flux.range(1, 10)
                .zipWith(Mono.just(atomicInteger)
                        .map(c -> c.getAndAdd(1)).repeat())
                .map(tuple2 -> "t1:" + tuple2.getT1() + "t2:" + tuple2.getT2())
                .doOnEach(System.out::println)
                .blockLast(Duration.ofSeconds(5));

    }
}
