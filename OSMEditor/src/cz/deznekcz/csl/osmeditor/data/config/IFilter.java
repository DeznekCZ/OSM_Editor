package cz.deznekcz.csl.osmeditor.data.config;

import cz.deznekcz.csl.osmeditor.data.AOSMItem;

import java.util.ArrayList;
import java.util.List;

public interface IFilter<T extends AOSMItem> {

    List<Painter> apply(T node);

    @SafeVarargs
    static <T extends AOSMItem> IFilter<T> of(IGroup<T>... groups) {
        return (node) -> {
            List<Painter> generators = new ArrayList<>();

            for (var entry : groups) {
                entry.apply(node, generators);
            }

            return generators;
        };
    }
}
