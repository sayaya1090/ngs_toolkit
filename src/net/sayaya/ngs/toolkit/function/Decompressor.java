package net.sayaya.ngs.toolkit.function;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import net.sayaya.ngs.toolkit.data.ChunkByte;
import net.sayaya.ngs.toolkit.data.ChunkCompressed;

public class Decompressor implements Function<ChunkCompressed, ChunkByte> {
	public static ChunkByte process(ChunkCompressed chunk) {
		if(chunk == null || chunk.getData() == null) return null;
		byte[] tmp = new byte[Compressor.BLOCK_SIZE];
		Inflater inf	= new Inflater(true);
		inf.setInput(chunk.getData().array(), 0, chunk.getRead());
		int count = 0;
		int total = 0;
		ByteBuffer buffer = ByteBuffer.allocate(Compressor.BLOCK_SIZE);
		do try {
			count = inf.inflate(tmp);
			buffer.put(tmp, 0, count);
			total += count;
		} catch(DataFormatException e) {e.printStackTrace();} while(count > 0);
		buffer.flip();

		int isize = total;
		if(isize!=chunk.getIsize()) throw new RuntimeException("BlockSize:" + isize + "/" + chunk.getIsize());

		CRC32 checksum	= new CRC32();
		checksum.update(buffer.array(), 0, isize);
		int crc = (int)checksum.getValue();
		if(crc!=chunk.getCrc()) throw new RuntimeException("CRC:" + crc + "/" +chunk.getCrc());
		return new ChunkByte(buffer, isize);
	}
	@Override
	public ChunkByte apply(ChunkCompressed chunk) {
		return Decompressor.process(chunk);
	}
}
