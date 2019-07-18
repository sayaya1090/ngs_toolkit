package net.sayaya.chip.toolkit.thermofisher.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import lombok.Data;
import lombok.experimental.Accessors;
import net.sayaya.chip.toolkit.thermofisher.data.CCBFile.DataGroup;
import net.sayaya.chip.toolkit.thermofisher.data.CCBFile.DataSet;

@Data
@Accessors(chain = true)
public class Cychp {
	private final CCBFile file;
	private final Summary chr;
	private final ProbeSet probes;
	private final AlgorithmData algorithmData;
	private final Segment segments;
	private final Genotyping genotyping;
	public Cychp(CCBFile file) {
		this.file = file;
		DataGroup[] group = file.getDataGroup();
		chr = Arrays.stream(group).filter(d->"Chromosomes".equals(d.getName())).findAny().map(Summary::new).orElse(null);
		probes = Arrays.stream(group).filter(d->"ProbeSets".equals(d.getName())).findAny().map(ProbeSet::new).orElse(null);
		algorithmData = Arrays.stream(group).filter(d->"AlgorithmData".equals(d.getName())).findAny().map(AlgorithmData::new).orElse(null);
		segments = Arrays.stream(group).filter(d->"Segments".equals(d.getName())).findAny().map(Segment::new).orElse(null);
		genotyping = Arrays.stream(group).filter(d->"Genotyping".equals(d.getName())).findAny().map(Genotyping::new).orElse(null);
	}
	
	@Data
	@Accessors(chain = true)
	public class Summary {
		private final DataSet data;
		private final Chromosome[] chrs;
		private final ByteBuffer buffer;
		public Summary(DataGroup group) {
			this.data = group.getData()[0];
			chrs = new Chromosome[data.getRowNum()];
			buffer = ByteBuffer.allocate(data.getFrameSize());
		}
		
