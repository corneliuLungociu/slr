package slr.control.controlUnit;

//import com.sun.media.protocol.vfw.VFWDeviceQuery;

import com.github.sarxos.webcam.Webcam;
import org.apache.commons.math3.complex.Complex;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import slr.Ui.DisplayFDWindow;
import slr.Ui.SLRWindow;
import slr.logic.imageProcessing.ImageProcessor;
import slr.logic.neuralNetwork.NeuralNetwork;
import slr.logic.neuralNetwork.errors.NeuralNetworkException;
import slr.logic.neuralNetwork.training.TrainingElement;
import slr.logic.neuralNetwork.training.TrainingSet;
import slr.logic.utils.Constants;
import slr.logic.utils.MathUtils;
import slr.logic.webCamControl.WebCamControl;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author corneliu
 */
public class SlrWindowController implements Observer {
    private ImageProcessor imageProcessor;
    private WebCamControl webCamControl;
    private NeuralNetwork neuralNetwork;
    private TrainingSet dataSet;
    private BufferedImage imageToDisplay;
    private BufferedImage loadedImage;
    private double[] FD;
    private SLRWindow mainWindow;

    private boolean webCamStarted = false;
    private boolean webCamPaused = false;
    private int viewType = 0;
    private double luminosity = 0;
    private String saveLocation = Constants.DEFAULT_SAVE_LOCATION;
    private boolean useSkinDetection;
    private int threshold;
    private boolean recognize = false;

    private int frameNR = 0;
    private int[] rezultat = new int[24];

