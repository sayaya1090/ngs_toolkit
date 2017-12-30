package net.sayaya.ngs.toolkit.supplier;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.sayaya.ngs.toolkit.data.ChunkByte;
import net.sayaya.ngs.toolkit.data.ChunkCompressed;
import net.sayaya.ngs.toolkit.function.Decompressor;

public final class SourceBGZF implements Supplier<Stream<String>> {
	private final static byte[] header	= new byte[] {0x1F, (byte)0x8B, 0x08, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xFF, 0x06, 0x00, 0x42, 0x43, 0x02, 0x00};
	private final FileChannel cin;
	private final long size;
	private final ByteBuffer headerBuffer	= ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
	private final ByteBuffer bsizeBuffer	= ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
	private final ByteBuffer crcBuffer		= ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
	private final ByteBuffer isizeBuffer	= ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
	private final static int xlen = 6;
	private final int bufferSize;
	
	public SourceBGZF(int bufferSize, Path path) {
		try {
			cin = FileChannel.open(path, StandardOpenOption.READ);
			size = cin.size();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.bufferSize = bufferSize;
	}
	public SourceBGZF(Path path) {
		this(10000, path);
	}
	
	private boolean hasNext() {
		if(!cin.isOpen()) return false;
		try {
			return cin.position() < size;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private synchronized ChunkCompressed getChunk() {
		headerBuffer.clear();
		bsizeBuffer.clear();
		crcBuffer.clear();
		isizeBuffer.clear();
		try {
			int read = cin.read(headerBuffer);
			if(read < 0) {
				cin.close();
				return null;
			} else {
				if(!Arrays.equals(headerBuffer.array(), header)) throw new EOFException();
				cin.read(bsizeBuffer);
				bsizeBuffer.flip();
				int blockSize = bsizeBuffer.getChar();
				ByteBuffer buffer = ByteBuffer.allocate(blockSize-xlen-19).order(ByteOrder.LITTLE_ENDIAN);
				read = cin.read(buffer);
	
				cin.read(crcBuffer);
				cin.read(isizeBuffer);
				crcBuffer.flip();
				int crc = crcBuffer.getInt();
				isizeBuffer.flip();
				int isize = isizeBuffer.getInt();
				buffer.flip();
				return new ChunkCompressed(buffer, read, isize, crc);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private StringBuffer prev = new StringBuffer();
	private List<String> toString(ChunkByte chunk) {
		if(chunk == null) return null;
		List<String> out = new LinkedList<String>();
		byte[] bytes = new byte[chunk.getRead()];
		chunk.getData().get(bytes);
		prev.append(new String(bytes));
		String str = prev.toString();
		prev.setLength(0);
		if(str.contains("\n")) {
			String[] split = str.split("\n");
			for(int i = 0; i < split.length-1; ++i) out.add(split[i]);
			if(str.endsWith("\n")) out.add(split[split.length-1]);
			else prev.append(split[split.length-1]);
		}
		return out;
	}
	
	@Override
	public Stream<String> get() {
		Iterator<ChunkCompressed> iter = new Iterator<ChunkCompressed>() {
			@Override
			public boolean hasNext() {
				return SourceBGZF.this.hasNext();
			}

			@Override
			public ChunkCompressed next() {
				return getChunk();
			}
		};
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE), false).limit(bufferSize)
		.map(Decompressor::process).filter(chunk->chunk.getRead() > 0)
		.map(this::toString).flatMap(Collection::stream).onClose(()->{
			try {
				cin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public Stream<String> stream() {
		return get();
	}
	
	public final static void main(String[] args) {
		try {
			String source1 = "data1.fastq.gz";
			SourceBGZF t = new SourceBGZF(new File(source1).toPath());
			long time = new Date().getTime();
			AtomicLong cnt = new AtomicLong(0);
			t.stream().forEach(line->{
				if(cnt.incrementAndGet() % 10000 == 0) System.out.println(cnt.get());
				if(cnt.get() > 11750200) System.out.println(line);
			});

			System.out.println(cnt);
			System.out.println(new Date().getTime() - time);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
