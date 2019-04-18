package net.sayaya.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Cytoband {
	private String chr;
	private long start;
	private long end;
	private String name;
	private String gieStain;
}
