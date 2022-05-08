package strd.jstrd.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SuppressWarnings({"squid:S1452", "unused"})  //usage of ? in generics is normal.
public class CustomCollectors {

    private CustomCollectors() {
    }

    /**
     *
     * @param <T> type of the records in the stream.
     * @return Collector collecting exactly one item from stream. If there isn't exactly one item in stream,
     * the collector will produce exception.
     */
    public static <T> Collector<T, ?, T> exactlyOneRecordCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException("Expected exactly one record in stream");
                    }
                    return list.get(0);
                }
        );
    }

    /**
     *
     * @param <T> type of the records in the stream.
     * @return Collector collecting exactly one item from stream. If there isn't exactly one item in stream,
     * the collector will produce exception.
     */
    public static <T> Collector<T, ?, Optional<T>> atMostOneRecordCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    int size = list.size();
                    if (size > 1) {
                        throw new IllegalStateException("Expected zero or one record in stream");
                    }
                    if (size == 0) {
                        return Optional.empty();
                    } else {
                        return Optional.of(list.get(0));
                    }
                }
        );
    }

    /**
     * @param <T> type of the records in the stream.
     * @return Collector collecting exactly one item from stream. If there isn't exactly one item in stream,
     * the collector will call provided consumer with number of found records and then return first match.
     */
    public static <T> Collector<T, ?, Optional<T>> lenientAtMostOneRecordCollector(IntConsumer moreThanOneRecordConsumer) {
        Objects.requireNonNull(moreThanOneRecordConsumer);
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    int size = list.size();
                    if (size > 1) {
                        moreThanOneRecordConsumer.accept(size);
                    }
                    if (size == 0) {
                        return Optional.empty();
                    } else {
                        return Optional.ofNullable(list.get(0));
                    }
                }
        );
    }

    /**
     * @param <T> type of the records in the stream.
     * @return Collector collecting exactly one item from stream. If there isn't exactly one item in stream,
     * the collector will call provided consumer with number of found records and then return first match.
     */
    public static <T> Collector<T, ?, Optional<T>> lenientAtMostOneRecordCollector(IntUnaryOperator takeIthRecord, Consumer<List<T>> duplicatesConsumer) {
        Objects.requireNonNull(takeIthRecord);
        Objects.requireNonNull(duplicatesConsumer);

        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    int size = list.size();
                    if (size > 1) {
                        duplicatesConsumer.accept(list);
                        return Optional.ofNullable(list.get(takeIthRecord.applyAsInt(size)));
                    }
                    if (size == 0) {
                        return Optional.empty();
                    } else {
                        return Optional.ofNullable(list.get(0));
                    }
                }
        );
    }

    /**
     * @param <T> type of the records in the stream.
     * @return Collector collecting exactly one item from stream. If there isn't exactly one item in stream,
     * the collector will call provided consumer with number of found records and then return first match. If
     * nothing is found, null is returned.
     */
    public static <T> Collector<T, ?, T> lenientAtMostOneRecordCollectorOrNull(IntConsumer moreThanOneRecordConsumer) {
        Objects.requireNonNull(moreThanOneRecordConsumer);
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    int size = list.size();
                    if (size > 1) {
                        moreThanOneRecordConsumer.accept(size);
                    }
                    if (size == 0) {
                        return null;
                    } else {
                        return list.get(0);
                    }
                }
        );
    }

    /**
     * @param <T> type of the records in the stream.
     * @return Collector collecting exactly one item from stream. If there isn't exactly one item in stream,
     * the collector will call provided consumer with number of found records and then return first match. If
     * nothing is found, null is returned.
     */
    public static <T> Collector<T, ?, T> lenientAtMostOneRecordCollectorOrNull(IntUnaryOperator takeIthRecord, Consumer<List<T>> duplicatesConsumer) {
        Objects.requireNonNull(takeIthRecord);
        Objects.requireNonNull(duplicatesConsumer);
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    int size = list.size();
                    if (size > 1) {
                        duplicatesConsumer.accept(list);
                        return list.get(takeIthRecord.applyAsInt(size));
                    }
                    if (size == 0) {
                        return null;
                    } else {
                        return list.get(0);
                    }
                }
        );
    }

    public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(Function<T, K> keyMapper,
                                                                                 Function<T, U> valueMapper) {
        return toLinkedHashMap(keyMapper, valueMapper, throwingMerger());
    }

    public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(Function<T, K> keyMapper,
                                                                                 Function<T, U> valueMapper,
                                                                                 BinaryOperator<U> mergeFunction) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(valueMapper);
        Objects.requireNonNull(mergeFunction);
        return Collectors.toMap(keyMapper,
                valueMapper,
                mergeFunction,
                LinkedHashMap::new);
    }

    public static IntUnaryOperator returnLastDuplicate() {
        return size -> size - 1;
    }

    public static IntUnaryOperator returnFirstDuplicate() {
        return size -> 0;
    }

    public static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }
}
