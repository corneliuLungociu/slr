/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package slr;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import slr.ui.SLRWindow;

/**
 * @author corneliu
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringContext.class);
        SLRWindow mainWindow = ctx.getBean(SLRWindow.class);
        mainWindow.initController();
        mainWindow.setVisible(true);
    }

}
