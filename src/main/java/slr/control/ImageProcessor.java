package slr.control;

import org.apache.commons.math3.complex.Complex;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slr.services.ImageProcessingService;
import slr.ui.SLRWindow;
import slr.services.impl.DefaultImageProcessingService;
import slr.utils.Constants;
import slr.utils.MathUtils;
import slr.services.PredictionService;

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

            BufferedImage iluminatedImage = imageProcessingService.iluminate(crtFrame, luminosity);
            BufferedImage preparedImg = preProcessImage(iluminatedImage, useSkinDetection, threshold);

            Point[] contour = imageProcessingService.getContour(preparedImg);
            contour = normalizeAsPoints(contour);

            double[] shapeDescription = MathUtils.computeCentroidDistance(contour);

            if (shapeDescription.length != 32) {
                System.out.println("SKIP CURRENT FRAME");
                return;
            }

            Complex[] FT = MathUtils.computeFourierTransforms(shapeDescription);
            FT = MathUtils.normalizeFourierTransforms(FT);
            double[] FD = MathUtils.computeFourierDescriptors(FT);

            if (computePrediction) {
                double[] crtPrediction = predictionService.predict(FD);

                if (crtFrameNr < Constants.FRAMES_TO_RECOGNIZE) {
                    crtFrameNr++;
                    int pozMax = max(crtPrediction);
                    overallPrediction[pozMax]++;
                } else {
                    mainWindow.setOutput(overallPrediction);
                    crtFrameNr = 0;
                    overallPrediction = new int[24];
                }
            }


            BufferedImage smallIluminated = imageProcessingService.resizeImage(iluminatedImage, 4);
            BufferedImage smallWireFrame = generateWireFrame(contour, true);
            BufferedImage smallSkin = generateSkin(true, iluminatedImage);
            BufferedImage smallFD = generateFD(true, FD);

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
            mainWindow.renderSmallIluminated(smallIluminated);
    }

    private int max(double[] output) {
        int m = 0;
        for (int i = 1; i < output.length; i++) {
            if (output[i] > output[m]) {
                m = i;
            }
        }
        return m;
    }

    private BufferedImage generateWireFrame(Point[] contour, boolean small) {

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

    private BufferedImage generateSkin(boolean small, BufferedImage originalImage) {
        BufferedImage skin;
        if (useSkinDetection) {
            skin = imageProcessingService.convertToBinaryUsingSkin(originalImage);
        } else {
            skin = imageProcessingService.convertToBinaryUsingThreshold(originalImage, threshold);
        }

        if (small) {
            skin = imageProcessingService.resizeImage(skin, 4);
        }

        return skin;
    }

    private BufferedImage generateFD(boolean small, double[] fd) {
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
            BufferedImage fdChart = chart.createBufferedImage(w, h);

            return fdChart;
        }

        return new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    }

    private BufferedImage preProcessImage(BufferedImage img, boolean useSkinDetection, int threshold) {
        BufferedImage preparedImage = new BufferedImage(img.getWidth(), img.getHeight(), 1);
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                preparedImage.setRGB(i, j, img.getRGB(i, j));
            }
        }


        int scale = img.getWidth() / 160;
        preparedImage = imageProcessingService.resizeImage(preparedImage, scale);
        //preparedImage = imageProcessor.iluminate(preparedImage, luminosity);

        if (useSkinDetection) {
            preparedImage = imageProcessingService.convertToBinaryUsingSkin(preparedImage);
        } else {
            preparedImage = imageProcessingService.convertToBinaryUsingThreshold(preparedImage, threshold);
        }

        preparedImage = imageProcessingService.removeNoise(preparedImage);
        //preparedImage = imageProcessor.detectEdges(preparedImage);

        return preparedImage;
    }

    private Point[] normalizeAsPoints(Point[] boundary) {
        return MathUtils.normalizeShapeSize(boundary, Constants.LARGE_SHAPE_SIZE);
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
