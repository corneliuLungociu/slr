package slr.logic.neuralNetwork.errors;

/**
 *
 * @author corneliu
 */
public class NeuralNetworkException extends Exception{
    private int errorCode;
    private String errorMessage;

    public NeuralNetworkException(int errorCode, String errorMessage){
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode(){
        return errorCode;
    }

    public String getErrorMessage(){
        return errorMessage;
    }
}
