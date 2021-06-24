package msk.bridge;

import hla.rti.*;
import hla.rti.jlc.RtiFactoryFactory;
import msk.HandlersHelper;
import msk.template.TemplateFederate;
import java.util.Random;

public class BridgeFederate extends TemplateFederate {


    public BridgeFederate() {
        super("BridgeFederate");
    }

    @Override
    protected void createRTIAmbassador() throws RTIexception{
        this.rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
    }

    @Override
    protected void createAmbassador() {
        this.fedamb = new BridgeAmbassador(this);
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

        int simObjectClassHandle = rtiamb.getObjectClassHandle("ObjectRoot.Car");
        int positionHandle = rtiamb.getAttributeHandle("position", simObjectClassHandle);
        int directionHandle = rtiamb.getAttributeHandle("direction", simObjectClassHandle);
        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory()
                .createAttributeHandleSet();
        attributes.add(positionHandle);
        attributes.add(directionHandle);

        rtiamb.subscribeObjectClassAttributes(simObjectClassHandle, attributes);
    }

    private double randomTime() {
        Random r = new Random();
        return 1 +(4 * r.nextDouble());
    }

    public static void main(String[] args) {
        try {
            new BridgeFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }

}
