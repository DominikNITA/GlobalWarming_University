package sample;

import application.Application;
import application.Zone;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableIntegerArray;
import javafx.collections.ObservableListBase;
import javafx.collections.ObservableMap;

public class Model {
    ObservableMap<Zone,Float> zonesWithAnomaly;
    protected IntegerProperty currentYear;
    Application application;

    public Model(Application application){
        zonesWithAnomaly = FXCollections.observableHashMap();
        currentYear = new SimpleIntegerProperty(0);
        this.application = application;
        currentYear.setValue(application.getAvailableYears().get(50));
        currentYear.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("Changed from: " + oldValue + " to " + newValue);
            }
        });
    }

    public ObservableMap<Zone, Float> getZonesWithAnomaly() {
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
}
