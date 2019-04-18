package net.sayaya.chip.toolkit.thermofisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

import net.sayaya.chip.toolkit.thermofisher.data.Cychp;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.CopyNumberSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.LohSet;

public class CychpLoader implements Supplier<Cychp> {
	private final Cychp file;
	public CychpLoader(Path cychp) {
		this.file = new Cychp(new CCBFileLoader(cychp).get());
	}

	@Override
	public Cychp get() {
		return file;
	}
	
	public final static void main(String[] args) throws IOException {
		CychpLoader instance = new CychpLoader(new File("C:\\Users\\Sangjay\\Downloads\\19MA_0201 JEON JAE HO_(CytoScan750K_Array).cy750K.cychp").toPath());
		System.out.println(instance.get().getChr().getChromosome(22));
		CopyNumberSet cns = instance.get().getSegments().getCopyNumber();
		for(int i = 0; i < cns.getCopyNumberCount(); ++i) {
			System.out.println(cns.getCopyNumber(i));
		}
		
		LohSet lohs = instance.get().getSegments().getLoh();
		for(int i = 0; i < lohs.getLohCount(); ++i) {
			System.out.println(lohs.getLoh(i));
		}
	}
}
