package net.sayaya.ngs.toolkit.consumer;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import net.sayaya.ngs.toolkit.data.Pair;
import net.sayaya.ngs.toolkit.data.Read;

public final class FastqWriterPairwise implements Consumer<Pair<Read, Read>>, Function<Pair<Read, Read>, Stream<String>[]> {
	private final FastqWriter sink1;
	private final FastqWriter sink2;
	public FastqWriterPairwise(SinkBGZF sink1, SinkBGZF sink2) {
		this.sink1 = new FastqWriter(sink1);
		this.sink2 = new FastqWriter(sink2);
	}
	@Override
	public void accept(Pair<Read, Read> r) {
		sink1.accept(r.getA());
		sink2.accept(r.getB());
	}
	
	public synchronized FastqWriterPairwise close() {
		sink1.close();
		sink2.close();
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public static Stream<String>[] toString(Pair<Read, Read> r) {
		return new Stream[] {
			FastqWriter.toString(r.getA())
			, FastqWriter.toString(r.getB())
		};
	}
	
	@Override
	public Stream<String>[] apply(Pair<Read, Read> r) {
		return toString(r);
	}
}
