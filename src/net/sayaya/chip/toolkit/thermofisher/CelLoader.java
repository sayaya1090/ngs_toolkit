package net.sayaya.chip.toolkit.thermofisher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.sayaya.chip.toolkit.thermofisher.data.Cel;
import net.sayaya.ngs.toolkit.illumina.SAVLoader;
import net.sayaya.ngs.toolkit.illumina.data.TileMetricsInfo;

public class CelLoader implements Supplier<Cel> {
	private final Cel data = new Cel();
	public CelLoader(Path cel) {
		try {
			FileChannel cin = FileChannel.open(cel, StandardOpenOption.READ);
			ByteBuffer byteReader = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN);
			ByteBuffer integerReader = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
			read(cin, byteReader);
			System.out.println((int)byteReader.get());
			read(cin, byteReader);
			data.setVersion((int)byteReader.get());
			System.out.println(data.getVersion());
			read(cin, integerReader);
			data.setDataGroupNum(integerReader.getInt());
			System.out.println(data.getDataGroupNum());
			read(cin, integerReader);
			read(cin, integerReader);
			ByteBuffer headerReader = ByteBuffer.allocate(2048).order(ByteOrder.LITTLE_ENDIAN);
			read(cin, headerReader);
			String header = new String(headerReader.array(), StandardCharsets.US_ASCII);
			System.out.println(header);
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

	@Override
	public Cel get() {
		return data;
	}
	
	public final static void main(String[] args) {
		CelLoader instance = new CelLoader(new File("C:\\Users\\Sangjay\\Downloads\\19MA_0201 JEON JAE HO_(CytoScan750K_Array).CEL").toPath());
	}

	public static void main(String[] args) {
		StandardCharsets;
	}
}
