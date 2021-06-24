package msk.traffic_control;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import msk.HandlersHelper;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Random;

public class TrafficFederate {
    public static final String READY_TO_RUN = "ReadyToRun";

    private RTIambassador rtiamb;
    private TrafficAmbassador fedamb;

    boolean lights_west = false;
    boolean lights_east = false;
    boolean lastLight = false;

    public void runFederate() throws RTIexception {
        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

        try
        {
            File fom = new File( "cars-bridges.fed" );
            rtiamb.createFederationExecution( "ExampleFederation",
                    fom.toURI().toURL() );
            log( "Created Federation" );
        }
        catch( FederationExecutionAlreadyExists exists )
        {
            log( "Didn't create federation, it already existed" );
        }
        catch(MalformedURLException urle )
        {
            log( "Exception processing fom: " + urle.getMessage() );
            urle.printStackTrace();
            return;
        }

        fedamb = new TrafficAmbassador();
        rtiamb.joinFederationExecution( "TrafficFederate", "ExampleFederation", fedamb );
        log( "Joined Federation as TrafficFederate");

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

        while (fedamb.running) {
            advanceTime(randomTime());
            changeLights();
            sendInteraction(fedamb.federateTime + fedamb.federateLookahead);

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

    private void changeLights(){
        if((!lights_west && !lights_east) && !lastLight){
            lastLight = true;
            lights_west = true;
        }
        else if((!lights_west && !lights_east) && lastLight){
            lastLight = false;
            lights_east = true;
        }
        else if((lights_west && !lights_east) || (!lights_west && lights_east)){
            lights_west = false;
            lights_east = false;
        }
    }

    private void waitForUser()
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

    private void enableTimePolicy() throws RTIexception
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

    private void sendInteraction(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        byte[] west = EncodingHelpers.encodeBoolean(lights_west);
        byte[] east = EncodingHelpers.encodeBoolean(lights_east);

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.ChangeLights");
        int westHandle = rtiamb.getParameterHandle( "west", interactionHandle );
        int eastHandle = rtiamb.getParameterHandle( "east", interactionHandle );

        parameters.add(westHandle, west);
        parameters.add(eastHandle, east);

        LogicalTime time = convertTime( timeStep );
        // TSO
        rtiamb.sendInteraction( interactionHandle, parameters, "tag".getBytes(), time );

    }
    // TODO: Important to change!
    private void publishAndSubscribe() throws RTIexception {
        // Change lights interaction
        int lightsHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.ChangeLights" );
        fedamb.lightsHandle = lightsHandle;
        rtiamb.publishInteractionClass( lightsHandle );
        // Stop simulation interaction
        int stopHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Finish");
        HandlersHelper.addInteractionClassHandler("InteractionRoot.Finish", stopHandle);
        rtiamb.subscribeInteractionClass(stopHandle);
    }

    private void advanceTime( double timestep ) throws RTIexception
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

    private double randomTime() {
        Random r = new Random();
        return 1 +(4 * r.nextDouble());
    }

    private LogicalTime convertTime( double time )
    {
        // PORTICO SPECIFIC!!
        return new DoubleTime( time );
    }

    /**
     * Same as for {@link #convertTime(double)}
     */
    private LogicalTimeInterval convertInterval( double time )
    {
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval( time );
    }

    public void log(String message)
    {
        System.out.println( "TrafficFederate   : " + message );
    }

    public static void main(String[] args) {
        try {
            new TrafficFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
