package net.sayaya.ngs.toolkit.function;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Function;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import net.sayaya.ngs.toolkit.data.ChunkByte;
import net.sayaya.ngs.toolkit.data.ChunkCompressed;

public class Compressor implements Function<ChunkByte, ChunkCompressed> {
	private final static byte[] header	= new byte[] {0x1F, (byte)0x8B, 0x08, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xFF, 0x06, 0x00, 0x42, 0x43, 0x02, 0x00};

	public static int BLOCK_SIZE = 65536;
	public static ChunkCompressed process(ChunkByte chunk) {
		ByteBuffer buffer = chunk.getData();
		if(buffer == null) return null;
		
		CRC32 checksum = new CRC32();
		byte[] data = buffer.array();
		checksum.reset();
		checksum.update(data, 0, chunk.getRead());
		int crc = (int)checksum.getValue();
		
		byte[] tmp = new byte[BLOCK_SIZE];
		Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
		def.setInput(data, 0, chunk.getRead());
		def.finish();
		int count = def.deflate(tmp);
		
		char bsize = (char)(count+25);
		ByteBuffer block = ByteBuffer.allocate(bsize+1).order(ByteOrder.LITTLE_ENDIAN);
		block.put(header).putChar(bsize).put(tmp, 0, count).putInt(crc).putInt(chunk.getRead());
		block.flip();
		return new ChunkCompressed(block, bsize+1, bsize, crc);
		
	}

	@Override
	public ChunkCompressed apply(ChunkByte chunk) {
		return Compressor.process(chunk);
	}
}
