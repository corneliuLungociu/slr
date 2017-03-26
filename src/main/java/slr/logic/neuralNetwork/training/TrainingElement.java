package slr.logic.neuralNetwork.training;

import java.util.Arrays;

/**
 *
 * @author corneliu
 */
public class TrainingElement {
    private double[] inputValues;
    private double[] expectedResponse;

    public TrainingElement(double[] inputValues, double[] expectedResponse) {
        this.inputValues = inputValues;
        this.expectedResponse = expectedResponse;
    }

    public TrainingElement() {
    }

    public double[] getExpectedResponse() {
        return expectedResponse;
    }

    public void setExpectedResponse(double[] expectedResponse) {
        this.expectedResponse = expectedResponse;
    }

    public double[] getInputValues() {
        return inputValues;
    }

    public void setInputValues(double[] inputValues) {
        this.inputValues = inputValues;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TrainingElement other = (TrainingElement) obj;
        if (!Arrays.equals(this.inputValues, other.inputValues)) {
            return false;
        }
        if (this.expectedResponse != other.expectedResponse) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }
}
