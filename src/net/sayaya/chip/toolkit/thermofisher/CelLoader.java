package net.sayaya.chip.toolkit.thermofisher;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

import net.sayaya.chip.toolkit.thermofisher.data.Cel;
import net.sayaya.util.ByteToHex;

public class CelLoader implements Supplier<Cel> {
	private final Cel data = new Cel();
	public CelLoader(Path cel) {
		try {
			FileChannel cin = FileChannel.open(cel, StandardOpenOption.READ);
			ByteBuffer byteBuffer = ByteBuffer.allocate(1).order(ByteOrder.BIG_ENDIAN);
			ByteBuffer charBuffer = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN);
			ByteBuffer integerBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
			ByteBuffer longBuffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
			ByteBuffer stringBuffer = null;
			
			read(cin, byteBuffer);
			System.out.println("Magic Number:"+(int)byteBuffer.get());
			read(cin, byteBuffer);
			data.setVersion((int)byteBuffer.get());
			System.out.println("Version:" + data.getVersion());
			read(cin, integerBuffer);
			data.setDataGroupNum(integerBuffer.getInt());
			System.out.println("Number of Data Groups:"+data.getDataGroupNum());
			read(cin, integerBuffer);
			System.out.println("File Position of Data Group:"+integerBuffer.getInt());

			System.out.println();
			while(true) {
				read(cin, integerBuffer);
				int leng = integerBuffer.getInt();
				stringBuffer = ByteBuffer.allocate(leng).order(ByteOrder.BIG_ENDIAN);
				read(cin, stringBuffer);
				String str = new String(stringBuffer.array(), StandardCharsets.US_ASCII);
				System.out.println("String:" + str);
				
				read(cin, integerBuffer);
				leng = integerBuffer.getInt();
				stringBuffer = ByteBuffer.allocate(leng).order(ByteOrder.BIG_ENDIAN);
				read(cin, stringBuffer);
				str = new String(stringBuffer.array(), StandardCharsets.US_ASCII);
				System.out.println("GUID:" + str);
				
				read(cin, integerBuffer);
				leng = integerBuffer.getInt();
				stringBuffer = ByteBuffer.allocate(leng*2).order(ByteOrder.BIG_ENDIAN);
				read(cin, stringBuffer);
				str = new String(stringBuffer.array(), StandardCharsets.US_ASCII);
				System.out.println("DATETIME:" + str);


				read(cin, integerBuffer);
				leng = integerBuffer.getInt();
				stringBuffer = ByteBuffer.allocate(leng*2).order(ByteOrder.BIG_ENDIAN);
				read(cin, stringBuffer);
				str = new String(stringBuffer.array(), StandardCharsets.UTF_16);
				System.out.println("Locale:" + str);
				
				
				read(cin, integerBuffer);
				int arrayLength = integerBuffer.getInt();
				System.out.println("Value length:" + arrayLength);
				for(int j = 0; j < arrayLength; ++j) {
					read(cin, integerBuffer);
					leng = integerBuffer.getInt();
					stringBuffer = ByteBuffer.allocate(leng*2).order(ByteOrder.BIG_ENDIAN);
					read(cin, stringBuffer);
					str = new String(stringBuffer.array(), StandardCharsets.UTF_16);
					System.out.println("\tparam:" + str);
					
					read(cin, integerBuffer);
					leng = integerBuffer.getInt();
					ByteBuffer tmpBuffer = ByteBuffer.allocate(leng).order(ByteOrder.BIG_ENDIAN);
					read(cin, tmpBuffer);
					
					read(cin, integerBuffer);
					leng = integerBuffer.getInt();
					stringBuffer = ByteBuffer.allocate(leng*2).order(ByteOrder.BIG_ENDIAN);
					read(cin, stringBuffer);
					str = new String(stringBuffer.array(), StandardCharsets.UTF_16);
					System.out.println("\ttype:" + str);
					
					if("text/plain".equals(str)) {
						System.out.println("\tvalue:" + new String(tmpBuffer.array(), StandardCharsets.UTF_16));
					} else if("text/ascii".equals(str)) {
						System.out.println("\tvalue:" + new String(tmpBuffer.array(), StandardCharsets.US_ASCII));
					} else if("text/x-calvin-integer-8".equals(str) || "text/x-calvin-unsigned-integer-8".equals(str)) {
						System.out.println("\tvalue:" + tmpBuffer.get());
					} else if("text/x-calvin-integer-16".equals(str) || "text/x-calvin-unsigned-integer-16".equals(str)) {
						System.out.println("\tvalue:" + tmpBuffer.getChar());
					} else if("text/x-calvin-integer-32".equals(str) || "text/x-calvin-unsigned-integer-32".equals(str)) {
						System.out.println("\tvalue:" + tmpBuffer.getInt());
					} else if("text/x-calvin-float".equals(str)) {
						System.out.println("\tvalue:" + tmpBuffer.getFloat());
					}
				}
				read(cin, integerBuffer);
				int idx = integerBuffer.getInt();
				if(idx <= 0) break;
			}
			
		//	chk(cin);
			read(cin, integerBuffer);
			System.out.println("Next Data Pos:" + integerBuffer.getInt());
			read(cin, integerBuffer);
			System.out.println("Data Pos:" + integerBuffer.getInt());
			read(cin, integerBuffer);
			int datasetCnt = integerBuffer.getInt();
			System.out.println("Number of dataset:" + datasetCnt);
			read(cin, integerBuffer);
			int leng = integerBuffer.getInt();
			stringBuffer = ByteBuffer.allocate(leng*2).order(ByteOrder.BIG_ENDIAN);
			read(cin, stringBuffer);
			String str = new String(stringBuffer.array(), StandardCharsets.UTF_16);
			System.out.println("data group name:" + str);
			

			for(int j = 1; j < 1; ++j) {
				
				read(cin, integerBuffer);
				System.out.println("P1:" + integerBuffer.getInt());
				read(cin, integerBuffer);
				System.out.println("P2:" + integerBuffer.getInt());
				read(cin, integerBuffer);
				leng = integerBuffer.getInt();
				stringBuffer = ByteBuffer.allocate(leng*2).order(ByteOrder.BIG_ENDIAN);
				read(cin, stringBuffer);
				str = new String(stringBuffer.array(), StandardCharsets.UTF_16);
				System.out.println("Data set Name:" + str);
				
				read(cin, integerBuffer);
				int arrayLength = integerBuffer.getInt();
				System.out.println("Value length:" + arrayLength);
				for(int k = 0; k < arrayLength; ++k) {
					read(cin, integerBuffer);
					leng = integerBuffer.getInt();
					stringBuffer = ByteBuffer.allocate(leng*2).order(ByteOrder.BIG_ENDIAN);
					read(cin, stringBuffer);
					str = new String(stringBuffer.array(), StandardCharsets.UTF_16);
					System.out.println("\tparam:" + str);
					
					read(cin, integerBuffer);
					leng = integerBuffer.getInt();
					ByteBuffer tmpBuffer = ByteBuffer.allocate(leng).order(ByteOrder.BIG_ENDIAN);
					read(cin, tmpBuffer);
					
					read(cin, integerBuffer);
					leng = integerBuffer.getInt();
					stringBuffer = ByteBuffer.allocate(leng*2).order(ByteOrder.BIG_ENDIAN);
					read(cin, stringBuffer);
					str = new String(stringBuffer.array(), StandardCharsets.UTF_16);
					System.out.println("\ttype:" + str);
					
					if("text/plain".equals(str)) {
						System.out.println("\tvalue:" + new String(tmpBuffer.array(), StandardCharsets.UTF_8));
					} else if("text/ascii".equals(str)) {
						System.out.println("\tvalue:" + new String(tmpBuffer.array(), StandardCharsets.US_ASCII));
					} else if("text/x-calvin-integer-8".equals(str) || "text/x-calvin-unsigned-integer-8".equals(str)) {
						System.out.println("\tvalue:" + tmpBuffer.get());
					} else if("text/x-calvin-integer-16".equals(str) || "text/x-calvin-unsigned-integer-16".equals(str)) {
						System.out.println("\tvalue:" + tmpBuffer.getChar());
					} else if("text/x-calvin-integer-32".equals(str) || "text/x-calvin-unsigned-integer-32".equals(str)) {
						System.out.println("\tvalue:" + tmpBuffer.getInt());
					} else if("text/x-calvin-float".equals(str)) {
						System.out.println("\tvalue:" + tmpBuffer.getFloat());
					}
				}
				read(cin, integerBuffer);
				arrayLength = integerBuffer.getInt();
				System.out.println("Number of Column:" + arrayLength);
				int size = 0;
				int block = 0;
				for(int k = 0; k < arrayLength; ++k) {
					read(cin, integerBuffer);
					leng = integerBuffer.getInt();
					stringBuffer = ByteBuffer.allocate(leng*2).order(ByteOrder.BIG_ENDIAN);
					read(cin, stringBuffer);
					str = new String(stringBuffer.array(), StandardCharsets.UTF_16);
					System.out.println("\tparam:" + str);
					
					read(cin, byteBuffer);
					int type = byteBuffer.get();
					System.out.println("\tbyte:" + type);
					
					read(cin, integerBuffer);
					if(type==7) size += integerBuffer.getInt()+1;
					else if(type==8) size += integerBuffer.getInt()+1;
					else size += integerBuffer.getInt();
					if(type == 7 || type == 8) block = 1;
					System.out.println("\tvalue:" + size);
				}
				
				read(cin, integerBuffer);
				arrayLength = integerBuffer.getInt();
				System.out.println("Number of Row:" + arrayLength);
				if(block > 0) {
					chk(cin);
					System.exit(-1);
				}
				cin.position(cin.position() + arrayLength*size+1);
				/*for(int k = 0; k < arrayLength; ++k) {
					read(cin, integerBuffer);
					integerBuffer.getFloat();
					// System.out.println("Value:" + integerBuffer.getFloat());
				}*/
			}
			//chk(cin);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final static void chk(FileChannel cin) {
		ByteBuffer tmp = ByteBuffer.allocate(100).order(ByteOrder.BIG_ENDIAN);
		read(cin, tmp);
		System.out.println(ByteToHex.getHex(tmp.array()));
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
}
