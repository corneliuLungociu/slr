package slr.utils;

/**
 *
 * @author corneliu
 */
public class Constants {
    public static final int LARGE_SHAPE_SIZE = 32;
    public static final int SMALL_SHAPE_SIZE = 8;
    public static final int LUMINOSITY = 200;

    public static final int NORMAL_VIEW = 0;
    public static final int WIREFRAME_VIEW = 1;
    public static final int SKIN_VIEW = 2;
    public static final int FD_VIEW = 3;
    

    public static final String WRONG_NUMBER_OF_INPUT_VALUES = "Wrong number of input values";
    public static int errWrongImput = -1;

    public static final double LEARNING_RATE = 0.3;
    public static final double MOMENTUM_AMOUNT = 0.1;
    public static final double NETWORK_MAX_ERROR = 0.2;
    public static final String DEFAULT_SAVE_LOCATION = "./";

    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int FRAMES_TO_RECOGNIZE = 3;
}
