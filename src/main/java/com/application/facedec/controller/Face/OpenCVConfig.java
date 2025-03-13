package com.application.facedec.controller.Face;

public class OpenCVConfig {
    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }
}
