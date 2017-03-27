package slr.services.impl;

import slr.logic.neuralNetwork.NeuralNetwork;
import slr.logic.neuralNetwork.errors.NeuralNetworkException;
import slr.logic.neuralNetwork.training.TrainingElement;
import slr.logic.neuralNetwork.training.TrainingSet;
import slr.utils.Constants;
import slr.services.PredictionException;
import slr.services.PredictionService;

import java.io.*;

public class NeuralNetworkPredictionService implements PredictionService {

    private NeuralNetwork neuralNetwork;
    private TrainingSet dataSet;

    public NeuralNetworkPredictionService() {
        init();
    }

    private void init() {
        try {
            loadNeuralNetwork();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public double[] predict(double[] features) {
        try {
            return neuralNetwork.feadForward(new TrainingElement(features, null));
        } catch (NeuralNetworkException e) {
            throw new PredictionException("Failed to predict sign.", e);
        }
    }














    private void initNeuralNetwork() {
        initForSLR();
        //initForIrisDataset();
        //initForOR();
        //initForXOR();
    }

    public void trainNeuralNetwork() {
        try {
            neuralNetwork.train(dataSet, Constants.LEARNING_RATE, Constants.ALPHA);
        } catch (NeuralNetworkException ex) {
            ex.printStackTrace();
        }
    }

    public void saveNeuralNetwork() throws IOException {
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream("neuralNetwork.txt"));
        out.writeObject(neuralNetwork);
        out.close();
    }

    private void loadNeuralNetwork() throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(this.getClass().getResourceAsStream("/neuralNetwork.txt"));
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
            while (linie != null) {
                String[] values = linie.split(",");

                double[] inputValues = new double[15];
                for (int i = 0; i < 15; i++) {
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
            ex.printStackTrace();
        }

        return ts;
    }

}
