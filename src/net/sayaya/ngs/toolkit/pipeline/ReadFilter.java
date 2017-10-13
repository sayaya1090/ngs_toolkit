package net.sayaya.ngs.toolkit.pipeline;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import net.sayaya.ngs.toolkit.consumer.FastqWriterPairwise;
import net.sayaya.ngs.toolkit.consumer.SinkBGZF;
import net.sayaya.ngs.toolkit.supplier.FastqReaderPairwise;
import net.sayaya.ngs.toolkit.supplier.SourceBGZF;

public class ReadFilter {
	public final static void main(String[] args) {
		try {
			String source1 = "data_3.fastq.gz";
			String source2 = "data_4.fastq.gz";
			SourceBGZF t1 = new SourceBGZF(new File(source1).toPath());
			SourceBGZF t2 = new SourceBGZF(new File(source2).toPath());
			FastqReaderPairwise f = new FastqReaderPairwise(t1, t2);
			
			String dest1 = "data_5.fastq.gz";
			String dest2 = "data_6.fastq.gz";
			SinkBGZF d1 = SinkBGZF.write(new File(dest1).toPath());
			SinkBGZF d2 = SinkBGZF.write(new File(dest2).toPath());
			
			long time = new Date().getTime();
			AtomicLong totalRead = new AtomicLong(0);
			AtomicLong totalBase = new AtomicLong(0);
			AtomicLong totalGC = new AtomicLong(0);
			AtomicLong totalN = new AtomicLong(0);
			f.stream().parallel().filter(pair->{
				if(pair.getA() == null) return false;
				if(pair.getB() == null) return false;
				if(pair.getA().getRead() == null || pair.getA().getRead().isEmpty()) return false;
				if(pair.getB().getRead() == null || pair.getB().getRead().isEmpty()) return false;
				return true;
			}).peek(pair->{
				String read1 = pair.getA().getRead();
				String read2 = pair.getB().getRead();
				totalRead.incrementAndGet();
				totalBase.addAndGet(read1.length() + read2.length());
				totalGC.addAndGet(read1.chars().filter(ch->ch=='G' | ch=='C').count());
				totalN.addAndGet(read1.chars().filter(ch->ch=='N').count());
			}).map(FastqWriterPairwise::toString).sequential().forEach(streams->{
				streams[0].forEach(d1);
				streams[1].forEach(d2);
			});
			d1.close();
			d2.close();
			
			System.out.println(new Date().getTime() - time);
			System.out.println("Total Read:" + totalRead.get());
			System.out.println("Total Base:" + totalBase.get());
			System.out.println("Total GC:" + totalGC.get());
			System.out.println("Total N:" + totalN.get());
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
