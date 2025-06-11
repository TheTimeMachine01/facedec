package com.application.facedec.service;

import com.application.facedec.entity.Employee;
import com.application.facedec.entity.Face;
import com.application.facedec.repository.FaceDetectionRepository;
import com.application.facedec.repository.UserRepository;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FaceDetectionService {
    private static final String HAAR_CASCADE_FILE = "haarcascade_frontalface_default.xml";
    private static final int RECTANGLE_THICKNESS = 2;

    @Autowired
    private FaceDetectionRepository faceDetectionRepository;

    @Autowired
    private UserRepository userRepository;


    private CascadeClassifier faceDetector;

    public List<Rect> detectFaces(MultipartFile file, Long userId) throws IOException, IOException {

        boolean faceExists = faceDetectionRepository.existsByUserId(userId);

        if (faceExists) {
            throw new RuntimeException("Face data already available for this user.");
        }

        File cascadeFile = new ClassPathResource(HAAR_CASCADE_FILE).getFile();
        System.out.println(STR."Attempting to load Haar Cascade from absolute path: \{cascadeFile.getAbsolutePath()}");
        CascadeClassifier faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());

        if (faceDetector.empty()) System.out.println("Face detector is empty"); else System.out.println("Face detector is loaded");

        String tempDir = System.getProperty("java.io.tmpdir");
        String tempFileName = STR."\{userId}_\{LocalDateTime.now().toString().replace(":", "-")}_\{file.getOriginalFilename()}";
        String tempFilePath = tempDir + File.separator + tempFileName;

        try {
            Files.write(Paths.get(tempFilePath), file.getBytes());
            Mat image = Imgcodecs.imread(tempFilePath);
            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(grayImage, faceDetections);

            List<Rect> faces = new ArrayList<>();
            for (Rect rect : faceDetections.toArray()) {
                faces.add(rect);
                Imgproc.rectangle(image, rect, new Scalar(0, 255, 0), RECTANGLE_THICKNESS);
            }

            String annotatedFilePath = STR."\{tempDir}/annotated_\{tempFileName}";
            Imgcodecs.imwrite(annotatedFilePath, image);

            System.out.println(STR."Detected {\{faces.size()}} faces in image: {\{tempFileName}}");

            Employee employee = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Store face data in the database
            Face face = faceDetectionRepository.findById(userId).orElse(new Face());
            face.setUser(employee);
            face.setEmp_id(userId);
            face.setFaceImageUrl(tempFilePath); // Store the temporary file path
            face.setCreatedAt(LocalDateTime.now());

            faceDetectionRepository.save(face);

            image.release();
            grayImage.release();

            return faces;
        } catch (Exception e) {
            throw new RuntimeException(STR."Error occured :\{e}");
        }
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
        List<Face> storedFaces = (List<Face>) faceDetectionRepository.findAllByUserId(userId);

        for (Face face : storedFaces) {
            String storedFacePath = face.getFaceImageUrl();
            double similarity = compareFaces(detectedFacePath, storedFacePath);
            if (similarity < 1000) { // Define a threshold for a match
                return true;
            }
        }
        return false;
    }


}

