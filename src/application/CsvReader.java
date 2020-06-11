package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CsvReader {
    private final static int StartingYear = 1880;
    private final static int YearsColumnOffset = 2;
    public static ArrayList<Integer> availableYears= new ArrayList<>();
    public static ArrayList<ZoneWithAnomalies> readFile(String path) {
        ArrayList<ZoneWithAnomalies> result = new ArrayList<>();
        try {
            FileReader file = new FileReader(path);
            BufferedReader bufRead = new BufferedReader(file);

            String line = bufRead.readLine();
            updateAvailableYears(line);
            line = bufRead.readLine();
            while (line != null) {
                String[] array = line.split(",");

                int latitude =  Integer.parseInt(array[0]);
                int longitude = Integer.parseInt(array[1]);
                Map<Integer, Float> temperatures = new HashMap<>();

                for(int i = 0; i < array.length - YearsColumnOffset; i++){

                    float val;
                    try{
                        val = Float.parseFloat(array[i+ YearsColumnOffset]);
                    }catch(Exception e){
                        val = Float.NaN;
                    }
                    temperatures.put(StartingYear+i,val);
                }
                Zone zone = new Zone(latitude,longitude);
                result.add(new ZoneWithAnomalies(temperatures,zone));

                line = bufRead.readLine();
            }
            bufRead.close();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void updateAvailableYears(String line){
        String[] array = line.split(",");
        for (int i = 2; i < array.length ; i++) {
            try{
                int temp = Integer.parseInt(array[i].substring(1,5));
                availableYears.add(temp);
            }catch(Exception e){
                System.out.println("Problem with parsing " + array[i]);;
            }
        }
    }
}
