package net.sayaya.ngs.toolkit.consumer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import net.sayaya.ngs.toolkit.data.ChunkByte;
import net.sayaya.ngs.toolkit.function.Compressor;
import net.sayaya.ngs.toolkit.supplier.SourceBGZF;

public class SinkBGZF implements Consumer<String> {
	private final FileChannel cout;
	private final ByteBuffer buffer = ByteBuffer.allocate(Compressor.BLOCK_SIZE).order(ByteOrder.LITTLE_ENDIAN);
	public static SinkBGZF write(Path path) throws IOException {
		return new SinkBGZF(path);
	}
	private SinkBGZF(Path path) throws IOException {
		cout = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
	@Override
	public synchronized void accept(String line) {
		byte[] data = (line + "\n").getBytes();
		if(data.length <= buffer.remaining()) buffer.put(data);
		else {
			int idx = buffer.remaining();
			buffer.put(data, 0, idx);
			buffer.rewind();
			ChunkByte chunk = Compressor.process(new ChunkByte(buffer, buffer.remaining()));
			
			try {
				cout.write(chunk.getData());
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			buffer.clear();
			buffer.put(data, idx, data.length-idx);
		}
	}
	public synchronized SinkBGZF close() {
		int pos = buffer.position();
		buffer.rewind();
		ChunkByte chunk = Compressor.process(new ChunkByte(buffer, pos));
		ChunkByte eof = Compressor.process(new ChunkByte(ByteBuffer.allocate(0), 0));
		try {
			cout.write(chunk.getData());
			cout.write(eof.getData());
			cout.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}		
		return this;
	}
	
	public final static void main(String[] args) {
		try {
			String source1 = "data_3.fastq.gz";
			String dest = "data_4.fastq.gz";
			SourceBGZF t = new SourceBGZF(new File(source1).toPath());
			SinkBGZF d = SinkBGZF.write(new File(dest).toPath());
			long time = new Date().getTime();
			AtomicLong cnt = new AtomicLong(0);
			t.stream().peek(line->{
				if(cnt.incrementAndGet() % 100000 == 0) System.out.println(line + "/" + cnt.get());
			}).forEach(d);
			d.close();
			System.out.println(cnt);
			System.out.println(new Date().getTime() - time);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
