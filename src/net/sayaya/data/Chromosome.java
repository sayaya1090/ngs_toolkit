package net.sayaya.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Chromosome implements Comparable<Chromosome> {
	private String name;
	private Cytoband[] cytobands;
	private long length;
	
	@Override
	public int compareTo(Chromosome other) {
		return Byte.compare(ordinal(), other.ordinal());
	}
	
	public static int compare(Chromosome chr1, Chromosome chr2) {
		return Byte.compare(chr1.ordinal(), chr2.ordinal());
	}
	
	public final byte ordinal() {
		String tmp = name;
		if(tmp.contains("chr")) tmp = tmp.replace("chr", "");
		if("X".equalsIgnoreCase(tmp)) return 24;
		else if("Y".equalsIgnoreCase(tmp)) return 25;
		else if("MT".equalsIgnoreCase(tmp)) return 26;
		else return (byte)(Byte.parseByte(tmp)-1);
	}
}
