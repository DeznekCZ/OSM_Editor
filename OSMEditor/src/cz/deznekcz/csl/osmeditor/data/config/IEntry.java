package cz.deznekcz.csl.osmeditor.data.config;

import cz.deznekcz.csl.osmeditor.data.AOSMItem;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public interface IEntry<T extends AOSMItem> {

    record SingleKeyEntry<T extends AOSMItem>(String key, List<Generator<T>> generators) implements IEntry<T> {

        @Override
        public List<String> keys() {
            return List.of(key);
        }

        @Override
        public void apply(T node, List<Painter> painters) {
            painters.addAll(generators
                    .stream()
                    .map(g -> g.apply(node))
                    .filter(Objects::nonNull)
                    .toList());
        }
    }

    @SafeVarargs
    static <T extends AOSMItem> IEntry<T> of(String key, Generator<T>... generators) {
        return new SingleKeyEntry<>(key, List.of(generators));
    }

    record MultiKeyEntry<T extends AOSMItem>(List<String> keys, List<Generator<T>> generators) implements IEntry<T> {

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
    }

    @SafeVarargs
    public static <T extends AOSMItem> IEntry<T> of(List<String> keys, Generator<T>... generators) {
        return new MultiKeyEntry<>(keys, List.of(generators));
    }

    static <T extends AOSMItem> IEntry<T> of(final List<String> keys, final List<Generator<T>> generators) {
        return new MultiKeyEntry<>(keys, generators);
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

    default IEntry<T> when(final Predicate<T> predicate, final Generator<T>... generators) {
        final IEntry<T> parent = this;
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