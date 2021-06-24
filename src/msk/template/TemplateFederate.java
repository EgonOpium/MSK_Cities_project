package msk.template;

import hla.rti.*;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;


public abstract class TemplateFederate {
    public static final String READY_TO_RUN = "ReadyToRun";

    protected RTIambassador rtiamb;
    protected TemplateAmbassador fedamb;
    protected String federateName;

    public TemplateFederate(String name){
        this.federateName = name;
    }

    protected void runFederate() throws RTIexception {
        createRTIAmbassador();

        try
        {
            File fom = new File("cars-bridges.fed");
            rtiamb.createFederationExecution( "ExampleFederation",
                    fom.toURI().toURL() );
            log( "Created Federation" );
        }
        catch( FederationExecutionAlreadyExists exists )
        {
            log( "Didn't create federation, it already existed" );
        }
        catch( MalformedURLException urle )
        {
            log( "Exception processing fom: " + urle.getMessage() );
            urle.printStackTrace();
            return;
        }

        createAmbassador();

        rtiamb.joinFederationExecution( federateName, "ExampleFederation", fedamb );
        log( "Joined Federation as " + federateName );

        rtiamb.registerFederationSynchronizationPoint( READY_TO_RUN, null );

        while( fedamb.isAnnounced == false )
        {
            rtiamb.tick();
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved( READY_TO_RUN );
        log( "Achieved sync point: " +READY_TO_RUN+ ", waiting for federation..." );

        while( fedamb.isReadyToRun == false )
        {
            rtiamb.tick();
        }
        enableTimePolicy();


        publishAndSubscribe();

        while(fedamb.running) {
            mainMethod();
            rtiamb.tick();
        }

        rtiamb.resignFederationExecution( ResignAction.NO_ACTION );
        log( "Resigned from Federation" );

        try
        {
            rtiamb.destroyFederationExecution( "ExampleFederation" );
            log( "Destroyed Federation" );
        }
        catch( FederationExecutionDoesNotExist dne )
        {
            log( "No need to destroy federation, it doesn't exist" );
        }
        catch( FederatesCurrentlyJoined fcj )
        {
            log( "Didn't destroy federation, federates still joined" );
        }


    }

    protected void waitForUser()
    {
        log( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );
        BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
        try
        {
            reader.readLine();
        }
        catch( Exception e )
        {
            log( "Error while waiting for user input: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    protected void enableTimePolicy() throws RTIexception
    {
        LogicalTime currentTime = convertTime( fedamb.federateTime );
        LogicalTimeInterval lookahead = convertInterval( fedamb.federateLookahead );

        this.rtiamb.enableTimeRegulation( currentTime, lookahead );

        while( fedamb.isRegulating == false )
        {
            rtiamb.tick();
        }

        this.rtiamb.enableTimeConstrained();

        while( fedamb.isConstrained == false )
        {
            rtiamb.tick();
        }
    }

    protected void sendInteraction(double timeStep) throws RTIexception {}

    protected void publishAndSubscribe() throws RTIexception {}

    protected void advanceTime( double timestep ) throws RTIexception
    {
        log("requesting time advance for: " + timestep);
        // request the advance
        fedamb.isAdvancing = true;
        LogicalTime newTime = convertTime( fedamb.federateTime + timestep );
        rtiamb.timeAdvanceRequest( newTime );
        while( fedamb.isAdvancing )
        {
            rtiamb.tick();
        }
    }

    protected LogicalTime convertTime( double time )
    {
        // PORTICO SPECIFIC!!
        return new DoubleTime( time );
    }

    protected LogicalTimeInterval convertInterval( double time )
    {
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval( time );
    }

    protected void log(String message)
    {
        System.out.println( this.federateName+"  : " + message );
    }

    protected abstract void createRTIAmbassador() throws RTIexception;
    protected abstract void createAmbassador();
    protected abstract void mainMethod() throws RTIexception;

}
