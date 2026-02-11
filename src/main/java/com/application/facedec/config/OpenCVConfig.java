package com.application.facedec.config;

import jakarta.annotation.PostConstruct;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy(false) // Force eager initialization even when spring.main.lazy-initialization=true
public class OpenCVConfig {
    private static final Logger logger = LoggerFactory.getLogger(OpenCVConfig.class);

    @PostConstruct
    public void init() {
        String libraryPath = System.getProperty("java.library.path");
        logger.info("java.library.path = {}", libraryPath);
        logger.info("Attempting to load OpenCV native library: {}", Core.NATIVE_LIBRARY_NAME);

        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            logger.info("OpenCV native library loaded successfully: {}", Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            logger.error("FATAL: Failed to load OpenCV native library '{}'. java.library.path='{}'",
                    Core.NATIVE_LIBRARY_NAME, libraryPath, e);

            // List files in java.library.path to debug missing library issues
             try {
                String[] paths = libraryPath.split(System.getProperty("path.separator"));
                for (String path : paths) {
                    java.io.File dir = new java.io.File(path);
                    if (dir.exists() && dir.isDirectory()) {
                        logger.error("Listing files in {}:", path);
                        String[] files = dir.list();
                        if (files != null) {
                            for (String file : files) {
                                logger.error(" - {}", file);
                            }
                        }
                    } else {
                        logger.error("Directory {} does not exist or is not a directory.", path);
                    }
                }
            } catch (Exception ex) {
                logger.error("Failed to list files in java.library.path", ex);
            }

            // Fail loudly — the app cannot function without OpenCV
            throw new RuntimeException("Cannot start application: OpenCV native library not found", e);
        }
    }
}
