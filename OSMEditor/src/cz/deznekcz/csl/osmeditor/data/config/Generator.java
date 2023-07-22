package cz.deznekcz.csl.osmeditor.data.config;

import cz.deznekcz.csl.osmeditor.data.AOSMItem;

public interface Generator<T extends AOSMItem> {
    Painter apply(T node);
}
