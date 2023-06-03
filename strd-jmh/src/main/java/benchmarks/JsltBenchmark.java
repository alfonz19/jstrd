package benchmarks;

import java.util.Map;
import java.util.concurrent.TimeUnit;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class JsltBenchmark {

    @State(Scope.Benchmark)
    public static class StateClass {
//        DataTransformation numberToObjectTransformation = new JsltTransformationFactory().create(Map.of(
//                "literalTemplate", "{\"number\": .}",
//                "rawOutput", "false"
//        ));

        public String getNumber() {
            return ""+10;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void jsltBenchmark(Blackhole blackhole, StateClass state) {

    }
}
