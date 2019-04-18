package net.sayaya.chip.toolkit.thermofisher;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.function.Supplier;

import net.sayaya.chip.toolkit.thermofisher.data.CCBFile;
import net.sayaya.chip.toolkit.thermofisher.data.CCBFile.CCBFileHeader;
import net.sayaya.chip.toolkit.thermofisher.data.CCBFile.Column;
import net.sayaya.chip.toolkit.thermofisher.data.CCBFile.DataGroup;
import net.sayaya.chip.toolkit.thermofisher.data.CCBFile.DataSet;

public class CCBFileLoader implements Supplier<CCBFile> {
	private final CCBFile data;
	private final ByteBuffer byteBuffer = ByteBuffer.allocate(1).order(ByteOrder.BIG_ENDIAN);
	private final ByteBuffer integerBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
	public CCBFileLoader(Path cel) {
		try {
			FileChannel cin = FileChannel.open(cel, StandardOpenOption.READ);
			data = new CCBFile(cin);
			read(cin, byteBuffer);									// Magin number(59)
			data.setVersion(readByte(cin));
			int dateGroupLength = readInt(cin);
			read(cin, integerBuffer);								// File Position of Data Group. Not used.
			LinkedList<CCBFileHeader> headers = new LinkedList<>();
			while(true) {
				CCBFileHeader header = new CCBFileHeader();
				headers.add(header);
				header.setHeaderName(readString(cin));
				header.setGuid(readString(cin));
				String date = readWString(cin);
				if(date!=null && !date.trim().isEmpty()) header.setDate(LocalDateTime.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(date)).atZone(ZoneId.of("Asia/Seoul"))));
				header.setLocale(readWString(cin));
				
				int arrayLength = readInt(cin);
				for(int j = 0; j < arrayLength; ++j) {
					String param = readWString(cin);
					ByteBuffer tmpBuffer = ByteBuffer.allocate(readInt(cin)).order(ByteOrder.BIG_ENDIAN);
					read(cin, tmpBuffer);
					String type = readWString(cin);
					String value = null;
					switch(type) {
					case "text/plain": value = new String(tmpBuffer.array(), StandardCharsets.UTF_16);			break;
					case "text/ascii": value = new String(tmpBuffer.array(), StandardCharsets.US_ASCII);		break;
					case "text/x-calvin-integer-8":
					case "text/x-calvin-unsigned-integer-8": value = String.valueOf((int)tmpBuffer.get());		break;
					case "text/x-calvin-integer-16":
					case "text/x-calvin-unsigned-integer-16": value = String.valueOf((int)tmpBuffer.getChar());	break;
					case "text/x-calvin-integer-32":
					case "text/x-calvin-unsigned-integer-32": value = String.valueOf(tmpBuffer.getInt());		break;
					case "text/x-calvin-float": value = String.valueOf(tmpBuffer.getFloat());					break;
					default: System.out.println(type); System.exit(-1);
					}
					header.put(param, value.trim());
				}
				if(readInt(cin) <= 0) break;
			}
			data.setHeaders(headers.stream().toArray(CCBFileHeader[]::new));

			LinkedList<DataGroup> groups = new LinkedList<>();
			for(int p = 0; p < dateGroupLength; ++p) {
				DataGroup group = data.new DataGroup();
				groups.add(group);
				group.setLast(readInt(cin)).setStart(readInt(cin));
				int datasetCnt = readInt(cin);
				group.setName(readWString(cin));
				LinkedList<DataSet> dataset = new LinkedList<>();
				for(int j = 0; j < datasetCnt; ++j) {
					DataSet set = data.new DataSet();
					dataset.add(set);
					set.setStart(readInt(cin)).setLast(readInt(cin)).setName(readWString(cin));
					int arrayLength = readInt(cin);
					for(int k = 0; k < arrayLength; ++k) {
						String param = readWString(cin);
						ByteBuffer tmpBuffer = ByteBuffer.allocate(readInt(cin)).order(ByteOrder.BIG_ENDIAN);
						read(cin, tmpBuffer);
						String type = readWString(cin);
						String value = null;
						switch(type) {
						case "text/plain": value = new String(tmpBuffer.array(), StandardCharsets.UTF_16);			break;
						case "text/ascii": value = new String(tmpBuffer.array(), StandardCharsets.US_ASCII);		break;
						case "text/x-calvin-integer-8":
						case "text/x-calvin-unsigned-integer-8": value = String.valueOf((int)tmpBuffer.get());		break;
						case "text/x-calvin-integer-16":
						case "text/x-calvin-unsigned-integer-16": value = String.valueOf((int)tmpBuffer.getChar());	break;
						case "text/x-calvin-integer-32":
						case "text/x-calvin-unsigned-integer-32": value = String.valueOf(tmpBuffer.getInt());		break;
						case "text/x-calvin-float": value = String.valueOf(tmpBuffer.getFloat());					break;
						default: System.out.println(type); System.exit(-1);
						}
						set.put(param, value.trim());
					}

					int columnLength = readInt(cin);
					LinkedList<Column> columns = new LinkedList<>();
					int frameSize = 0;
					for(int k = 0; k < columnLength; ++k) {
						Column column = data.new Column()
							.setName(readWString(cin))
							.setType(readByte(cin))
							.setSize(readInt(cin));
						columns.add(column);
						frameSize += column.getSize();
					}
					set.setColumns(columns.stream().toArray(Column[]::new)).setFrameSize(frameSize).setRowNum(readInt(cin));
					cin.position(set.getLast());
				}
				group.setData(dataset.stream().toArray(DataSet[]::new));
				if(cin.size()<=cin.position()) break;
			}
			data.setDataGroup(groups.stream().toArray(DataGroup[]::new));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	
	private final byte readByte(FileChannel cin) {
		read(cin, byteBuffer);
		return byteBuffer.get();
	}
	
	private final int readInt(FileChannel cin) {
		read(cin, integerBuffer);
		return integerBuffer.getInt();
	}
	
	private final String readString(FileChannel cin) {
		read(cin, integerBuffer);
		int leng = integerBuffer.getInt();
		ByteBuffer stringBuffer = ByteBuffer.allocate(leng);
		read(cin, stringBuffer);
		return new String(stringBuffer.array(), StandardCharsets.US_ASCII).trim();
	}
	
	private final String readWString(FileChannel cin) {
		read(cin, integerBuffer);
		int leng = integerBuffer.getInt();
		ByteBuffer stringBuffer = ByteBuffer.allocate(leng*2);
		read(cin, stringBuffer);
		return new String(stringBuffer.array(), StandardCharsets.UTF_16).trim();
	}

	@Override
	public CCBFile get() {
		return data;
	}
	
	public final static void main(String[] args) {
		CCBFileLoader instance = new CCBFileLoader(new File("C:\\Users\\Sangjay\\Downloads\\19MA_0201 JEON JAE HO_(CytoScan750K_Array).cy750K.cychp").toPath());
		System.out.println(instance.get());
	}
}
