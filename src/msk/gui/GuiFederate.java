package msk.gui;
import hla.rti.RTIexception;
import hla.rti.*;
import hla.rti.jlc.RtiFactoryFactory;
import msk.HandlersHelper;
import msk.statistics.CarStatistic;
import msk.template.TemplateFederate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class GuiFederate extends TemplateFederate {
    protected ArrayList<CarStatistic> carList = new ArrayList<CarStatistic>();
    protected ArrayList<CarStatistic> carListFinished = new ArrayList<CarStatistic>();
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
    }

    public void updateGui(String text){
        log("Gui otrzymalo informacje: "+text);
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
        int speedHandle = rtiamb.getAttributeHandle("position", simObjectClassHandle);
        int directionHandle = rtiamb.getAttributeHandle("direction", simObjectClassHandle);

        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory()
                .createAttributeHandleSet();
        attributes.add(speedHandle);
        attributes.add(directionHandle);
        rtiamb.subscribeObjectClassAttributes(simObjectClassHandle, attributes);
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
