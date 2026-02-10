package com.application.facedec;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FacedecApplication {
//	static {
//		// Load OpenCV native library
//		try {
//			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//			System.out.println(STR."OpenCV native library loaded successfully: \{Core.NATIVE_LIBRARY_NAME}");
//		} catch (UnsatisfiedLinkError e) {
//			System.err.println(STR."Error loading OpenCV native library: \{e.getMessage()}");
//			System.err.println("Please ensure the OpenCV native library (e.g., opencv_javaXXX.dll, libopencv_javaXXX.so) is in your Java library path or a location accessible to your application.");
//			// You might want to exit the application if it can't run without OpenCV
//			System.exit(1);
//		}
//	}

	public static void main(String[] args) {
		SpringApplication.run(FacedecApplication.class, args);
	}

}
