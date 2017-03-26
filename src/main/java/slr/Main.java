/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package slr;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.math3.complex.Complex;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import slr.Ui.SLRWindow;
import slr.Ui.Test;
import slr.control.controlUnit.SystemControl;

/**
 *
 * @author corneliu
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
            SLRWindow window = new SLRWindow();
            window.setVisible(true);
    }

}
