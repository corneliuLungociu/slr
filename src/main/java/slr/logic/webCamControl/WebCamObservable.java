package slr.logic.webCamControl;

import java.util.Observable;

/**
 *
 * @author corneliu
 */
public class WebCamObservable extends Observable{
    boolean changed = false;;

    public void change(){
        changed = ! changed;
        setChanged();
    }

}
