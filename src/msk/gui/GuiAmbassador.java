package msk.gui;

import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import msk.HandlersHelper;
import msk.template.TemplateAmbassador;

public class GuiAmbassador extends TemplateAmbassador {
    private final GuiFederate guiFederate;
    public GuiAmbassador(GuiFederate guiFederate){
        super(guiFederate);
        this.guiFederate = guiFederate;
    }
    @Override
    protected void log( String message )
    {
        System.out.println( "GuiFederateAmbassador: "+ message );
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
