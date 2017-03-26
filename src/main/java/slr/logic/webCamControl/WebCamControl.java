/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package slr.logic.webCamControl;

import ij.ImagePlus;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.CannotRealizeException;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

/**
 *
 * @author corneliu
 */
public class WebCamControl extends Thread{
    private WebCamObservable observable;
    private ImagePlus crtFrame;
    private String webCamName;
    private Player player;

    public WebCamControl(String webCamName){
        observable = new WebCamObservable();
        this.webCamName = webCamName;
    }

    public void addObserver(Observer obs){
        observable.addObserver(obs);
    }

    public ImagePlus getCrtFrame(){
        return crtFrame;
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

    public void run(){
        try {
            // Create capture device
            //CaptureDeviceInfo deviceInfo = CaptureDeviceManager.getDevice("vfw:Microsoft WDM Image Capture (Win32):0");
            CaptureDeviceInfo deviceInfo = CaptureDeviceManager.getDevice(webCamName);
            player = Manager.createRealizedPlayer(deviceInfo.getLocator());
            player.start();

            // Wait a few seconds for camera to initialise (otherwise img==null)
            Thread.sleep(2500);
            FrameGrabbingControl frameGrabber = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");

            boolean cond = true;
            while (cond){

                Buffer buf = frameGrabber.grabFrame();
                // Convert frame to an buffered image so it can be processed and saved
                Image img = new BufferToImage((VideoFormat) buf.getFormat()).createImage(buf);

                crtFrame = new ImagePlus("", img);
                
                observable.change();
                observable.notifyObservers();
            }


        } catch (IOException ex) {
            Logger.getLogger(WebCamControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoPlayerException ex) {
            Logger.getLogger(WebCamControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CannotRealizeException ex) {
            Logger.getLogger(WebCamControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
                Logger.getLogger(WebCamControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopWebCam(){
        player.stop();
        player.deallocate();
    }
}
