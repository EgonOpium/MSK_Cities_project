package msk.car;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import msk.Configuration;
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

    private List<Car> carList;
    private List<Car> carListToDelete;

    double lastUpdate;

    public void runFederate() throws RTIexception {
        {
            rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
            carList = new ArrayList<Car>();
            carListToDelete = new ArrayList<Car>();
        }

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


        for(int i = 0; i< Configuration.initialCarNumber; i++){
            createCar();
        }
        Random rnd = new Random();
        while (fedamb.running) {
            if(rnd.nextFloat() < Configuration.makeNewCarChance){
                createCar();
            }
            checkToDelete();
            Collections.sort(carList);
            if(carList.size() > 0){
                lastUpdate = carList.get(0).getNextUpdate();
                advanceTime(carList.get(0).getNextUpdate());

                log("Time advanced to: "+fedamb.federateTime);
                for (Car car : carList)
                {
                    log("!!Car: "+car.getHandle()+ ", next update: " + car.getNextUpdate());
                    if(fedamb.federateTime == car.getNextUpdate()){
                        updateAttributeValues(car);
                    }
                }

                rtiamb.tick();
            }
            else{
                if(fedamb.federateTime == lastUpdate){
                    sendStopInteraction(fedamb.federateTime + fedamb.federateLookahead);
                    log("Stop interaction sent.");
                    for (int i = 0; i < 3; i++) {
                        rtiamb.tick();
                    }
                    fedamb.running = false;

                }
                else{
                    log("Guess what now... :(");
                }
            }
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

    private void waitForUser() {
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

    private void enableTimePolicy() throws RTIexception {
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

    private void updateAttributeValues( Car car ) throws RTIexception {
        SuppliedAttributes attributes =
                RtiFactoryFactory.getRtiFactory().createSuppliedAttributes();

        car.update(fedamb.federateTime, fedamb.lights_west, fedamb.lights_east);

        byte[] positionValue = EncodingHelpers.encodeString(car.getPosition());
        byte[] directionValue = EncodingHelpers.encodeString(car.getDirection());

        int classHandle = rtiamb.getObjectClass( car.getHandle() );

        int positionHandle = rtiamb.getAttributeHandle( "position", classHandle );
        int directionHandle = rtiamb.getAttributeHandle("direction", classHandle);

        attributes.add( positionHandle, positionValue );
        attributes.add( directionHandle, directionValue );

        LogicalTime time = convertTime( fedamb.federateTime + fedamb.federateLookahead );
        rtiamb.updateAttributeValues( car.getHandle(), attributes, generateTag(), time );
    }

    private void sendStopInteraction(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Finish");

        LogicalTime time = convertTime( timeStep );

        rtiamb.sendInteraction(interactionHandle, parameters,"tag".getBytes(), time );

//        advanceTime(fedamb.federateTime + 10);
//        fedamb.running = false;
    }

    private void publishAndSubscribe() throws RTIexception {
        int carHandle   = rtiamb.getObjectClassHandle( "ObjectRoot.Car" );
        int positionHandle = rtiamb.getAttributeHandle("position", carHandle);
        int directionHandle = rtiamb.getAttributeHandle("direction", carHandle);
        int speedHandle = rtiamb.getAttributeHandle("speed", carHandle);
        int bridgeSpeedHandle = rtiamb.getAttributeHandle("bridgeSpeed", carHandle);


        AttributeHandleSet attributes =
                RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add( positionHandle );
        attributes.add( directionHandle );
        attributes.add( speedHandle );
        attributes.add( bridgeSpeedHandle );

        rtiamb.publishObjectClass(carHandle, attributes);

        int lightsHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.ChangeLights" );
        HandlersHelper.addInteractionClassHandler("InteractionRoot.ChangeLights", lightsHandle);
        rtiamb.subscribeInteractionClass( lightsHandle );

        int stopHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Finish");
        HandlersHelper.addInteractionClassHandler("InteractionRoot.Finish", stopHandle);
        rtiamb.publishInteractionClass(stopHandle);
        rtiamb.subscribeInteractionClass(stopHandle);
    }

    private void advanceTime( double timestep ) throws RTIexception {
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

    private void checkToDelete() throws RTIexception {
        for (Car car : carList)
        {
            if(car.getPosition() == "END"){
                carListToDelete.add(car);
            }
        }
        for(Car car : carListToDelete){
            deleteObject(car);
        }
        carListToDelete.clear();
    }

    private void createCar() throws RTIexception {
        int objectHandle = registerObject();
        carList.add(new Car(objectHandle, fedamb.federateTime));
    }

    private int registerObject() throws RTIexception {
        int classHandle = rtiamb.getObjectClassHandle( "ObjectRoot.Car" );
        return rtiamb.registerObjectInstance( classHandle );
    }

    private void deleteObject(Car car) throws RTIexception {
        carList.remove(car);
        rtiamb.deleteObjectInstance(car.getHandle(), generateTag());
        log("Object with handle: "+car.getHandle() +" has been deleted!");
    }

    private LogicalTime convertTime( double time ) {
        // PORTICO SPECIFIC!!
        return new DoubleTime( time );
    }

    private LogicalTimeInterval convertInterval( double time ) {
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval( time );
    }

    public void log(String message)
    {
        System.out.println( "CarFederate   : " + message );
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
