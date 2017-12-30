package net.sayaya.ngs.toolkit.supplier;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.sayaya.ngs.toolkit.data.Pair;
import net.sayaya.ngs.toolkit.data.Read;

public final class FastqReaderPairwise implements Supplier<Stream<Pair<Read, Read>>> {
	private final Stream<String> source1;
	private final Stream<String> source2;
	public FastqReaderPairwise(Stream<String> source1, Stream<String> source2) {
		this.source1 = source1.sequential();
		this.source2 = source2.sequential();
	}
	
	@Override
	public Stream<Pair<Read, Read>> get() {
		Iterator<Read> iter1 = FastqReader.toReadIterator(FastqReaderPairwise.this.source1.iterator());
		Iterator<Read> iter2 = FastqReader.toReadIterator(FastqReaderPairwise.this.source2.iterator());
		Iterator<Pair<Read, Read>> iter = new Iterator<Pair<Read, Read>>() {
			@Override
			public boolean hasNext() {
				return iter1.hasNext() && iter2.hasNext();
			}

			@Override
			public Pair<Read, Read> next() {
				return new Pair<Read, Read>().setA(iter1.next()).setB(iter2.next());
			}	
		};
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE), false);
	}

	public Stream<Pair<Read, Read>> stream() {
		return get();
	}
	
	public final static void main(String[] args) {
		try {
			String source1 = "data1.fastq.gz";
			String source2 = "data2.fastq.gz";
			SourceBGZF t1 = new SourceBGZF(new File(source1).toPath());
			SourceBGZF t2 = new SourceBGZF(new File(source2).toPath());
			FastqReaderPairwise f = new FastqReaderPairwise(t1.get(), t2.get());
			long time = new Date().getTime();
			AtomicLong cnt = new AtomicLong(0);
			f.stream().parallel().forEach(line->{
				if(cnt.incrementAndGet() % 100000 == 0) System.out.println(cnt.get());
			});

			System.out.println(cnt);
			System.out.println(new Date().getTime() - time);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
