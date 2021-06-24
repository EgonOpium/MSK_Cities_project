package msk.car;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import msk.HandlersHelper;
import msk.template.TemplateAmbassador;
import msk.template.TemplateFederate;
import org.portico.impl.hla13.types.DoubleTime;


public class CarAmbassador extends TemplateAmbassador {


    public boolean lights_west;
    public boolean lights_east;

    private final CarFederate carFederate;

    public CarAmbassador(CarFederate carFederate) {
        super(carFederate);
        this.carFederate = carFederate;
        lights_west = false;
        lights_east = false;
    }

    @Override
    public void log( String message )
    {
        System.out.println( "CarFederateAmbassador: " + message );
    }


    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag )
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        receiveInteraction(interactionClass, theInteraction, tag, null, null);
    }

    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction, byte[] tag,
                                    LogicalTime theTime,
                                    EventRetractionHandle eventRetractionHandle )
    {
        StringBuilder builder = new StringBuilder( "Interaction Received:" );

        if(interactionClass == HandlersHelper
                .getInteractionHandleByName("InteractionRoot.Finish")){
            this.running = false;
            log( "Simulation stopped!" );
        }
        else if(interactionClass == HandlersHelper
                .getInteractionHandleByName("InteractionRoot.ChangeLights")){
            try {
                this.carFederate.setLights_west(EncodingHelpers.decodeBoolean(theInteraction.getValue(0)));
                this.carFederate.setLights_east(EncodingHelpers.decodeBoolean(theInteraction.getValue(1)));
            } catch (ArrayIndexOutOfBounds ignored) {
            }
        }
    }
}
