package slr.services;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface ImageProcessingService {

    BufferedImage convertToGrayScale(BufferedImage image);

    BufferedImage removeNoise(BufferedImage image);

    BufferedImage convertToBinaryUsingSkin(BufferedImage image);

    BufferedImage convertToBinaryUsingThreshold(BufferedImage image, int thresshold);

    BufferedImage iluminate(BufferedImage image, double amount);

    BufferedImage detectEdgesCanny(BufferedImage image);

    BufferedImage detectEdges(BufferedImage image);

    BufferedImage resizeImage(BufferedImage img, int scale);

    Point[] getContour(BufferedImage img);

    Point[][] getContourForTwoShapes(BufferedImage img);
}
