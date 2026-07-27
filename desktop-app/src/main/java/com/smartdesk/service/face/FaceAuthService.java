package com.smartdesk.service.face;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FaceAuthService {

    private static final Size MIN_FACE_SIZE = new Size(110, 110);
    private static final Size NORMALIZED_FACE_SIZE = new Size(160, 160);
    private static final Size THUMBNAIL_SIZE = new Size(32, 32);
    private static final double MIN_SHARPNESS = 85.0;
    private static final double MIN_BRIGHTNESS = 45.0;
    private static final double MAX_BRIGHTNESS = 205.0;
    private static final double MIN_MATCH_SCORE = 0.63;
    private static final double MIN_MATCH_MARGIN = 0.08;
    private static final double[] DETECTION_ANGLES = {0.0, -12.0, 12.0};

    private final Path datasetRoot;
    private CascadeClassifier faceCascade;

    public FaceAuthService(@Value("${smartdesk.face.dataset-dir:./desktop-app/dataset}") String datasetDirectory) {
        this.datasetRoot = Path.of(datasetDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(datasetRoot);
            faceCascade = new CascadeClassifier(copyCascadeToTempFile().toString());
            if (faceCascade.empty()) {
                throw new IllegalStateException("Unable to load haarcascade_frontalface_default.xml");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize face recognition assets", ex);
        }
    }

    public boolean hasCompleteEnrollment(String username) {
        return getMissingPoses(username).isEmpty();
    }

    public List<FaceEnrollmentPose> getMissingPoses(String username) {
        if (username == null || username.isBlank()) {
            return List.of(FaceEnrollmentPose.values());
        }

        Path userDir = resolveUserDirectory(username);
        if (!Files.isDirectory(userDir)) {
            return List.of(FaceEnrollmentPose.values());
        }

        List<FaceEnrollmentPose> missing = new ArrayList<>();
        for (FaceEnrollmentPose pose : FaceEnrollmentPose.values()) {
            if (!Files.exists(userDir.resolve(pose.fileName()))) {
                missing.add(pose);
            }
        }
        return missing;
    }

    public EnumSet<FaceEnrollmentPose> getCapturedPoses(String username) {
        EnumSet<FaceEnrollmentPose> captured = EnumSet.noneOf(FaceEnrollmentPose.class);
        Path userDir = resolveUserDirectory(username);
        if (!Files.isDirectory(userDir)) {
            return captured;
        }

        for (FaceEnrollmentPose pose : FaceEnrollmentPose.values()) {
            if (Files.exists(userDir.resolve(pose.fileName()))) {
                captured.add(pose);
            }
        }
        return captured;
    }

    public FaceCaptureResult captureEnrollmentPose(String username, FaceEnrollmentPose pose, Mat frame) {
        if (username == null || username.isBlank()) {
            return FaceCaptureResult.failure("Username is missing for face enrollment.");
        }
        if (pose == null) {
            return FaceCaptureResult.failure("Select a pose before capturing.");
        }
        if (frame == null || frame.empty()) {
            return FaceCaptureResult.failure("Camera frame was empty. Please try again.");
        }

        FaceSample sample = extractFaceSample(frame);
        if (sample == null) {
            return FaceCaptureResult.failure("No clear face was detected. Center your face and try again.");
        }
        if (sample.sharpness < MIN_SHARPNESS) {
            return FaceCaptureResult.failure("The frame is too blurry. Hold still and move closer.");
        }
        if (sample.brightness < MIN_BRIGHTNESS || sample.brightness > MAX_BRIGHTNESS) {
            return FaceCaptureResult.failure("Lighting is not ideal. Adjust the light and try again.");
        }

        try {
            Path userDir = resolveUserDirectory(username);
            Files.createDirectories(userDir);
            Imgcodecs.imwrite(userDir.resolve(pose.fileName()).toString(), sample.normalizedFace);
            writeAuditStamp(userDir);
            return FaceCaptureResult.success(pose.label() + " captured successfully.");
        } catch (IOException ex) {
            return FaceCaptureResult.failure("Failed to save the captured face image.");
        }
    }

    public FaceVerificationResult verify(String username, List<Mat> frames) {
        if (username == null || username.isBlank()) {
            return FaceVerificationResult.failure("Username is required for face verification.", 0.0, 0.0);
        }

        List<Path> claimedSamples = loadEnrollmentSamples(username);
        if (claimedSamples.size() < FaceEnrollmentPose.values().length) {
            return FaceVerificationResult.failure(
                    "Face registration is incomplete. Please register all required angles again.",
                    0.0,
                    0.0
            );
        }

        List<UserTemplate> otherTemplates = loadOtherUserTemplates(username);
        double bestClaimedScore = 0.0;
        double nearestOtherScore = 0.0;
        int validFrames = 0;

        for (Mat frame : frames) {
            if (frame == null || frame.empty()) {
                continue;
            }

            FaceSample sample = extractFaceSample(frame);
            if (sample == null || sample.sharpness < MIN_SHARPNESS) {
                continue;
            }

            validFrames++;
            FaceDescriptor probe = buildDescriptor(sample.normalizedFace);
            bestClaimedScore = Math.max(bestClaimedScore, bestScoreAgainst(claimedSamples, probe));
            nearestOtherScore = Math.max(nearestOtherScore, bestScoreAgainstTemplates(otherTemplates, probe));
        }

        if (validFrames == 0) {
            return FaceVerificationResult.failure(
                    "No usable face frame was detected. Look straight at the camera and retry.",
                    bestClaimedScore,
                    nearestOtherScore
            );
        }

        if (bestClaimedScore >= MIN_MATCH_SCORE && (bestClaimedScore - nearestOtherScore) >= MIN_MATCH_MARGIN) {
            return FaceVerificationResult.success(
                    String.format(Locale.ENGLISH, "Face verified with confidence %.0f%%.", bestClaimedScore * 100.0),
                    bestClaimedScore,
                    nearestOtherScore
            );
        }

        return FaceVerificationResult.failure(
                String.format(
                        Locale.ENGLISH,
                        "Face authentication failed. Best match %.0f%% was not strong enough.",
                        bestClaimedScore * 100.0
                ),
                bestClaimedScore,
                nearestOtherScore
        );
    }

    private Path copyCascadeToTempFile() throws IOException {
        Path cascadeFile = Files.createTempFile("smartdesk-haarcascade", ".xml");
        cascadeFile.toFile().deleteOnExit();
        try (InputStream inputStream = getClass().getResourceAsStream("/haarcascades/haarcascade_frontalface_default.xml")) {
            if (inputStream == null) {
                throw new IOException("haarcascade_frontalface_default.xml not found on classpath");
            }
            Files.copy(inputStream, cascadeFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return cascadeFile;
    }

    private void writeAuditStamp(Path userDir) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Files.writeString(userDir.resolve("enrollment.txt"), "lastUpdated=" + timestamp + System.lineSeparator());
    }

    private Path resolveUserDirectory(String username) {
        String safeUsername = username.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
        return datasetRoot.resolve(safeUsername);
    }

    private List<Path> loadEnrollmentSamples(String username) {
        Path userDir = resolveUserDirectory(username);
        if (!Files.isDirectory(userDir)) {
            return List.of();
        }

        List<Path> samples = new ArrayList<>();
        for (FaceEnrollmentPose pose : FaceEnrollmentPose.values()) {
            Path sample = userDir.resolve(pose.fileName());
            if (Files.exists(sample)) {
                samples.add(sample);
            }
        }
        return samples;
    }

    private List<UserTemplate> loadOtherUserTemplates(String username) {
        try {
            if (!Files.isDirectory(datasetRoot)) {
                return List.of();
            }

            try (var directories = Files.list(datasetRoot)) {
                return directories
                        .filter(Files::isDirectory)
                        .filter(path -> !path.getFileName().toString().equalsIgnoreCase(username))
                        .map(this::loadTemplateSet)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            }
        } catch (IOException ex) {
            return List.of();
        }
    }

    private List<UserTemplate> loadTemplateSet(Path userDir) {
        List<UserTemplate> templates = new ArrayList<>();
        for (FaceEnrollmentPose pose : FaceEnrollmentPose.values()) {
            Path sample = userDir.resolve(pose.fileName());
            if (!Files.exists(sample)) {
                continue;
            }

            Mat stored = Imgcodecs.imread(sample.toString(), Imgcodecs.IMREAD_GRAYSCALE);
            if (stored.empty()) {
                continue;
            }
            templates.add(new UserTemplate(userDir.getFileName().toString(), buildDescriptor(stored)));
        }
        return templates;
    }

    private double bestScoreAgainst(List<Path> storedSamples, FaceDescriptor probe) {
        double best = 0.0;
        for (Path sample : storedSamples) {
            Mat stored = Imgcodecs.imread(sample.toString(), Imgcodecs.IMREAD_GRAYSCALE);
            if (stored.empty()) {
                continue;
            }
            best = Math.max(best, score(probe, buildDescriptor(stored)));
        }
        return best;
    }

    private double bestScoreAgainstTemplates(List<UserTemplate> templates, FaceDescriptor probe) {
        double best = 0.0;
        for (UserTemplate template : templates) {
            best = Math.max(best, score(probe, template.descriptor));
        }
        return best;
    }

    private FaceDescriptor buildDescriptor(Mat normalizedFace) {
        Mat thumbnail = new Mat();
        Imgproc.resize(normalizedFace, thumbnail, THUMBNAIL_SIZE);
        thumbnail.convertTo(thumbnail, CvType.CV_32F, 1.0 / 255.0);

        float[] pixels = new float[(int) thumbnail.total()];
        thumbnail.get(0, 0, pixels);

        Mat histogram = new Mat();
        Imgproc.calcHist(
                Collections.singletonList(normalizedFace),
                new MatOfInt(0),
                new Mat(),
                histogram,
                new MatOfInt(32),
                new MatOfFloat(0f, 256f)
        );
        Core.normalize(histogram, histogram, 1.0, 0.0, Core.NORM_L1);

        ORB orb = ORB.create(256);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        orb.detectAndCompute(normalizedFace, new Mat(), keyPoints, descriptors);

        return new FaceDescriptor(pixels, histogram, descriptors);
    }

    private double score(FaceDescriptor probe, FaceDescriptor stored) {
        double pixelScore = cosineSimilarity(probe.pixels, stored.pixels);
        double histogramScore = normalizeCorrelation(Imgproc.compareHist(probe.histogram, stored.histogram, Imgproc.HISTCMP_CORREL));
        double orbScore = orbMatchScore(probe.descriptors, stored.descriptors);
        return (pixelScore * 0.5) + (histogramScore * 0.2) + (orbScore * 0.3);
    }

    private double cosineSimilarity(float[] first, float[] second) {
        if (first.length != second.length) {
            return 0.0;
        }

        double numerator = 0.0;
        double firstNorm = 0.0;
        double secondNorm = 0.0;

        for (int index = 0; index < first.length; index++) {
            numerator += first[index] * second[index];
            firstNorm += first[index] * first[index];
            secondNorm += second[index] * second[index];
        }

        if (firstNorm == 0.0 || secondNorm == 0.0) {
            return 0.0;
        }

        return numerator / (Math.sqrt(firstNorm) * Math.sqrt(secondNorm));
    }

    private double normalizeCorrelation(double correlation) {
        return Math.max(0.0, Math.min(1.0, (correlation + 1.0) / 2.0));
    }

    private double orbMatchScore(Mat probeDescriptors, Mat storedDescriptors) {
        if (probeDescriptors.empty() || storedDescriptors.empty()) {
            return 0.0;
        }

        BFMatcher matcher = BFMatcher.create(Core.NORM_HAMMING, false);
        List<MatOfDMatch> matches = new ArrayList<>();
        matcher.knnMatch(probeDescriptors, storedDescriptors, matches, 2);

        double goodMatches = 0.0;
        for (MatOfDMatch pair : matches) {
            var candidates = pair.toArray();
            if (candidates.length < 2) {
                continue;
            }
            if (candidates[0].distance < (0.75 * candidates[1].distance)) {
                goodMatches++;
            }
        }

        double baseline = Math.max(12.0, Math.min(probeDescriptors.rows(), storedDescriptors.rows()));
        return Math.min(1.0, goodMatches / baseline);
    }

    private FaceSample extractFaceSample(Mat sourceFrame) {
        DetectionCandidate candidate = null;

        for (double angle : DETECTION_ANGLES) {
            Mat rotated = rotate(sourceFrame, angle);
            Mat gray = new Mat();
            Imgproc.cvtColor(rotated, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(gray, gray);

            MatOfRect detections = new MatOfRect();
            faceCascade.detectMultiScale(
                    gray,
                    detections,
                    1.1,
                    4,
                    Objdetect.CASCADE_SCALE_IMAGE,
                    MIN_FACE_SIZE,
                    new Size()
            );

            for (Rect rect : detections.toArray()) {
                if (candidate == null || rect.area() > candidate.faceRect.area()) {
                    candidate = new DetectionCandidate(gray, rect);
                }
            }
        }

        if (candidate == null) {
            return null;
        }

        Rect paddedRect = padRect(candidate.faceRect, candidate.grayFrame.width(), candidate.grayFrame.height());
        Mat normalizedFace = new Mat(candidate.grayFrame, paddedRect).clone();
        Imgproc.resize(normalizedFace, normalizedFace, NORMALIZED_FACE_SIZE);

        double sharpness = varianceOfLaplacian(normalizedFace);
        double brightness = Core.mean(normalizedFace).val[0];

        return new FaceSample(normalizedFace, sharpness, brightness);
    }

    private Rect padRect(Rect rect, int frameWidth, int frameHeight) {
        int paddingX = (int) Math.round(rect.width * 0.15);
        int paddingY = (int) Math.round(rect.height * 0.20);

        int x = Math.max(0, rect.x - paddingX);
        int y = Math.max(0, rect.y - paddingY);
        int width = Math.min(frameWidth - x, rect.width + (paddingX * 2));
        int height = Math.min(frameHeight - y, rect.height + (paddingY * 2));
        return new Rect(x, y, width, height);
    }

    private double varianceOfLaplacian(Mat image) {
        Mat laplacian = new Mat();
        Imgproc.Laplacian(image, laplacian, CvType.CV_64F);
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(laplacian, mean, stddev);
        double sigma = stddev.get(0, 0)[0];
        return sigma * sigma;
    }

    private Mat rotate(Mat source, double angle) {
        if (angle == 0.0d) {
            return source.clone();
        }

        Point center = new Point(source.width() / 2.0, source.height() / 2.0);
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        Mat rotated = new Mat();
        Imgproc.warpAffine(source, rotated, rotationMatrix, source.size(), Imgproc.INTER_LINEAR);
        return rotated;
    }

    private record FaceSample(Mat normalizedFace, double sharpness, double brightness) {
    }

    private record DetectionCandidate(Mat grayFrame, Rect faceRect) {
    }

    private record FaceDescriptor(float[] pixels, Mat histogram, Mat descriptors) {
    }

    private record UserTemplate(String username, FaceDescriptor descriptor) {
    }
}
