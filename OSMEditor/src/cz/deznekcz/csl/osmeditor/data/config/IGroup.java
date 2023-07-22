package cz.deznekcz.csl.osmeditor.data.config;

import cz.deznekcz.csl.osmeditor.data.AOSMItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IGroup<T extends AOSMItem> {

    record EntryGroup<T extends AOSMItem>(String key, Map<String, IEntry<T>> map) implements IGroup<T> {
        @Override
        public void apply(T node, List<Painter> painters) {
            String tagValue;
            if ((tagValue = node.getStringTag(key, null)) == null)
                return; // no painter for this group;

            IEntry<T> entry = map.getOrDefault(tagValue, null);
            if (entry != null)
                entry.apply(node, painters);
        }
    }

    @SafeVarargs
    static <T extends AOSMItem> IGroup<T> of(final String key, final IEntry<T>... entries) {
        final Map<String, IEntry<T>> map = new HashMap<>();
        for (IEntry<T> entry: entries) {
            for (String keyEntry: entry.keys()) {
                map.put(keyEntry, entry);
            }
        }
        return new EntryGroup<>(key, map);
    }

    record GeneratorGroup<T extends AOSMItem>(String key, Generator<T>[] generators) implements IGroup<T> {
        @Override
        public void apply(T node, List<Painter> painters) {
            if (!node.hasTag(key))
                return; // no painter for this group;

            Arrays.stream(generators).map(g -> g.apply(node)).forEach(painters::add);
        }
    }

    @SafeVarargs
    static <T extends AOSMItem> IGroup<T> of(final String key, final Generator<T>... generators) {
        return new GeneratorGroup<>(key, generators);
    }

    void apply(T node, List<Painter> painters);
}
