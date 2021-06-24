package msk.gui;
import hla.rti.RTIexception;
import hla.rti.*;
import hla.rti.jlc.RtiFactoryFactory;
import msk.HandlersHelper;
import msk.template.TemplateFederate;
import javax.swing.SwingUtilities;
import java.util.Random;

public class GuiFederate extends TemplateFederate {

    private boolean start;

    public GuiFederate() {
        super("GuiFederate");
        start = false;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame("Gui");
            }
        });
    }

    @Override
    protected void createRTIAmbassador() throws RTIexception{
        this.rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

    }

    @Override
    protected void createAmbassador() {
        this.fedamb = new GuiAmbassador(this);
    }

    @Override
    protected void mainMethod() throws RTIexception {
        advanceTime(randomTime());
        if(!start){
            start=true;
            MainFrame.start();
        }
        updateGui();
    }

    private void updateGui(){

    }

    @Override
    public void sendInteraction(double timeStep) throws RTIexception{

    }

    @Override
    public void publishAndSubscribe() throws RTIexception{
        int stopHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Finish");
        HandlersHelper.addInteractionClassHandler("InteractionRoot.Finish", stopHandle);
        rtiamb.subscribeInteractionClass(stopHandle);
    }

    private double randomTime() {
        Random r = new Random();
        return 1 +(4 * r.nextDouble());
    }

    public static void main(String[] args) {
        try {
            new GuiFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
