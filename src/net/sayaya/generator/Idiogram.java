package net.sayaya.generator;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import net.sayaya.chip.toolkit.thermofisher.CychpLoader;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.CNNeutralLohSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.CopyNumberSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.CopyNumberState;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.Loh;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.LohSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.MosaicismSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.NormalDiploidSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.Option;
import net.sayaya.data.Chromosome;
import net.sayaya.data.Cytoband;
import net.sayaya.ngs.toolkit.data.Pair;
import net.sayaya.util.CytobandLoader;

public class Idiogram {
	private final static int MARGIN_TOP = 30;
	private final static int CHROMOSOME_WIDTH = 30;
	private final static int CHROMOSOME_HEIGHT = 350;
	final static long SCALE_OUT = 700000;
	private final static int CHROMOSOME_MARGIN = 55;
	final static int COLS_PER_ROW = 12;
	
	public static Graphics2D draw(Graphics2D g, Chromosome... chrs) throws FontFormatException, IOException {
		Font font = Font.createFont(Font.TRUETYPE_FONT, new File("font/KaiGenGothicKR-Regular.ttf")).deriveFont(12.0f);
		g.setFont(font);
		Map<Integer, Integer> HEIGHT_MAX = new HashMap<>();
		for(int i = 0; i < chrs.length; ++i) {
			Chromosome chr = chrs[i];
			Integer row = i/COLS_PER_ROW;
			if(!HEIGHT_MAX.containsKey(row)) HEIGHT_MAX.put(row, -1);
			HEIGHT_MAX.put(row, Math.max(HEIGHT_MAX.get(row), (int)(chr.getLength()/ (double) SCALE_OUT)));
		}
		for(int i = 0; i < chrs.length; ++i) {
			Chromosome chr = chrs[i];
			Cytoband[] cytobands = chr.getCytobands();
			int splitIndex = (int) Arrays.stream(chrs[i].getCytobands()).dropWhile(s->!s.getGieStain().equals("acen")).count();
			Cytoband[] cytoband1 = Arrays.copyOf(cytobands, cytobands.length-splitIndex+1);
			Cytoband[] cytoband2 = Arrays.copyOfRange(cytobands, cytobands.length-splitIndex+1, cytobands.length);
			Pair<Integer, Integer> map = transform(chrs, i, 0);
			double posX = map.getA();
			double posY = map.getB();
			{
				Graphics2D g1 = (Graphics2D) g.create();
				double h = Arrays.stream(cytoband1).mapToLong(Cytoband::getEnd).max().getAsLong() / (double) SCALE_OUT;
				g1.clip(new ChromosomeRect((int) Math.round(posX), (int) Math.round(posY), CHROMOSOME_WIDTH, (int) Math.round(h), CHROMOSOME_WIDTH / 3));
				for (int j = 0; j < cytoband1.length; ++j) {
					Cytoband cytoband = cytoband1[j];
					double start = posY + cytoband.getStart() / (double) SCALE_OUT;
					double height = (cytoband.getEnd() - cytoband.getStart()) / (double) SCALE_OUT;
					if ("acen".equals(cytoband.getGieStain())) {
					} else {
						Color c1 = toColorFill(cytoband.getGieStain());
						Color c2 = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), (int) (c1.getAlpha() * 0.65));
						GradientPaint gradient = new GradientPaint(Math.round(posX - CHROMOSOME_WIDTH), 0, c2, Math.round(posX - 10), 0, c1, true);
						g1.setPaint(gradient);
						g1.fillRect((int) Math.round(posX), (int) Math.round(start), 30, (int) Math.round(height));
					}
				}
				g1.dispose();
				g.setColor(Color.GRAY);
				g.draw(new ChromosomeRect((int) Math.round(posX), (int) Math.round(posY), CHROMOSOME_WIDTH, (int) Math.round(h), CHROMOSOME_WIDTH/3));
			} {
				Graphics2D g1 = (Graphics2D) g.create();
				g1 = (Graphics2D) g.create();
				double y = Arrays.stream(cytoband2).mapToLong(Cytoband::getStart).min().getAsLong() / (double) SCALE_OUT;
				double h = Arrays.stream(cytoband2).mapToLong(Cytoband::getEnd).max().getAsLong() / (double) SCALE_OUT;
				g1.clip(new ChromosomeRect((int) Math.round(posX), (int) Math.round(posY + y), CHROMOSOME_WIDTH, (int) Math.round(h-y), CHROMOSOME_WIDTH / 3));
				for (int j = 0; j < cytoband2.length; ++j) {
					Cytoband cytoband = cytoband2[j];
					double start = posY + cytoband.getStart() / (double) SCALE_OUT;
					double height = (cytoband.getEnd() - cytoband.getStart()) / (double) SCALE_OUT;
					if ("acen".equals(cytoband.getGieStain())) {
					} else {
						Color c1 = toColorFill(cytoband.getGieStain());
						Color c2 = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), (int) (c1.getAlpha() * 0.50));
						GradientPaint gradient = new GradientPaint(Math.round(posX - CHROMOSOME_WIDTH), 0, c2, Math.round(posX - 10), 0, c1, true);
						g1.setPaint(gradient);
						g1.fillRect((int) Math.round(posX), (int) Math.round(start), 30, (int) Math.round(height));
					}
				}
				g1.dispose();
				g.setColor(Color.GRAY);
				g.draw(new ChromosomeRect((int) Math.round(posX), (int) Math.round(posY + y), CHROMOSOME_WIDTH, (int) Math.round(h-y), CHROMOSOME_WIDTH / 3));
			}
			FontMetrics metrics = g.getFontMetrics(font);
			int x = (int) (posX + (CHROMOSOME_WIDTH - metrics.stringWidth(chr.getName())) / 2.0);
			g.setColor(Color.BLACK);
			g.drawString(chr.getName(), x, (i/COLS_PER_ROW)%2==0?((int) posY - 5):((int) posY + chr.getLength()/SCALE_OUT +5+font.getSize()));
		}
		return g;
	}
	private final static class ChromosomeRect extends Path2D.Float {
		public ChromosomeRect(int x, int y, float width, float height, float radius) {
			moveTo(x, y + radius);
			lineTo(x, y + height - radius);
			quadTo(x, y + height, x + radius, y + height);
			lineTo(x + width - radius, y + height);
			quadTo(x + width, y + height, x + width, y + height - radius);
			lineTo(x + width, y + radius);
			quadTo(x + width, y, x + width - radius, y);
			lineTo(x + radius, y);
			quadTo(x, y, x, y + radius);
			closePath();
		}
	}
	static Pair<Integer, Integer> transform(Chromosome[] chrs, int chr, long spot) {
		int row = chr/COLS_PER_ROW;
		int posX = (CHROMOSOME_WIDTH+CHROMOSOME_MARGIN)*(chr%COLS_PER_ROW);
		int posY ;
		if(row%2 != 0) {
			long heightMax = IntStream.range(0, chrs.length).filter(n->n/COLS_PER_ROW == row).mapToLong(n->chrs[n].getLength()).max().getAsLong();
			posY = (int)(MARGIN_TOP+CHROMOSOME_HEIGHT*row+(heightMax-chrs[chr].getLength())/ (double) SCALE_OUT);
		} else posY = MARGIN_TOP+CHROMOSOME_HEIGHT*row;
		int delta = (int)(spot / (double) SCALE_OUT);
		return new Pair<Integer, Integer>().setA(posX).setB(posY+delta);
	}
	
	private final static Color toColorFill(String gieStain) {
		switch(gieStain) {
		case "gneg": return new Color(0, 12, 33, (int)(256*0.1));
		case "gpos25": return new Color(0, 12, 33, (int)(256*0.25));
		case "gpos50": return new Color(0, 12, 33, (int)(256*0.50));
		case "gpos75": return new Color(0, 12, 33, (int)(256*0.75));
		case "gpos100": return new Color(0, 12, 33, (int)(256*0.9));
		case "acen": 	return Color.WHITE;
		case "gvar": 	return Color.LIGHT_GRAY;
		case "stalk": 	return Color.DARK_GRAY;
		default: return Color.WHITE;
		}
	}
	
	private static Color toColorLine(String gieStain) {
		/*switch(gieStain) {
		case "gneg": return Color.DARK_GRAY;
		case "gpos25": return Color.DARK_GRAY;
		case "gpos50": return Color.DARK_GRAY;
		case "gpos75": return Color.DARK_GRAY;
		case "gpos100": return Color.DARK_GRAY;
		case "acen":
		case "gvar":
		case "stalk": 	return Color.DARK_GRAY;
		default: return Color.WHITE;
		}*/
		return Color.GRAY;
	}
	
	public final static void main(String[] args) throws IOException, FontFormatException {
		Map<String, List<Cytoband>> cytobands = new CytobandLoader(new File("./cytoband.txt").toPath()).get().collect(Collectors.groupingBy(Cytoband::getChr));
		Chromosome[] chrs = cytobands.keySet().stream()
		.map(chr->{
			Cytoband[] c = cytobands.get(chr).stream().toArray(Cytoband[]::new);
			long length = Arrays.stream(c).mapToLong(Cytoband::getEnd).max().getAsLong();
			return new Chromosome().setName(chr).setCytobands(c).setLength(length);
		}).sorted().toArray(Chromosome[]::new);

		Cychp cychp = new CychpLoader(new File("C:\\Users\\Sangjay\\git\\ngs_toolkit\\20190715_161937_002_2.cyto.dxchp").toPath()).get();
		BufferedImage bi = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setBackground(Color.WHITE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.clearRect(0, 0, 1024, 768);

		Idiogram.draw(g, chrs);
		CopyNumberSet cns = cychp.getSegments().getCopyNumber();
		CopyNumber.draw(g, cns, chrs);
		/*
		LohSet lohs = cychp.getSegments().getLoh();
		if(lohs!=null) for(int i = 0; i < lohs.getLohCount(); ++i) {
			Loh cn = lohs.getLoh(i);
			if(cn.getLoh() == Option.EXIST) {
				int ordinal = cn.getChr()-1;
				if(ordinal > 22) ordinal = ordinal-1;
				Pair<Integer, Integer> pos1 = transform(chrs, ordinal, cn.getStart());
				Pair<Integer, Integer> pos2 = transform(chrs, ordinal, cn.getStop());
				g.setColor(new Color(189, 26, 55, 200));
				g.fillRect(pos1.getA()+CHROMOSOME_WIDTH+22, pos1.getB(), 12, pos2.getB()-pos1.getB());
				
				if(pos2.getB()-pos1.getB() > 5) {
					g.setColor(Color.GRAY);
					Pair<Integer, Integer> pos3 = transform(chrs, ordinal, (cn.getStop()+cn.getStart())/2);
					g.drawString("●", pos1.getA()+CHROMOSOME_WIDTH+22, pos3.getB()+3);
				}
			}
		}*/
		
		CNNeutralLohSet lohs2 = cychp.getSegments().getCNNeutralLoh();
		if(lohs2!=null) for(int i = 0; i < lohs2.getCNNeutralLohCount(); ++i) {
		//	System.out.println(lohs2.getCNNeutralLoh(i));
		}
		
		NormalDiploidSet diploid = cychp.getSegments().getDiploid();
		if(diploid!=null) for(int i = 0; i < diploid.getNormalDiploidCount(); ++i) {
		//	System.out.println(diploid.getNormalDiploid(i));
		}
		
		MosaicismSet mosaicism = cychp.getSegments().getMosaicism();
		if(mosaicism!=null) for(int i = 0; i < mosaicism.getMosaicismCount(); ++i) {
		//	System.out.println(mosaicism.getMosaicism(i));
		}
		ImageIO.write(bi, "png", new File("./output.png"));
	}
}
