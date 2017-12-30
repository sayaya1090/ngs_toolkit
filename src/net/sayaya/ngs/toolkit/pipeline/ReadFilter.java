package net.sayaya.ngs.toolkit.pipeline;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import net.sayaya.ngs.toolkit.consumer.FastqWriterPairwise;
import net.sayaya.ngs.toolkit.consumer.SinkBGZF;
import net.sayaya.ngs.toolkit.supplier.FastqReaderPairwise;
import net.sayaya.ngs.toolkit.supplier.SourceBGZF;

public class ReadFilter {
	public final static void main(String[] args) {
		try {
			String source1 = "Undetermined_S0_L001_R1_001.fastq.gz";
			String source2 = "Undetermined_S0_L001_R2_001.fastq.gz";
			String source3 = "Undetermined_S0_L002_R1_001.fastq.gz";
			String source4 = "Undetermined_S0_L002_R2_001.fastq.gz";
			
			String[] s1 = {source1, source3};
			String[] s2 = {source2, source4};
			
			Stream<String> r1 = Arrays.stream(s1).sorted().map(fn->new File(fn)).map(File::toPath).map(SourceBGZF::new).map(SourceBGZF::get).reduce(Stream::concat).get();
			Stream<String> r2 = Arrays.stream(s2).sorted().map(fn->new File(fn)).map(File::toPath).map(SourceBGZF::new).map(SourceBGZF::get).reduce(Stream::concat).get();
			
			FastqReaderPairwise f = new FastqReaderPairwise(r1, r2);
			
			String dest1 = "data_5.fastq.gz";
			String dest2 = "data_6.fastq.gz";
			SinkBGZF d1 = SinkBGZF.write(new File(dest1).toPath());
			SinkBGZF d2 = SinkBGZF.write(new File(dest2).toPath());
			
			long time = new Date().getTime();
			AtomicLong totalRead = new AtomicLong(0);
			AtomicLong totalBase = new AtomicLong(0);
			AtomicLong totalGC = new AtomicLong(0);
			AtomicLong totalN = new AtomicLong(0);
			AtomicLong totalQ20 = new AtomicLong(0);
			AtomicLong totalQ30 = new AtomicLong(0);
			f.stream().parallel().filter(pair->{
				System.out.println(pair);
				if(pair.getA() == null) return false;
				if(pair.getB() == null) return false;
				if(pair.getA().getRead() == null || pair.getA().getRead().isEmpty()) return false;
				if(pair.getB().getRead() == null || pair.getB().getRead().isEmpty()) return false;
				return true;
			}).peek(pair->{
				System.out.println(pair);
				String read1 = pair.getA().getRead();
				String read2 = pair.getB().getRead();
				totalRead.incrementAndGet();
				totalBase.addAndGet(read1.length() + read2.length());
				totalGC.addAndGet(read1.chars().filter(ch->ch=='G' | ch=='C').count() + read2.chars().filter(ch->ch=='G' | ch=='C').count());
				totalN.addAndGet(read1.chars().filter(ch->ch=='N').count() + read2.chars().filter(ch->ch=='N').count());
				totalQ20.addAndGet(read1.chars().filter(v->v>=53).count() + read2.chars().filter(v->v>=53).count());
				totalQ30.addAndGet(read1.chars().filter(v->v>=63).count() + read2.chars().filter(v->v>=63).count());
			}).map(FastqWriterPairwise::toString).sequential().forEach(streams->{
				// System.out.println(streams[0] + ", " + streams[1]);
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
