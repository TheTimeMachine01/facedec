package com.application.facedec.config;

import jakarta.annotation.PostConstruct;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenCVConfig {
    private static final Logger logger = LoggerFactory.getLogger(OpenCVConfig.class);

    @PostConstruct
    public void init() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            logger.info("OpenCV native library loaded successfully: {}", Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            logger.error("Failed to load OpenCV library: {}", e.getMessage());
        }
    }
}
