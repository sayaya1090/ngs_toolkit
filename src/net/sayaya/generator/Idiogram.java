package net.sayaya.generator;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import net.sayaya.chip.toolkit.thermofisher.CychpLoader;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.CNNeutralLohSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.CopyNumber;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.CopyNumberSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.CopyNumberState;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.LohSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.MosaicismSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.NormalDiploidSet;
import net.sayaya.data.Chromosome;
import net.sayaya.data.Cytoband;
import net.sayaya.ngs.toolkit.data.Pair;
import net.sayaya.util.CytobandLoader;

public class Idiogram {
	private final static int MARGIN_TOP = 30;
	private final static int CHROMOSOME_WIDTH = 30;
	private final static int CHROMOSOME_HEIGHT = 500;
	private final static long SCALE_OUT = 700000;
	private final static int CHROMOSOME_MARGIN = 55;
	private final static int COLS_PER_ROW = 12;
	
	public static Graphics2D draw(Graphics2D g, Chromosome... chrs) throws FontFormatException, IOException {
		Font font = Font.createFont(Font.TRUETYPE_FONT, new File("font/KaiGenGothicKR-Regular.ttf")).deriveFont(12.0f);
		g.setFont(font);
		for(int i = 0; i < chrs.length; ++i) {
			double posX = (CHROMOSOME_WIDTH+CHROMOSOME_MARGIN)*(i%COLS_PER_ROW);
			double posY = MARGIN_TOP+(CHROMOSOME_HEIGHT)*(i/COLS_PER_ROW);
			Chromosome chr = chrs[i];
			Cytoband[] cytobands = chr.getCytobands();
			for(int j = 0; j < cytobands.length; ++j) {
				Cytoband cytoband = cytobands[j];
				double start = posY + cytoband.getStart() / (double)SCALE_OUT;
				double height = (cytoband.getEnd() - cytoband.getStart()) / (double)SCALE_OUT;
				Color c1 = toColorFill(cytoband.getGieStain());
				Color c2 = Color.WHITE;
				GradientPaint gradient = new GradientPaint(Math.round(posX-CHROMOSOME_WIDTH), 0, c2, Math.round(posX-10), 0, c1, true);
				g.setPaint(gradient);
				g.fillRect((int)Math.round(posX), (int)Math.round(start), 30, (int)Math.round(height));
				g.setColor(toColorLine(cytoband.getGieStain()));
				g.drawRect((int)Math.round(posX), (int)Math.round(start), 30, (int)Math.round(height));
			}
			FontMetrics metrics = g.getFontMetrics(font);
			int x = (int)(posX + (CHROMOSOME_WIDTH - metrics.stringWidth(chr.getName())) / 2.0);
			g.setColor(Color.BLACK);
			g.drawString(chr.getName(), x, (int)posY-5);
		}
		return g;
	}
	
	public static Pair<Integer, Integer> transform(int chr, long spot) {
		int posX = (CHROMOSOME_WIDTH+CHROMOSOME_MARGIN)*(chr%COLS_PER_ROW);
		int posY = MARGIN_TOP+(CHROMOSOME_HEIGHT)*(chr/COLS_PER_ROW);
		int delta = (int)(spot / SCALE_OUT);
		return new Pair<Integer, Integer>().setA(posX).setB(posY+delta);
	}
	
	private final static Color toColorFill(String gieStain) {
		switch(gieStain) {
		case "gneg": return new Color(33, 67, 104, (int)(256*0.1));
		case "gpos25": return new Color(33, 67, 104, (int)(256*0.25));
		case "gpos50": return new Color(33, 67, 104, (int)(256*0.50));
		case "gpos75": return new Color(33, 67, 104, (int)(256*0.75));
		case "gpos100": return new Color(33, 67, 104, (int)(256*0.9));
		case "acen": 	return Color.WHITE;
		case "gvar": 	return Color.LIGHT_GRAY;
		case "stalk": 	return Color.DARK_GRAY;
		default: return Color.WHITE;
		}
	}
	
