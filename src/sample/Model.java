package sample;

import application.Application;
import application.Zone;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableIntegerArray;
import javafx.collections.ObservableListBase;
import javafx.collections.ObservableMap;

import java.util.Map;

public class Model {
    //ObservableMap<Zone,Float> zonesWithAnomaly;
    private MapProperty<Zone,Float> zonesWithAnomaly;
    private IntegerProperty currentYear;
    private Application application;
    private float min,max;
    private ObjectProperty<Zone> selectedZone;

    public Model(Application application){
        zonesWithAnomaly = new SimpleMapProperty<>();
        currentYear = new SimpleIntegerProperty(0);
        this.application = application;
        currentYear.setValue(application.getAvailableYears().get(50));
        currentYear.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                zonesWithAnomaly.setValue(FXCollections.observableMap(application.getAnomaliesByYear(newValue.intValue())));
            }
        });

        selectedZone = new SimpleObjectProperty<>();

        min = application.getMinAnomaly();
        max = application.getMaxAnomaly();
    }

    public ObservableMap<Zone, Float> getZonesWithAnomaly() {
        return zonesWithAnomaly.get();
    }

    public MapProperty<Zone, Float> zonesWithAnomalyProperty() {
        return zonesWithAnomaly;
    }

    public int getCurrentYear() {
        return currentYear.get();
    }

    public IntegerProperty currentYearProperty() {
        return currentYear;
    }

    public Application getApplication() {
        return application;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public void IncrementCurrentYear() {
        currentYear.setValue(currentYear.getValue() + 1);
    }

    public Zone getSelectedZone() {
        return selectedZone.get();
    }

    public ObjectProperty<Zone> selectedZoneProperty() {
        return selectedZone;
    }
}
