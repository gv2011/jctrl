package com.github.gv2011.process;

import static com.github.gv2011.util.CollectionUtils.pair;
import static com.github.gv2011.util.CollectionUtils.toOpt;
import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.icol.ICollections.listOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.spi.ToolProvider;

import org.slf4j.Logger;

import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.M2t;
import com.github.gv2011.m2t.M2tFactory;
import com.github.gv2011.m2t.Scope;
import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.Pair;
import com.github.gv2011.util.StringUtils;
import com.github.gv2011.util.icol.IList;
import com.github.gv2011.util.icol.ISortedSet;
import com.github.gv2011.util.icol.Opt;

public class ProcessStarter {

	private static final Logger LOG = getLogger(ProcessStarter.class);


	public static void main(final String[] args) {
		final ArtifactRef artifact = BeanUtils.beanBuilder(ArtifactRef.class)
	      .setTStr(ArtifactRef::groupId).to("com.github.gv2011.jctrl")
	      .setTStr(ArtifactRef::artifactId).to("jctrl-process")
	      .setTStr(ArtifactRef::version).to("0.0.3-SNAPSHOT")
	      .build()
	    ;
		run(artifact, Opt.empty());
	}

	public static void run(final ArtifactRef artifact, final Opt<String> mainClass) {

		final Path artifactJar;
		final ISortedSet<Path> classpath;
		final M2tFactory m2tf = ServiceLoader.load(M2tFactory.class).findFirst().get();
		try(final M2t m2t = m2tf.create()){
			classpath = m2t.getClasspath(artifact , Scope.RUNTIME);
			artifactJar = m2t.resolve(artifact);
			LOG.info("Arifact jar: {}.", artifactJar);
			LOG.info(classpath.stream().map(Path::toString).collect(joining("\n")));
		}

		final Pair<String, Opt<String>> moduleAndMainClass = getModule(artifactJar);
		LOG.info("Module and main class: {}.", moduleAndMainClass);
		final String module = moduleAndMainClass.getKey();
		verify(!(moduleAndMainClass.getValue().isEmpty() && mainClass.isEmpty()));

		final ProcessBuilder processBuilder = new ProcessBuilder();
		final IList<String> command = listOf(
			"/programs/jdk-14/bin/java", "-p",
			classpath.stream().map(Path::toString).collect(joining(File.pathSeparator)),
			"-m",
			module + mainClass.map(m->"/"+m).orElse("")
		);
		LOG.info("Command: {}.", command);
		processBuilder.command(command);
		System.out.println();
		final Process process = call(processBuilder::start);

		final Thread t = new Thread(()->{
			final InputStream err = process.getErrorStream();
			int b = call(()->err.read());
			while(b!=-1){
				System.err.print((char)b);
				b = call(()->err.read());
			}
			LOG.info("Error EOS");
		});
		t.start();

		final InputStream in = process.getInputStream();
		int b = call(()->in.read());
		while(b!=-1){
			System.out.print((char)b);
			b = call(()->in.read());
		}
		LOG.info("EOS");
		call(()->t.join());
		final int exitValue = call(()->process.waitFor());
		System.out.println();
		LOG.info("Process terminated with {}.", exitValue);
	}


	private static Pair<String,Opt<String>> getModule(final Path artifactJar) {
		final ToolProvider jar = ToolProvider.findFirst("jar").get();
		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream(outStream, false, UTF_8);
		final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
		final PrintStream err = new PrintStream(errStream, false, UTF_8);
		jar.run(
	        out,
	        err,
	        "--describe-module",
	        "--file",
	        artifactJar.toString()
		);
		verify(errStream.toByteArray().length==0);
		final String result = new String(outStream.toByteArray(), UTF_8);
		LOG.info("Module description: {}.", result);
		final String moduleAndVersion = result.substring(0, result.indexOf(' '));
		final int i = moduleAndVersion.indexOf('@');
		final String module = i!=-1 ? moduleAndVersion.substring(0, i) : moduleAndVersion;
		final String mainClassPrefix = "main-class ";
		final Opt<String> mainClass = StringUtils.split(result, '\n')
			.tail().stream()
			.map(String::trim)
			.filter(l->l.startsWith(mainClassPrefix))
			.collect(toOpt())
			.map(l->l.substring(mainClassPrefix.length()))
		;
		return pair(module, mainClass);
	}

}
