package net.sayaya.ngs.toolkit.illumina.data;

import java.nio.ByteBuffer;

import lombok.Data;

@Data
public final class TileMetricsInfo {
	private final short lane;
	private final short tile;
	private final short met;
	private final float value;
	
	public TileMetricsInfo(ByteBuffer buffer) {
		lane = buffer.getShort();
		tile = buffer.getShort();
		met = buffer.getShort();
		value = buffer.getFloat();
	}
}
