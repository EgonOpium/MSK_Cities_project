package msk.car;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import msk.Configuration;
import msk.HandlersHelper;
import msk.template.TemplateFederate;
import hla.rti.RTIexception;
import java.util.*;



public class CarFederate extends TemplateFederate {
    public static final String READY_TO_RUN = "ReadyToRun";

    private List<Car> carList;
    private List<Car> carListToDelete;
    private boolean start;
    double lastUpdate;
    Random rnd;
    private boolean lights_west = false;
    private boolean lights_east = false;

    public CarFederate() {
        super("CarFederate");
        rnd = new Random();
        start = true;

        this.carList = new ArrayList<Car>();
        this.carListToDelete = new ArrayList<Car>();
    }
    @Override
    protected void createRTIAmbassador() throws RTIexception {
        this.rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
    }

    @Override
    protected void createAmbassador() {
        this.fedamb = new CarAmbassador(this);
    }

    @Override
    protected void mainMethod() throws RTIexception {
        if(start){
            start = false;
            for (int i = 0; i < Configuration.initialCarNumber; i++) {
                createCar();
            }
        }
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

    private void updateAttributeValues( Car car ) throws RTIexception {
        SuppliedAttributes attributes =
                RtiFactoryFactory.getRtiFactory().createSuppliedAttributes();

        car.update(this.fedamb.federateTime, this.lights_west, this.lights_east);

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

    protected void sendStopInteraction(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Finish");

        LogicalTime time = convertTime( timeStep );

        rtiamb.sendInteraction(interactionHandle, parameters,"tag".getBytes(), time );

//        advanceTime(fedamb.federateTime + 10);
//        fedamb.running = false;
    }

    public boolean isLights_west() {
        return lights_west;
    }

    public void setLights_west(boolean lights_west) {
        this.lights_west = lights_west;
    }

    public boolean isLights_east() {
        return lights_east;
    }

    public void setLights_east(boolean lights_east) {
        this.lights_east = lights_east;
    }


    @Override
    protected void publishAndSubscribe() throws RTIexception {
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
    @Override
    protected void advanceTime( double timestep ) throws RTIexception {
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

    @Override
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
