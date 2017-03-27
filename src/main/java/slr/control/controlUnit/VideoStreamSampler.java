package slr.control.controlUnit;

import com.github.sarxos.webcam.Webcam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class VideoStreamSampler {

    @Autowired
    private ImageProcessor imageProcessor;

    private Thread crtThread;

    public VideoStreamSampler(ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
    }

    public void start(Webcam webcam) {
        if (crtThread != null) {
            throw new IllegalStateException("The image sampling is already started.");
        }

        crtThread = new Thread(() -> {
            proneImages(webcam);
        });

        crtThread.start();
    }

    public void stop() {
        try {
            crtThread.interrupt();
            crtThread.join();
            crtThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void proneImages(Webcam webcam) {
        while (true) {
            if (Thread.interrupted()) {
                return;
            }
            BufferedImage image = webcam.getImage();
            imageProcessor.update(image);
        }
    }
}
