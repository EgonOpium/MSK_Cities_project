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
    private int ITERATIONS = 50;
    private RTIambassador rtiamb;
    private CarAmbassador fedamb;
    private final double timeStep           = 10.0;

    protected float speed;
    protected float bridge_speed;
    protected String position;

    public void runFederate() throws RTIexception {
        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

        //TODO: Here I have to change path to FOM Model
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
        catch(MalformedURLException urle )
        {
            log( "Exception processing fom: " + urle.getMessage() );
            urle.printStackTrace();
            return;
        }

        // TODO: Still FOM Model needs to be changed
        fedamb = new CarAmbassador();
        rtiamb.joinFederationExecution( "CarFederate", "ExampleFederation", fedamb );
        log( "Joined Federation as CarFederate");

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
        log("Published and subscribed!");

        int objectHandle = registerObject();
        log("Object Car created with handle: "+objectHandle);

        int counter = 0;

        while (fedamb.running) {
            advanceTime(randomTime());
            updateAttributeValues(objectHandle);
            counter++;
            if (counter >= ITERATIONS) {
                sendInteraction(fedamb.federateTime + fedamb.federateLookahead);
            }
            rtiamb.tick();
        }
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
    private void updateAttributeValues( int objectHandle ) throws RTIexception
    {
        ///////////////////////////////////////////////
        // create the necessary container and values //
        ///////////////////////////////////////////////
        // create the collection to store the values in, as you can see
        // this is quite a lot of work
        SuppliedAttributes attributes =
                RtiFactoryFactory.getRtiFactory().createSuppliedAttributes();
        Random rnd = new Random();
        // generate the new values
        // we use EncodingHelpers to make things nice friendly for both Java and C++
        byte[] speedValue = EncodingHelpers.encodeString(Integer.toString(rnd.nextInt(5)+50) );
        // TODO: Position and bridge speed needs to be changed!
        byte[] positionValue = EncodingHelpers.encodeString( "ab:" + getLbts() );
        byte[] bridgeValue = EncodingHelpers.encodeString( "ac:" + getLbts() );

        // get the handles
        // this line gets the object class of the instance identified by the
        // object instance the handle points to
        int classHandle = rtiamb.getObjectClass( objectHandle );
        int speedHandle = rtiamb.getAttributeHandle( "speed", classHandle );
        int positionHandle = rtiamb.getAttributeHandle( "position", classHandle );
        int bridgeSpeedHandle = rtiamb.getAttributeHandle( "bridgeSpeed", classHandle );

        // put the values into the collection
        attributes.add( speedHandle, speedValue );
        attributes.add( positionHandle, positionValue );
        attributes.add( bridgeSpeedHandle, bridgeValue );

        //////////////////////////
        // do the actual update //
        //////////////////////////
        rtiamb.updateAttributeValues( objectHandle,attributes, generateTag() );

        // note that if you want to associate a particular timestamp with the
        // update. here we send another update, this time with a timestamp:
        LogicalTime time = convertTime( fedamb.federateTime + fedamb.federateLookahead );
        rtiamb.updateAttributeValues( objectHandle, attributes, generateTag(), time );
    }

    private void sendInteraction(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
//        Random random = new Random();
//        int speedInt = random.nextInt(20) + 1;
//        byte[] speed = EncodingHelpers.encodeInt(speedInt);
        byte[] stop = EncodingHelpers.encodeString("Stop");

//
        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.SimulationStop");
//        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Car");
//        int objectHandle = rtiamb.getObjectClassHandle("ObjectRoot.Car");
//        int speedHandle = rtiamb.getParameterHandle( "speed", objectHandle );
//
        int stopHandle = rtiamb.getParameterHandle("run", interactionHandle);
        parameters.add(stopHandle, stop);
//
        log("");
        LogicalTime time = convertTime( timeStep );
//        log("Sending actual speed: " + speedInt);
//        // TSO
//        rtiamb.obje

        rtiamb.sendInteraction(interactionHandle, parameters,"tag".getBytes(), time );

        log("Car - Stop sended!");
//        rtiamb.sendInteraction( interactionHandle, parameters, "tag".getBytes(), time );
//        // RO
//        rtiamb.sendInteraction( interactionHandle, parameters, "tag".getBytes() );
    }
    // TODO: Important to change!
    private void publishAndSubscribe() throws RTIexception {
        int carHandle   = rtiamb.getObjectClassHandle( "ObjectRoot.Car" );
        int speedHandle = rtiamb.getAttributeHandle("speed", carHandle);
        int bridgeSpeedHandle = rtiamb.getAttributeHandle("bridgeSpeed", carHandle);
        int positionHandle = rtiamb.getAttributeHandle("position", carHandle);

        AttributeHandleSet attributes =
                RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add( speedHandle );
        attributes.add( bridgeSpeedHandle );
        attributes.add( positionHandle );

        rtiamb.publishObjectClass(carHandle, attributes);

        int lightsHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.ChangeLights" );
        fedamb.lightsHandle = lightsHandle;
        rtiamb.subscribeInteractionClass( lightsHandle );

        int stopHandle = rtiamb.getInteractionClassHandle("InteractionRoot.SimulationStop");
        fedamb.stopHandle = stopHandle;
        rtiamb.publishInteractionClass(stopHandle);
        rtiamb.subscribeInteractionClass(stopHandle);
        log("Published and subscribed stop!");
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

    private int registerObject() throws RTIexception
    {
        int classHandle = rtiamb.getObjectClassHandle( "ObjectRoot.Car" );
        return rtiamb.registerObjectInstance( classHandle );
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

    private double getLbts()
    {
        return fedamb.federateTime + fedamb.federateLookahead;
    }

    private byte[] generateTag()
    {
        return (""+System.currentTimeMillis()).getBytes();
    }

    public static void main(String[] args) {
        try {
            new CarFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
       
}
