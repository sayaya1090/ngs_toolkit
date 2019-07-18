package net.sayaya.chip.toolkit.thermofisher.filter;

import net.sayaya.chip.toolkit.thermofisher.data.Cychp;

public class LOHOnly {
	public static <T extends Cychp.Filterable> boolean test(T t) {
		if(t instanceof Cychp.Loh) {
			int cnt = t.getMarkerCount();
			long size = t.getSize();
			return cnt >= 50 && size >= 3000;
		} else return true;
	}
}
