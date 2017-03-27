package slr.utils;

import java.awt.Point;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 *
 * @author corneliu
 */
public class MathUtils {

    public static Point computeCentroid(Point[] boundary){
        Point c = new Point();
        double x = 0;
        double y = 0;
        for (Point p:boundary){
            x += p.x;
            y += p.y;
        }
        x = x/boundary.length;
        y = y/boundary.length;

        c.setLocation(x, y);

        return c;
    }

    public static double[] computeCentroidDistance(Point[] boundary){
        double[] dist = new double[boundary.length];
        Point centroid = computeCentroid(boundary);

        for (int i=0; i<boundary.length; i++){
            dist[i] = boundary[i].distance(centroid);
        }
        
        return dist;
    }

    public static double[] computeCentroidDistance(Point[] boundary, Point centroid){
        double[] dist = new double[boundary.length];

        for (int i=0; i<boundary.length; i++){
            dist[i] = boundary[i].distance(centroid);
        }

        return dist;
    }

    public static double[] normalizeShapeSize(double[] boundary, int size){
        double[] normal = new double[size];
        int step = boundary.length / size;

        for (int i=0; i<size; i++){
            normal[i] = boundary[i*step];
        }

        return normal;
    }

    public static Point[] normalizeShapeSize(Point[] boundary, int size){
        if (boundary.length < size){
            return boundary;
        }
        Point[] normal = new Point[size];
        int step = boundary.length / size;
        int  remaining = boundary.length % size;

        //int rest = (int)Math.ceil((double)remaining / (double)size);
        int restAdded = 0;

        for (int i=0; i<size; i++){
            normal[i] = boundary[i*step + restAdded];
            if (restAdded < remaining){
                restAdded++;
            }
        }

        return normal;
    }

    public static Complex[] computeFourierTransforms(double[] contur) {
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        return fft.transform(contur, TransformType.FORWARD);
    }

    public static Complex[] normalizeFourierTransforms(Complex[] fd) {
        Complex[] normal = new Complex[fd.length];
        normal[0] = fd[0];
        for (int i=1; i<fd.length; i++){
            normal[i] = fd[i].divide(normal[0]);
        }
        return normal;
    }

    /**
     *
     * @param fourierTransforms
     * @return
     * use the polar reprezentation of the complex numbers and return the magnitude
     * (ignore the phase)
     */
    public static double[] computeFourierDescriptors(Complex[] fourierTransforms){
        double[] descriptors = new double[fourierTransforms.length/2 - 1];

        for (int i=1; i<fourierTransforms.length/2; i++){
            double im = fourierTransforms[i].getImaginary();
            double real = fourierTransforms[i].getReal();

            descriptors[i-1] = Math.sqrt(im*im + real*real);
        }

        return descriptors;
    }
}
