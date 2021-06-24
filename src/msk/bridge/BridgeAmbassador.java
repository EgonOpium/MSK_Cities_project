package msk.bridge;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import msk.HandlersHelper;
import msk.statistics.CarStatistic;
import msk.template.TemplateAmbassador;

import java.util.ArrayList;
import java.util.Objects;


public class BridgeAmbassador extends TemplateAmbassador {

    private ArrayList<Integer> carQueueWest;
    private ArrayList<Integer> carQueueEast;
    private ArrayList<Integer> onBridge;
    private boolean bridgeFree;
    public BridgeAmbassador(BridgeFederate bridgeFederate){
        super(bridgeFederate);
        carQueueWest = new ArrayList<>();
        carQueueEast = new ArrayList<>();
        onBridge = new ArrayList<>();
        bridgeFree = true;
    }
    @Override
    protected void log( String message )
    {
        System.out.println( "BridgeFederateAmbassador: "+ message );
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
    @Override
    public void discoverObjectInstance(int theObject, int theObjectClass, String objectName) {
        System.out.println("Pojawil sie nowy obiekt typu " + objectName);
//        HandlersHelper.addObjectClassHandler(theObject, theObjectClass );
//        carList.add(new CarStatistic(theObject, federateTime));
    }
    @Override
    public void removeObjectInstance(int theObject, byte[] userSuppliedTag) throws ObjectNotKnown, FederateInternalError {
//        try {
//            removeObjectInstance(theObject, userSuppliedTag, null, null);
//        } catch (InvalidFederationTime invalidFederationTime) {
//            invalidFederationTime.printStackTrace();
//        }
    }
    @Override
    public void removeObjectInstance(int theObject, byte[] userSuppliedTag, LogicalTime theTime, EventRetractionHandle retractionHandle) throws ObjectNotKnown, InvalidFederationTime, FederateInternalError {
//        for(CarStatistic car : carList){
//            if(car.theObject == theObject){
//                carListFinished.add(car);
//            }
//        }
    }

    @Override
    public void reflectAttributeValues(int theObject,
                                       ReflectedAttributes theAttributes, byte[] tag) {
        reflectAttributeValues(theObject, theAttributes, tag, null, null);
    }
    // position posibilities - TO_BRIDGE, ON_BRIDGE, AFTER_BRIDGE, IN_QUEUE
    @Override
    public void reflectAttributeValues(int theObject,
                                       ReflectedAttributes theAttributes, byte[] tag, LogicalTime theTime,
                                       EventRetractionHandle retractionHandle) {

        try {
            if( EncodingHelpers.decodeString(theAttributes.getValue(0)) == "IN_QUEUE_WEST"){
                this.carQueueWest.add(theObject);
            }
            else if( EncodingHelpers.decodeString(theAttributes.getValue(0)) == "IN_QUEUE_EAST"){
                this.carQueueEast.add(theObject);
            }
            else if( EncodingHelpers.decodeString(theAttributes.getValue(0)) == "ON_BRIDGE"){
                for (Integer car:carQueueEast) {
                    if(car == theObject) {
                        if (carQueueEast.contains(car)) {
                            carQueueEast.remove(car);
                        } else {
                            carQueueWest.remove(car);
                        }
                        onBridge.add(car);
                    }
                }
                if(onBridge.size()>0){
                    this.bridgeFree = false;
                }
            }
            else if( EncodingHelpers.decodeString(theAttributes.getValue(0)) == "AFTER_BRIDGE"){
                onBridge.remove(theObject);
                if(onBridge.size()>0){
                    this.bridgeFree = false;
                }
            }
        } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
            arrayIndexOutOfBounds.printStackTrace();
        }


    }
}
