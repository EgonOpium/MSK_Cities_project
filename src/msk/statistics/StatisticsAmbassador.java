package msk.statistics;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import msk.HandlersHelper;
import msk.car.CarFederate;
import org.portico.impl.hla13.types.DoubleTime;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class StatisticsAmbassador extends NullFederateAmbassador {

    protected boolean running = true;

    protected double federateTime        = 0.0;
    protected double federateLookahead   = 1.0;
    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;

    protected boolean isAnnounced        = false;
    protected boolean isReadyToRun       = false;

    ArrayList<CarStatistic> carList = new ArrayList<CarStatistic>();
    ArrayList<CarStatistic> carListFinished = new ArrayList<CarStatistic>();

    public void timeRegulationEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isRegulating = true;
    }

    public void timeConstrainedEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isConstrained = true;
    }


    public void synchronizationPointRegistrationFailed( String label )
    {
        log( "Failed to register sync point: " + label );
    }

    public void synchronizationPointRegistrationSucceeded( String label )
    {
        log( "Successfully registered sync point: " + label );
    }

    public void announceSynchronizationPoint( String label, byte[] tag )
    {
        log( "Synchronization point announced: " + label );
        if( label.equals(CarFederate.READY_TO_RUN) )
            this.isAnnounced = true;
    }

    public void federationSynchronized( String label )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(CarFederate.READY_TO_RUN) )
            this.isReadyToRun = true;
    }


    public void receiveInteraction(int interactionClass,
                                   ReceivedInteraction theInteraction, byte[] tag) {

        receiveInteraction(interactionClass, theInteraction, tag, null, null);
    }

    public void receiveInteraction(int interactionClass,
                                   ReceivedInteraction theInteraction, byte[] tag,
                                   LogicalTime theTime, EventRetractionHandle eventRetractionHandle) {
        if (interactionClass == HandlersHelper
                .getInteractionHandleByName("InteractionRoot.Finish")) {
            running = false;
            log( "Simulation stopped!" );
        }
    }

    public void timeAdvanceGrant( LogicalTime theTime )
    {
        this.federateTime = convertTime( theTime );
        this.isAdvancing = false;
    }

    private double convertTime( LogicalTime logicalTime )
    {
        // PORTICO SPECIFIC!!
        return ((DoubleTime)logicalTime).getTime();
    }

    private void log(String message) {
        System.out.println("StatisticsAmbassador: " + message);
    }

    public void discoverObjectInstance(int theObject, int theObjectClass, String objectName) {
        System.out.println("Pojawil sie nowy obiekt typu " + objectName);
//        HandlersHelper.addObjectClassHandler(theObject, theObjectClass );
        carList.add(new CarStatistic(theObject, federateTime));
    }

    public void removeObjectInstance(int theObject, byte[] userSuppliedTag) throws ObjectNotKnown, FederateInternalError {
        try {
            removeObjectInstance(theObject, userSuppliedTag, null, null);
        } catch (InvalidFederationTime invalidFederationTime) {
            invalidFederationTime.printStackTrace();
        }
    }

    public void removeObjectInstance(int theObject, byte[] userSuppliedTag, LogicalTime theTime, EventRetractionHandle retractionHandle) throws ObjectNotKnown, InvalidFederationTime, FederateInternalError {
        for(CarStatistic car : carList){
            if(car.theObject == theObject){
                carListFinished.add(car);
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
        for(CarStatistic car : carList){
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
//        log("Reflected attributes...");
//        StringBuilder builder = new StringBuilder("Reflection for object:");
//
//        builder.append(" handle=" + theObject);
////		builder.append(", tag=" + EncodingHelpers.decodeString(tag));
//
//        // print the attribute information
//        builder.append(", attributeCount=" + theAttributes.size());
//        builder.append("\n");
//
//        for (int i = 1; i < theAttributes.size(); i++) {
//            try {
//                // print the attibute handle
//                builder.append("\tattributeHandle=");
//                builder.append(theAttributes.getAttributeHandle(i));
//                // print the attribute value
//                builder.append(", attributeValue=");
//                builder.append(EncodingHelpers.decodeString(theAttributes
//                        .getValue(i)));
//                builder.append(", time=");
//                builder.append(theTime);
//                builder.append("\n");
//            } catch (ArrayIndexOutOfBounds aioob) {
//                // won't happen
//                log("Won't happen");
//            }
//        }
//        log(builder.toString());


    }


}
