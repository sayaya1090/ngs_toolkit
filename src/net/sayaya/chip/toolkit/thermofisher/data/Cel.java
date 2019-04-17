package net.sayaya.chip.toolkit.thermofisher.data;

import lombok.Data;

@Data
public class Cel {
	private int version;
	private int dataGroupNum;
	
	
	private Header header;
	
	public final static class Header {
		
	}
	
	public final static class Intensity {
		
	}
	
	public final static class Masks {
		
	}
	
	public final static class Outliers {
		
	}
	
	public final static class Modified {
		
	}
}
