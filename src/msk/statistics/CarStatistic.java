package msk.statistics;

public class CarStatistic {

    public int theObject;
    private double creationTime = 0;
    private double timeToBridge = 0;
    private double timeOnBridge = 0;
    private double timeAfterBridge = 0;
    private double finishTime = 0;
    private String direction;


    public CarStatistic(int object, double creationTime){
        this.theObject = object;
        this.creationTime = creationTime;
    }

    public double getTimeToBridge() {
        return timeToBridge;
    }

    public void setTimeToBridge(double time) {
        log("Will be substracting: "+time+ " - "+this.creationTime);
        this.timeToBridge = time - this.creationTime;
    }

    public double getTimeOnBridge() {
        return timeOnBridge;
    }

    public void setTimeOnBridge(double time) {
        log("Will be substracting: "+time+ " - "+this.timeToBridge);
        this.timeOnBridge = time - this.timeToBridge;
    }

    public double getTimeAfterBridge() {
        return timeAfterBridge;
    }

    public void setTimeAfterBridge(double time) {
        log("Will be substracting: "+time+ " - "+this.timeOnBridge);
        this.timeAfterBridge = time - timeOnBridge;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }

    public double getCreationTime() {
        return creationTime;
    }

    private void log(String message) {
        System.out.println("CarStatistic: " + message);
    }


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
