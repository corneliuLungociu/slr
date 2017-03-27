package slr.logic.neuralNetwork;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import slr.logic.neuralNetwork.errors.NeuralNetworkException;
import slr.logic.neuralNetwork.training.TrainingElement;
import slr.logic.neuralNetwork.training.TrainingSet;
import slr.utils.Constants;

/**
 *
 * @author corneliu
 */
public class NeuralNetwork implements Serializable {
    private List<Layer> layers;
    private Layer fakeLayer;
    private double learningRate;

    // used for momentum;
    private double alpha;

    public NeuralNetwork(int inputNeurons, int outputNeurons){
        layers = new LinkedList<Layer>();
        
        fakeLayer = new Layer(inputNeurons);
        Layer input = new Layer(inputNeurons);
        Layer output = new Layer(outputNeurons);
        
        fakeLayer.fullyForwardConnect(input);
        input.fullyForwardConnect(output);
        layers.add(input);
        layers.add(output);
    }

    public void addHiddenLayer(int nrOfNeurons){
        Layer newHidden = new Layer(nrOfNeurons);
        Layer output = layers.get(layers.size()-1);
        Layer lastHidden = layers.get(layers.size()-2);

        lastHidden.fullyForwardConnect(newHidden);
        newHidden.fullyForwardConnect(output);

        layers.set(layers.size()-1, newHidden);
        layers.add(output);
    }

    public double[] feadForward(TrainingElement te) throws NeuralNetworkException{
        initFakeLayer(te);
        layers.get(0).feedForward(te);

        return getNetworkOutput();
    }

    public double[] getNetworkOutput(){
        double[] networkOutput = new double[layers.get(layers.size()-1).getNrOfNeurons()];

        for (int i=0; i<networkOutput.length; i++){
            networkOutput[i] = layers.get(layers.size()-1).getNeurons().get(i).getOutputValue();
        }

        return networkOutput;
    }

    public void train(TrainingSet dataSet, double learningRate, double alpha) throws NeuralNetworkException{
        this.learningRate = learningRate;
        this.alpha = alpha;
        
        double decadeRate = 2d / 1000000d;
        double i=0;

        TrainingSet trainingSet = new TrainingSet();
        TrainingSet validationSet = new TrainingSet();
        generateValidationAndTrainingSet(dataSet, trainingSet, validationSet);

        System.out.println("Start: " + new Date().toGMTString());

        double networkError;

        initNetworkError();
        for (TrainingElement te : validationSet.getTrainingElements()){
                initFakeLayer(te);
                layers.get(0).feedForward(te);
        }

        // error on validation set
        double bestNetworkError = 100;
        networkError = getNetworkError();

        do{
            initNetworkError();
            for (TrainingElement te : trainingSet.getTrainingElements()){
                initFakeLayer(te);

                layers.get(0).feedForward(te);
                propagateBackwards(te);
            }
            if (learningRate > 0.01){
                learningRate -= decadeRate;
            }

            // each k iterations check the networ error on validation set
            if (i % 10000 == 0){
                // error on training set
                networkError = getNetworkError();
                System.out.print(i + ": " + networkError + "          ");

                initNetworkError();
                for (TrainingElement te : validationSet.getTrainingElements()){
                        initFakeLayer(te);
                        layers.get(0).feedForward(te);
                }

                // error on validation set
                networkError = getNetworkError();
                System.out.println(networkError + "    " +learningRate);

                if (networkError < bestNetworkError){
                    bestNetworkError = networkError;
                    
                    ObjectOutput out;
                    try {
                        out = new ObjectOutputStream(new FileOutputStream("neuralNetwork.txt"));
                        out.writeObject(this);
                        out.close();
                    } catch (IOException ex) {
                        Logger.getLogger(NeuralNetwork.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // generate new training and validation sets
                generateValidationAndTrainingSet(dataSet, trainingSet, validationSet);
            }

            
            i++;
        }while (networkError > Constants.NETWORK_MAX_ERROR);
        System.out.println("Stop: " + new Date().toGMTString());
    }

    private void initFakeLayer(TrainingElement te) throws NeuralNetworkException{
        if (te.getInputValues().length == fakeLayer.getNrOfNeurons()){
            for (int i=0; i < te.getInputValues().length; i++){
                fakeLayer.getNeurons().get(i).setOutputValue(te.getInputValues()[i]);
            }
        }else{
            throw new NeuralNetworkException(Constants.errWrongImput, Constants.WRONG_NUMBER_OF_INPUT_VALUES);
        }
    }

    private void propagateBackwards(TrainingElement te){
        
        for (int i=layers.size()-1; i>=0; i--){
            layers.get(i).calculateError(te);
            layers.get(i).updateWeights(learningRate, alpha);
        }
    }

    public double getNetworkError(){
        double networkError = 0;
        for (Neuron n : layers.get(layers.size()-1).getNeurons()){
            networkError += n.getNetworkError();
        }

        return networkError / 2;
    }

    private void initNetworkError() {
        for (Neuron n : layers.get(layers.size()-1).getNeurons()){
            n.setNetworkError(0);
        }
    }

    private TrainingSet generateValidationAndTrainingSet(TrainingSet dataSet, TrainingSet trainingSet, TrainingSet validationSet){
        // asume there are 6 elements for each cathegory, then
        //keep in validation set two elements from each category
        trainingSet.getTrainingElements().clear();
        validationSet.getTrainingElements().clear();

        Random r = new Random();
        int p1 = r.nextInt(6);
        int p2 = r.nextInt(6);
        while (p2==p1){
            p2 = r.nextInt(6);
        }

        for (int i=0; i<dataSet.getTrainingElements().size(); i++){
            if (i % 6 == p1 || i%6 == p2){
                validationSet.addTrainingElement(dataSet.getTrainingElement(i));
            }else{
                trainingSet.addTrainingElement(dataSet.getTrainingElement(i));
            }
        }

        return validationSet;
    }
    
}
