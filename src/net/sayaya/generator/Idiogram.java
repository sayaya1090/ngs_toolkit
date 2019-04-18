package net.sayaya.generator;

import java.awt.Color;
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

import net.sayaya.data.Chromosome;
import net.sayaya.data.Cytoband;
import net.sayaya.util.CytobandLoader;

public class Idiogram {
	public static Graphics2D draw(Graphics2D g, Chromosome... chrs) {
		for(int i = 0; i < chrs.length; ++i) {
			int posX = 40*i;
			int posY = 100;
			Chromosome chr = chrs[i];
			Cytoband[] cytobands = chr.getCytobands();
			for(int j = 0; j < cytobands.length; ++j) {
				Cytoband cytoband = cytobands[j];
				int height = (int) ((cytoband.getEnd() - cytoband.getStart()) / 400000);
				Color c1 = toColorFill(cytoband.getGieStain());
				Color c2 = Color.WHITE;
				GradientPaint gradient = new GradientPaint(posX-30, 0, c2, posX-10, 0, c1, true);
				g.setPaint(gradient);
				g.fillRect(posX, posY, 30, height);
				g.setColor(toColorLine(cytoband.getGieStain()));
				g.drawRect(posX, posY, 30, height);
				posY += height;
			}
		}
		return g;
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
		switch(gieStain) {
		case "gneg": return Color.DARK_GRAY;
		case "gpos25": return Color.DARK_GRAY;
		case "gpos50": return Color.DARK_GRAY;
		case "gpos75": return Color.DARK_GRAY;
		case "gpos100": return Color.DARK_GRAY;
		case "acen":
		case "gvar":
		case "stalk": 	return Color.DARK_GRAY;
		default: return Color.WHITE;
		}
	}
	
	public final static void main(String[] args) throws IOException {
		Map<String, List<Cytoband>> cytobands = new CytobandLoader(new File("./cytoband.txt").toPath()).get().collect(Collectors.groupingBy(Cytoband::getChr));
		Chromosome[] chrs = cytobands.keySet().stream()
		.map(chr->{
			Cytoband[] c = cytobands.get(chr).stream().toArray(Cytoband[]::new);
			long length = Arrays.stream(c).mapToLong(Cytoband::getEnd).max().getAsLong();
			return new Chromosome().setName(chr).setCytobands(c).setLength(length);
		}).sorted().peek(s->System.out.println(s)).toArray(Chromosome[]::new);

		BufferedImage bi = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setBackground(Color.WHITE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.clearRect(0, 0, 1024, 768);
		Idiogram.draw(g, chrs);
		ImageIO.write(bi, "png", new File("./output.png"));
	}
}
