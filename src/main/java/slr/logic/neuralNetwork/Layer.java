package slr.logic.neuralNetwork;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import slr.logic.neuralNetwork.training.TrainingElement;

/**
 *
 * @author corneliu
 */
public class Layer implements Serializable {
    private int nrOfNeurons;
    private List<Neuron> neurons;

    public Layer(int nrOfNeurons) {
        neurons = new LinkedList<Neuron>();
        this.nrOfNeurons = nrOfNeurons;
        
        for (int i=0; i<nrOfNeurons; i++){
            neurons.add(new Neuron(this));
        }
    }

    public int getNrOfNeurons() {
        return nrOfNeurons;
    }

    public void setNumberOfNourons(int nrOfNeurons){
        if (neurons.isEmpty()){
            for (int i=0; i<nrOfNeurons; i++){
                neurons.add(new Neuron(this));
            }
        }else{
            Layer previous = (neurons.get(0).getInputConnections().isEmpty())?
                                    null
                                    :neurons.get(0).getInputConnections().get(0).getNeuron().getLayer();

            Layer next = (neurons.get(0).getOutputConnections().isEmpty())?
                                    null
                                    :neurons.get(0).getOutputConnections().get(0).getNeuron().getLayer();

            neurons.clear();
            for (int i=0; i<nrOfNeurons; i++){
                Neuron n = new Neuron(this);
                n.createInputConnections(previous);
                n.createOutputConnections(next);
                
                neurons.add(n);
            }
        }
    }

    public void fullyForwardConnect(Layer next){
        for (Neuron n : neurons){
            n.createOutputConnections(next);
        }
    }

    public void feedForward(TrainingElement te){
        int i=0;
        for (Neuron n : neurons){
            n.calculateOutput();
            if (this.isOutputLayer() && te.getExpectedResponse() != null){
                n.calculateNetworkError(te.getExpectedResponse()[i++]);
            }
            
        }
        
        if (!this.isOutputLayer()){
            neurons.get(0).getOutputConnections().get(0).getNeuron().getLayer().feedForward(te);
        }
    }

    public List<Neuron> getNeurons() {
        return neurons;
    }

    public boolean isInputLayer(){
        // before the input layer there is a fake layer
        return neurons.get(0).getInputConnections().get(0).getNeuron().getInputConnections().isEmpty();
    }

    public boolean isOutputLayer(){
        return neurons.get(0).getOutputConnections().isEmpty();
    }

    public void calculateError(TrainingElement te){
        if (isOutputLayer()){
            for (int i=0; i<neurons.size(); i++){
                neurons.get(i).calculateErrorForOutputLayer(te.getExpectedResponse()[i]);
            }
        }else{
            for (int i=0; i<neurons.size(); i++){
                neurons.get(i).calculateErrorForHiddenLayer();
            }
        }
    }

    public void updateWeights(double learningRate, double alpha){
        for (Neuron n : neurons){
            n.updateWeights(learningRate, alpha);
        }
    }
}
