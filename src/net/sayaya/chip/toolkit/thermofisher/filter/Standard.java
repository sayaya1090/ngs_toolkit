package net.sayaya.chip.toolkit.thermofisher.filter;

import net.sayaya.chip.toolkit.thermofisher.data.Cychp;

public class Standard {
	public static <T extends Cychp.Filterable> boolean test(T t) {
		int cnt = t.getMarkerCount();
		long size = t.getSize();
		return cnt >= 50 && size >= 400;
	}
}
