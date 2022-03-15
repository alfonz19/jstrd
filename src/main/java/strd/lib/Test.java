package strd.lib;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
    private static final Logger log = LoggerFactory.getLogger(Test.class);
    private AtomicInteger value = new AtomicInteger();

    public static void main(String[] args) {
//        new Test().test();
        Flux<Integer> range = Flux.range(1, 10);
        range.subscribe(x -> System.out.println("first: "+x+", thread: "+Thread.currentThread().getName()));
        new Thread(() -> range.subscribe(x -> System.out.println("second: "+x+", thread: "+Thread.currentThread().getName()))).start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public Flux<Integer> createNumberSequence() {
        return Flux.<Integer>create(sink -> {
            Test.this.consumer = new Consumer<Integer>() {
                @Override
                public void accept(Integer t) {
                    sink.next(t);
                }
            };
        }).publishOn(Schedulers.newSingle("test"));
    }

}