		public int getChromosomeCount() {
			return chrs.length;
		}
		public Chromosome getChromosome(int idx) throws IOException {
			if(idx >= getChromosomeCount()) return null;
			if(chrs[idx]==null) {
				data.getStart();
				file.getChannel().position(data.getStart()+data.getFrameSize()*idx);
				read(file.getChannel(), buffer);
				Chromosome chr = new Chromosome().setId(buffer.get());
				int len = buffer.getInt();
				byte[] str = new byte[len];
				buffer.get(str, 0, len);
				chr.setLabel(new String(str).trim())
				.setStart(buffer.getInt())
				.setMarkerCount(buffer.getInt())
				.setSignalMin(buffer.getFloat())
				.setSignalMax(buffer.getFloat())
				.setCnstateMedian(buffer.getFloat())
				.setFreqHom(buffer.getFloat())
				.setFreqHet(buffer.getFloat())
				.setMosaicism(buffer.getFloat())
				.setLoh(buffer.getFloat())
				.setSignalMedian(buffer.getFloat());
				chrs[idx] = chr;
			}
			return chrs[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public final static class Chromosome {
		private byte id;
		private String label;
		private long start;
		private int markerCount;
		private float signalMin;
		private float signalMax;
		private float cnstateMedian;
		private float freqHom;
		private float freqHet;
		private float mosaicism;
		private float loh;
		private float signalMedian;
	}
	
	@Data
	@Accessors(chain = true)
	public class ProbeSet {
		private final DataGroup data;
		private final ProbeCopyNumberSet probeCopyNumber;
		private final ProbeAllelePeakSet probeAllelePeak;
		public ProbeSet(DataGroup data) {
			this.data = data;
			DataSet[] dataset = data.getData();
			probeCopyNumber = Arrays.stream(dataset).filter(d->"CopyNumber".equals(d.getName())).findAny().map(ProbeCopyNumberSet::new).orElse(null);
			probeAllelePeak = Arrays.stream(dataset).filter(d->"AllelePeaks".equals(d.getName())).findAny().map(ProbeAllelePeakSet::new).orElse(null);
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class ProbeCopyNumberSet {
		private final DataSet data;
		private final ProbeCopyNumber[] probes;
		private final ByteBuffer buffer;
		public ProbeCopyNumberSet(DataSet data) {
			this.data = data;
			probes = new ProbeCopyNumber[data.getRowNum()];
			buffer = ByteBuffer.allocate(data.getFrameSize());
		}
		
		public int getProbeCount() {
			return probes.length;
		}
		public ProbeCopyNumber getProbe(int idx) throws IOException {
			if(idx >= getProbeCount()) return null;
			if(probes[idx]==null) {
				data.getStart();
				file.getChannel().position(data.getStart()+data.getFrameSize()*idx);
				read(file.getChannel(), buffer);
				ProbeCopyNumber value = new ProbeCopyNumber();
				int len = buffer.getInt();
				byte[] str = new byte[len];
				buffer.get(str, 0, len);
				value.setName(new String(str).trim())
				.setChr(buffer.get())
				.setPosition(buffer.getInt())
				.setLog2Ratio(buffer.getFloat())
				.setLog2RatioWeighted(buffer.getFloat())
				.setSmoothSignal(buffer.getFloat());
				if(value.getName().startsWith("C")) value.setType(ProbeType.COPYNUMBER);
				else if(value.getName().startsWith("S")) value.setType(ProbeType.SNP);
				probes[idx] = value;
			}
			return probes[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class ProbeCopyNumber {
		private ProbeType type;
		private String name;
		private byte chr;
		private long position;
		private float log2Ratio;
		private float log2RatioWeighted;
		private float smoothSignal;
	}
	
	public enum ProbeType {
		COPYNUMBER, SNP
	}
	
	@Data
	@Accessors(chain = true)
	public class ProbeAllelePeakSet {
		private final DataSet data;
		private final ProbeAllelePeak[] probes;
		private final ByteBuffer buffer;
		public ProbeAllelePeakSet(DataSet data) {
			this.data = data;
			probes = new ProbeAllelePeak[data.getRowNum()];
			buffer = ByteBuffer.allocate(data.getFrameSize());
		}
		
		public int getProbeCount() {
			return probes.length;
		}
		public ProbeAllelePeak getProbe(int idx) throws IOException {
			if(idx >= getProbeCount()) return null;
			if(probes[idx]==null) {
				data.getStart();
				file.getChannel().position(data.getStart()+data.getFrameSize()*idx);
				read(file.getChannel(), buffer);
				ProbeAllelePeak value = new ProbeAllelePeak();
				int len = buffer.getInt();
				byte[] str = new byte[len];
				buffer.get(str, 0, len);
				value.setName(new String(str).trim())
				.setChr(buffer.get())
				.setPosition(buffer.getInt())
				.setPeak0(buffer.getInt())
				.setPeak1(buffer.getInt());
				if(value.getName().startsWith("C")) value.setType(ProbeType.COPYNUMBER);
				else if(value.getName().startsWith("S")) value.setType(ProbeType.SNP);
				probes[idx] = value;
			}
			return probes[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class ProbeAllelePeak {
		private ProbeType type;
		private String name;
		private byte chr;
		private long position;
		private long peak0;
		private long peak1;
	}
	
	@Data
	@Accessors(chain = true)
	public class AlgorithmData {
		private final DataSet data;
		private final MarkerABSignal[] signals;
		private final ByteBuffer buffer;
		private boolean hasSignalA = false;
		private boolean hasSignalB = false;
		public AlgorithmData(DataGroup data) {
			DataSet[] dataset = data.getData();
			this.data = Arrays.stream(dataset).filter(d->"MarkerABSignal".equals(d.getName())).findAny().orElse(null);
			assert data!=null;
			signals = new MarkerABSignal[this.data.getRowNum()];
			buffer = ByteBuffer.allocate(this.data.getFrameSize());
			hasSignalA = Arrays.stream(this.data.getColumns()).filter(c->"SignalA".equals(c.getName())).findAny().isPresent();
			hasSignalB = Arrays.stream(this.data.getColumns()).filter(c->"SignalB".equals(c.getName())).findAny().isPresent();
		}
		
		public int getMarkerABSignalCount() {
			return signals.length;
		}
		public MarkerABSignal getMarkerABSignal(int idx) throws IOException {
			if(idx >= getMarkerABSignalCount()) return null;
			if(signals[idx]==null) {
				data.getStart();
				file.getChannel().position(data.getStart()+data.getFrameSize()*idx);
				read(file.getChannel(), buffer);
				MarkerABSignal value = new MarkerABSignal()
				.setIndex(buffer.getInt());
				if(hasSignalA) value.setSignalA(buffer.getFloat());
				if(hasSignalB) value.setSignalB(buffer.getFloat());
				value.setSCAR(buffer.getFloat());
				signals[idx] = value;
			}
			return signals[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public final static class MarkerABSignal {
		private long index;
		private float signalA;
		private float signalB;
		private float SCAR;
	}
	
	@Data
	@Accessors(chain = true)
	public class Segment {
		private final DataGroup data;
		private final CopyNumberSet copyNumber;
		private final LohSet loh;
		private final CNNeutralLohSet cNNeutralLoh;
		private final NormalDiploidSet diploid;
		private final MosaicismSet mosaicism;
		public Segment(DataGroup data) {
			this.data = data;
			DataSet[] dataset = data.getData();
			copyNumber = Arrays.stream(dataset).filter(d->"CN".equals(d.getName())).findAny().map(CopyNumberSet::new).orElse(null);
			loh = Arrays.stream(dataset).filter(d->"LOH".equals(d.getName())).findAny().map(LohSet::new).orElse(null);
			cNNeutralLoh = Arrays.stream(dataset).filter(d->"CNNeutralLOH".equals(d.getName())).findAny().map(CNNeutralLohSet::new).orElse(null);
			diploid = Arrays.stream(dataset).filter(d->"NormalDiploid".equals(d.getName())).findAny().map(NormalDiploidSet::new).orElse(null);
			mosaicism = Arrays.stream(dataset).filter(d->"Mosaicism".equals(d.getName())).findAny().map(MosaicismSet::new).orElse(null);
		}
	}

	public interface Filterable {

		int getMarkerCount();
		long getStart();
		long getStop();
		default long getSize() {
			return getStop() - getStart();
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class CopyNumberSet {
		private final DataSet data;
		private final CopyNumber[] copynumbers;
		private final ByteBuffer buffer;
		public CopyNumberSet(DataSet data) {
			this.data = data;
			copynumbers = new CopyNumber[data.getRowNum()];
			buffer = ByteBuffer.allocate(data.getFrameSize());
		}
		
		public int getCopyNumberCount() {
			return copynumbers.length;
		}
		public CopyNumber getCopyNumber(int idx) throws IOException {
			if(idx >= getCopyNumberCount()) return null;
			if(copynumbers[idx]==null) {
				data.getStart();
				file.getChannel().position(data.getStart()+data.getFrameSize()*idx);
				read(file.getChannel(), buffer);
				CopyNumber value = new CopyNumber().setId(buffer.getInt())
				.setChr(buffer.get())
				.setStart(buffer.getInt())
				.setStop(buffer.getInt())
				.setMarkerCount(buffer.getInt())
				.setMarkerDistanceMean(buffer.getInt());
				int state = (int)buffer.getFloat();
				value.setState(CopyNumberState.values()[state]).setConfidence(buffer.getFloat());
				copynumbers[idx] = value;
			}
			return copynumbers[idx];
		}
		public CopyNumber[] getCopynumbers() {
			for(int i = 0; i < getCopyNumberCount(); ++i) {
				try {
					getCopyNumber(i);
				} catch(Exception e) {}
			}
			return copynumbers;
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class CopyNumber implements Filterable {
		private int id;
		private byte chr;
		private long start;
		private long stop;
		private int markerCount;
		private int markerDistanceMean;
		private CopyNumberState state;
		private float confidence;
	}
	
	public enum CopyNumberState {
		ABSENT, LOSS, NORMAL, GAIN, GAIN2
	}
	
	@Data
	@Accessors(chain = true)
	public class LohSet {
		private final DataSet data;
		private final Loh[] lohs;
		private final ByteBuffer buffer;
		public LohSet(DataSet data) {
			this.data = data;
			lohs = new Loh[data.getRowNum()];
			buffer = ByteBuffer.allocate(data.getFrameSize());
		}
		
		public int getLohCount() {
			return lohs.length;
		}
		public Loh getLoh(int idx) throws IOException {
			if(idx >= getLohCount()) return null;
			if(lohs[idx]==null) {
				data.getStart();
				file.getChannel().position(data.getStart()+data.getFrameSize()*idx);
				read(file.getChannel(), buffer);
				Loh value = new Loh().setId(buffer.getInt())
				.setChr(buffer.get())
				.setStart(buffer.getInt())
				.setStop(buffer.getInt())
				.setMarkerCount(buffer.getInt())
				.setMarkerDistanceMean(buffer.getInt());
				if(buffer.get()==0)value.setLoh(Option.NOT_EXIST);
				else value.setLoh(Option.EXIST);
				value.setConfidence(buffer.getFloat());
				lohs[idx] = value;
			}
			return lohs[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class Loh implements Filterable {
		private int id;
		private byte chr;
		private long start;
		private long stop;
		private int markerCount;
		private int markerDistanceMean;
		private Option loh;
		private float confidence;
	}
	
	public enum Option {
		NOT_EXIST, EXIST
	}
	
	@Data
	@Accessors(chain = true)
	public class CNNeutralLohSet {
		private final DataSet data;
		private final CNNeutralLoh[] lohs;
		private final ByteBuffer buffer;
		public CNNeutralLohSet(DataSet data) {
			this.data = data;
			lohs = new CNNeutralLoh[data.getRowNum()];
			buffer = ByteBuffer.allocate(data.getFrameSize());
		}
		
		public int getCNNeutralLohCount() {
			return lohs.length;
		}
		public CNNeutralLoh getCNNeutralLoh(int idx) throws IOException {
			if(idx >= getCNNeutralLohCount()) return null;
			if(lohs[idx]==null) {
				data.getStart();
				file.getChannel().position(data.getStart()+data.getFrameSize()*idx);
				read(file.getChannel(), buffer);
				CNNeutralLoh value = new CNNeutralLoh().setId(buffer.getInt())
				.setChr(buffer.get())
				.setStart(buffer.getInt())
				.setStop(buffer.getInt())
				.setMarkerCount(buffer.getInt())
				.setMarkerDistanceMean(buffer.getInt());
				if(buffer.get()==0)value.setLoh(Option.NOT_EXIST);
				else value.setLoh(Option.EXIST);
				value.setConfidence(buffer.getFloat());
				lohs[idx] = value;
			}
			return lohs[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class CNNeutralLoh implements Filterable {
		private int id;
		private byte chr;
		private long start;
		private long stop;
		private int markerCount;
		private int markerDistanceMean;
		private Option loh;
		private float confidence;
	}
	
	@Data
	@Accessors(chain = true)
	public class NormalDiploidSet {
		private final DataSet data;
		private final NormalDiploid[] diploid;
		private final ByteBuffer buffer;
		public NormalDiploidSet(DataSet data) {
			this.data = data;
			diploid = new NormalDiploid[data.getRowNum()];
			buffer = ByteBuffer.allocate(data.getFrameSize());
		}
		
		public int getNormalDiploidCount() {
			return diploid.length;
		}
		public NormalDiploid getNormalDiploid(int idx) throws IOException {
			if(idx >= getNormalDiploidCount()) return null;
			if(diploid[idx]==null) {
				data.getStart();
				file.getChannel().position(data.getStart()+data.getFrameSize()*idx);
				read(file.getChannel(), buffer);
				NormalDiploid value = new NormalDiploid().setId(buffer.getInt())
				.setChr(buffer.get())
				.setStart(buffer.getInt())
				.setStop(buffer.getInt())
				.setMarkerCount(buffer.getInt())
				.setMarkerDistanceMean(buffer.getInt());
				if(buffer.get()==0)value.setDiploid(Option.NOT_EXIST);
				else value.setDiploid(Option.EXIST);
				value.setConfidence(buffer.getFloat());
				diploid[idx] = value;
			}
			return diploid[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class NormalDiploid implements Filterable {
		private int id;
		private byte chr;
		private long start;
		private long stop;
		private int markerCount;
		private int markerDistanceMean;
		private Option diploid;
		private float confidence;
	}
	
	@Data
	@Accessors(chain = true)
	public class MosaicismSet {
		private final DataSet data;
		private final Mosaicism[] diploid;
		private final ByteBuffer buffer;
		public MosaicismSet(DataSet data) {
			this.data = data;
			diploid = new Mosaicism[data.getRowNum()];
			buffer = ByteBuffer.allocate(data.getFrameSize());
		}
		
		public int getMosaicismCount() {
			return diploid.length;
		}
		public Mosaicism getMosaicism(int idx) throws IOException {
			if(idx >= getMosaicismCount()) return null;
			if(diploid[idx]==null) {
				data.getStart();
				file.getChannel().position(data.getStart()+data.getFrameSize()*idx);
				read(file.getChannel(), buffer);
				Mosaicism value = new Mosaicism().setId(buffer.getInt())
				.setChr(buffer.get())
				.setStart(buffer.getInt())
				.setStop(buffer.getInt())
				.setMarkerCount(buffer.getInt())
				.setMarkerDistanceMean(buffer.getInt());
				if(buffer.get()==0)value.setMosaicism(Option.NOT_EXIST);
				else value.setMosaicism(Option.EXIST);
				value.setConfidence(buffer.getFloat())
				.setState(buffer.getFloat())
				.setMixture(buffer.getFloat());
				diploid[idx] = value;
			}
			return diploid[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class Mosaicism implements Filterable {
		private int id;
		private byte chr;
		private long start;
		private long stop;
		private int markerCount;
		private int markerDistanceMean;
		private Option mosaicism;
		private float confidence;
		private float state;
		private float mixture;
	}
	
	private final static boolean read(FileChannel channel, ByteBuffer buffer) {
		buffer.clear();
		try {
		int read = channel.read(buffer);
		buffer.flip();
		if(read == buffer.capacity()) return true; 
		return false;
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class Genotyping {
		private final DataSet data;
		private final Genotype[] genotypes;
		private final ByteBuffer buffer;
		public Genotyping(DataGroup group) {
			this.data = group.getData()[0];
			genotypes = new Genotype[data.getRowNum()];
			buffer = ByteBuffer.allocate(data.getFrameSize());
		}
		
		public int getGenotypeCount() {
			return genotypes.length;
		}
		public Genotype getGenotype(int idx) throws IOException {
			if(idx >= getGenotypeCount()) return null;
			if(genotypes[idx]==null) {
				data.getStart();
				file.getChannel().position(data.getStart()+data.getFrameSize()*idx);
				read(file.getChannel(), buffer);
				Genotype value = new Genotype().setId(buffer.getInt())
				.setCallingValue(buffer.get())
				.setConfidence(buffer.getFloat())
				.setCallingValueForce(buffer.get())
				.setSignalA(buffer.getFloat())
				.setSignalB(buffer.getFloat())
				.setStrength(buffer.getFloat())
				.setContrast(buffer.getFloat());
				
				byte state = value.getCallingValue();
				if(state == 6) value.setCall(GenotypeCalling.WILD);
				else if(state == 7) value.setCall(GenotypeCalling.HOMO);
				else if(state == 8) value.setCall(GenotypeCalling.HETERO);
				else value.setCall(GenotypeCalling.NO_CALL);
				
				byte state2 = value.getCallingValueForce();
				if(state2 == 6) value.setCallForce(GenotypeCalling.WILD);
				else if(state2 == 7) value.setCallForce(GenotypeCalling.HOMO);
				else if(state2 == 8) value.setCallForce(GenotypeCalling.HETERO);
				else value.setCallForce(GenotypeCalling.NO_CALL);
				genotypes[idx] = value;
			}
			return genotypes[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class Genotype {
		private int id;
		private GenotypeCalling call;
		private byte callingValue;
		private float confidence;
		private GenotypeCalling callForce;
		private byte callingValueForce;
		private float signalA;
		private float signalB;
		private float strength;
		private float contrast;
	}
	
	public enum GenotypeCalling {
		NO_CALL, WILD, HETERO, HOMO;
	}
}
