package msk.car;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import msk.HandlersHelper;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;


import hla.rti.RTIambassador;
import hla.rti.RTIexception;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.*;

public class CarFederate {
    public static final String READY_TO_RUN = "ReadyToRun";



    private RTIambassador rtiamb;
    private CarAmbassador fedamb;
    private final double timeStep           = 10.0;

    private int ITERATIONS = 50;

    private List<Car> carList;
    private List<Car> carListToDelete;

    public void runFederate() throws RTIexception {
        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
        carList = new ArrayList<Car>();
        carListToDelete = new ArrayList<Car>();
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


        for(int i=0;i<5;i++){
            createCar();
        }
        Random rnd = new Random();
        while (fedamb.running) {
            if(rnd.nextFloat() < 0.3){
                createCar();
            }
            checkToDelete();
            Collections.sort(carList);
            if(carList.size() > 0){
                advanceTime(carList.get(0).getNextUpdate());
                log("Time propably advanced to: "+fedamb.federateTime);
                for (Car car : carList)
                {
                    log("!!Car: "+car.getHandle()+ ", next update: " + car.getNextUpdate());
                    if(fedamb.federateTime == car.getNextUpdate()){
                        updateAttributeValues(car);
                        log("Car: "+car.getHandle()+ ", attributes updated");
                    }
                    else{
                        log("Car: "+car.getHandle()+ ", attributes not updated");
                    }

                }

                rtiamb.tick();
            }
            else{
                sendInteraction(fedamb.federateTime + fedamb.federateLookahead);
            }
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

    private void updateAttributeValues( Car car ) throws RTIexception
    {
        SuppliedAttributes attributes =
                RtiFactoryFactory.getRtiFactory().createSuppliedAttributes();

        car.update(fedamb.federateTime, fedamb.lights_west, fedamb.lights_east);

        byte[] positionValue = EncodingHelpers.encodeString(car.getPosition());

        int classHandle = rtiamb.getObjectClass( car.getHandle() );

        int positionHandle = rtiamb.getAttributeHandle( "position", classHandle );

        attributes.add( positionHandle, positionValue );

        LogicalTime time = convertTime( fedamb.federateTime + fedamb.federateLookahead );
        rtiamb.updateAttributeValues( car.getHandle(), attributes, generateTag(), time );
    }

    private void sendInteraction(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
//        Random random = new Random();
//        int speedInt = random.nextInt(20) + 1;
//        byte[] speed = EncodingHelpers.encodeInt(speedInt);
//        byte[] stop = EncodingHelpers.encodeString("Stop");

//
        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Finish");
//        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Car");
//        int objectHandle = rtiamb.getObjectClassHandle("ObjectRoot.Car");
//        int speedHandle = rtiamb.getParameterHandle( "speed", objectHandle );
//
//        int stopHandle = rtiamb.getParameterHandle("run", interactionHandle);
//        parameters.add(stopHandle, stop);
//
//        log("");
        LogicalTime time = convertTime( timeStep );
//        log("Sending actual speed: " + speedInt);
//        // TSO
//        rtiamb.obje

        rtiamb.sendInteraction(interactionHandle, parameters,"tag".getBytes(), time );
        fedamb.running = false;
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
        HandlersHelper.addInteractionClassHandler("InteractionRoot.ChangeLights", lightsHandle);
        rtiamb.subscribeInteractionClass( lightsHandle );

        int stopHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Finish");
        HandlersHelper.addInteractionClassHandler("InteractionRoot.Finish", stopHandle);
        rtiamb.publishInteractionClass(stopHandle);
        rtiamb.subscribeInteractionClass(stopHandle);

        log("Published and subscribed!");
    }

    private void advanceTime( double timestep ) throws RTIexception
    {
        log("requesting time advance for: " + timestep + ", current simtime: "+fedamb.federateTime);
        // request the advance
        fedamb.isAdvancing = true;
        //LogicalTime newTime = convertTime( fedamb.federateTime + timestep );
        LogicalTime newTime = convertTime(timestep);
        rtiamb.timeAdvanceRequest( newTime );
        while( fedamb.isAdvancing )
        {
            rtiamb.tick();
        }
    }

    private void checkToDelete() throws RTIexception{
        for (Car car : carList)
        {
            if(car.getPosition() == "END"){
                carListToDelete.add(car);
            }
        }
        for(Car car : carListToDelete){
//            carList.remove(car);
            deleteObject(car);
        }
        carListToDelete.clear();
    }

    private void createCar() throws RTIexception
    {
        int objectHandle = registerObject();
        log("Object Car created with handle: "+objectHandle);
        carList.add(new Car(objectHandle, fedamb.federateTime));
    }

    private int registerObject() throws RTIexception
    {
        int classHandle = rtiamb.getObjectClassHandle( "ObjectRoot.Car" );
        return rtiamb.registerObjectInstance( classHandle );
    }

    private void deleteObject(Car car) throws RTIexception
    {
        carList.remove(car);
        rtiamb.deleteObjectInstance(car.getHandle(), generateTag());
        log("Object with handle: "+car.getHandle() +" has been deleted!");
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
