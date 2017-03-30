package slr.services.impl;

import slr.logic.neuralNetwork.NeuralNetwork;
import slr.logic.neuralNetwork.errors.NeuralNetworkException;
import slr.logic.neuralNetwork.training.TrainingElement;
import slr.logic.neuralNetwork.training.TrainingSet;
import slr.services.PredictionException;
import slr.services.PredictionService;
import slr.utils.Constants;

import java.io.*;

public class NeuralNetworkPredictionService implements PredictionService {

    private NeuralNetwork neuralNetwork;

    @Override
    public void initForPrediction(String modelPath) {
        try {
            loadNeuralNetwork(modelPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initForTraining() {
        neuralNetwork = new NeuralNetwork(15, 24);
        neuralNetwork.addHiddenLayer(20);
    }

    @Override
    public double[] predict(double[] features) {
        try {
            return neuralNetwork.feadForward(new TrainingElement(features, null));
        } catch (NeuralNetworkException e) {
            throw new PredictionException("Failed to predict sign.", e);
        }
    }

    @Override
    public void trainModel(String trainingSetPath) {
        try {
            TrainingSet dataSet = constructDataSet(trainingSetPath);
            neuralNetwork.train(dataSet, Constants.LEARNING_RATE, Constants.MOMENTUM_AMOUNT);
        } catch (NeuralNetworkException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void saveModel() throws IOException {
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream("neuralNetwork.txt"));
        out.writeObject(neuralNetwork);
        out.close();
    }

    private TrainingSet constructDataSet(String trainingSetPath) {
        TrainingSet ts = new TrainingSet();

        try {
            BufferedReader br = new BufferedReader(new FileReader(trainingSetPath));
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

    private void loadNeuralNetwork(String modelPath) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(this.getClass().getResourceAsStream(modelPath));
        neuralNetwork = (NeuralNetwork) in.readObject();
        in.close();
    }

}
