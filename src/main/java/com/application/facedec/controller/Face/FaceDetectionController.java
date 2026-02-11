package com.application.facedec.controller.Face;

import com.application.facedec.config.SecurityUtils;
import com.application.facedec.dto.Attendance.InLogResponse;
import com.application.facedec.entity.User.Employee;
import com.application.facedec.exceptions.GlobalExceptionHandler;
import com.application.facedec.repository.Face.FaceDetectionRepository;
import com.application.facedec.service.Face.FaceDetectionService;
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
    public ResponseEntity<Map<String, Object>> detectFaces(@RequestParam("file") MultipartFile file) {

        Employee currentUser = securityUtils.getAuthenticatedUser();
        Long userId = currentUser.getId();

        System.out.println(userId);

        if (faceDetectionRepository.existsByUserId(userId)) {
            return geh.handleError("Face data already available for this user",false, HttpStatus.CONFLICT);
        }

        try {
            List<Rect> faces = faceDetectionService.detectFaces(file, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("faces", faces);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", false);
            errorResponse.put("message", "Error processing image.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/match")
    public ResponseEntity<InLogResponse> matchFace(@RequestParam("detectedFace") MultipartFile detectedFace, @RequestParam("userId") Long userId) {

        InLogResponse inLogResponse = new InLogResponse();

        try {

            String tempDir = System.getProperty("java.io.tmpdir");
            String tempFilePath = tempDir + java.io.File.separator + detectedFace.getOriginalFilename();
            java.nio.file.Files.write(java.nio.file.Paths.get(tempFilePath), detectedFace.getBytes());
            System.out.println(STR."The Temp path is : \{tempFilePath}");


            InLogResponse response = faceDetectionService.matchFace(tempFilePath, userId);
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(tempFilePath));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();

            inLogResponse.setStatus(false);
            inLogResponse.setFcsScore(0);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", false);
            errorResponse.put("error", "Error processing image.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(inLogResponse);
        }
    }
}


