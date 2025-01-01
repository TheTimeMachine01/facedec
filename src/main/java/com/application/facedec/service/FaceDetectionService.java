package com.application.facedec.service;

import com.application.facedec.entity.FaceDetection;
import com.application.facedec.repository.FaceDetectionRepository;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FaceDetectionService {
    private static final String HAAR_CASCADE_FILE = "src/main/resources/haarcascade_frontalface_default.xml";

    @Autowired
    private FaceDetectionRepository faceDetectionRepository;

    public List<Rect> detectFaces(MultipartFile file) throws Exception {
        // Load the Haar Cascade for face detection
        CascadeClassifier faceDetector = new CascadeClassifier(HAAR_CASCADE_FILE);

        // Save the uploaded image locally
        String tempDir = System.getProperty("java.io.tmpdir");
        String tempFilePath = tempDir + File.separator + file.getOriginalFilename();
        Files.write(Paths.get(tempFilePath), file.getBytes());

        // Read the image using OpenCV
        Mat image = Imgcodecs.imread(tempFilePath);

        // Convert to grayscale (Haar Cascade works best with grayscale images)
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Detect faces
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(grayImage, faceDetections);

        // Get the face rectangles
        List<Rect> faces = new ArrayList<>();
        for (Rect rect : faceDetections.toArray()) {
            faces.add(rect);
            // Draw rectangles around detected faces (optional)
            Imgproc.rectangle(image, rect, new Scalar(0, 255, 0), 2);
        }

        // Save annotated image for debugging (optional)
        String annotatedFilePath = tempDir + "/annotated_" + file.getOriginalFilename();
        Imgcodecs.imwrite(annotatedFilePath, image);

        return faces;
    }

    public double compareFaces(String faceImagePath1, String faceImagePath2) {
        // Load images
        Mat image1 = Imgcodecs.imread(faceImagePath1, Imgcodecs.IMREAD_GRAYSCALE);
        Mat image2 = Imgcodecs.imread(faceImagePath2, Imgcodecs.IMREAD_GRAYSCALE);

        // Resize images to the same size
        Imgproc.resize(image1, image1, new Size(160, 160));
        Imgproc.resize(image2, image2, new Size(160, 160));

        // Compute the structural similarity index (SSIM) or use other metrics
        Mat diff = new Mat();
        Core.absdiff(image1, image2, diff);

        Scalar sumDiff = Core.sumElems(diff);
        double totalDifference = sumDiff.val[0]; // The lower the difference, the more similar the images

        return totalDifference; // Use this as the similarity score
    }

    public boolean matchFace(String detectedFacePath, Long userId) {
        List<FaceDetection> storedFaces = faceDetectionRepository.findByUserId(userId);

        for (FaceDetection face : storedFaces) {
            String storedFacePath = face.getFaceImageUrl();
            double similarity = compareFaces(detectedFacePath, storedFacePath);
            if (similarity < 1000) { // Define a threshold for a match
                return true;
            }
        }
        return false;
    }


}
