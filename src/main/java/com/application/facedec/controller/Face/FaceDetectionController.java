package com.application.facedec.controller.Face;

import com.application.facedec.config.SecurityUtils;
import com.application.facedec.entity.Employee;
import com.application.facedec.exceptions.GlobalExceptionHandler;
import com.application.facedec.repository.FaceDetectionRepository;
import com.application.facedec.service.FaceDetectionService;
import org.opencv.core.Rect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/face")
public class FaceDetectionController {

    @Autowired
    private FaceDetectionService faceDetectionService;

    @Autowired
    private FaceDetectionRepository faceDetectionRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private GlobalExceptionHandler geh;

    @PostMapping("/detect")
    public ResponseEntity<?> detectFaces(@RequestParam("file") MultipartFile file) {

        Employee currentUser = securityUtils.getAuthenticatedUser();
        Long userId = currentUser.getId();

        if (faceDetectionRepository.existsByUserId(userId)) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("Face data already available for this user.");
            return geh.handleError("Face data already available for this user", HttpStatus.CONFLICT);
        }

        try {
            List<Rect> faces = faceDetectionService.detectFaces(file, userId);
            return ResponseEntity.ok(faces);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing image.");
        }
    }

    @PostMapping("/match")
    public ResponseEntity<?> matchFace(@RequestParam("detectedFace") MultipartFile detectedFace, @RequestParam("userId") Long userId) {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            String tempFilePath = tempDir + java.io.File.separator + detectedFace.getOriginalFilename();
            java.nio.file.Files.write(java.nio.file.Paths.get(tempFilePath), detectedFace.getBytes());
            System.out.println(STR."The Temp path is : \{tempFilePath}");

            boolean matched = faceDetectionService.matchFace(tempFilePath, userId);
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(tempFilePath));

            // Returning JSON format response
            Map<String, Object> response = new HashMap<>();
            response.put("status", STR."\{matched}");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", false);
            errorResponse.put("error", "Error processing image.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}


