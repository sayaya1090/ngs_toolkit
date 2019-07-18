package net.sayaya.chip.toolkit.thermofisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

import net.sayaya.chip.toolkit.thermofisher.data.Cychp;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.AlgorithmData;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.CNNeutralLohSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.CopyNumberSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.Genotyping;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.LohSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.MosaicismSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.NormalDiploidSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.ProbeAllelePeakSet;
import net.sayaya.chip.toolkit.thermofisher.data.Cychp.ProbeCopyNumberSet;

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
		CychpLoader instance = new CychpLoader(new File("C:\\Users\\Sangjay\\git\\ngs_toolkit\\20190715_161937_002_2.cyto.dxchp").toPath());
		System.out.println(instance.get().getChr().getChromosome(22));
		ProbeCopyNumberSet pcns = instance.get().getProbes().getProbeCopyNumber();
		if(pcns!=null) for(int i = 0; i < 10; ++i) {
			System.out.println(pcns.getProbe(i));
		}
		ProbeAllelePeakSet paps = instance.get().getProbes().getProbeAllelePeak();
		if(paps!=null) for(int i = 0; i < 10; ++i) {
			System.out.println(paps.getProbe(i));
		}
		
		AlgorithmData algorithm = instance.get().getAlgorithmData();
		if(algorithm!=null) for(int i = 0; i < 10; ++i) {
			System.out.println(algorithm.getMarkerABSignal(i));
		}
		
		CopyNumberSet cns = instance.get().getSegments().getCopyNumber();
		if(cns!=null) for(int i = 0; i < cns.getCopyNumberCount(); ++i) {
			System.out.println(cns.getCopyNumber(i));
		}
		
		LohSet lohs = instance.get().getSegments().getLoh();
		if(lohs!=null) for(int i = 0; i < lohs.getLohCount(); ++i) {
			System.out.println(lohs.getLoh(i));
		}
		
		CNNeutralLohSet lohs2 = instance.get().getSegments().getCNNeutralLoh();
		if(lohs2!=null) for(int i = 0; i < lohs2.getCNNeutralLohCount(); ++i) {
			System.out.println(lohs2.getCNNeutralLoh(i));
		}
		
		NormalDiploidSet diploid = instance.get().getSegments().getDiploid();
		if(diploid!=null) for(int i = 0; i < diploid.getNormalDiploidCount(); ++i) {
			System.out.println(diploid.getNormalDiploid(i));
		}
		
		MosaicismSet mosaicism = instance.get().getSegments().getMosaicism();
		if(mosaicism!=null) for(int i = 0; i < mosaicism.getMosaicismCount(); ++i) {
			System.out.println(mosaicism.getMosaicism(i));
		}
		Genotyping gt = instance.get().getGenotyping();
		if(gt!=null) for(int i = 0; i < gt.getGenotypeCount(); ++i) {
		//	System.out.println(gt.getGenotype(i));
		}
	}
}