    public SlrWindowController(SLRWindow window ){
        imageProcessor = new ImageProcessor();
        
        //initNeuralNetwork();
        //trainNeuralNetwork();
        
        try {
            //saveNeuralNetwork();
            loadNeuralNetwork();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SlrWindowController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SlrWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.mainWindow = window;
    }


//------------------------------    WEB CAM CONTROL     ------------------------
    public void detectAvailableWebCams(DefaultComboBoxModel<Webcam> model) {
        Webcam.getWebcams().stream().forEach(model::addElement);
    }

    public void startWebCam(Webcam selectedDevice){
        this.webCamStarted = true;
        this.webCamPaused = false;

        webCamControl = new WebCamControl(selectedDevice);
        webCamControl.addObserver(this);
        new Thread(webCamControl).start();
    }

    public void stopWebCam(){
        this.webCamStarted = false;
        this.webCamPaused = true;

        webCamControl.stopWebCam();
    }

    public void takeSnapshot() throws IOException{

        BufferedImage snapshot = new BufferedImage(imageToDisplay.getWidth(), imageToDisplay.getHeight(), imageToDisplay.getType());
        for (int i=1; i<snapshot.getWidth(); i++){
            for (int j=1; j<snapshot.getHeight(); j++){
                snapshot.setRGB(i, j, imageToDisplay.getRGB(i, j));
            }
        }

        String path = mainWindow.getFilePath();
        if (path != null){
            ImageIO.write( snapshot, "jpeg", new File (path) );
        }
        
    }


//----------------------------  IMAGE PROCESSING    ----------------------------
    public BufferedImage prepareImage(BufferedImage img, boolean useSkinDetection, int threshold){
        BufferedImage preparedImage = new BufferedImage(img.getWidth(), img.getHeight(), 1);
        for (int i=0; i<img.getWidth(); i++){
            for (int j=0; j<img.getHeight(); j++){
                preparedImage.setRGB(i, j, img.getRGB(i,j));
            }
        }


        int scale = img.getWidth() / 160;
        preparedImage = imageProcessor.resizeImage(preparedImage, scale);
        //preparedImage = imageProcessor.iluminate(preparedImage, luminosity);

        if (useSkinDetection){
            preparedImage = imageProcessor.convertToBinaryUsingSkin(preparedImage);
        }else{
            preparedImage = imageProcessor.convertToBinaryUsingThreshold(preparedImage, threshold);
        }
        
        preparedImage = imageProcessor.removeNoise(preparedImage);
        //preparedImage = imageProcessor.detectEdges(preparedImage);

        return preparedImage;
    }
   
    public double[] normalizeShapeSize(Point[] boundary, int size){
        double[] normal = MathUtils.computeCentroidDistance(boundary);
        normal = MathUtils.normalizeShapeSize(normal, size);
        
        return normal;
    }

    public double[] normalizeShapeSize(Point[][] boundary){
        Point[] allPoints = new Point[boundary[0].length + boundary[1].length];
        for (int i=0; i<boundary[0].length; i++){
            allPoints[i] = boundary[0][i];
        }

        for (int i=0; i<boundary[1].length; i++){
            allPoints[boundary[0].length+i] = boundary[1][i];
        }

        double[] normal = MathUtils.computeCentroidDistance(allPoints);
        normal = MathUtils.normalizeShapeSize(normal, Constants.LARGE_SHAPE_SIZE);

        return normal;
    }

    public Point[] normalizeAsPoints(Point[] boundary){
        return MathUtils.normalizeShapeSize(boundary, Constants.LARGE_SHAPE_SIZE);
    }

    public Point[] normalizeAsPoints(Point[][] boundary){
        Point[] allPoints = new Point[boundary[0].length + boundary[1].length];
        for (int i=0; i<boundary[0].length; i++){
            allPoints[i] = boundary[0][i];
        }

        for (int i=0; i<boundary[1].length; i++){
            allPoints[boundary[0].length+i] = boundary[1][i];
        }
        return MathUtils.normalizeShapeSize(allPoints, Constants.LARGE_SHAPE_SIZE);
    }

    public double[] computeFourierDescriptors(BufferedImage preparedImage) {
        Point[] contur = imageProcessor.getContour(preparedImage);
        double[] shapeDescription = normalizeShapeSize(contur, Constants.LARGE_SHAPE_SIZE);
        Complex[] FT = MathUtils.computeFourierTransforms(shapeDescription);
        FT = MathUtils.normalizeFourierTransforms(FT);
        double[] FD = MathUtils.computeFourierDescriptors(FT);

        return FD;
    }

    public void update(Observable o, Object arg) {
        if (!webCamPaused) {
                BufferedImage buffImg = webCamControl.getCrtFrame();

                BufferedImage iluminatedImage = imageProcessor.iluminate(buffImg, luminosity);
                BufferedImage preparedImg = prepareImage(iluminatedImage, useSkinDetection, threshold);

                Point[] contour = imageProcessor.getContour(preparedImg);
                contour = normalizeAsPoints(contour);

                double[] shapeDescription = MathUtils.computeCentroidDistance(contour);
                try {
                    if (shapeDescription.length == 32){
                        Complex[] FT = MathUtils.computeFourierTransforms(shapeDescription);
                        FT = MathUtils.normalizeFourierTransforms(FT);
                        FD = MathUtils.computeFourierDescriptors(FT);

                        if (recognize){
                            neuralNetwork.feadForward(new TrainingElement(FD, null));

                            if (frameNR < Constants.FRAMES_TO_RECOGNIZE){
                                frameNR++;
                                int pozMax = max(neuralNetwork.getNetworkOutput());
                                rezultat[pozMax]++;
                            }else{
                                mainWindow.setOutput(rezultat);
                                frameNR = 0;
                                rezultat = new int[24];
                            }
                        }


                    }

                } catch (NeuralNetworkException ex) {
                    Logger.getLogger(SlrWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }

                BufferedImage smallIluminated = imageProcessor.resizeImage(iluminatedImage, 4);
                BufferedImage smallWireFrame = generateWireFrame(contour, true);
                BufferedImage smallSkin = generateSkin(true, iluminatedImage);
                BufferedImage smallFD = generateFD(true, FD);

                switch (viewType){
                    case Constants.NORMAL_VIEW:{
                        imageToDisplay = iluminatedImage;
                        break;
                    }
                    case Constants.WIREFRAME_VIEW:{
                        imageToDisplay = generateWireFrame(contour, false);
                        break;
                    }
                    case Constants.SKIN_VIEW:{
                        imageToDisplay = generateSkin(false, iluminatedImage);
                        break;
                    }
                    default:{
                        // FD View
                        imageToDisplay = generateFD(false, FD);
                    }
                }

                mainWindow.renderFrame(imageToDisplay);

                mainWindow.renderFD(smallFD);
                mainWindow.renderSkin(smallSkin);
                mainWindow.renderWire(smallWireFrame);
                mainWindow.renderSmallIluminated(smallIluminated);

        }
    }

        private int max(double[] output){
            int m = 0;
            for (int i=1; i<output.length; i++){
                if (output[i] > output[m]){
                    m = i;
                }
            }
            return m;
        }

    public BufferedImage generateWireFrame(Point[] contour, boolean small){

        int scale = 1;
        int w = 160;
        int h = 120;
        if (!small){
            w = 640;
            h = 480;
            scale = 4;
        }
        
        BufferedImage wireFrame = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = wireFrame.createGraphics();
        if (contour != null && contour.length > 2){
            for (int i=0; i<contour.length-1; i++){
                g.drawLine(contour[i].x*scale, contour[i].y*scale, contour[i+1].x*scale, contour[i+1].y*scale);
                g.setColor(Color.red);
                g.fillOval(contour[i].x*scale, contour[i].y*scale, 4, 4);
                g.setColor(Color.white);
            }

            g.drawLine(contour[0].x*scale, contour[0].y*scale, contour[contour.length-1].x*scale, contour[contour.length-1].y*scale);
            g.setColor(Color.red);
            g.fillOval(contour[contour.length-1].x*scale, contour[contour.length-1].y*scale, 4, 4);
        }

        return wireFrame;
    }

    public BufferedImage generateSkin(boolean small, BufferedImage originalImage){
        BufferedImage skin;
        if (useSkinDetection){
            skin = imageProcessor.convertToBinaryUsingSkin(originalImage);
        }else{
            skin = imageProcessor.convertToBinaryUsingThreshold(originalImage, threshold);
        }

        if (small){
            skin = imageProcessor.resizeImage(skin, 4);
        }

        return skin;
    }

    public BufferedImage generateFD(boolean small, double[] fd){
        int w = 160;
        int h = 120;
        if (!small){
            w = 640;
            h = 480;
        }

        if (fd != null){
            XYSeries serie2D = new XYSeries("");

            for (int i=0; i<fd.length; i++){
                serie2D.add(i, fd[i]);
            }

            XYDataset xyDataSet = new XYSeriesCollection(serie2D);
            JFreeChart chart = ChartFactory.createXYLineChart("","","",xyDataSet,PlotOrientation.VERTICAL,false, false, false);
            BufferedImage fdChart = chart.createBufferedImage(w, h);

            return fdChart;
        }
        
        return new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    }

//-----------------------------     NEURAL NETWORK -----------------------------
    private void initNeuralNetwork() {
        initForSLR();
        //initForIrisDataset();
        //initForOR();
        //initForXOR();
    }

    public void trainNeuralNetwork(){
        try {
            neuralNetwork.train(dataSet, Constants.LEARNING_RATE, Constants.ALPHA);
        } catch (NeuralNetworkException ex) {
            Logger.getLogger(SlrWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveNeuralNetwork() throws IOException{
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream("neuralNetwork.txt"));
        out.writeObject(neuralNetwork);
        out.close();
    }

    public void loadNeuralNetwork() throws IOException, ClassNotFoundException{
        File file = new File("neuralNetwork.txt");
        ObjectInputStream in = new ObjectInputStream(this.getClass().getResourceAsStream("/neuralNetwork.txt"));
        // Deserialize the object
        neuralNetwork = (NeuralNetwork) in.readObject();
        in.close();
    }

    private void initForSLR() {
        neuralNetwork = new NeuralNetwork(15, 24);
        neuralNetwork.addHiddenLayer(20);
        dataSet = getDataSetForSLR();
    }

    private TrainingSet getDataSetForSLR() {
        TrainingSet ts = new TrainingSet();

        try {
            BufferedReader br = new BufferedReader(new FileReader("signLanguageTrainingSet/trainingSet.txt"));
            String linie = br.readLine();
            while (linie != null){
                String[] values = linie.split(",");

                double[] inputValues = new double[15];
                for (int i=0; i<15; i++){
                    inputValues[i] = Double.parseDouble(values[i].trim());
                }

                double[] expectedResponse = new double[24];
                expectedResponse[Integer.parseInt(values[15].trim())] = 1;

                TrainingElement te = new TrainingElement(inputValues, expectedResponse);
                ts.addTrainingElement(te);

                linie = br.readLine();
            }

            br.close();

        } catch (IOException ex) {
            Logger.getLogger(SlrWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ts;
    }
    
//--------------------------    FOR TESTING FUNCTIONALITY   --------------------
    public void loadImage(){
        JFileChooser c = new JFileChooser(saveLocation);
        c.setMultiSelectionEnabled(false);
        c.setAcceptAllFileFilterUsed(false);
        c.setFileFilter(new FileNameExtensionFilter("JPEG file", "jpeg", "jpg"));
        c.setApproveButtonToolTipText("Load Image");
        c.setDialogTitle("Load Image");

        int option = c.showOpenDialog(null);
        if (option == JFileChooser.APPROVE_OPTION){
            try {
                String fileName = c.getSelectedFile().getAbsolutePath();
                loadedImage = ImageIO.read(new File(fileName));
                int scale = loadedImage.getWidth() / 320;
                loadedImage = imageProcessor.resizeImage(loadedImage, scale);

                mainWindow.renderLoadedImage(loadedImage);
            } catch (IOException ex) {
                Logger.getLogger(SlrWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void recognize(){

        double[] expected = new double[24];
        expected[Constants.ALPHABET.indexOf(mainWindow.getSelectedLeter())] = 1;

        BufferedImage preparedImage = prepareImage(loadedImage, useSkinDetection, threshold);

        Point[] contour = imageProcessor.getContour(preparedImage);
        contour = normalizeAsPoints(contour);

        Graphics2D g = loadedImage.createGraphics();
        if (contour != null && contour.length > 2){
            for (int i=0; i<contour.length-1; i++){
                g.drawLine(contour[i].x*2, contour[i].y*2, contour[i+1].x*2, contour[i+1].y*2);
                g.setColor(Color.red);
                g.fillOval(contour[i].x*2, contour[i].y*2, 4, 4);
                g.setColor(Color.white);
            }

            g.drawLine(contour[0].x*2, contour[0].y*2, contour[contour.length-1].x*2, contour[contour.length-1].y*2);
            g.setColor(Color.red);
            g.fillOval(contour[contour.length-1].x*2, contour[contour.length-1].y*2, 4, 4);
        }


        double[] shapeDescription = MathUtils.computeCentroidDistance(contour);
        try {
            if (shapeDescription.length == 32){
                Complex[] FT = MathUtils.computeFourierTransforms(shapeDescription);
                FT = MathUtils.normalizeFourierTransforms(FT);
                FD = MathUtils.computeFourierDescriptors(FT);

                neuralNetwork.feadForward(new TrainingElement(FD, null));


                BufferedImage error = generateErrorGraphic(expected, neuralNetwork.getNetworkOutput());
                mainWindow.renderErrorImage(error);
            }

        } catch (NeuralNetworkException ex) {
            Logger.getLogger(SlrWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
//        catch (MathException ex) {
//            Logger.getLogger(SystemControl.class.getName()).log(Level.SEVERE, null, ex);
//        }

        mainWindow.renderLoadedImage(loadedImage);
        
    }

    private BufferedImage generateErrorGraphic(double[] expected, double[] output){
        int w = 460;
        int h = 280;

        XYSeries expectedSeries = new XYSeries("Expected");
        XYSeries outputSeries = new XYSeries("Actual Output");

        for (int i=0; i<expected.length; i++){
            expectedSeries.add(i, expected[i]);
            outputSeries.add(i, output[i]);
        }
        XYDataset expectedData = new XYSeriesCollection(expectedSeries);
        XYDataset outputData = new XYSeriesCollection(outputSeries);

        JFreeChart chart = ChartFactory.createXYLineChart("Error Graphic","Output","FD",expectedData,PlotOrientation.VERTICAL,true, false, false);
        chart.getXYPlot().setDataset(1, outputData);

        //chart.getXYPlot().setRenderer(0, new DeviationRenderer(true, true));
        //chart.getXYPlot().setRenderer(1, new DeviationRenderer(true, true));

        chart.getXYPlot().setRenderer(0, new XYLineAndShapeRenderer(true, true));
        chart.getXYPlot().setRenderer(1, new XYLineAndShapeRenderer(true, true));

        BufferedImage fdChart = chart.createBufferedImage(w, h);

        return fdChart;
    }


//------------------------------    GETERS AND SETERS   ------------------------
    public void setLuminosity(double luminosity) {
        this.luminosity = (luminosity-50) / 100;
    }

    public void wireFrameView(){
        this.viewType = Constants.WIREFRAME_VIEW;
    }

    public void skinView(){
        this.viewType = Constants.SKIN_VIEW;
    }

    public void normalView(){
        this.viewType = Constants.NORMAL_VIEW;
    }

    public void fdView(){
        this.viewType = Constants.FD_VIEW;
    }

    public String getSaveLocation() {
        return saveLocation;
    }

    public void setSaveLocation(String saveLocation) {
        this.saveLocation = saveLocation;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public boolean isUseSkinDetection() {
        return useSkinDetection;
    }

    public void setUseSkinDetection(boolean useSkinDetection) {
        this.useSkinDetection = useSkinDetection;
    }

    public void displayFourierDescriptors(){
        new DisplayFDWindow(FD).setVisible(true);
    }

    public boolean isRecognize() {
        return recognize;
    }

    public void setRecognize(boolean recognize) {
        this.recognize = recognize;
    }

    public boolean isWebCamPaused() {
        return webCamPaused;
    }

    public void setWebCamPaused(boolean webCamPaused) {
        this.webCamPaused = webCamPaused;
    }

    public boolean isWebCamStarted() {
        return webCamStarted;
    }

    public void setWebCamStarted(boolean webCamStarted) {
        this.webCamStarted = webCamStarted;
    }
}