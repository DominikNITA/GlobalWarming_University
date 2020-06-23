package backend;

public class YearlyStatistics {
    private float minTemperature;
    private float maxTemperature;
    private float meanTemperature;

    public YearlyStatistics(float minTemperature, float maxTemperature, float meanTemperature) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.meanTemperature = meanTemperature;
    }

    public float getMinTemperature() {
        return minTemperature;
    }

    public float getMaxTemperature() {
        return maxTemperature;
    }

    public float getMeanTemperature() {
        return meanTemperature;
    }
}
