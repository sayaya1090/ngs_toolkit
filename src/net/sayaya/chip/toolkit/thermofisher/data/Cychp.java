package net.sayaya.chip.toolkit.thermofisher.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import lombok.Data;
import lombok.experimental.Accessors;
import net.sayaya.chip.toolkit.thermofisher.data.CCBFile.DataGroup;
import net.sayaya.chip.toolkit.thermofisher.data.CCBFile.DataSet;

@Data
@Accessors(chain = true)
public class Cychp {
	private final CCBFile file;
	private final Summary chr;
	private final Segment segments;
	public Cychp(CCBFile file) {
		this.file = file;
		chr = new Summary(file.getDataGroup()[0]);
		segments = new Segment(file.getDataGroup()[3]);
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
	public class ProbeSets {
		public ProbeSets(DataGroup group) {
			
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class AlgorithmData {
		
	}
	
	@Data
	@Accessors(chain = true)
	public class Segment {
		private final DataGroup data;
		private final CopyNumberSet copyNumber;
		private final LohSet loh;
		public Segment(DataGroup data) {
			this.data = data;
			copyNumber = new CopyNumberSet(data.getData()[0]);
			loh = new LohSet(data.getData()[1]);
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
				.setMarker(buffer.getInt())
				.setMarkerDistanceMean(buffer.getInt())
				.setState(buffer.getFloat())
				.setConfidence(buffer.getFloat());
				copynumbers[idx] = value;
			}
			return copynumbers[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class CopyNumber {
		private int id;
		private byte chr;
		private long start;
		private long stop;
		private int marker;
		private int markerDistanceMean;
		private float state;
		private float confidence;
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
				.setMarker(buffer.getInt())
				.setMarkerDistanceMean(buffer.getInt())
				.setLoh(buffer.get())
				.setConfidence(buffer.getFloat());
				lohs[idx] = value;
			}
			return lohs[idx];
		}
	}
	
	@Data
	@Accessors(chain = true)
	public class Loh {
		private int id;
		private byte chr;
		private long start;
		private long stop;
		private int marker;
		private int markerDistanceMean;
		private byte loh;
		private float confidence;
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
}
