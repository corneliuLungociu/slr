package slr.ui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import slr.control.SlrWindowController;
import slr.utils.Constants;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author corneliu
 */
@Component
public class SLRWindow extends javax.swing.JFrame {

    @Autowired
    private SlrWindowController windowController;

    private DefaultComboBoxModel<String> webCamComboModel;
    private DefaultComboBoxModel<String> alphabetModel;
    private DefaultComboBoxModel<String> alphabeForTeModel;

    private WebCamState webCamState = WebCamState.STOPPED;

    private BufferedImage loadedImage;
    private String imageFileLocation = Constants.DEFAULT_SAVE_LOCATION;
    private long trainingExamplesCnt;

    @PostConstruct
    public void init() {
        initComponents();
        detectionTypeButtonGroup.add(skinDetection);
        detectionTypeButtonGroup.add(luminosityDetection);

        // init webcam combobox
        webCamComboModel = new DefaultComboBoxModel<>();
        webCamList.setModel(webCamComboModel);

        // init alphabet combobox
        alphabetModel = new DefaultComboBoxModel<>();
        alphabeForTeModel = new DefaultComboBoxModel<>();
        for (char a = 'A'; a <= 'Z'; a++){
            alphabetModel.addElement(a + "");
            alphabeForTeModel.addElement(a + "");
        }
        alphabet.setModel(alphabetModel);
        alphabetForTeGeneration.setModel(alphabeForTeModel);

        trainingSetLocation.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                findNumberOfTrainingExamples();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                findNumberOfTrainingExamples();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                findNumberOfTrainingExamples();
            }
        });

        alphabetForTeGeneration.addActionListener(e -> findNumberOfTrainingExamples());
        alphabetForTeGeneration.setSelectedIndex(0);

        // init tabbed panel
        tabPane.addTab("Test SLR", TestSLR);
        tabPane.addChangeListener(this::tabChangedAction);

        minimize();
    }

    private void findNumberOfTrainingExamples() {
        if (StringUtils.isEmpty(trainingSetLocation.getText())) {
            return;
        }
        String location = trainingSetLocation.getText();
        String letter = (String) alphabetForTeGeneration.getSelectedItem();
        Path tePath = Paths.get(location);
        try {
            trainingExamplesCnt = Files.find(tePath, 1, (path, basicFileAttributes) -> path.getFileName().toString().startsWith(letter) && path.getFileName().toString().endsWith("jpg")).count();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void initController() {
        windowController.setLuminosity(luminosity.getValue());
        Set<String> availableWebCams = windowController.getAvailableWebCams();
        for (String availableWebCam : availableWebCams) {
            webCamComboModel.addElement(availableWebCam);
        }

        skinDetection.setSelected(true);
    }

    private void tabChangedAction(ChangeEvent e) {
        JTabbedPane pane = (JTabbedPane)e.getSource();

        int selectedTab = pane.getSelectedIndex();

        if (selectedTab == 1){
            resizeForTab2();
        }else{
            if (extendOptions.getText().equals(">")){
                minimize();
            }else{
                maximize();
            }
        }
    }

    private void minimize(){
        setSize(720, getSize().height);
    }

    private void maximize(){
        setSize(1080, getSize().height);
    }

    private void resizeForTab2(){
        setSize(530, getSize().height);
    }

    public void renderFD(BufferedImage img ){
        FDView.setIcon(new ImageIcon(img));
    }

    public void renderSkin(BufferedImage img ){
        SkinView.setIcon(new ImageIcon(img));
    }

    public void renderWire(BufferedImage img ){
        WireView1.setIcon(new ImageIcon(img));
    }

    public void renderSmallIluminated(BufferedImage img){
        smallIluminated.setIcon(new ImageIcon(img));
    }

    private void renderLoadedImage(BufferedImage img){
        testImage.setIcon(new ImageIcon(img));
    }

    private void renderErrorImage(BufferedImage img){
        errorGraph.setIcon(new ImageIcon(img));
    }

    private String getSelectedLetter(){
        return alphabet.getSelectedItem().toString();
    }

    private String getSaveFilePath(){
        JFileChooser saveDialog = new JFileChooser(imageFileLocation);
        saveDialog.setMultiSelectionEnabled(false);
        saveDialog.setAcceptAllFileFilterUsed(false);
        saveDialog.setFileFilter(new FileNameExtensionFilter("JPEG file", "jpeg", "jpg"));
        saveDialog.setApproveButtonToolTipText("Save snapshot");
        saveDialog.setDialogTitle("Save Snapshot");

        int val = saveDialog.showSaveDialog(this);

        if (val == JFileChooser.APPROVE_OPTION){
            String fileName = saveDialog.getSelectedFile().getName();
            imageFileLocation = saveDialog.getCurrentDirectory().toString();

            return imageFileLocation + "\\" +fileName + ".jpg";
        }

        return null;
    }

    public void setOutput(int[] out){
        int pozMax = 0;
        for (int i=1; i<out.length; i++){
            if (out[i] > out[pozMax]){
                pozMax = i;
            }
        }
        outLabel.setText(Constants.ALPHABET.charAt(pozMax) + "");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        detectionTypeButtonGroup = new javax.swing.ButtonGroup();
        tabPane = new javax.swing.JTabbedPane();
        LiveSRL = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        FDView = new javax.swing.JLabel();
        smallIluminated = new javax.swing.JLabel();
        SkinView = new javax.swing.JLabel();
        WireView1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        luminosity = new javax.swing.JSlider();
        skinDetection = new javax.swing.JRadioButton();
        luminosityDetection = new javax.swing.JRadioButton();
        threshold = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        generateTrainingExample = new javax.swing.JButton();
        webCamPanel = new javax.swing.JPanel();
        playButton = new javax.swing.JButton();
        stop = new javax.swing.JButton();
        recognize = new javax.swing.JButton();
        takeSnapshot = new javax.swing.JButton();
        outLabel = new javax.swing.JLabel();
        webCamContent = new javax.swing.JPanel();
        webCamLable = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        webCamList = new javax.swing.JComboBox();
        extendOptions = new javax.swing.JLabel();
        TestSLR = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        browseImage = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        testImage = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        alphabet = new javax.swing.JComboBox();
        alphabetForTeGeneration = new JComboBox();
        jPanel3 = new javax.swing.JPanel();
        errorGraph = new javax.swing.JLabel();
        recognizeSingleImage = new javax.swing.JButton();

        trainingSetLocation = new JTextField();
        trainingSetLocation.setSize(new Dimension(300, 50));
        trainingSetLocation.setMaximumSize(new Dimension(300, 50));
        trainingSetLocation.setPreferredSize(new Dimension(300, 50));
        alphabetForTeGeneration.setSize(new Dimension(50, 50));
        alphabetForTeGeneration.setMaximumSize(new Dimension(50, 50));
        alphabetForTeGeneration.setPreferredSize(new Dimension(50, 50));

        webCamWrapperPannel = new JPanel();
        webCamWrapperPannel.add(webCamLable);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        LiveSRL.setPreferredSize(new java.awt.Dimension(1054, 650));

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Options panel"));
        optionsPanel.setPreferredSize(new java.awt.Dimension(640, 480));

        FDView.setText("FD");
        FDView.setPreferredSize(new java.awt.Dimension(150, 100));
        FDView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                FDViewMouseClicked(evt);
            }
        });

        smallIluminated.setText("Normal View");
        smallIluminated.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smallIluminatedMouseClicked(evt);
            }
        });

        SkinView.setText("Skin View");
        SkinView.setPreferredSize(new java.awt.Dimension(150, 100));
        SkinView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SkinViewMouseClicked(evt);
            }
        });

        WireView1.setText("WireFrame View");
        WireView1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                WireView1MouseClicked(evt);
            }
        });

        jLabel1.setText("Luminosity");

        luminosity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                luminosityStateChanged(evt);
            }
        });

        skinDetection.setText("Skin detection");
        skinDetection.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                skinDetectionStateChanged(evt);
            }
        });

        luminosityDetection.setText("Luminosity detection");

        threshold.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                thresholdStateChanged(evt);
            }
        });

        jLabel2.setText("Threshold");

        generateTrainingExample.setText("Collect Training Example");
        generateTrainingExample.setEnabled(false);
        generateTrainingExample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collectTrainingExample(evt);
            }
        });

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addComponent(smallIluminated, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SkinView, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(skinDetection))
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addComponent(WireView1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FDView, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(luminosityDetection)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(threshold, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(trainingSetLocation)
                .addComponent(alphabetForTeGeneration)
                    .addComponent(generateTrainingExample)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(luminosity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(smallIluminated, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SkinView, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(WireView1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FDView, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(trainingSetLocation)
                .addComponent(alphabetForTeGeneration)
                .addComponent(generateTrainingExample)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 180, Short.MAX_VALUE)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(luminosity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(skinDetection)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(luminosityDetection)
                        .addComponent(jLabel2))
                    .addComponent(threshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47))
        );

        webCamPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Web Cam", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(51, 51, 255))); // NOI18N

        playButton.setText("Play");
        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playActionPerformed(evt);
            }
        });

        stop.setText("Stop");
        stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopActionPerformed(evt);
            }
        });

        recognize.setText("Recognize");
        recognize.setEnabled(false);
        recognize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recognizeActionPerformed(evt);
            }
        });

        takeSnapshot.setText("Take Snapshot");
        takeSnapshot.setEnabled(false);
        takeSnapshot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                takeSnapshotActionPerformed(evt);
            }
        });

        outLabel.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N

        webCamContent.setBorder(javax.swing.BorderFactory.createTitledBorder("WebCamContent"));
        webCamContent.setPreferredSize(new java.awt.Dimension(640, 480));

        webCamLable.setText("                        No webcam started.                                ");

        javax.swing.GroupLayout webCamContentLayout = new javax.swing.GroupLayout(webCamContent);
        webCamContent.setLayout(webCamContentLayout);
        webCamContentLayout.setHorizontalGroup(
            webCamContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(webCamWrapperPannel, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
        );
        webCamContentLayout.setVerticalGroup(
            webCamContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(webCamWrapperPannel, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
        );

        jLabel3.setText("Available Web Cameras:");

        webCamList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        extendOptions.setFont(new java.awt.Font("Tw Cen MT Condensed Extra Bold", 0, 24)); // NOI18N
        extendOptions.setText(">");
        extendOptions.setToolTipText("Options");
        extendOptions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                extendOptionsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                extendOptionsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                extendOptionsMouseExited(evt);
            }
        });

        javax.swing.GroupLayout webCamPanelLayout = new javax.swing.GroupLayout(webCamPanel);
        webCamPanel.setLayout(webCamPanelLayout);
        webCamPanelLayout.setHorizontalGroup(
            webCamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webCamPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(webCamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(webCamPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(webCamList, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(webCamContent, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(webCamPanelLayout.createSequentialGroup()
                        .addComponent(playButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stop)
                        .addGap(145, 145, 145)
                        .addComponent(outLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(webCamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, webCamPanelLayout.createSequentialGroup()
                                .addComponent(takeSnapshot)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(recognize))
                            .addComponent(extendOptions, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addGap(10, 10, 10))
        );
        webCamPanelLayout.setVerticalGroup(
            webCamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, webCamPanelLayout.createSequentialGroup()
                .addGroup(webCamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(webCamList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(webCamContent, javax.swing.GroupLayout.PREFERRED_SIZE, 499, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webCamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(webCamPanelLayout.createSequentialGroup()
                        .addGroup(webCamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(playButton)
                            .addComponent(stop)
                            .addComponent(recognize)
                            .addComponent(takeSnapshot))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(extendOptions))
                    .addComponent(outLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(36, 36, 36))
        );

        javax.swing.GroupLayout LiveSRLLayout = new javax.swing.GroupLayout(LiveSRL);
        LiveSRL.setLayout(LiveSRLLayout);
        LiveSRLLayout.setHorizontalGroup(
            LiveSRLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LiveSRLLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(webCamPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        LiveSRLLayout.setVerticalGroup(
            LiveSRLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LiveSRLLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(LiveSRLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 625, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(webCamPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 627, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        tabPane.addTab("Live SLR", LiveSRL);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Image"));

        browseImage.setText("Browse Image");
        browseImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseImageActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        testImage.setBackground(new java.awt.Color(255, 255, 255));
        testImage.setText("Test Image");
        testImage.setPreferredSize(new java.awt.Dimension(150, 100));
        testImage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                testImageMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(testImage, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(testImage, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel5.setText("Expected Response");

        alphabet.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(browseImage)
                    .addComponent(jLabel5)
                    .addComponent(alphabet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(browseImage)
                .addGap(28, 28, 28)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alphabet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(186, 186, 186))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Standard Deviation"));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(errorGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(errorGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
        );

        recognizeSingleImage.setText("Recognize");
        recognizeSingleImage.setEnabled(false);
        recognizeSingleImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recognizeSingleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout TestSLRLayout = new javax.swing.GroupLayout(TestSLR);
        TestSLR.setLayout(TestSLRLayout);
        TestSLRLayout.setHorizontalGroup(
            TestSLRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TestSLRLayout.createSequentialGroup()
                .addGroup(TestSLRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(TestSLRLayout.createSequentialGroup()
                        .addGap(212, 212, 212)
                        .addComponent(recognizeSingleImage))
                    .addGroup(TestSLRLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(TestSLRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        TestSLRLayout.setVerticalGroup(
            TestSLRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TestSLRLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(recognizeSingleImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPane, javax.swing.GroupLayout.PREFERRED_SIZE, 1053, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(TestSLR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(403, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(tabPane, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(TestSLR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void playActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playActionPerformed
        takeSnapshot.setEnabled(true);
        recognize.setEnabled(true);
        generateTrainingExample.setEnabled(true);
        String webCamName = (String) webCamList.getSelectedItem();

        switch (webCamState) {
            case STOPPED: {
                Webcam webcam = windowController.startWebCam(webCamName);
                if (webCamPanel != null) {
                    webCamWrapperPannel.remove(webCamPanel);
                }
                webCamPanel = new WebcamPanel(webcam);
                webCamWrapperPannel.add(webCamPanel);
                webCamWrapperPannel.remove(webCamLable);

                playButton.setText("Pause");
                webCamList.setEnabled(false);
                webCamState = WebCamState.STARTED;
                break;
            }
            case STARTED: {
                playButton.setText("Play");
                windowController.pauseWebCam(webCamName);
                webCamState = WebCamState.PAUSED;
                break;
            }
            case PAUSED: {
                playButton.setText("Pause");
                windowController.resumeWebCam(webCamName);
                webCamState = WebCamState.STARTED;
                break;
            }
            default: throw new UnsupportedOperationException("Unknown web cam state: " + webCamState);
        }
    }

    private void luminosityStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_luminosityStateChanged
        windowController.setLuminosity(luminosity.getValue());
    }//GEN-LAST:event_luminosityStateChanged

    private void takeSnapshotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_takeSnapshotActionPerformed
        try {
            String webCamName = (String) webCamList.getSelectedItem();
            windowController.takeSnapshot(webCamName, getSaveFilePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "An error occured while saving. The file was not saved.", "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_takeSnapshotActionPerformed

    private void skinDetectionStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_skinDetectionStateChanged
        windowController.setUseSkinDetection(skinDetection.isSelected());
        if (skinDetection.isSelected()) {
            threshold.setEnabled(false);
        } else {
            threshold.setEnabled(true);
            windowController.setThreshold((int) (((float) threshold.getValue() / 100) * 255));
        }
    }//GEN-LAST:event_skinDetectionStateChanged

    private void thresholdStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_thresholdStateChanged
        windowController.setThreshold((int)(((float)threshold.getValue() / 100)*255));
    }//GEN-LAST:event_thresholdStateChanged

    private void collectTrainingExample(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayFDActionPerformed
        try {
            windowController.collectTrainingExample(
                    (String)webCamList.getSelectedItem(),
                    (String)alphabetForTeGeneration.getSelectedItem(),
                    trainingSetLocation.getText(),
                    (int) trainingExamplesCnt++,
                    skinDetection.isSelected(),
                    threshold.getValue(), luminosity.getValue());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_displayFDActionPerformed

    private void smallIluminatedMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smallIluminatedMouseClicked
//        windowController.normalView();
    }//GEN-LAST:event_smallIluminatedMouseClicked

    private void WireView1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_WireView1MouseClicked
//        windowController.wireFrameView();
    }//GEN-LAST:event_WireView1MouseClicked

    private void SkinViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SkinViewMouseClicked
//        windowController.skinView();
    }//GEN-LAST:event_SkinViewMouseClicked

    private void FDViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_FDViewMouseClicked
//        windowController.fdView();
    }//GEN-LAST:event_FDViewMouseClicked

    private void recognizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recognizeActionPerformed
        if (windowController.isRecognize()){
            recognize.setText("Recognize");
        }else{
            recognize.setText("Stop Recognizing");
        }
        windowController.computePrediction(!windowController.isRecognize());
    }//GEN-LAST:event_recognizeActionPerformed

    private void stopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopActionPerformed
        if (webCamState == WebCamState.STARTED || webCamState == WebCamState.PAUSED) {
            String webCamName = (String) webCamList.getSelectedItem();
            windowController.stopWebCam(webCamName);

            playButton.setText("Play");
            webCamList.setEnabled(true);
            webCamWrapperPannel.remove(webCamPanel);
            webCamWrapperPannel.add(webCamLable);
            webCamWrapperPannel.repaint();

            webCamState = WebCamState.STOPPED;
        }
    }//GEN-LAST:event_stopActionPerformed

    private void extendOptionsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extendOptionsMouseEntered
        extendOptions.setForeground(Color.GRAY);
    }//GEN-LAST:event_extendOptionsMouseEntered

    private void extendOptionsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extendOptionsMouseExited
        extendOptions.setForeground(Color.BLACK);
    }//GEN-LAST:event_extendOptionsMouseExited

    private void extendOptionsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extendOptionsMouseClicked
        if (getSize().width == 720){
            maximize();
            extendOptions.setText("<");
        }else{
            minimize();
            extendOptions.setText(">");
        }
    }//GEN-LAST:event_extendOptionsMouseClicked

    private void testImageMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_testImageMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_testImageMouseClicked

    private void browseImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseImageActionPerformed
        JFileChooser loadFileDialog = new JFileChooser(imageFileLocation);
        loadFileDialog.setMultiSelectionEnabled(false);
        loadFileDialog.setAcceptAllFileFilterUsed(false);
        loadFileDialog.setFileFilter(new FileNameExtensionFilter("JPEG file", "jpeg", "jpg"));
        loadFileDialog.setApproveButtonToolTipText("Load Image");
        loadFileDialog.setDialogTitle("Load Image");

        int option = loadFileDialog.showOpenDialog(null);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            String fileName = loadFileDialog.getSelectedFile().getAbsolutePath();
            imageFileLocation = loadFileDialog.getCurrentDirectory().toString();
            loadedImage = windowController.readImage(fileName);
            renderLoadedImage(loadedImage);
        } catch (IOException ex) {
            Logger.getLogger(SlrWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }

        recognizeSingleImage.setEnabled(true);
    }

    private void recognizeSingleActionPerformed(java.awt.event.ActionEvent evt) {
        double[] prediction = windowController.recognizeSingleImage(loadedImage, skinDetection.isSelected(), threshold.getValue(), luminosity.getValue());

        if (prediction == null) {
            return;
        }

        double[] expected = new double[24];
        expected[Constants.ALPHABET.indexOf(getSelectedLetter())] = 1;
        BufferedImage errorImage = generateErrorGraphic(expected, prediction);
        renderErrorImage(errorImage);
        renderLoadedImage(loadedImage);
    }

    private BufferedImage generateErrorGraphic(double[] expected, double[] output) {
        int w = 460;
        int h = 280;

        XYSeries expectedSeries = new XYSeries("Expected");
        XYSeries outputSeries = new XYSeries("Actual Output");

        for (int i = 0; i < expected.length; i++) {
            expectedSeries.add(i, expected[i]);
            outputSeries.add(i, output[i]);
        }
        XYDataset expectedData = new XYSeriesCollection(expectedSeries);
        XYDataset outputData = new XYSeriesCollection(outputSeries);

        JFreeChart chart = ChartFactory.createXYLineChart("Error Graphic", "Output", "FD", expectedData, PlotOrientation.VERTICAL, true, false, false);
        chart.getXYPlot().setDataset(1, outputData);

        //chart.getXYPlot().setRenderer(0, new DeviationRenderer(true, true));
        //chart.getXYPlot().setRenderer(1, new DeviationRenderer(true, true));

        chart.getXYPlot().setRenderer(0, new XYLineAndShapeRenderer(true, true));
        chart.getXYPlot().setRenderer(1, new XYLineAndShapeRenderer(true, true));

        return chart.createBufferedImage(w, h);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel FDView;
    private javax.swing.JPanel LiveSRL;
    private javax.swing.JLabel SkinView;
    private javax.swing.JPanel TestSLR;
    private javax.swing.JLabel WireView1;
    private javax.swing.JComboBox alphabet;
    private javax.swing.JButton browseImage;
    private javax.swing.ButtonGroup detectionTypeButtonGroup;
    private javax.swing.JButton generateTrainingExample;
    private javax.swing.JLabel errorGraph;
    private javax.swing.JLabel extendOptions;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSlider luminosity;
    private javax.swing.JRadioButton luminosityDetection;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JLabel outLabel;
    private javax.swing.JButton playButton;
    private javax.swing.JButton recognize;
    private javax.swing.JButton recognizeSingleImage;
    private javax.swing.JRadioButton skinDetection;
    private javax.swing.JLabel smallIluminated;
    private javax.swing.JButton stop;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JButton takeSnapshot;
    private javax.swing.JLabel testImage;
    private javax.swing.JSlider threshold;
    private javax.swing.JPanel webCamContent;
    private javax.swing.JComboBox webCamList;
    private javax.swing.JPanel webCamPanel;

    private javax.swing.JLabel webCamLable;
    private JPanel webCamWrapperPannel;

    private javax.swing.JComboBox alphabetForTeGeneration;
    private JTextField trainingSetLocation;
    // End of variables declaration//GEN-END:variables

    private enum WebCamState {
        STARTED,
        STOPPED,
        PAUSED
    }
}
