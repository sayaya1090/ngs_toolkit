package net.sayaya.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sayaya.data.Cytoband;


public class CytobandLoader implements Supplier<Stream<Cytoband>> {
	private final List<Cytoband> data;
	public CytobandLoader(Path cytoband) throws IOException {
		List<String> lines = Files.readAllLines(cytoband);
		data = lines.stream().map(line->{
			String[] split = line.split("\t", -1);
			String chr = split[0].trim();
			long start = Long.parseLong(split[1].trim());
			long end = Long.parseLong(split[2].trim());
			String name = split[3].trim();
			String gieStain = split[4].trim();
			return new Cytoband().setChr(chr).setStart(start).setEnd(end).setName(name).setGieStain(gieStain);
		}).collect(Collectors.toList());
	}

	@Override
	public Stream<Cytoband> get() {
		return data.stream();
	}
}
