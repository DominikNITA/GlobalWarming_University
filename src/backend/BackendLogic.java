package backend;

import java.util.*;

public class BackendLogic {
    private final ArrayList<ZoneWithAnomalies> zonesWithAnomalies;
    private final ArrayList<Integer> availableYears;

    public BackendLogic() {
        String path = this.getClass().getResource("../resources/tempanomaly_4x4grid.csv").getPath();
        zonesWithAnomalies = CsvReader.readFile(path);
        availableYears = CsvReader.availableYears;
    }

    public ArrayList<Integer> getAvailableYears() {
        return availableYears;
    }

    //Time: 50ms
    public float getMinAnomaly(){
        float min = Float.MAX_VALUE;
        for (ZoneWithAnomalies zone: zonesWithAnomalies
             ) {
            float zoneMin = zone.getMin();
            if(zoneMin < min){
                min = zoneMin;
            }
        }
        return min;
    }

    //Time: 50ms
    public float getMaxAnomaly(){
        float max = Float.MIN_VALUE;
        for (ZoneWithAnomalies zone : zonesWithAnomalies){
            float zoneMax = zone.getMax();
            if(zoneMax > max){
                max = zoneMax;
            }
        }
        return max;
    }

    //Time: <5ms
    public float getAnomalyValue(Zone zone, int year){
        float result = Float.NaN;
        for(ZoneWithAnomalies zoneWithAnomalies : zonesWithAnomalies){
            if(zoneWithAnomalies.getZone().equals(zone)){
                result = zoneWithAnomalies.getValueByYear(year);
            }
        }
        return result;
    }

    //Time: 50ms
    public Map<Zone,Float> getAnomaliesByYear(int year){
        Map<Zone,Float> anomalies = new LinkedHashMap<>();
        for (ZoneWithAnomalies zoneWithAnomalies : zonesWithAnomalies){
            anomalies.put(zoneWithAnomalies.getZone(),zoneWithAnomalies.getValueByYear(year));
        }
        return anomalies;
    }

    //Time : 1-2ms
    public List<Float> getYearlyAnomaliesForZone(Zone zone){
        List<Float> anomalies = new ArrayList<>();
        for (ZoneWithAnomalies zoneWithAnomalies : zonesWithAnomalies){
            if(zoneWithAnomalies.getZone().equals(zone)){
                anomalies = new ArrayList<>(zoneWithAnomalies.getTemperaturesByYear().values());
            }
        }
        return anomalies;
    }

    public YearlyStatistics getYearlyStatistics(int year){
        Map<Zone, Float> anomalies = getAnomaliesByYear(year);
        float min = Float.MAX_VALUE,max = Float.MIN_VALUE,sum = 0;
        for (Map.Entry<Zone, Float> entry : anomalies.entrySet()) {
            sum = entry.getValue().isNaN() ? sum : sum + entry.getValue();
            if(min > entry.getValue()){
                min = entry.getValue();
            }
            if(max < entry.getValue()){
                max = entry.getValue();
            }
        }
        return new YearlyStatistics(min,max,sum/anomalies.size());
    }
}
