package com.github.gv2011.process.example;

import static com.github.gv2011.util.ex.Exceptions.call;

import java.nio.file.Paths;

import com.github.gv2011.util.FileUtils;

public class Main {


	public static void main(final String[] args) {
		System.out.println("Hello World!");
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			FileUtils.writeText("Shutdown", Paths.get("shutdown.txt"));
			System.out.println("Shutdown");
		}));
		final int b = call(()->System.in.read());
		while(b!=-1){
			System.out.write(b);
		}
		for(int i=1; i<10; i++){
			System.out.println("Bye");
			call(()->Thread.sleep(500));
		}
	}

}
