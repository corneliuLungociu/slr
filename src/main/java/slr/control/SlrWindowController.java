package slr.control;

import com.github.sarxos.webcam.Webcam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slr.services.ImageProcessingService;
import slr.services.PredictionService;
import slr.utils.Constants;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author corneliu
 */
@Service
public class SlrWindowController {

    @Autowired
    private ImageProcessingService imageProcessingService;

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private VideoStreamSampler videoStreamSampler;

    @Autowired
    private ImageProcessor imageProcessor;

    private Map<String, Webcam> availableWebCams;

    @PostConstruct
    private void init() {
        availableWebCams = new HashMap<>();
        for (Webcam webCam : Webcam.getWebcams()) {
            availableWebCams.put(webCam.getName(), webCam);
        }

        //initNeuralNetwork();
        //trainNeuralNetwork();
        //saveNeuralNetwork();
    }

    public Set<String> getAvailableWebCams() {
        return availableWebCams.keySet();
    }

    public Webcam startWebCam(String selectedDevice) {
        Webcam webcam = availableWebCams.get(selectedDevice);
        if (webcam == null) {
            throw new RuntimeException("Webcam: " + selectedDevice + " unavailable.");
        }

        webcam.open();
        videoStreamSampler.start(webcam);

        return webcam;
    }

    public void takeSnapshot(String selectedWebCam, String filePath) throws IOException {
        if (filePath == null) {
            return;
        }

        Webcam webcam = availableWebCams.get(selectedWebCam);
        if (webcam == null) {
            throw new IllegalArgumentException("Unknown webcam: " + selectedWebCam);
        }

        if (!webcam.isOpen()) {
            throw new IllegalStateException(String.format("Webcam [%s] is not started. can't take snapshot", selectedWebCam));
        }

        BufferedImage snapshot = webcam.getImage();
        ImageIO.write(snapshot, "jpeg", new File(filePath));
    }

    public BufferedImage readImage(String filePath) throws IOException {
        BufferedImage loadedImage = ImageIO.read(new File(filePath));
        int scale = loadedImage.getWidth() / 320;
        loadedImage = imageProcessingService.scale(loadedImage, scale);
        return loadedImage;
    }

    public double[] recognizeSingleImage(BufferedImage loadedImage, boolean useSkinDetection, int luminosityThreshold, double luminosity) {

        double[] FD = imageProcessingService.extractFeatures(useSkinDetection, luminosityThreshold, loadedImage, luminosity);
        if (FD == null) {
            return null;
        }

        double[] prediction = predictionService.predict(FD);


        drawContourOnImage(loadedImage, useSkinDetection, luminosityThreshold, luminosity);
        return prediction;
    }

    private void drawContourOnImage(BufferedImage loadedImage, boolean useSkinDetection, int luminosityThreshold, double luminosity) {
        Point[] contour  = imageProcessingService.extractContour(loadedImage, useSkinDetection, luminosityThreshold, luminosity);

        Graphics2D imageWithContour = loadedImage.createGraphics();
        if (contour != null && contour.length > 2) {
            for (int i = 0; i < contour.length - 1; i++) {
                imageWithContour.drawLine(contour[i].x * 2, contour[i].y * 2, contour[i + 1].x * 2, contour[i + 1].y * 2);
                imageWithContour.setColor(Color.red);
                imageWithContour.fillOval(contour[i].x * 2, contour[i].y * 2, 4, 4);
                imageWithContour.setColor(Color.white);
            }

            imageWithContour.drawLine(contour[0].x * 2, contour[0].y * 2, contour[contour.length - 1].x * 2, contour[contour.length - 1].y * 2);
            imageWithContour.setColor(Color.red);
            imageWithContour.fillOval(contour[contour.length - 1].x * 2, contour[contour.length - 1].y * 2, 4, 4);
        }
    }

    public void stopWebCam(String webCamName) {
        Webcam webcam = availableWebCams.get(webCamName);
        if (webcam == null) {
            throw new IllegalArgumentException("Unknown web cam: " + webCamName);
        }
        videoStreamSampler.stop();
        webcam.close();
    }


    public void pauseWebCam(String webCamName) {
        // TODO: implement this
    }

    public void resumeWebCam(String webCamName) {
        // TODO: implement this
    }

    public void setLuminosity(double luminosity) {
        imageProcessor.setLuminosity((luminosity - 50) / 100);
    }

//    public void wireFrameView() {
//        this.viewType = Constants.WIREFRAME_VIEW;
//    }
//
//    public void skinView() {
//        this.viewType = Constants.SKIN_VIEW;
//    }
//
//    public void normalView() {
//        this.viewType = Constants.NORMAL_VIEW;
//    }
//
//    public void fdView() {
//        this.viewType = Constants.FD_VIEW;
//    }

    public void setThreshold(int threshold) {
        imageProcessor.setThreshold(threshold);
    }

    public void setUseSkinDetection(boolean useSkinDetection) {
        imageProcessor.setUseSkinDetection(useSkinDetection);
    }

//    public void displayFourierDescriptors() {
//        new DisplayFDWindow(FD).setVisible(true);
//    }

    public boolean isRecognize() {
        return imageProcessor.computesPrediction();
    }

    public void computePrediction(boolean recognize) {
        imageProcessor.setComputePrediction(recognize);
    }

    public void collectTrainingExample(String selectedWebCam, String letter, String location, int trainingExampleCounter, boolean useSkinDetection, int luminosityThreshold, double luminosity) throws IOException {
        Webcam webcam = availableWebCams.get(selectedWebCam);
        if (webcam == null) {
            throw new IllegalArgumentException("Unknown webcam: " + selectedWebCam);
        }

        BufferedImage image = webcam.getImage();
        double[] FD = imageProcessingService.extractFeatures(useSkinDetection, luminosityThreshold, image, luminosity);

        if (FD == null) {
            return;
        }

        int label = Constants.ALPHABET.indexOf(letter);
        String line = Arrays.stream(FD).mapToObj(value -> "" + value).collect(Collectors.joining(",")) + "," + label;

        Path tsLocation = Paths.get(location, letter + ".csv");
        Path teLocation = Paths.get(location, letter + "-" + trainingExampleCounter + ".jpg");
        Files.write(tsLocation, Collections.singletonList(line), StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        ImageIO.write(image, "jpeg", teLocation.toFile());
    }
}