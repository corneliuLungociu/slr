/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package slr.logic.neuralNetwork;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author corneliu
 */
public class Neuron implements Serializable{
    private List<Connection> inputConnections;
    private List<Connection> outputConnections;
    private Layer layer;
    private double outputValue;
    private double error;
    private double networkError;

    public Neuron(Layer layer) {
        inputConnections = new LinkedList<Connection>();
        outputConnections = new LinkedList<Connection>();
        this.layer = layer;
        outputValue = 0;
        networkError = 0;
    }

    public Neuron(Layer layer, List<Connection> inputConnections, List<Connection> outputConnections) {
        this.inputConnections = new LinkedList<Connection>();
        this.outputConnections = new LinkedList<Connection>();

        this.inputConnections.addAll(inputConnections);
        this.outputConnections.addAll(outputConnections);
        this.layer = layer;
    }

    public double getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(double outputValue){
        this.outputValue = outputValue;
    }

    public double getNetworkError(){
        return networkError;
    }

    public void setNetworkError(double networkError){
        this.networkError = networkError;
    }

    public void createOutputConnections(Layer next){
        if (next != null){
            Random r = new Random();
            outputConnections = new LinkedList<Connection>();
            for (Neuron n : next.getNeurons()){
                outputConnections.add(new Connection((double)(r.nextInt(20))/100, this, n));
            }
        }
    }

    public void createInputConnections(Layer prev){
        if (prev != null){
            Random r = new Random();
            inputConnections = new LinkedList<Connection>();
            for (Neuron n : prev.getNeurons()){
                n.addOutputConnection(new Connection(r.nextInt(20)/100, n, this));
            }
        }
    }

    private double summingFunction(){
        double s = 0;
        for (Connection c : inputConnections){
            s = s + c.getNeuron().outputValue * c.getWeight();
        }
        return s;
    }

    private double transferFunction(double summation){
        return 1 / (1 + Math.exp(-summation));
    }

    public double calculateOutput(){
        double summation = summingFunction();
        outputValue = transferFunction(summation);
        return outputValue;
    }

    public void addInputConnection(Connection c){
        inputConnections.add(c);
    }

    public void addOutputConnection(Connection c){
        outputConnections.add(c);
    }

    public List<Connection> getInputConnections() {
        return inputConnections;
    }

    public List<Connection> getOutputConnections() {
        return outputConnections;
    }

    public Layer getLayer() {
        return layer;
    }

    public void calculateErrorForOutputLayer(double expectedOutput){
        error = outputValue * (1 - outputValue) * (expectedOutput - outputValue);
    }

    public void calculateErrorForHiddenLayer(){
        
        double sum = 0;
        for (Connection con : outputConnections){
            sum = sum + con.getOldWeight() * con.getNeuron().getError();
        }

        error = outputValue * (1 - outputValue) * sum;
    }

    public void calculateNetworkError(double expectedOutput){
        networkError = networkError + ((expectedOutput - outputValue) * (expectedOutput - outputValue));
    }

    public void updateWeights(double learningRate, double alpha){
        // Wij = Wij + n * deltaj * * Xij

        for (Connection con : inputConnections){
            double momentum = alpha * con.getDeltaWeight();
            double deltaWeigt = learningRate * error * con.getNeuron().getOutputValue() + momentum;
            
            con.updateWeight(deltaWeigt);
            con.setDeltaWeight(deltaWeigt);
        }

        for (Connection con : outputConnections){
            con.setOldWeight(con.getWeight());
        }
    }

    public double getError(){
        return error;
    }

}
