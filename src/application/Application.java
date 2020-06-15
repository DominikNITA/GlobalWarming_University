package application;

import java.util.*;

public class Application {
    private ArrayList<ZoneWithAnomalies> zonesWithAnomalies;
    private ArrayList<Integer> availableYears;

    public Application() {
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


    public static void main(String[] args) {
        Application app = new Application();
        Zone testZone = new Zone(88,162);

        long startTime = System.nanoTime();
        Map<Zone,Float> testMap = app.getAnomaliesByYear(1952);
        long endTime = System.nanoTime();

        System.out.println(testMap);
        // get difference of two nanoTime values
        long timeElapsed = endTime - startTime;

        System.out.println("Execution time in nanoseconds  : " + timeElapsed);

        System.out.println("Execution time in milliseconds : " +
                timeElapsed / 1000000);
    }
}
