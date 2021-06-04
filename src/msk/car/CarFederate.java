package msk.car;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;


import hla.rti.RTIambassador;
import hla.rti.RTIexception;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Random;

public class CarFederate {
    public static final String READY_TO_RUN = "ReadyToRun";

    private RTIambassador rtiamb;
    private CarAmbassador fedamb;
    private final double timeStep           = 10.0;

    protected float speed;
    protected float bridge_speed;
    protected String position;

    public void runFederate() throws RTIexception {
//        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
//
//        //TODO: Here I have to change path to FOM Model
//        try
//        {
//            File fom = new File( "producer-consumer.fed" );
//            rtiamb.createFederationExecution( "ExampleFederation",
//                    fom.toURI().toURL() );
//            log( "Created Federation" );
//        }
//        catch( FederationExecutionAlreadyExists exists )
//        {
//            log( "Didn't create federation, it already existed" );
//        }
//        catch(MalformedURLException urle )
//        {
//            log( "Exception processing fom: " + urle.getMessage() );
//            urle.printStackTrace();
//            return;
//        }
//
//        // TODO: Still FOM Model needs to be changed
//        fedamb = new CarAmbassador();
//        rtiamb.joinFederationExecution( "ConsumerFederate", "ExampleFederation", fedamb );
//        log( "Joined Federation as ProducerFederate");
//
//        rtiamb.registerFederationSynchronizationPoint( READY_TO_RUN, null );
//
//        while( fedamb.isAnnounced == false )
//        {
//            rtiamb.tick();
//        }
//
//        waitForUser();
//
//        rtiamb.synchronizationPointAchieved( READY_TO_RUN );
//        log( "Achieved sync point: " +READY_TO_RUN+ ", waiting for federation..." );
//        while( fedamb.isReadyToRun == false )
//        {
//            rtiamb.tick();
//        }
//
//        enableTimePolicy();
//
//        publishAndSubscribe();
//
//        while (fedamb.running) {
//            advanceTime(randomTime());
//            sendInteraction(fedamb.federateTime + fedamb.federateLookahead);
//            rtiamb.tick();
//        }
        log("You should not see this. - CarFederate run loop.");
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
        Random random = new Random();
        int quantityInt = random.nextInt(10) + 1;
        byte[] quantity = EncodingHelpers.encodeInt(quantityInt);

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.GetProduct");
        int quantityHandle = rtiamb.getParameterHandle( "quantity", interactionHandle );

        parameters.add(quantityHandle, quantity);

        LogicalTime time = convertTime( timeStep );
        log("Sending GetProduct: " + quantityInt);
        // TSO
        rtiamb.sendInteraction( interactionHandle, parameters, "tag".getBytes(), time );
//        // RO
//        rtiamb.sendInteraction( interactionHandle, parameters, "tag".getBytes() );
    }
    // TODO: Important to change!
    private void publishAndSubscribe() throws RTIexception {
        int carHandle = rtiamb.getObjectClassHandle( "ObjectRoot.Car" );
        int speedHandle = rtiamb.getParameterHandle("speed", carHandle);
        int bridgeSpeedHandle = rtiamb.getParameterHandle("bridgeSpeed", carHandle);
        int positionHandle = rtiamb.getParameterHandle("position", carHandle);

        AttributeHandleSet attributes =
                RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add( speedHandle );
        attributes.add( bridgeSpeedHandle );
        attributes.add( positionHandle );

        rtiamb.publishObjectClass(carHandle, attributes);

        int lightsHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.ChangeLights" );
        fedamb.lightsHandle = lightsHandle;
        rtiamb.subscribeInteractionClass( lightsHandle );
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
        System.out.println( "CarFederate   : " + message );
    }

    public static void main(String[] args) {
        try {
            new CarFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
       
}
