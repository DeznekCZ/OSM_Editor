package cz.deznekcz.csl.osmeditor.data.config;

import cz.deznekcz.csl.osmeditor.data.AOSMItem;

import java.util.ArrayList;
import java.util.List;

public interface IFilter<T extends AOSMItem> {

    List<Painter> apply(T node);

    record Filter<T extends AOSMItem>(IGroup<T>[] groups) implements IFilter<T> {
        @Override
        public List<Painter> apply(T node) {
            List<Painter> painters = new ArrayList<>();

            for (var entry : groups) {
                entry.apply(node, painters);
            }

            return painters;
        }
    }

    @SafeVarargs
    static <T extends AOSMItem> IFilter<T> of(final IGroup<T>... groups) {
        return new Filter(groups);
    }
}
