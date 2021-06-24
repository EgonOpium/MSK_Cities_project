package msk;

public class Configuration {
    /*
        Car Federate
     */
    // Distances
    public static final int distanceWest = 2500;
    public static final int distanceEast = 3500;
    public static final int distanceBridge = 200;

    // Car creator values
    public static final int initialCarNumber = 5;
    public static final double makeNewCarChance = 0.04;

    // car speed = rnd.nextInt(carSpeedDiff)+carSpeedMin;
    public static final int carSpeedDiff = 10;
    public static final int carSpeedMin = 50;

    // car bridge_speed = rnd.nextInt(bridgeCarSpeedDiff)+bridgeCarSpeedMin;
    public static final int bridgeCarSpeedDiff = 5;
    public static final int bridgeCarSpeedMin = 30;

    /*
        Traffic Federate
     */



}
