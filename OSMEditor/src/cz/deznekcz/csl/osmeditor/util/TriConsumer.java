package cz.deznekcz.csl.osmeditor.util;

@FunctionalInterface
public interface TriConsumer<S1, S2, S3> {
	public void consume(S1 s1, S2 s2, S3 s3);
}
