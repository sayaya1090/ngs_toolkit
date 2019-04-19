package net.sayaya.data;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.Data;
import lombok.experimental.Accessors;
import net.sayaya.chip.toolkit.thermofisher.CychpLoader;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.ProbeAllelePeak;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.ProbeAllelePeakSet;

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
	
	public final static void main(String[] args) throws IOException {
		BufferedImage bi = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setBackground(Color.WHITE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.clearRect(0, 0, 1024, 768);
		
		CychpLoader instance = new CychpLoader(new File("C:\\Users\\Sangjay\\Downloads\\19ML_0141 BAEK JUN_(CytoScan750K_Array).cy750K.cychp").toPath());
		ProbeAllelePeakSet paps = instance.get().getProbes().getProbeAllelePeak();
		int SCALE = 3000000;
		g.setColor(Color.blue);
		int prev = 0;
		int prevChr = 0;
		int start = 0;
		if(paps!=null) for(int i = 0; i < paps.getProbeCount(); ++i) {
			ProbeAllelePeak probe = paps.getProbe(i);
			// if(probe.getChr() != 5) continue;
			int x = (int)(probe.getPosition() / SCALE);
			int y = (int) probe.getPeak0();
			if(prevChr!=probe.getChr()) {
				start += prev;
				System.out.println(probe.getChr() + ":"  + start);
			}
			prev = x;
			prevChr = probe.getChr();
			g.drawArc(start+x, y, 1, 1, 0, 360);
		}
		
		
		
		ImageIO.write(bi, "png", new File("./signal.png"));
	}
}
