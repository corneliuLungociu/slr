package slr.services.impl;

import com.jhlabs.image.*;
import ij.ImagePlus;
import org.springframework.stereotype.Service;
import slr.services.ImageProcessingService;
import slr.services.impl.image.CannyEdgeDetector;
import slr.services.impl.image.contours.Contour;
import slr.services.impl.image.contours.ContourTracer;
import slr.utils.Constants;
import slr.utils.MathUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.List;

@Service
public class DefaultImageProcessingService implements ImageProcessingService {

    @Override
    public BufferedImage preProcessImage(BufferedImage img, boolean useSkinDetection, int luminosityThreshold) {
        BufferedImage preparedImage = new BufferedImage(img.getWidth(), img.getHeight(), 1);
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                preparedImage.setRGB(i, j, img.getRGB(i, j));
            }
        }

        int scale = img.getWidth() / 160;
        preparedImage = scale(preparedImage, scale);
        //preparedImage = imageProcessor.iluminate(preparedImage, luminosity);

        if (useSkinDetection) {
            preparedImage = convertToBinaryUsingSkin(preparedImage);
        } else {
            preparedImage = toBinaryImageUsingLuminosityThreshold(preparedImage, luminosityThreshold);
        }

        preparedImage = removeNoise(preparedImage);
        //preparedImage = imageProcessor.detectEdges(preparedImage);

        return preparedImage;
    }


    @Override
    public BufferedImage convertToGrayScale(BufferedImage image) {
        GrayscaleFilter grayFilter = new GrayscaleFilter();

        BufferedImage grayImage = grayFilter.filter(image, null);
        return grayImage;
    }

    private BufferedImage removeNoise(BufferedImage image) {
        ReduceNoiseFilter reduceNoiseFilter = new ReduceNoiseFilter();
        return reduceNoiseFilter.filter(image, null);
    }

    @Override
    public BufferedImage convertToBinaryUsingSkin(BufferedImage image) {

        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {

                ColorModel cm = image.getColorModel();

                int r = cm.getRed(image.getRGB(i, j));
                int g = cm.getGreen(image.getRGB(i, j));
                int b = cm.getBlue(image.getRGB(i, j));

                if (isSkinRGB_L(r, g, b) && isSkinRGB_G(r, g, b)) {
                    newImage.setRGB(i, j, image.getRGB(i, j));

                    for (int k = i - 2; k < i + 2; k++) {
                        for (int l = j - 2; l < j + 2; l++) {
                            if (k > 0 && k < image.getWidth()
                                    && l > 0 && l < image.getHeight()) {
                                newImage.setRGB(k, l, image.getRGB(i, j));
                            }
                        }
                    }
                } else {
                    //newImage.setRGB(i, j, Color.BLACK.getRGB());
                }
            }
        }

        return newImage;
    }

    @Override
    public BufferedImage toBinaryImageUsingLuminosityThreshold(BufferedImage image, int luminosityThreshold) {
        ThresholdFilter thresholdFilter = new ThresholdFilter(luminosityThreshold);
        return thresholdFilter.filter(image, null);
    }

    @Override
    public BufferedImage illuminate(BufferedImage image, double amount) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), 1);

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color c = new Color(image.getRGB(i, j));
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();
                int m = (r + g + b) / 3;

                int value = (int) (amount * Constants.LUMINOSITY);
                r += value;
                g += value;
                b += value;

                r = r > 255 ? 255 : r;
                g = g > 255 ? 255 : g;
                b = b > 255 ? 255 : b;

                r = r < 0 ? 0 : r;
                g = g < 0 ? 0 : g;
                b = b < 0 ? 0 : b;

                c = new Color(r, g, b);
                newImage.setRGB(i, j, c.getRGB());

            }
        }
        return newImage;
    }

    private int max(int a, int b, int c) {
        int d = a > b ? a : b;
        return d > c ? d : c;
    }

    private int min(int a, int b, int c) {
        int d = a < b ? a : b;
        return d < c ? d : c;
    }

    private boolean isSkinRGB_L(int r, int g, int b) {
        int tmp = r - g;
        if (tmp < 0) tmp = -tmp;

        if (r > 95 && g > 40 && b > 20 && max(r, g, b) - min(r, g, b) > 15) {
            if ((tmp > 15) && r > g && r > b) {
                return true;
            }
        } else if (r > 220 && g > 210 && b > 170) {
            if ((tmp > 25) && r > b && g > b) {
                return true;
            }
        }
        return false;
    }

    private boolean isSkinRGB_G(int r, int g, int b) {
        if (((0.836 * g) - 14) < b && b < ((0.836 * g) + 44)) {
            return true;
        }
        return false;
    }

    @Override
    public BufferedImage detectEdgesCanny(BufferedImage image) {
        CannyEdgeDetector detector = new CannyEdgeDetector();
        //adjust its parameters as desired
        // best values so far
        detector.setLowThreshold(4f);
        detector.setHighThreshold(10f);
        //detector.setLowThreshold(0.5f);
        //detector.setHighThreshold(20f);
        //apply it to an image
        detector.setSourceImage(image);
        detector.process();

        return detector.getEdgesImage();
    }

    @Override
    public BufferedImage detectEdgesEdgeFilter(BufferedImage image) {
        EdgeFilter edgeFilter = new EdgeFilter();
        return edgeFilter.filter(image, null);
    }

    @Override
    public BufferedImage scale(BufferedImage img, int scale) {
        ScaleFilter scaleFilter = new ScaleFilter(img.getWidth() / scale, img.getHeight() / scale);
        BufferedImage scaledImage = new BufferedImage(img.getWidth() / scale, img.getHeight() / scale, img.getType());
        scaleFilter.filter(img, scaledImage);

        return scaledImage;
    }

    @Override
    public Point[] getContourOfLargestShape(BufferedImage img) {
        ImagePlus imgp = new ImagePlus("", img);
        ContourTracer contourTracer = new ContourTracer(imgp.getProcessor());
        List<Contour> contours = contourTracer.getOuterContours();

        Contour longestContour;
        if (!contours.isEmpty()) {
            longestContour = contours.get(0);
            for (int i = 1; i < contours.size(); i++) {
                if (contours.get(i).getLength() > longestContour.getLength()) {
                    longestContour = contours.get(i);
                }
            }

            List<Point> points = longestContour.getPoints();
            return points.toArray(new Point[points.size()]);
        }

        return new Point[0];
    }

    @Override
    public Point[][] getContourForTwoShapes(BufferedImage img) {
        ImagePlus imgp = new ImagePlus("", img);
        ContourTracer contourTracer = new ContourTracer(imgp.getProcessor());
        List<Contour> contours = contourTracer.getOuterContours();

        Contour longestContour = contours.get(0);
        Contour secondContour = null;
        for (int i = 1; i < contours.size(); i++) {
            if (contours.get(i).getLength() >= longestContour.getLength()) {
                secondContour = longestContour;
                longestContour = contours.get(i);
            } else if (secondContour == null || contours.get(i).getLength() > secondContour.getLength()) {
                secondContour = contours.get(i);
            }
        }

        Point[][] all = new Point[2][];

        List<Point> contur1AsList = longestContour.getPoints();
        Point[] contur1AsArray = contur1AsList.toArray(new Point[0]);
        all[0] = contur1AsArray;

        if (secondContour != null) {
            List<Point> contur2AsList = secondContour.getPoints();
            Point[] contur2AsArray = contur2AsList.toArray(new Point[0]);
            all[1] = contur2AsArray;
        } else {
            all[1] = new Point[0];
        }

        return all;
    }

    @Override
    public Point[] reduceDataSize(Point[] boundary) {
        return MathUtils.normalizeShapeSize(boundary, Constants.LARGE_SHAPE_SIZE);
    }
}
