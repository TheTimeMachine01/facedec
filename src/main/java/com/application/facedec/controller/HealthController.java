package com.application.facedec.controller;

//import nu.pattern.OpenCV;
import com.application.facedec.config.SecurityUtils;
import com.application.facedec.entity.Employee;
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
//        System.loadLibrary("lib" + Core.NATIVE_LIBRARY_NAME + ".so");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        return "OpenCV loaded Sucessfully!";
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
