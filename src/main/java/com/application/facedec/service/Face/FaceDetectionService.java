package com.application.facedec.service.Face;

import com.application.facedec.dto.Attendance.InLogResponse;
import com.application.facedec.entity.User.Employee;
import com.application.facedec.entity.Face.Face;
import com.application.facedec.repository.Face.FaceDetectionRepository;
import com.application.facedec.repository.User.UserRepository;
import org.opencv.core.*;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

        InputStream cascadeStream = new ClassPathResource(HAAR_CASCADE_FILE).getInputStream();

//        File cascadeFile = new ClassPathResource(HAAR_CASCADE_FILE).getFile();
        File cascadeTempFile = File.createTempFile("haarcascade_", ".xml");
        cascadeTempFile.deleteOnExit(); // Ensure the temporary file is deleted on exit

        Files.copy(cascadeStream, cascadeTempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        cascadeStream.close();

        System.out.println("Haar Cascade loaded from temporary file: " + cascadeTempFile.getAbsolutePath());

//        System.out.println(STR."Attempting to load Haar Cascade from absolute path: \{cascadeFile.getAbsolutePath()}");

//        CascadeClassifier faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());

        // Load the CascadeClassifier using the path of the temporary file
        CascadeClassifier faceDetector = new CascadeClassifier(cascadeTempFile.getAbsolutePath());

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
            face.setFaceImageData(file.getBytes()); // Store image bytes in DB
            face.setFaceImageUrl(tempFilePath); // Keep file path for debugging/logging, though it's ephemeral
            face.setCreatedAt(LocalDateTime.now());

            faceDetectionRepository.save(face);

            image.release();
            grayImage.release();

            // Don't forget to release the cascade detector
            faceDetector.empty();

            return faces;
        } catch (Exception e) {
            throw new RuntimeException(STR."Error occured :\{e}");
        }
    }


    public double compareFaces(String faceImagePath1, byte[] faceImageBytes2) {
        // Load images
        Mat image1 = Imgcodecs.imread(faceImagePath1, Imgcodecs.IMREAD_GRAYSCALE);

        // Decode the stored image bytes directly into a Mat
        MatOfByte matOfByte = new MatOfByte(faceImageBytes2);
        Mat image2 = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_GRAYSCALE);

        if (image1.empty() || image2.empty()) {
            throw new RuntimeException("Could not decode one of the images for comparison.");
        }

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

    public InLogResponse matchFace(String detectedFacePath, Long userId) {

        // Fetch the single stored face for the user. Assuming only one face per user.
        Face storedFace = faceDetectionRepository.findByUserId(userId);

        InLogResponse inLogResponse = new InLogResponse();

        // Check if a face was found. If not, there's nothing to compare against.
        if (storedFace == null) {
            System.err.println("No stored face found for userId: " + userId);
            inLogResponse.setFcsScore(0);
            inLogResponse.setStatus(false);
            return inLogResponse;
        }

        byte[] storedFaceBytes = storedFace.getFaceImageData();

        if (storedFaceBytes == null || storedFaceBytes.length == 0) {
            System.err.println("No stored face data found for userId: " + userId);
            inLogResponse.setFcsScore(0);
            inLogResponse.setStatus(false);
            return inLogResponse;
        }

        double similarity = compareFaces(detectedFacePath, storedFaceBytes);

        System.out.println("Face comparison similarity score for userId " + userId + ": " + similarity);

        // Use a threshold to determine if it's a match.
        // The value '1000' is a placeholder; you will need to fine-tune this.
        if (similarity < 1100000) {
            System.out.println("Match successful!");

            inLogResponse.setFcsScore((long) similarity);
            inLogResponse.setStatus(true);
            return inLogResponse;

        } else {
            System.out.println("Match failed. Similarity score exceeded threshold.");

            inLogResponse.setFcsScore((long) similarity);
            inLogResponse.setStatus(true);
            return inLogResponse;
        }
    }
}