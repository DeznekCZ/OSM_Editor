package cz.deznekcz.csl.osmeditor.data.config;

import cz.deznekcz.csl.osmeditor.data.AOSMItem;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public interface IEntry<T extends AOSMItem> {

    @SafeVarargs
    static <T extends AOSMItem> IEntry<T> of(String key, Generator<T>... generators) {
        return IEntry.of(List.of(key), List.of(generators));
    }

    @SafeVarargs
    public static <T extends AOSMItem> IEntry<T> of(List<String> keys, Generator<T>... generators) {
        return IEntry.of(keys, List.of(generators));
    }

    static <T extends AOSMItem> IEntry<T> of(List<String> keys, List<Generator<T>> generators) {
        return new IEntry<T>() {
            @Override
            public List<String> keys() {
                return keys;
            }

            @Override
            public void apply(T node, List<Painter> painters) {
                painters.addAll(generators
                        .stream()
                        .map(g -> g.apply(node))
                        .filter(Objects::nonNull)
                        .toList());
            }
        };
    }

    static <T extends AOSMItem, V> Predicate<T> isTag(String tag, Function<String, V> converter, V value) {
        return (node) -> Objects.equals(node.getTag(tag, converter, value), value);
    }

    static <T extends AOSMItem> Predicate<T> isStringTag(String tag, String value) {
        return isTag(tag, Function.identity(), value);
    }

    static <T extends AOSMItem> Predicate<T> isIntegerTag(String tag, int value) {
        return isTag(tag, Integer::parseInt, value);
    }

    static <T extends AOSMItem> Predicate<T> isDoubleTag(String tag, double value) {
        return isTag(tag, Double::parseDouble, value);
    }

    List<String> keys();

    void apply(T node, List<Painter> painters);

    default IEntry<T> when(Predicate<T> predicate, Generator<T>... generators) {
        IEntry<T> parent = this;
        return new IEntry<>() {

            @Override
            public List<String> keys() {
                return parent.keys();
            }

            @Override
            public void apply(T node, List<Painter> painters) {
                if (predicate.test(node)) {
                    painters.addAll(Arrays
                            .stream(generators)
                            .map(g -> g.apply(node))
                            .filter(Objects::nonNull)
                            .toList());
                } else {
                    parent.apply(node, painters);
                }
            }
        };
    }
}