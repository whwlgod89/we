package kr.co.theunify.wear.sensor;

public interface ConnectionEvent {
    public void onStatusChanged(Sensor.CONNECT_STATE state);
    public void onDetect(Sensor sensor);
}