	private final static Color toColorLine(String gieStain) {
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
	
	public final static void main3(String[] args) throws IOException, FontFormatException {
		Map<String, List<Cytoband>> cytobands = new CytobandLoader(new File("./cytoband.txt").toPath()).get().collect(Collectors.groupingBy(Cytoband::getChr));
		Chromosome[] chrs = cytobands.keySet().stream()
		.map(chr->{
			Cytoband[] c = cytobands.get(chr).stream().toArray(Cytoband[]::new);
			long length = Arrays.stream(c).mapToLong(Cytoband::getEnd).max().getAsLong();
			return new Chromosome().setName(chr).setCytobands(c).setLength(length);
		}).sorted().toArray(Chromosome[]::new);

		BufferedImage bi = new BufferedImage(1280, 1024, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setBackground(Color.WHITE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.clearRect(0, 0, 1280, 1024);
		Idiogram.draw(g, chrs);
		ImageIO.write(bi, "png", new File("./output.png"));
	}
	
	public final static void main(String[] args) throws IOException, FontFormatException {
		Map<String, List<Cytoband>> cytobands = new CytobandLoader(new File("./cytoband.txt").toPath()).get().collect(Collectors.groupingBy(Cytoband::getChr));
		Chromosome[] chrs = cytobands.keySet().stream()
		.map(chr->{
			Cytoband[] c = cytobands.get(chr).stream().toArray(Cytoband[]::new);
			long length = Arrays.stream(c).mapToLong(Cytoband::getEnd).max().getAsLong();
			return new Chromosome().setName(chr).setCytobands(c).setLength(length);
		}).sorted().peek(s->System.out.println(s)).toArray(Chromosome[]::new);

		Cychp cychp = new CychpLoader(new File("C:\\Users\\Sangjay\\Downloads\\19ML_0141 BAEK JUN_(CytoScan750K_Array).cy750K.cychp").toPath()).get();
		BufferedImage bi = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setBackground(Color.WHITE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.clearRect(0, 0, 1024, 768);
		Idiogram.draw(g, chrs);
		
		
		CopyNumberSet cns = cychp.getSegments().getCopyNumber();
		if(cns!=null) for(int i = 0; i < cns.getCopyNumberCount(); ++i) {
			CopyNumber cn = cns.getCopyNumber(i);
			if(cn.getState() == CopyNumberState.LOSS /*|| cn.getState() == CopyNumberState.ABSENT*/) {
				int ordinal = cn.getChr()-1;
				if(ordinal > 22) ordinal = ordinal-1;
				Pair<Integer, Integer> pos1 = transform(ordinal, cn.getStart());
				Pair<Integer, Integer> pos2 = transform(ordinal, cn.getStop());
				g.setColor(new Color(189, 26, 55, 200));
				g.fillRect(pos1.getA()+CHROMOSOME_WIDTH+5, pos1.getB(), 11, pos2.getB()-pos1.getB());
				
				if(pos2.getB()-pos1.getB() > 5) {
					g.setColor(Color.WHITE);
					Pair<Integer, Integer> pos3 = transform(ordinal, (cn.getStop()+cn.getStart())/2);
					g.drawChars("▼".toCharArray(), 0, 1, pos1.getA()+CHROMOSOME_WIDTH+4, pos3.getB()+3);
				}
			} else if(cn.getState() == CopyNumberState.GAIN || cn.getState() == CopyNumberState.GAIN2) {
				int ordinal = cn.getChr()-1;
				if(ordinal > 22) ordinal = ordinal-1;
				Pair<Integer, Integer> pos1 = transform(ordinal, cn.getStart());
				Pair<Integer, Integer> pos2 = transform(ordinal, cn.getStop());
				g.setColor(new Color(12, 76, 138, 200));
				g.fillRect(pos1.getA()+CHROMOSOME_WIDTH+5, pos1.getB(), 11, pos2.getB()-pos1.getB());
				if(pos2.getB()-pos1.getB() > 5) {
					g.setColor(Color.WHITE);
					Pair<Integer, Integer> pos3 = transform(ordinal, (cn.getStop()+cn.getStart())/2);
					g.drawChars("▲".toCharArray(), 0, 1, pos1.getA()+CHROMOSOME_WIDTH+4, pos3.getB()+3);
				}
			}
		}
		
		LohSet lohs = cychp.getSegments().getLoh();
		if(lohs!=null) for(int i = 0; i < lohs.getLohCount(); ++i) {
		//	System.out.println(lohs.getLoh(i));
		}
		
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
