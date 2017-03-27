package slr.control;

import com.github.sarxos.webcam.Webcam;
import org.apache.commons.math3.complex.Complex;
import slr.ui.DisplayFDWindow;
import slr.ui.SLRWindow;
import slr.control.controlUnit.ImageProcessor;
import slr.control.controlUnit.VideoStreamSampler;
import slr.services.impl.DefaultImageProcessingService;
import slr.utils.Constants;
import slr.utils.MathUtils;
import slr.services.impl.NeuralNetworkPredictionService;
import slr.services.PredictionService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author corneliu
 */
public class SlrWindowController {

    private SLRWindow mainWindow;

    private DefaultImageProcessingService imageProcessingService;
    private VideoStreamSampler videoStreamSampler;
    private PredictionService predictionService;
    private ImageProcessor webcamListener;

    private BufferedImage loadedImage;
    private double[] FD;

    private String saveLocation = Constants.DEFAULT_SAVE_LOCATION;

    private Map<String, Webcam> availableWebCams;

    public SlrWindowController(SLRWindow window) {
        imageProcessingService = new DefaultImageProcessingService();
        predictionService = new NeuralNetworkPredictionService();
        webcamListener = new ImageProcessor(imageProcessingService, predictionService, window);
        videoStreamSampler = new VideoStreamSampler(webcamListener);

        initializeWebCams();

        //initNeuralNetwork();
        //trainNeuralNetwork();
        //saveNeuralNetwork();
        this.mainWindow = window;
    }


    //------------------------------    WEB CAM CONTROL     ------------------------
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

    public void takeSnapshot(String selectedWebCam) throws IOException {

        Webcam webcam = availableWebCams.get(selectedWebCam);
        if (webcam == null) {
            throw new IllegalArgumentException("Unknown webcam: " + selectedWebCam);
        }

        BufferedImage snapshot = webcam.getImage();
        String path = mainWindow.getFilePath();
        if (path != null) {
            ImageIO.write(snapshot, "jpeg", new File(path));
        }
    }


    //----------------------------  IMAGE PROCESSING    ----------------------------

    public double[] normalizeShapeSize(Point[] boundary, int size) {
        double[] normal = MathUtils.computeCentroidDistance(boundary);
        normal = MathUtils.normalizeShapeSize(normal, size);

        return normal;
    }

    public double[] normalizeShapeSize(Point[][] boundary) {
        Point[] allPoints = new Point[boundary[0].length + boundary[1].length];
        for (int i = 0; i < boundary[0].length; i++) {
            allPoints[i] = boundary[0][i];
        }

        for (int i = 0; i < boundary[1].length; i++) {
            allPoints[boundary[0].length + i] = boundary[1][i];
        }

        double[] normal = MathUtils.computeCentroidDistance(allPoints);
        normal = MathUtils.normalizeShapeSize(normal, Constants.LARGE_SHAPE_SIZE);

        return normal;
    }

    public Point[] normalizeAsPoints(Point[][] boundary) {
        Point[] allPoints = new Point[boundary[0].length + boundary[1].length];
        for (int i = 0; i < boundary[0].length; i++) {
            allPoints[i] = boundary[0][i];
        }

        for (int i = 0; i < boundary[1].length; i++) {
            allPoints[boundary[0].length + i] = boundary[1][i];
        }
        return MathUtils.normalizeShapeSize(allPoints, Constants.LARGE_SHAPE_SIZE);
    }

    public double[] computeFourierDescriptors(BufferedImage preparedImage) {
        Point[] contur = imageProcessingService.getContour(preparedImage);
        double[] shapeDescription = normalizeShapeSize(contur, Constants.LARGE_SHAPE_SIZE);
        Complex[] FT = MathUtils.computeFourierTransforms(shapeDescription);
        FT = MathUtils.normalizeFourierTransforms(FT);
        double[] FD = MathUtils.computeFourierDescriptors(FT);

        return FD;
    }



