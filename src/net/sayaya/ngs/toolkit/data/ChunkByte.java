package net.sayaya.ngs.toolkit.data;

import java.nio.ByteBuffer;

public class ChunkByte {
	private final int read;
	private final ByteBuffer data;
	
	public ChunkByte(ByteBuffer data, int read) {
		this.read = read;
		this.data = data;
	}
	public int getRead() {
		return read;
	}
	public ByteBuffer getData() {
		return data;
	}
}
