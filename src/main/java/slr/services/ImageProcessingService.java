package slr.services;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface ImageProcessingService {

    BufferedImage preProcessImage(BufferedImage img, boolean useSkinDetection, int luminosityThreshold);

    BufferedImage convertToBinaryUsingSkin(BufferedImage image);

    BufferedImage toBinaryImageUsingLuminosityThreshold(BufferedImage image, int luminosityThreshold);

    BufferedImage illuminate(BufferedImage image, double amount);

    BufferedImage scale(BufferedImage img, int scale);

    Point[] getContourOfLargestShape(BufferedImage img);

    Point[] reduceDataSize(Point[] boundary);

    // TODO: make some tests with these
    BufferedImage detectEdgesCanny(BufferedImage image);

    BufferedImage detectEdgesEdgeFilter(BufferedImage image);

    // TODO: use this to collect more features?
    Point[][] getContourForTwoShapes(BufferedImage img);

    BufferedImage convertToGrayScale(BufferedImage image);
}
