package msk.statistics;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import msk.HandlersHelper;
import msk.car.CarFederate;
import msk.template.TemplateAmbassador;
import msk.template.TemplateFederate;
import org.portico.impl.hla13.types.DoubleTime;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class StatisticsAmbassador extends TemplateAmbassador {

    protected final StatisticsFederate statisticsFederate;
    public StatisticsAmbassador(StatisticsFederate statisticsFederate) {
        super(statisticsFederate);
        this.statisticsFederate = statisticsFederate;
    }

    @Override
    protected void log(String message) {
        System.out.println("StatisticsAmbassador: " + message);
    }

    public void discoverObjectInstance(int theObject, int theObjectClass, String objectName) {
        System.out.println("Pojawil sie nowy obiekt typu " + objectName);
//        HandlersHelper.addObjectClassHandler(theObject, theObjectClass );
        statisticsFederate.carList.add(new CarStatistic(theObject, federateTime));
    }

    public void removeObjectInstance(int theObject, byte[] userSuppliedTag) throws ObjectNotKnown, FederateInternalError {
        try {
            removeObjectInstance(theObject, userSuppliedTag, null, null);
        } catch (InvalidFederationTime invalidFederationTime) {
            invalidFederationTime.printStackTrace();
        }
    }

    public void removeObjectInstance(int theObject, byte[] userSuppliedTag, LogicalTime theTime, EventRetractionHandle retractionHandle) throws ObjectNotKnown, InvalidFederationTime, FederateInternalError {
        for(CarStatistic car : statisticsFederate.carList){
            if(car.theObject == theObject){
                statisticsFederate.carListFinished.add(car);
            }
        }
    }

    public void reflectAttributeValues(int theObject,
                                       ReflectedAttributes theAttributes, byte[] tag) {
        reflectAttributeValues(theObject, theAttributes, tag, null, null);
    }
    // position posibilities - TO_BRIDGE, ON_BRIDGE, AFTER_BRIDGE, IN_QUEUE
    public void reflectAttributeValues(int theObject,
                                       ReflectedAttributes theAttributes, byte[] tag, LogicalTime theTime,
                                       EventRetractionHandle retractionHandle) {
        for(CarStatistic car : statisticsFederate.carList){
            if (car.theObject == theObject){

                try {
                    String status = EncodingHelpers.decodeString(theAttributes.getValue(0));
                    String direction = EncodingHelpers.decodeString(theAttributes.getValue(1));
                    car.setDirection(direction);
                    if(Objects.equals(status, new String("ON_BRIDGE"))){
                        car.setTimeToBridge(federateTime);
                    }
                    else if(Objects.equals(status, new String("AFTER_BRIDGE"))){
                        car.setTimeOnBridge(federateTime);

                    }
                    else if(Objects.equals(status, new String("END"))){
                        car.setTimeAfterBridge(federateTime);
                        car.setFinishTime(federateTime);
                    }
                    else{
                        log("Car: "+theObject+", status: "+status);
                    }
                } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
                    arrayIndexOutOfBounds.printStackTrace();
                }
            }
        }



    }
    @Override
    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag )
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        receiveInteraction(interactionClass, theInteraction, tag, null, null);
    }
    @Override
    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction, byte[] tag,
                                    LogicalTime theTime,
                                    EventRetractionHandle eventRetractionHandle )
    {
        if(interactionClass == HandlersHelper
                .getInteractionHandleByName("InteractionRoot.Finish")){
            this.running = false;
            log( "Simulation stopped!" );
        }
    }

}
