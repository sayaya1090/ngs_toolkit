package net.sayaya.ngs.toolkit.consumer;

import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import net.sayaya.ngs.toolkit.data.Read;
import net.sayaya.ngs.toolkit.supplier.FastqReader;
import net.sayaya.ngs.toolkit.supplier.SourceBGZF;

public class FastqWriter implements Consumer<Read>, Function<Read, Stream<String>> {
	private final SinkBGZF sink;
	public FastqWriter(SinkBGZF sink) {
		this.sink = sink;
	}
	@Override
	public void accept(Read r) {
		sink.accept(r.getHeader());
		sink.accept(r.getRead());
		sink.accept("+");
		if(r.getQuality()!=null) sink.accept(r.getQuality());
	}
	
	public synchronized FastqWriter close() {
		sink.close();
		return this;
	}
	
	@Override
	public Stream<String> apply(Read r) {
		return toString(r);
	}
	
	public static Stream<String> toString(Read r) {
		return Stream.of(r.getHeader(), r.getRead(), "+", r.getQuality()!=null?r.getQuality():null).filter(Objects::nonNull);
	}
	
	public final static void main(String[] args) {
		try {
			String source1 = "data1.fastq.gz";
			String dest = "data_1.fastq.gz";
			SourceBGZF t = new SourceBGZF(new File(source1).toPath());
			FastqReader f = new FastqReader(t.get());
			SinkBGZF d = SinkBGZF.write(new File(dest).toPath());
			FastqWriter w = new FastqWriter(d);
			long time = new Date().getTime();
			AtomicLong totalRead = new AtomicLong(0);
			AtomicLong totalBase = new AtomicLong(0);
			AtomicLong totalGC = new AtomicLong(0);
			AtomicLong totalN = new AtomicLong(0);
			AtomicLong totalQ20 = new AtomicLong(0);
			AtomicLong totalQ30 = new AtomicLong(0);
			
			f.stream().parallel()
			.forEach(read->{
				totalRead.incrementAndGet();
				String read1 = read.getRead();
				totalBase.addAndGet(read1.length());
				totalGC.addAndGet(read1.chars().filter(ch->ch=='G' | ch=='C').count());
				totalN.addAndGet(read1.chars().filter(ch->ch=='N').count());
				totalQ20.addAndGet(read1.chars().filter(v->v>=53).count());
				totalQ30.addAndGet(read1.chars().filter(v->v>=63).count());
			})/*.forEach(w)*/;
			
			w.close();
			System.out.println(totalRead.get());
			System.out.println(totalBase.get());
			System.out.println(totalGC.get());
			System.out.println(totalN.get());
			System.out.println(totalQ20.get());
			System.out.println(totalQ30.get());
			System.out.println(new Date().getTime() - time);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
