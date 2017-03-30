package slr.services;

import java.io.IOException;

public interface PredictionService {

    void initForPrediction(String modelPath);

    double[] predict(double[] features);

    void initForTraining();

    void trainModel(String trainingSetPath);

    void saveModel() throws IOException;
}
