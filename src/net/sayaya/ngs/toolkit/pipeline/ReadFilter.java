package net.sayaya.ngs.toolkit.pipeline;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import net.sayaya.ngs.toolkit.supplier.FastqReader;
import net.sayaya.ngs.toolkit.supplier.SourceBGZF;

public class ReadFilter {
	public final static void main(String[] args) {
		try {
			AtomicLong totalRead = new AtomicLong(0);
			AtomicLong totalBase = new AtomicLong(0);
			AtomicLong totalGC = new AtomicLong(0);
			AtomicLong totalN = new AtomicLong(0);
			AtomicLong totalQ20 = new AtomicLong(0);
			AtomicLong totalQ30 = new AtomicLong(0);
			
			Arrays.stream(args)
			.map(fileName->new SourceBGZF(new File(fileName.trim()).toPath()))
			.map(source->new FastqReader(source.get()))
			.parallel()
			.flatMap(FastqReader::get)
			.forEach(read->{
				totalRead.incrementAndGet();
				String read1 = read.getRead();
				totalBase.addAndGet(read1.length());
				totalGC.addAndGet(read1.chars().filter(ch->ch=='G' | ch=='C').count());
				totalN.addAndGet(read1.chars().filter(ch->ch=='N').count());
				totalQ20.addAndGet(read1.chars().filter(v->v>=53).count());
				totalQ30.addAndGet(read1.chars().filter(v->v>=63).count());
			});
			
			System.out.println("Total Read:" + totalRead.get());
			System.out.println("Total Base:" + totalBase.get());
			System.out.println("Total GC:" + totalGC.get());
			System.out.println("Total N:" + totalN.get());
			System.out.println("Total Q20:" + totalQ20.get());
			System.out.println("Total Q30:" + totalQ30.get());
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
