package net.sayaya.ngs.toolkit.consumer;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import net.sayaya.ngs.toolkit.data.Pair;
import net.sayaya.ngs.toolkit.data.Read;
import net.sayaya.ngs.toolkit.data.Trio;

public final class FastqWriterPairwiseUMI implements Consumer<Trio<Read, Read, Read>>, Function<Trio<Read, Read, Read>, Stream<String>[]> {
	private final FastqWriter sink1;
	private final FastqWriter sink2;
	private final FastqWriter sink3;
	public FastqWriterPairwiseUMI(SinkBGZF sink1, SinkBGZF sink2, SinkBGZF sink3) {
		this.sink1 = new FastqWriter(sink1);
		this.sink2 = new FastqWriter(sink2);
		this.sink3 = new FastqWriter(sink3);
	}
	@Override
	public void accept(Trio<Read, Read, Read> r) {
		sink1.accept(r.getA());
		sink2.accept(r.getB());
		sink3.accept(r.getC());
	}
	
	public synchronized FastqWriterPairwiseUMI close() {
		sink1.close();
		sink2.close();
		sink3.close();
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public static Stream<String>[] toString(Trio<Read, Read, Read> r) {
		return new Stream[] {
			FastqWriter.toString(r.getA())
			, FastqWriter.toString(r.getB())
			, FastqWriter.toString(r.getC())
		};
	}
	
	@Override
	public Stream<String>[] apply(Trio<Read, Read, Read> r) {
		return toString(r);
	}
}
