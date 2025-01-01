package com.application.facedec.config;

public class OpenCVConfig {
    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }
}
