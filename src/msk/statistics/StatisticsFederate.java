package msk.statistics;

import hla.rti.*;
import hla.rti.jlc.RtiFactoryFactory;
import msk.HandlersHelper;
import msk.bridge.BridgeAmbassador;
import msk.template.TemplateFederate;
import java.util.ArrayList;

public class StatisticsFederate extends TemplateFederate {

    protected ArrayList<CarStatistic> carList = new ArrayList<CarStatistic>();
    protected ArrayList<CarStatistic> carListFinished = new ArrayList<CarStatistic>();

    public StatisticsFederate() {
        super("StatisticsFederate");
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        int simObjectClassHandle = rtiamb.getObjectClassHandle("ObjectRoot.Car");
        int speedHandle = rtiamb.getAttributeHandle("position", simObjectClassHandle);
        int directionHandle = rtiamb.getAttributeHandle("direction", simObjectClassHandle);

        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory()
                .createAttributeHandleSet();
        attributes.add(speedHandle);
        attributes.add(directionHandle);
        rtiamb.subscribeObjectClassAttributes(simObjectClassHandle, attributes);


        int stopHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Finish");
        HandlersHelper.addInteractionClassHandler("InteractionRoot.Finish", stopHandle);
        rtiamb.subscribeInteractionClass(stopHandle);
    }

    @Override
    protected void log( String message )
    {
        System.out.println( "StatisticsFederate  : " + message );
    }

    @Override
    protected void createRTIAmbassador() throws RTIexception{
        this.rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
    }

    @Override
    protected void createAmbassador() {
        this.fedamb = new StatisticsAmbassador(this);
    }

    @Override
    protected void mainMethod() throws RTIexception {
        advanceTime(1.0);
        for(CarStatistic car : this.carListFinished){
            log("Car handle: "+car.theObject
                    +", \n Direction: "+car.getDirection()
                    +", \n Time of creation: "+car.getCreationTime()
                    +", \n Time to bridge: "+car.getTimeToBridge()
                    +", \n Time on bridge: "+car.getTimeOnBridge()
                    +", \n Time after bridge: "+car.getTimeAfterBridge()
                    +", \n Time of finishing: "+car.getFinishTime()
                    +", \n Time of journey: "+(car.getFinishTime() - car.getCreationTime()));
        }

        log("Time check before finish: "+fedamb.federateTime);
    }

    public void addCarToFinished(CarStatistic car){
        this.carListFinished.add(car);
    }

    public static void main(String[] args) {

        try {
            new StatisticsFederate().runFederate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
