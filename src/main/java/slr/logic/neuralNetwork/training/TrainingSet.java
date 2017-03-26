/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package slr.logic.neuralNetwork.training;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author corneliu
 */
public class TrainingSet {
    List<TrainingElement> trainingElements;

    public TrainingSet() {
        trainingElements = new LinkedList<TrainingElement>();
    }

    public TrainingSet(List<TrainingElement> trainingElements) {
        trainingElements = new LinkedList<TrainingElement>();
        this.trainingElements.addAll(trainingElements);
    }

    public List<TrainingElement> getTrainingElements() {
        return trainingElements;
    }

    public void setTrainingElements(List<TrainingElement> trainingElements) {
        this.trainingElements = trainingElements;
    }

    public void addTrainingElement(TrainingElement te){
        trainingElements.add(te);
    }

    public TrainingElement getTrainingElement(int i){
        if (i<trainingElements.size()){
            return trainingElements.get(i);
        }else{
            return null;
        }
    }

    public void removeTrainingElement(TrainingElement te){
        trainingElements.remove(te);
    }
}