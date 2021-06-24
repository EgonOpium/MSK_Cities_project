package msk.traffic_control;

import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import msk.HandlersHelper;
import msk.template.TemplateAmbassador;

public class TrafficAmbassador extends TemplateAmbassador {
    private final TrafficFederate trafficFederate;
    public int lightsHandle = 0;
    public TrafficAmbassador(TrafficFederate trafficFederate){
        super(trafficFederate);
        this.trafficFederate = trafficFederate;
    }
    @Override
    protected void log( String message )
    {
        System.out.println( "TrafficFederateAmbassador: "+ message );
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
        this.lightsHandle = HandlersHelper.getInteractionHandleByName("InteractionRoot.ChangeLights");
        if(interactionClass == HandlersHelper
                .getInteractionHandleByName("InteractionRoot.Finish")){
            this.running = false;
            log( "Simulation stopped!" );
        }
    }
}
