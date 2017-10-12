package net.sayaya.ngs.toolkit.data;

public class Read {
	private String id;
	private String header;
	private String read;
	private String qual;

	public Read(String id, String read, String quality) {
		this.id = id.substring(0, id.indexOf(' '));
		header = id;
		this.read = read;
		this.qual = quality;
	}
	
	public String getId() {
		return id;
	}
	
	public String getHeader() {
		return header;
	}

	public String getRead() {
		return read;
	}

	public String getQuality() {
		return qual;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Read:")
			.append("id=").append(id)
			.append(",header=").append(header)
			.append(",read=").append(read)
			.append(",qual=").append(qual)
			.append("]")
		;
		return sb.toString();
	}
}
