package msk.gui;
import hla.rti.RTIexception;
import hla.rti.*;
import hla.rti.jlc.RtiFactoryFactory;
import msk.HandlersHelper;
import msk.template.TemplateFederate;

import java.util.Random;

public class GuiFederate extends TemplateFederate {

    public GuiFederate() {
        super("GuiFederate");
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
