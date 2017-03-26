import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SwingCapturePanel extends Panel implements ActionListener {

    private final JButton captureButton;
    private final ImagePanel imagePanel;
    private final Webcam webcam;
    private final WebcamPanel webcamPanel;

    public SwingCapturePanel() {
        setLayout(new BorderLayout());
        setSize(320, 550);

        imagePanel = new ImagePanel();
        captureButton = new JButton("Capture");
        captureButton.addActionListener(this);

        webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setDisplayDebugInfo(true);
        webcamPanel.setImageSizeDisplayed(true);
        webcamPanel.setMirrored(true);

        add(webcamPanel, BorderLayout.NORTH);
        add(captureButton, BorderLayout.CENTER);
        add(imagePanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        Frame mainFrame = new Frame("SwingCapture");
        SwingCapturePanel capturePanel = new SwingCapturePanel();

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                capturePanel.webcam.close();
                System.exit(0);
            }
        });

        mainFrame.add("Center", capturePanel);
        mainFrame.pack();
        mainFrame.setSize(new Dimension(320, 550));
        mainFrame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        JComponent c = (JComponent) e.getSource();

        if (c == captureButton) {
            BufferedImage img = webcam.getImage();
            imagePanel.setImage(img);
            try {
                ImageIO.write(img, "JPG", new File("/tmp/capture.jpg"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    class ImagePanel extends Panel {
        public Image myimg = null;

        public ImagePanel() {
            setLayout(null);
            setSize(320, 240);
        }

        public void setImage(Image img) {
            this.myimg = img;
            repaint();
        }

        public void paint(Graphics g) {
            if (myimg != null) {
                g.drawImage(myimg, 0, 0, this);
            }
        }
    }

}
