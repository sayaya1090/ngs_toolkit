package net.sayaya.chip.toolkit.thermofisher.data;

import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CCBFile {
	private final FileChannel channel;
	private int version;
	private int dataGroupNum;
	private CCBFileHeader[] headers;
	private DataGroup[] dataGroup;
	public CCBFile(FileChannel channel) {
		this.channel = channel;
	}
	@Data
	@Accessors(chain = true)
	public final static class CCBFileHeader {
		private String headerName;
		private String guid;
		private LocalDateTime date;
		private String locale;
		private Map<String, String> parameters = new HashMap<>();
		public CCBFileHeader put(String key, String value) {
			parameters.put(key, value);
			return this;
		}
		public String get(String key) {
			return parameters.get(key);
		}
	}
	
	@Data
	@Accessors(chain = true)
	public final class DataGroup {
		private long start;
		private long last;
		private String name;
		private DataSet[] data;
	}
	
	@Data
	@Accessors(chain = true)
	public final class DataSet {
		private long start;
		private long last;
		private String name;
		private Map<String, String> parameters = new HashMap<>();
		private Column[] columns;
		private int rowNum;
		private int frameSize;
		public DataSet put(String key, String value) {
			parameters.put(key, value);
			return this;
		}
		public String get(String key) {
			return parameters.get(key);
		}
	}
	
	@Data
	@Accessors(chain = true)
	public final class Column {
		private String name;
		private byte type;
		private int size;
	}
}
