package slr.control;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slr.services.ImageProcessingService;
import slr.services.PredictionService;
import slr.ui.SLRWindow;
import slr.utils.Constants;

import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class ImageProcessor {

    @Autowired
    private ImageProcessingService imageProcessingService;

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private SLRWindow mainWindow;

    private int crtFrameNr;
    private int[] overallPrediction = new int[24];

    private boolean computePrediction;
    private double luminosity;
    private boolean useSkinDetection;
    private int threshold;

    public void update(BufferedImage crtFrame) {

        double[] FD = imageProcessingService.extractFeatures(useSkinDetection, threshold, crtFrame, luminosity);
        if (FD == null) {
            System.out.println("SKIP CURRENT FRAME");
            return;
        }

        if (computePrediction) {
            double[] crtPrediction = predictionService.predict(FD);

            if (crtFrameNr < Constants.FRAMES_TO_RECOGNIZE) {
                crtFrameNr++;
                int pozMax = getIndexOfMaxElement(crtPrediction);
                overallPrediction[pozMax]++;
            } else {
                mainWindow.setOutput(overallPrediction);
                crtFrameNr = 0;
                overallPrediction = new int[24];
            }
        }

//TODO: implement debug mode switch
//        if (debugMode) {
        // TODO: invoke first scale, then illuminate, then everything else
        BufferedImage illuminatedImage = imageProcessingService.illuminate(crtFrame, luminosity);
        BufferedImage smallIlluminated = imageProcessingService.scale(illuminatedImage, 4);
        BufferedImage smallWireFrame = generateWireFrameImage(illuminatedImage, true);
        BufferedImage smallSkin = generateSkinImage(true, illuminatedImage);
        BufferedImage smallFD = generateFdPlot(true, FD);

//            switch (viewType) {
//                case Constants.NORMAL_VIEW: {
//                    imageToDisplay = iluminatedImage;
//                    break;
//                }
//                case Constants.WIREFRAME_VIEW: {
//                    imageToDisplay = generateWireFrame(contour, false);
//                    break;
//                }
//                case Constants.SKIN_VIEW: {
//                    imageToDisplay = generateSkin(false, iluminatedImage);
//                    break;
//                }
//                default: {
//                    // FD View
//                    imageToDisplay = generateFD(false, FD);
//                }
//            }
//
//            mainWindow.renderFrame(imageToDisplay);

        mainWindow.renderFD(smallFD);
        mainWindow.renderSkin(smallSkin);
        mainWindow.renderWire(smallWireFrame);
        mainWindow.renderSmallIluminated(smallIlluminated);
//        }
    }

    private int getIndexOfMaxElement(double[] output) {
        int maxIndex = 0;
        for (int i = 1; i < output.length; i++) {
            if (output[i] > output[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private BufferedImage generateWireFrameImage(BufferedImage image, boolean small) {

        Point[] contour = imageProcessingService.extractContour(image, useSkinDetection, threshold, luminosity);

        int scale = 1;
        int w = 160;
        int h = 120;
        if (!small) {
            w = 640;
            h = 480;
            scale = 4;
        }

        BufferedImage wireFrame = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = wireFrame.createGraphics();
        if (contour != null && contour.length > 2) {
            for (int i = 0; i < contour.length - 1; i++) {
                g.drawLine(contour[i].x * scale, contour[i].y * scale, contour[i + 1].x * scale, contour[i + 1].y * scale);
                g.setColor(Color.red);
                g.fillOval(contour[i].x * scale, contour[i].y * scale, 4, 4);
                g.setColor(Color.white);
            }

            g.drawLine(contour[0].x * scale, contour[0].y * scale, contour[contour.length - 1].x * scale, contour[contour.length - 1].y * scale);
            g.setColor(Color.red);
            g.fillOval(contour[contour.length - 1].x * scale, contour[contour.length - 1].y * scale, 4, 4);
        }

        return wireFrame;
    }

    private BufferedImage generateSkinImage(boolean small, BufferedImage originalImage) {
        BufferedImage skin;
        if (useSkinDetection) {
            skin = imageProcessingService.toBinaryUsingSkin(originalImage);
        } else {
            skin = imageProcessingService.toBinaryImageUsingLuminosityThreshold(originalImage, threshold);
        }

        if (small) {
            skin = imageProcessingService.scale(skin, 4);
        }

        return skin;
    }

    private BufferedImage generateFdPlot(boolean small, double[] fd) {
        int w = 160;
        int h = 120;
        if (!small) {
            w = 640;
            h = 480;
        }

        if (fd != null) {
            XYSeries serie2D = new XYSeries("");

            for (int i = 0; i < fd.length; i++) {
                serie2D.add(i, fd[i]);
            }

            XYDataset xyDataSet = new XYSeriesCollection(serie2D);
            JFreeChart chart = ChartFactory.createXYLineChart("", "", "", xyDataSet, PlotOrientation.VERTICAL, false, false, false);

            return chart.createBufferedImage(w, h);
        }

        return new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    }

    public void setComputePrediction(boolean computePrediction) {
        this.computePrediction = computePrediction;
    }

    public void setLuminosity(double luminosity) {
        this.luminosity = luminosity;
    }

    public void setUseSkinDetection(boolean useSkinDetection) {
        this.useSkinDetection = useSkinDetection;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public boolean computesPrediction() {
        return computePrediction;
    }
}
