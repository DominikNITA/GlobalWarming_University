package application;

import java.util.Objects;

public class Zone {
    private int latitude;
    private int longitude;

    public Zone(int latitude, int longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zone zone = (Zone) o;
        return latitude == zone.latitude &&
                longitude == zone.longitude;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "Zone{" +
                 + latitude +
                "," + longitude +
                '}';
    }
}
