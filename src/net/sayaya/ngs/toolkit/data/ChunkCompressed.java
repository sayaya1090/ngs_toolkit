package net.sayaya.ngs.toolkit.data;

import java.nio.ByteBuffer;

public final class ChunkCompressed extends ChunkByte {
	private final int isize;
	private final int crc;
	
	public ChunkCompressed(ByteBuffer buffer, int read, int isize, int crc) {
		super(buffer, read);
		this.isize = isize;
		this.crc = crc;
	}

	public int getIsize() {
		return isize;
	}

	public int getCrc() {
		return crc;
	}
}
