package net.sayaya.ngs.toolkit.illumina;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.sayaya.ngs.toolkit.illumina.data.TileMetricsInfo;

public class SAVLoader implements Supplier<Stream<TileMetricsInfo>>, Iterator<TileMetricsInfo> {
	private final FileChannel cin;
	private final ByteBuffer bufferData = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);
	private boolean hasNext = false;
	public SAVLoader(Path tileMetricsOutBin) {
		try {
			cin = FileChannel.open(tileMetricsOutBin, StandardOpenOption.READ);
			ByteBuffer bufferHeader = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);		
			read(cin, bufferHeader);
			hasNext = read(cin, bufferData);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public final static void main(String[] args) {
		SAVLoader instance = new SAVLoader(new File("InterOp/TileMetricsOut.bin").toPath());
		System.out.println(instance.stream().filter(t->t.getMet()==(short)100).mapToDouble(t->t.getValue()).average().getAsDouble());		// Density
		SAVLoader instance2 = new SAVLoader(new File("InterOp/TileMetricsOut.bin").toPath());
		System.out.println(instance2.stream().filter(t->t.getMet()==(short)101).mapToDouble(t->t.getValue()).average().getAsDouble());		// PF
		SAVLoader instance3 = new SAVLoader(new File("InterOp/TileMetricsOut.bin").toPath());
		System.out.println(instance3.stream().filter(t->103 == t.getMet()).mapToDouble(t->t.getValue()).sum()*89/1000000000);		// Yield
	}
	
	@Override
	public boolean hasNext() {
		hasNext = read(cin, bufferData);
		return hasNext;
	}


	@Override
	public TileMetricsInfo next() {
		return new TileMetricsInfo(bufferData);
	}
	
	@Override
	public Stream<TileMetricsInfo> get() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE), false)
		.onClose(()->{
			try {
				System.out.println("Close2");
				cin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public Stream<TileMetricsInfo> stream() {
		return get();
	}
	
	private final static boolean read(FileChannel channel, ByteBuffer buffer) {
		buffer.clear();
		try {
		int read = channel.read(buffer);
		buffer.flip();
		if(read == buffer.capacity()) return true; 
		return false;
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
