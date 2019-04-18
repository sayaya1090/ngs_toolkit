package net.sayaya.chip.toolkit.thermofisher;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Supplier;

import net.sayaya.chip.toolkit.thermofisher.data.Cel;

public class CelLoader implements Supplier<Cel> {
	private final Cel data = new Cel();
	public CelLoader(Path cel) {

	}

	@Override
	public Cel get() {
		return data;
	}
	
	public final static void main(String[] args) {
		CelLoader instance = new CelLoader(new File("C:\\Users\\Sangjay\\Downloads\\19MA_0201 JEON JAE HO_(CytoScan750K_Array).CEL").toPath());
	}
}
