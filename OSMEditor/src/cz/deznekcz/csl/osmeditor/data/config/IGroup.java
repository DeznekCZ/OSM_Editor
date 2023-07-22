package cz.deznekcz.csl.osmeditor.data.config;

import cz.deznekcz.csl.osmeditor.data.AOSMItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IGroup<T extends AOSMItem> {

    @SafeVarargs
    static <T extends AOSMItem> IGroup<T> of(String key, IEntry<T>... entries) {
        Map<String, IEntry<T>> map = new HashMap<>();
        for (IEntry<T> entry: entries) {
            for (String keyEntry: entry.keys()) {
                map.put(keyEntry, entry);
            }
        }
        return (node, painters) -> {
            String tagValue;
            if ((tagValue = node.getStringTag(key, null)) == null)
                return; // no painter for this group;

            IEntry<T> entry = map.getOrDefault(tagValue, null);
            if (entry != null)
                entry.apply(node, painters);
        };
    }

    @SafeVarargs
    static <T extends AOSMItem> IGroup<T> of(String key, Generator<T>... generators) {
        return (node, painters) -> {
            if (node.hasTag(key))
                return; // no painter for this group;

            Arrays.stream(generators).map(g -> g.apply(node)).forEach(painters::add);
        };
    }

    void apply(T node, List<Painter> painters);
}
