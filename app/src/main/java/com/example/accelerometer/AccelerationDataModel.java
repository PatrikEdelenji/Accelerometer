package com.example.accelerometer;

public class AccelerationDataModel {
    private int id;
    private double totalValue;
    private double xValue;
    private double yValue;
    private double zValue;
    private long timestamp;

    public AccelerationDataModel(int id, double totalValue, double xValue, double yValue, double zValue, long timestamp) {
        this.id = id;
        this.totalValue = totalValue;
        this.xValue = xValue;
        this.yValue = yValue;
        this.zValue = zValue;
        this.timestamp = timestamp;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue() {
        this.totalValue = totalValue;
    }

    public double getXValue() {
        return xValue;
    }

    public void setXValue(double xValue) {
        this.xValue = xValue;
    }

    public double getYValue() {
        return yValue;
    }

    public void setYValue(double yValue) {
        this.yValue = yValue;
    }

    public double getZValue() {
        return zValue;
    }

    public void setZValue(double zValue) {
        this.zValue = zValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}