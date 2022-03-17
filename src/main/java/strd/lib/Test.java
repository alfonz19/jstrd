package strd.lib;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
    private static final Logger log = LoggerFactory.getLogger(Test.class);
    public static final int buttons = 15;
    private AtomicInteger value = new AtomicInteger();

    public static void main(String[] args) {
//        new Test().test();
//        threadTest();
        new Test().testThreadPerButton();
    }

    private void testThreadPerButton() {
        int buttons = 15;
        List<Integer> ints = IntStream.rangeClosed(1, buttons).boxed().collect(Collectors.toList());
        List<Integer> duplicatedInts = IntStream.range(0, 3).boxed().flatMap(e -> ints.stream()).collect(Collectors.toList());
        Collections.shuffle(duplicatedInts);


//        Flux<Integer> numbers = Flux.fromIterable(duplicatedInts);
//        Flux<GroupedFlux<Integer, Integer>> groupedFluxFlux = numbers.groupBy(Function.identity());
//        Flux<List<Integer>> listFlux = groupedFluxFlux.flatMap(Flux::collectList);
//        listFlux.flatMap(Flux::fromIterable).subscribe(this::print);
        Scheduler my1 = Schedulers.newParallel("my", buttons);
//        Schedulers.boundedElastic()

        Flux<Integer> numbers = Flux.fromIterable(duplicatedInts);
        Flux<GroupedFlux<Integer, Integer>> groupedFluxFlux = numbers.groupBy(Function.identity());
        Flux<Flux<Integer>> my = groupedFluxFlux.map(g -> g.publishOn(my1));
        Disposable subscribe = my.subscribe(stream -> stream.subscribe(this::print));

        System.out.println("waiting!");
        sleep(10000);
        System.out.println("I'm done!");
        subscribe.dispose();
        my1.dispose();
        System.out.println("disposing");

    }

    private void print(Integer integer) {
        System.out.println(String.format("value: %s from thread %s", integer, Thread.currentThread().getName()));
        sleep(1000);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private static void threadTest() {
        Flux<Integer> range = Flux.range(1, 10);
        range.subscribe(x -> System.out.println("first: "+x+", thread: "+Thread.currentThread().getName()));
        new Thread(() -> range.subscribe(x -> System.out.println("second: "+x+", thread: "+Thread.currentThread().getName()))).start();
        sleep(10000);
    }

    public Consumer<Integer> consumer;

    private void test() {



        Flux<Integer> hotFlux = createNumberSequence();
        hotFlux.subscribe(this::printValue);


        System.out.println("flux created.");
        sleep(2000);
        consumer.accept(value.incrementAndGet());
        consumer.accept(value.incrementAndGet());
        consumer.accept(value.incrementAndGet());

        incrementInNewThread();
        incrementInNewThread();

        System.out.println("done");

        sleep(20000);
        System.out.println("nothing should be printed again");
        hotFlux.subscribe(this::printValue);
    }

    private void printValue(Integer e) {
        System.out.println("next: "+ e+"\t\t"+Thread.currentThread().getName());
    }

    private void incrementInNewThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                consumer.accept(value.incrementAndGet());
            }
        }).start();
    }

    public Flux<Integer> createNumberSequence() {
        return Flux.<Integer>create(sink -> Test.this.consumer = sink::next).publishOn(Schedulers.newSingle("test"));
    }

}
