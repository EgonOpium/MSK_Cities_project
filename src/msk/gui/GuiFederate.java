package msk.gui;

// TODO: Not started yet... : (

import hla.rti.RTIexception;
import msk.car.CarFederate;

import java.util.Random;

public class GuiFederate {
    public void runFederate() throws RTIexception {
        Random rnd = new Random();

        for(int i=0;i<10;i++){
            rnd.nextFloat();
            log("New float: "+rnd.nextFloat());
        }

    }

    public void log(String message)
    {
        System.out.println( "GuiFederate   : " + message );
    }
    public static void main(String[] args) {
        try {
            new GuiFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }



    }
}
