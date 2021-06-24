package msk.traffic_control;

import hla.rti.LogicalTime;
import hla.rti.RTIexception;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import msk.HandlersHelper;
import msk.template.TemplateFederate;

import java.util.Random;

public class TrafficFederate extends TemplateFederate {

    private boolean lights_west = false;
    private boolean lights_east = false;
    private boolean lastLight = false;

    public TrafficFederate() {
        super("TrafficFederate");
    }

    @Override
    protected void createRTIAmbassador() throws RTIexception {
        this.rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
    }

    @Override
    protected void createAmbassador() {
        this.fedamb = new TrafficAmbassador(this);
    }

    @Override
    protected void mainMethod() throws RTIexception {
        advanceTime(randomTime());
            changeLights();
            sendInteraction(fedamb.federateTime + fedamb.federateLookahead);
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

    @Override
    public void sendInteraction(double timeStep) throws RTIexception{
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

    @Override
    public void publishAndSubscribe() throws RTIexception{
        // Change lights interaction
        int lightsHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.ChangeLights" );
        // fedamb.lightsHandle = lightsHandle;
        rtiamb.publishInteractionClass( lightsHandle );
        // Stop simulation interaction
        int stopHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Finish");
        HandlersHelper.addInteractionClassHandler("InteractionRoot.Finish", stopHandle);
        rtiamb.subscribeInteractionClass(stopHandle);
    }

    private double randomTime() {
        Random r = new Random();
        return 1 +(4 * r.nextDouble());
    }
    @Override
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
