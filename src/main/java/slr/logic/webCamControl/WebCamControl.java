/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package slr.logic.webCamControl;

import com.github.sarxos.webcam.Webcam;

import java.awt.image.BufferedImage;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author corneliu
 */
public class WebCamControl implements Runnable {
    private WebCamObservable observable;
    private BufferedImage crtFrame;
    private Webcam webCam;

    public WebCamControl(Webcam webCam){
        observable = new WebCamObservable();
        this.webCam = webCam;
    }

    public void addObserver(Observer obs){
        observable.addObserver(obs);
    }

    public BufferedImage getCrtFrame(){
        return crtFrame;
    }

    public void run(){
        try {
            if (!webCam.open()) {
                throw new RuntimeException("Failed to start the web cam: " + webCam.getName());
            }

            while (true){
                crtFrame = webCam.getImage();

                observable.change();
                observable.notifyObservers();
            }


        } catch (Exception ex) {
            Logger.getLogger(WebCamControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopWebCam(){
        webCam.close();
    }
}
