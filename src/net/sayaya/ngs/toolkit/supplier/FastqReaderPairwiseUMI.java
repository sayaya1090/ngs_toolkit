package net.sayaya.ngs.toolkit.supplier;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.sayaya.ngs.toolkit.data.Read;
import net.sayaya.ngs.toolkit.data.Trio;

public final class FastqReaderPairwiseUMI implements Supplier<Stream<Trio<Read, Read, Read>>> {
	private final Stream<String> source1;
	private final Stream<String> source2;
	private final Stream<String> sourceUMI;
	public FastqReaderPairwiseUMI(Stream<String> source1, Stream<String> source2, Stream<String> sourceUMI) {
		this.source1 = source1.sequential();
		this.source2 = source2.sequential();
		this.sourceUMI = sourceUMI.sequential();
	}
	
	@Override
	public Stream<Trio<Read, Read, Read>> get() {
		Iterator<Read> iter1 = FastqReader.toReadIterator(FastqReaderPairwiseUMI.this.source1.iterator());
		Iterator<Read> iter2 = FastqReader.toReadIterator(FastqReaderPairwiseUMI.this.source2.iterator());
		Iterator<Read> iter3 = FastqReader.toReadIterator(FastqReaderPairwiseUMI.this.sourceUMI.iterator());
		Iterator<Trio<Read, Read, Read>> iter = new Iterator<Trio<Read, Read, Read>>() {
			@Override
			public boolean hasNext() {
				return iter1.hasNext() && iter2.hasNext() && iter3.hasNext();
			}

			@Override
			public Trio<Read, Read, Read> next() {
				return new Trio<Read, Read, Read>().setA(iter1.next()).setB(iter2.next()).setC(iter3.next());
			}	
		};
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE), false);
	}

	public Stream<Trio<Read, Read, Read>> stream() {
		return get();
	}
}
