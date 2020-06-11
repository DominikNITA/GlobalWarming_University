package application;

import java.util.Map;

public class ZoneWithAnomalies {
    private Map<Integer, Float> temperaturesByYear;
    private Zone zone;

    public ZoneWithAnomalies(Map<Integer, Float> temperaturesByYear, Zone zone) {
        this.temperaturesByYear = temperaturesByYear;
        this.zone = zone;
    }

    public Zone getZone() {
        return zone;
    }

    public Map<Integer, Float> getTemperaturesByYear(){
        return temperaturesByYear;
    }

    public float getMin(){
        float min = Float.MAX_VALUE;
        for (Map.Entry<Integer,Float> entry:temperaturesByYear.entrySet()
             ) {
            if(entry.getValue() < min){
                min = entry.getValue();
            }
        }
        return min;
    }

    public float getMax(){
        float max = Float.MIN_VALUE;
        for(Map.Entry<Integer,Float> entry:temperaturesByYear.entrySet()){
            if(entry.getValue() > max){
                max = entry.getValue();
            }
        }
        return max;
    }

    public float getValueByYear(int year){
        float result = Float.NaN;
        for(Map.Entry<Integer,Float> entry:temperaturesByYear.entrySet()){
            if(entry.getKey() == year){
                result = entry.getValue();
            }
        }
        return result;
    }

    //TODO: delete
    private void checkmin(float value, Float min){
        if(value < min){
            min = value;
        }
    }

    @Override
    public String toString() {
        return "ZoneWithAnomalies{" +
                "temperaturesByYear=" + temperaturesByYear +
                ", zone=" + zone +
                '}';
    }
}
