package net.sayaya.generator;

import net.sayaya.chip.toolkit.thermofisher.data.Cychp;
import net.sayaya.chip.toolkit.thermofisher.filter.Standard;
import net.sayaya.data.Chromosome;
import net.sayaya.ngs.toolkit.data.Pair;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CopyNumber {
	private final static int OFFSET_X = 35;
	public static Graphics2D draw(Graphics2D g, Cychp.CopyNumberSet cns, Chromosome... chrs) throws FontFormatException, IOException {
		if(cns==null) return g;
		if(cns.getCopynumbers()==null) return g;
		List<Integer> hits = Arrays.stream(cns.getCopynumbers())
								   .filter(Objects::nonNull)
								   .filter(cn->{
			if(cn.getState() == Cychp.CopyNumberState.LOSS) return true;
			return cn.getState() == Cychp.CopyNumberState.GAIN || cn.getState() == Cychp.CopyNumberState.GAIN2;
		}).filter(Standard::test).map(cn->{
			int ordinal = cn.getChr()-1;
			if(ordinal > 22) ordinal = ordinal-1;
			return ordinal;
		}).distinct().collect(Collectors.toList());
		if(hits.isEmpty()) return g;
		Font font = Font.createFont(Font.TRUETYPE_FONT, new File("font/KaiGenGothicKR-Regular.ttf")).deriveFont(6.0f);
		g.setFont(font);
		for(int i: hits) {
			Pair<Integer, Integer> map = Idiogram.transform(chrs, i, 0);
			double posX = map.getA();
			double posY = map.getB();
			FontMetrics metrics = g.getFontMetrics(font);
			int x = (int) (posX + OFFSET_X + (12 - metrics.stringWidth("CN")) / 2.0);
			g.setColor(Color.BLACK);
			g.drawString("CN", x, (i/Idiogram.COLS_PER_ROW)%2==0?((int) posY - 5):((int) posY + chrs[i].getLength()/Idiogram.SCALE_OUT +5+font.getSize()));
		}

		for(int i = 0; i < cns.getCopyNumberCount(); ++i) {
			Cychp.CopyNumber cn = cns.getCopyNumber(i);
			if(!Standard.test(cn)) continue;
			if(cn.getState() == Cychp.CopyNumberState.LOSS /*|| cn.getState() == CopyNumberState.ABSENT*/) {
				int ordinal = cn.getChr()-1;
				if(ordinal > 22) ordinal = ordinal-1;
				Pair<Integer, Integer> pos1 = Idiogram.transform(chrs, ordinal, cn.getStart());
				Pair<Integer, Integer> pos2 = Idiogram.transform(chrs, ordinal, cn.getStop());
				g.setColor(new Color(189, 26, 55, 200));
				g.fillRect(pos1.getA()+OFFSET_X, pos1.getB(), 12, pos2.getB()-pos1.getB());
				if(pos2.getB()-pos1.getB() > 5) {
					g.setColor(Color.GRAY);
					Pair<Integer, Integer> pos3 = Idiogram.transform(chrs, ordinal, (cn.getStop()+cn.getStart())/2);
					g.drawString("▼", pos1.getA()+OFFSET_X, pos3.getB()+3);
				}
			} else if(cn.getState() == Cychp.CopyNumberState.GAIN || cn.getState() == Cychp.CopyNumberState.GAIN2) {
				int ordinal = cn.getChr()-1;
				if(ordinal > 22) ordinal = ordinal-1;
				Pair<Integer, Integer> pos1 = Idiogram.transform(chrs, ordinal, cn.getStart());
				Pair<Integer, Integer> pos2 = Idiogram.transform(chrs, ordinal, cn.getStop());
				g.setColor(new Color(12, 76, 138, 200));
				g.fillRect(pos1.getA()+OFFSET_X, pos1.getB(), 12, pos2.getB()-pos1.getB());
				if(pos2.getB()-pos1.getB() > 5) {
					g.setColor(Color.GRAY);
					Pair<Integer, Integer> pos3 = Idiogram.transform(chrs, ordinal, (cn.getStop()+cn.getStart())/2);
					g.drawString("▲", pos1.getA()+OFFSET_X, pos3.getB()+3);
				}
			}
		}
		return g;
	}
}
