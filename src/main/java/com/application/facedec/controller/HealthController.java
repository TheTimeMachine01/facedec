package com.application.facedec.controller;

//import nu.pattern.OpenCV;
import com.application.facedec.config.SecurityUtils;
import com.application.facedec.entity.Employee;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.opencv.core.Core;

@RestController
public class HealthController {

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/health")
    public String healthCheck() {
        return "Application is running!: "+ System.getProperty("java.library.path");
    }

    @GetMapping("/lib")
    public String Libcheck() {
        try {
            // Attempt a very simple OpenCV operation to verify the native library is loaded and working.
            // When BUILD_FAT_JAVA_LIB=ON, the native library (libopencv_javaXXX.so)
            // is embedded in opencv-XXX.jar and loaded automatically by the Java bindings.
            // We do NOT call System.loadLibrary() explicitly here.

            Mat dummyMat = new Mat(10, 10, CvType.CV_8UC1); // Create a 10x10 single-channel matrix
            String opencvVersion = Core.VERSION; // Get the OpenCV version string

            // Release the matrix to free memory (good practice)
            dummyMat.release();

            // If we reach here, it means OpenCV native code was successfully loaded and initialized
            return "Application is healthy! OpenCV version: " + opencvVersion + ". Dummy Mat created successfully.";

        } catch (Exception e) {
            // If any exception occurs during OpenCV operation, it means something is wrong
            // (e.g., native library not loaded, or an issue during Mat instantiation)
            e.printStackTrace(); // Log the full stack trace to your application logs for debugging
            return "Application health check failed! Error with OpenCV: " + e.getMessage();
        }
    }

    @GetMapping("/user")
    public String authUser() {
        Employee currentUser = securityUtils.getAuthenticatedUser();

        Long userId = currentUser.getId();
        String name = currentUser.getName();

        System.out.println(STR."UserId: \{userId}");
        System.out.println(STR."User Name: \{name}");
        return "Okay";
    }
}