    //--------------------------    FOR TESTING FUNCTIONALITY   --------------------
    public void loadImage() {
        JFileChooser c = new JFileChooser(saveLocation);
        c.setMultiSelectionEnabled(false);
        c.setAcceptAllFileFilterUsed(false);
        c.setFileFilter(new FileNameExtensionFilter("JPEG file", "jpeg", "jpg"));
        c.setApproveButtonToolTipText("Load Image");
        c.setDialogTitle("Load Image");

        int option = c.showOpenDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                String fileName = c.getSelectedFile().getAbsolutePath();
                loadedImage = ImageIO.read(new File(fileName));
                int scale = loadedImage.getWidth() / 320;
                loadedImage = imageProcessingService.resizeImage(loadedImage, scale);

                mainWindow.renderLoadedImage(loadedImage);
            } catch (IOException ex) {
                Logger.getLogger(SlrWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void recognize() {
//
//        double[] expected = new double[24];
//        expected[Constants.ALPHABET.indexOf(mainWindow.getSelectedLeter())] = 1;
//
//        BufferedImage preparedImage = prepareImage(loadedImage, useSkinDetection, threshold);
//
//        Point[] contour = imageProcessor.getContour(preparedImage);
//        contour = normalizeAsPoints(contour);
//
//        Graphics2D g = loadedImage.createGraphics();
//        if (contour != null && contour.length > 2) {
//            for (int i = 0; i < contour.length - 1; i++) {
//                g.drawLine(contour[i].x * 2, contour[i].y * 2, contour[i + 1].x * 2, contour[i + 1].y * 2);
//                g.setColor(Color.red);
//                g.fillOval(contour[i].x * 2, contour[i].y * 2, 4, 4);
//                g.setColor(Color.white);
//            }
//
//            g.drawLine(contour[0].x * 2, contour[0].y * 2, contour[contour.length - 1].x * 2, contour[contour.length - 1].y * 2);
//            g.setColor(Color.red);
//            g.fillOval(contour[contour.length - 1].x * 2, contour[contour.length - 1].y * 2, 4, 4);
//        }
//
//
//        double[] shapeDescription = MathUtils.computeCentroidDistance(contour);
//        try {
//            if (shapeDescription.length == 32) {
//                Complex[] FT = MathUtils.computeFourierTransforms(shapeDescription);
//                FT = MathUtils.normalizeFourierTransforms(FT);
//                FD = MathUtils.computeFourierDescriptors(FT);
//
//                double[] prediction = predictionService.predict(FD);
//
//
//                BufferedImage error = generateErrorGraphic(expected, prediction);
//                mainWindow.renderErrorImage(error);
//            }
//
//        } catch (Exception ex) {
//            Logger.getLogger(SlrWindowController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        mainWindow.renderLoadedImage(loadedImage);
//
    }

//    private BufferedImage generateErrorGraphic(double[] expected, double[] output) {
//        int w = 460;
//        int h = 280;
//
//        XYSeries expectedSeries = new XYSeries("Expected");
//        XYSeries outputSeries = new XYSeries("Actual Output");
//
//        for (int i = 0; i < expected.length; i++) {
//            expectedSeries.add(i, expected[i]);
//            outputSeries.add(i, output[i]);
//        }
//        XYDataset expectedData = new XYSeriesCollection(expectedSeries);
//        XYDataset outputData = new XYSeriesCollection(outputSeries);
//
//        JFreeChart chart = ChartFactory.createXYLineChart("Error Graphic", "Output", "FD", expectedData, PlotOrientation.VERTICAL, true, false, false);
//        chart.getXYPlot().setDataset(1, outputData);
//
//        //chart.getXYPlot().setRenderer(0, new DeviationRenderer(true, true));
//        //chart.getXYPlot().setRenderer(1, new DeviationRenderer(true, true));
//
//        chart.getXYPlot().setRenderer(0, new XYLineAndShapeRenderer(true, true));
//        chart.getXYPlot().setRenderer(1, new XYLineAndShapeRenderer(true, true));
//
//        BufferedImage fdChart = chart.createBufferedImage(w, h);
//
//        return fdChart;
//    }

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

    private void initializeWebCams() {
        availableWebCams = new HashMap<>();
        for (Webcam webCam : Webcam.getWebcams()) {
            availableWebCams.put(webCam.getName(), webCam);
        }
    }

    //------------------------------    GETERS AND SETERS   ------------------------
    public void setLuminosity(double luminosity) {
        webcamListener.setLuminosity((luminosity - 50) / 100);
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

    public String getSaveLocation() {
        return saveLocation;
    }

    public void setSaveLocation(String saveLocation) {
        this.saveLocation = saveLocation;
    }

    public void setThreshold(int threshold) {
        webcamListener.setThreshold(threshold);
    }

    public void setUseSkinDetection(boolean useSkinDetection) {
        webcamListener.setUseSkinDetection(useSkinDetection);
    }

    public void displayFourierDescriptors() {
        new DisplayFDWindow(FD).setVisible(true);
    }

    public boolean isRecognize() {
        return webcamListener.computesPrediction();
    }

    public void computePrediction(boolean recognize) {
        webcamListener.setComputePrediction(recognize);
    }
}