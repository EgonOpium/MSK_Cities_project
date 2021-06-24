package msk.template;

import hla.rti.LogicalTime;
import hla.rti.RTIambassador;
import hla.rti.jlc.NullFederateAmbassador;
import msk.gui.GuiAmbassador;
import org.portico.impl.hla13.types.DoubleTime;

public abstract class TemplateAmbassador extends NullFederateAmbassador {
    protected double federateTime        = 0.0;
    protected double federateLookahead   = 1.0;

    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;

    protected boolean isAnnounced        = false;
    protected boolean isReadyToRun       = false;

    protected boolean running 			 = true;

    protected final TemplateFederate templateFederate;

    //-----------------------------------------------------------------
    //
    //              HERE IS THE PLACE FOR EXTERNAL VARIABLES
    //
    //-----------------------------------------------------------------

    public TemplateAmbassador(TemplateFederate templateFederate) {
        this.templateFederate = templateFederate;
    }

    protected double convertTime(LogicalTime logicalTime )
    {
        // PORTICO SPECIFIC!!
        return ((DoubleTime)logicalTime).getTime();
    }

    protected void log( String message )
    {
        System.out.println( "FederateAmbassador: " + message );
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
        if( label.equals(TemplateFederate.READY_TO_RUN) )
            this.isAnnounced = true;
    }

    public void federationSynchronized( String label )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(TemplateFederate.READY_TO_RUN) )
            this.isReadyToRun = true;
    }

    /**
     * The RTI has informed us that time regulation is now enabled.
     */
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

    public void timeAdvanceGrant( LogicalTime theTime )
    {
        this.federateTime = convertTime( theTime );
        this.isAdvancing = false;
    }


    //-----------------------------------------------------------------
    //
    //              HERE IS THE PLACE FOR EXTERNAL METHODS
    //
    //-----------------------------------------------------------------
}
