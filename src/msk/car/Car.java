package msk.car;

import msk.Configuration;

import java.util.Random;

public class Car implements Comparable<Car>{
    private float speed;
    private float bridge_speed;

    // position posibilities - TO_BRIDGE, ON_BRIDGE, AFTER_BRIDGE, IN_QUEUE
    private String position;
    private int objectHandle;
    private String direction;
    private int distance;

    private double nextUpdate;
    Random rnd = new Random();
    public Car(int objectHandle, double currentTime){
        if(rnd.nextBoolean()){
            direction = "WEST";
            distance = Configuration.distanceWest;
        }
        else{
            direction = "EAST";
            distance = Configuration.distanceEast;
        }
        position = "TO_BRIDGE";
        speed = rnd.nextInt(Configuration.carSpeedDiff)+ Configuration.carSpeedMin;
        bridge_speed = rnd.nextInt(Configuration.bridgeCarSpeedDiff)+Configuration.bridgeCarSpeedMin;

        this.objectHandle = objectHandle;

        countTime(currentTime);
        log("Heading "+direction);
    }
    public int getHandle(){
        return objectHandle;
    }

    private void countTime(double currentTime){
        if(position == "TO_BRIDGE"){
            nextUpdate = currentTime + ((double)this.distance / (double)this.speed);
            log("Next update setted to: "+nextUpdate);
        }
        else if(position == "ON_BRIDGE"){
            nextUpdate = currentTime + ((double)Configuration.distanceBridge / (double)this.bridge_speed );
            log("Next update setted to: "+nextUpdate);
        }
        else if(position == "AFTER_BRIDGE"){
            nextUpdate = currentTime + ( (double)this.distance / (double)this.speed);
            log("Next update setted to: "+nextUpdate);
        }
        else{
            nextUpdate = currentTime + 1;
            log("In queue, Next update setted to: "+nextUpdate);
        }
    }

    public void changeStatus(String status, double currentTime){
        if(status == "ON_BRIDGE"){
            position = status;
        }
        else if(status == "AFTER_BRIDGE"){
            position = status;
            distance = (direction == "WEST") ? Configuration.distanceEast : Configuration.distanceWest;
        }
        else{
            position = status;
        }
        countTime(currentTime);
    }



    public String getPosition(){
        return position;
    }

    public Double getNextUpdate(){
        return nextUpdate;
    }

    public void update(double currentTime, boolean light_west, boolean light_east){
        if(currentTime == nextUpdate){
            if(position == "TO_BRIDGE" || position == "IN_QUEUE_WEST" || position == "IN_QUEUE_EAST"){
                if(direction == "WEST"){
                    if(light_west){
                        changeStatus("ON_BRIDGE", currentTime);
                        log("I've got on bridge.");
                    }
                    else{
                        changeStatus("IN_QUEUE_WEST", currentTime);
                        log("Waiting in queue west.");
                    }
                }
                else if(direction == "EAST"){
                    if(light_east){
                        changeStatus("ON_BRIDGE", currentTime);
                        log("I've got on bridge.");
                    }
                    else{
                        changeStatus("IN_QUEUE_EAST", currentTime);
                        log("Waiting in queue east.");
                    }
                }
            }
            else if(position == "ON_BRIDGE"){
                changeStatus("AFTER_BRIDGE", currentTime);
                log("I've left bridge.");
            }
            else{
                position = "END";
                log("I've reached final point.");
            }
        }
        else{
            log("Update with no changes!");
        }
    }

    public String getDirection() {
        return direction;
    }

    public void log(String message)
    {
        System.out.println( "CarObject   : Handle: " + this.getHandle()+ ", " + message );
    }
    @Override
    public int compareTo(Car car){
        return this.getNextUpdate().compareTo(car.getNextUpdate());
    }

}
