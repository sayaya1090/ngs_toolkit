package net.sayaya.ngs.toolkit.supplier;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.sayaya.ngs.toolkit.data.Read;

public final class FastqReader implements Supplier<Stream<Read>> {
	private final Stream<String> source;
	public FastqReader(Stream<String> source) {
		this.source = source.sequential();
	}

	static Iterator<Read> toReadIterator(Iterator<String> source) {
		return new Iterator<Read>() {
			private final LinkedList<Read> buffer = new LinkedList<Read>();
			@Override
			public synchronized boolean hasNext() {
				if(!buffer.isEmpty()) return true;
				String[] lines = new String[4];
				String escapeFlag = null;
				if(source.hasNext()) lines[0] = source.next().trim();
				else return false;	
				if(source.hasNext()) lines[1] = source.next().trim();
				else return false;
				if(source.hasNext()) lines[2] = source.next().trim();
				else return false;
				if(source.hasNext()) {
					lines[3] = source.next().trim();
					if(lines[3].startsWith("@") && lines[3].contains(":") && lines[3].contains(" ") && (lines[3].length() != lines[1].length())) {
						escapeFlag = lines[3];
						lines[3] = null;
					}
				} else return false;
				
				buffer.addLast(new Read(lines[0], lines[1], lines[3]));
				if(escapeFlag!=null) escape(escapeFlag);
				return true;
			}
			
			public synchronized void escape(String header) {
				String[] lines = new String[3];
				String escapeFlag = null;
				if(source.hasNext()) lines[0] = source.next().trim();
				if(source.hasNext()) lines[1] = source.next().trim();
				if(source.hasNext()) {
					lines[2] = source.next().trim();
					if(lines[2].startsWith("@") && lines[2].contains(":") && lines[2].contains(" ") ) {
						escapeFlag = lines[2];
						lines[2] = null;
					}
				}
				buffer.addLast(new Read(header, lines[0], lines[2]));
				if(escapeFlag!=null) escape(escapeFlag);
			}

			@Override
			public Read next() {
				return buffer.poll();
			}
		};
	}
	@Override
	public Stream<Read> get() {
		Iterator<Read> iter = toReadIterator(FastqReader.this.source.iterator());
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE), false)
		.filter(Objects::nonNull)
		.filter(read->!read.isEmpty());
	}

	public Stream<Read> stream() {
		return get();
	}
	
	public final static void main(String[] args) {
		try {
			String source1 = "data1.fastq.gz";
			SourceBGZF t = new SourceBGZF(new File(source1).toPath());
			FastqReader f = new FastqReader(t.get());
			long time = new Date().getTime();
			AtomicLong cnt = new AtomicLong(0);
			f.stream().forEach(line->{
				if(cnt.incrementAndGet() % 10000 == 0) System.out.println(cnt.get());
				System.out.println(line);
			});

			System.out.println(cnt);
			System.out.println(new Date().getTime() - time);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
