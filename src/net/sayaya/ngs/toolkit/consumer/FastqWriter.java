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
			FastqReader f = new FastqReader(t);
			SinkBGZF d = SinkBGZF.write(new File(dest).toPath());
			FastqWriter w = new FastqWriter(d);
			long time = new Date().getTime();
			AtomicLong cnt = new AtomicLong(0);
			f.stream().peek(line->{
				if(cnt.incrementAndGet() % 100000 == 0) System.out.println(line + "/" + cnt.get());
			}).map(FastqWriter::toString);
			w.close();
			System.out.println(cnt);
			System.out.println(new Date().getTime() - time);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
