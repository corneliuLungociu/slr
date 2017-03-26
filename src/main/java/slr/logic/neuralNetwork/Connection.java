package slr.logic.neuralNetwork;

import java.io.Serializable;

/**
 *
 * @author corneliu
 */
public class Connection implements Serializable {
    
    private double weight;

    // is used in the raining algorithm to avoid parsing the layers two times in the backPropagation phase;
    private double oldWeight;

    // is used to add momentum
    private double deltaWeight;

    private Neuron neuron;

    public Connection(double weight, Neuron sourceNeuron, Neuron destNeuron) {
        this.weight = weight;
        this.oldWeight = weight;
        this.neuron = destNeuron;
        neuron.addInputConnection(new Connection(sourceNeuron, weight));
    }

    private Connection(Neuron sourceNeuron, double weight){
        this.neuron = sourceNeuron;
        this.weight = weight;
    }

    public Neuron getNeuron() {
        return neuron;
    }

    public double getWeight() {
        return weight;
    }

    public void updateWeight(double deltaW){
        this.weight = weight + deltaW;
    }

    public void setWeight(double weight){
        this.weight = weight;
    }

    public double getOldWeight() {
        return oldWeight;
    }

    public void setOldWeight(double oldWeight) {
        this.oldWeight = oldWeight;
    }

    public double getDeltaWeight() {
        return deltaWeight;
    }

    public void setDeltaWeight(double deltaWeight) {
        this.deltaWeight = deltaWeight;
    }
}
