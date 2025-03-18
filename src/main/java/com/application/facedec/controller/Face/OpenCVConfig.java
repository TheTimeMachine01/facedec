package com.application.facedec.controller.Face;

import org.opencv.core.Core;

public class OpenCVConfig {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}
