package slr.logic.imageProcessing;

import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.ReduceNoiseFilter;
import com.jhlabs.image.ScaleFilter;
import com.jhlabs.image.ThresholdFilter;
import ij.ImagePlus;
import java.awt.Color;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.util.List;
import javax.xml.bind.DataBindingException;
import org.apache.commons.math3.complex.Complex;
import slr.logic.imageProcessing.contours.Contour;
import slr.logic.imageProcessing.contours.ContourTracer;
import slr.logic.utils.Constants;

/**
 *
 * @author corneliu
 */
public class ImageProcessor {
    public BufferedImage convertToGrayScale(BufferedImage image){
        GrayscaleFilter grayFilter = new GrayscaleFilter();

        BufferedImage grayImage = grayFilter.filter(image, null);
        return grayImage;
    }

    public BufferedImage removeNoise(BufferedImage image){
        ReduceNoiseFilter reduceNoiseFilter = new ReduceNoiseFilter();
        BufferedImage noNoise = reduceNoiseFilter.filter(image, null);
        return noNoise;
    }

    public BufferedImage convertToBinaryUsingSkin(BufferedImage image){

        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

        for (int i=0; i<image.getWidth(); i++){
            for (int j=0; j<image.getHeight(); j++){

                ColorModel cm = image.getColorModel();

                int r = cm.getRed(image.getRGB(i, j));
                int g = cm.getGreen(image.getRGB(i, j));
                int b = cm.getBlue(image.getRGB(i, j));

                if (isSkinRGB_L(r, g, b) && isSkinRGB_G(r, g, b)){
                        newImage.setRGB(i, j, image.getRGB(i, j));
                        
                        for (int k = i - 2; k<i + 2; k++){
                            for (int l = j -2; l < j+2; l++){
                                if (k > 0 && k < image.getWidth()
                                        && l > 0&& l < image.getHeight()){
                                    newImage.setRGB(k, l, image.getRGB(i, j));
                                }
                            }
                        }
                }else{
                    //newImage.setRGB(i, j, Color.BLACK.getRGB());
                }
            }
        }

        return newImage;
    }

    public BufferedImage convertToBinaryUsingThreshold(BufferedImage image, int thresshold){
        ThresholdFilter thresholdFilter = new ThresholdFilter(thresshold);
        return thresholdFilter.filter(image, null);
    }

    public BufferedImage iluminate(BufferedImage image, double amount){
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), 1);

        for (int i=0; i<image.getWidth(); i++){
            for (int j=0; j<image.getHeight(); j++){
                Color c = new Color(image.getRGB(i, j));
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();
                int m = (r+g+b)/3;

                int value = (int)(amount*Constants.LUMINOSITY);
                r += value;
                g += value;
                b += value;

                r = r>255?255:r;
                g = g>255?255:g;
                b = b>255?255:b;

                r = r<0?0:r;
                g = g<0?0:g;
                b = b<0?0:b;

                c = new Color(r, g, b);
                newImage.setRGB(i, j, c.getRGB());

            }
        }
        return newImage;
    }

    private int max(int a, int b, int c){
        int d = a>b?a:b;
        return d>c?d:c;
    }

    private int min(int a, int b, int c){
        int d = a<b?a:b;
        return d<c?d:c;
    }

    private boolean isSkinRGB_L(int r, int g, int b){
        int tmp = r-g;
        if (tmp < 0) tmp = -tmp;

        if (r > 95 && g > 40 && b > 20 && max(r, g, b) - min(r, g, b) > 15)
        {
            if ((tmp > 15) && r > g && r > b)
            {
                return true;
            }
        }
        else if (r > 220 && g > 210 && b > 170)
        {
            if ((tmp > 25) && r > b && g > b)
            {
                return true;
            }
        }
        return false;
    }

    private boolean isSkinRGB_G(int r, int g, int b){
        if (((0.836 * g) - 14) < b && b < ((0.836 * g) + 44)){
            return true;
        }
        return false;
    }

    public BufferedImage detectEdgesCanny(BufferedImage image){
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
            BufferedImage edges = detector.getEdgesImage();

            return edges;
    }

    public BufferedImage detectEdges(BufferedImage image){
        // TO DO
        EdgeFilter edgeFilter = new EdgeFilter();
        return edgeFilter.filter(image, null);
    }

    public BufferedImage resizeImage(BufferedImage img, int scale){
        ScaleFilter scaleFilter = new ScaleFilter(img.getWidth()/scale, img.getHeight()/scale);
        BufferedImage scaledImage = new BufferedImage(img.getWidth()/scale, img.getHeight()/scale, img.getType());
        scaleFilter.filter(img, scaledImage);

        return scaledImage;
    }

    public Point[] getContour(BufferedImage img){
        ImagePlus imgp = new ImagePlus("", img);
        ContourTracer contourTracer = new ContourTracer(imgp.getProcessor());
        List<Contour> contours = contourTracer.getOuterContours();
        
        Contour longestContour = null;
        if (!contours.isEmpty()){
            longestContour = contours.get(0);
            for (int i=1; i<contours.size(); i++){
                if (contours.get(i).getLength() > longestContour.getLength()){
                    longestContour = contours.get(i);
                }
            }

            List<Point> points = longestContour.getPoints();
            return points.toArray(new Point[0]);
        }

        return new Point[0];
    }

    public Point[][] getContourForTwoShapes(BufferedImage img){
        ImagePlus imgp = new ImagePlus("", img);
        ContourTracer contourTracer = new ContourTracer(imgp.getProcessor());
        List<Contour> contours = contourTracer.getOuterContours();

        Contour longestContour = contours.get(0);
        Contour secondContour = null;
        for (int i=1; i<contours.size(); i++){
            if (contours.get(i).getLength() >= longestContour.getLength()){
                secondContour = longestContour;
                longestContour = contours.get(i);
            }else if (secondContour==null || contours.get(i).getLength() > secondContour.getLength()){
                secondContour = contours.get(i);
            }
        }

        Point[][] all = new Point[2][];

        List<Point> contur1AsList = longestContour.getPoints();
        Point[] contur1AsArray = contur1AsList.toArray(new Point[0]);
        all[0] = contur1AsArray;

        if (secondContour != null){
            List<Point> contur2AsList = secondContour.getPoints();
            Point[] contur2AsArray = contur2AsList.toArray(new Point[0]);
            all[1] = contur2AsArray;
        }else{
            all[1] = new Point[0];
        }

        return all;
    }

    public BufferedImage imgPlusToBufferedImage(ImagePlus imgp){
         int h = imgp.getHeight();
         int w = imgp.getWidth();

         ij.process.ImageProcessor imgproc = imgp.getProcessor();

         BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
         for (int i=1; i<w; i++){
             for (int j=1; j<h; j++){
                bi.setRGB(i,j, imgproc.getPixel(i, j));
             }
         }

         return bi;
    }
}