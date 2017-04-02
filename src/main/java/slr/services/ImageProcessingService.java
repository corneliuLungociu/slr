package slr.services;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface ImageProcessingService {

    double[] extractFeatures(boolean useSkinDetection, int luminosityThreshold, BufferedImage image, double luminosity);

    Point[] extractContour(BufferedImage image, boolean useSkinDetection, int luminosityThreshold, double luminosity);

    BufferedImage toBinaryUsingSkin(BufferedImage image);

    BufferedImage toBinaryImageUsingLuminosityThreshold(BufferedImage image, int luminosityThreshold);

    BufferedImage illuminate(BufferedImage image, double amount);

    BufferedImage scale(BufferedImage img, int scale);

    // TODO: make some tests with these
    BufferedImage detectEdgesCanny(BufferedImage image);

    BufferedImage detectEdgesEdgeFilter(BufferedImage image);

    // TODO: use this to collect more features?
    Point[][] getContourForTwoShapes(BufferedImage img);

    BufferedImage convertToGrayScale(BufferedImage image);
}
