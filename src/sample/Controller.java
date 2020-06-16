package sample;

import application.Application;
import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import org.w3c.dom.ls.LSOutput;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private Pane pane3D;

    @FXML
    private Pane yearStatisticsPane;

    @FXML
    private Pane zoneStatisticsPane;

    @FXML
    private VBox scaleLegendBox;

    @FXML
    private Slider timeSlider;

    @FXML
    private Slider yearSlider;

    @FXML
    private Spinner<Number> yearSpinner;

    @FXML
    private Button playPauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private ChoiceBox modeDropdown;

    private Model model;
    private Application application;

    private final int scaleCount = 8;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Group root3D = new Group();
        // Load geometry
        ObjModelImporter objImporter = new ObjModelImporter();
        try{
            URL modelUrl = this.getClass().getResource("../resources/earth.obj");
            objImporter.read(modelUrl);
        } catch (ImportException e){
            System.out.println(e.getMessage());
        }
        MeshView[] meshViews = objImporter.getImport();
        Group earth = new Group(meshViews);
        Group earthWithTowns = new Group(earth);
        root3D.getChildren().add(earthWithTowns);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        new CameraManager(camera, pane3D, root3D);

        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(-180);
        light.setTranslateY(-90);
        light.setTranslateZ(-120);
        light.getScope().addAll(root3D);
        root3D.getChildren().add(light);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        ambientLight.getScope().addAll(root3D);
        root3D.getChildren().add(ambientLight);

        SubScene subScene = new SubScene(root3D,850,800,false, SceneAntialiasing.BALANCED);
        subScene.setViewOrder(10f);
        subScene.setCamera(camera);
        subScene.setFill(Color.GREY);
        pane3D.getChildren().addAll(subScene);

        application = new Application();
        model = new Model(application);

        model.currentYearProperty().bindBidirectional(yearSlider.valueProperty());
        model.currentYearProperty().bindBidirectional(yearSpinner.getValueFactory().valueProperty());
//        Spinner<Integer> spinner = new Spinner<Integer>(application.getAvailableYears().get(0),
//                application.getAvailableYears().get(application.getAvailableYears().size()-1),
//                application.getAvailableYears().get(0)
//                ,1);
//        spinner.prefHeight(26f);
//        spinner.prefWidth(100f);
//        yearSelectorZone.getChildren().add(spinner);
//        spinnerValue = model.currentYearProperty().asObject();


        //spinner.getValueFactory().valueProperty().bindBidirectional(model.currentYearProperty().asObject());

//        System.out.println("WORKING");
//        model.currentYear.addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                //spinner.getValueFactory().valueProperty().setValue((Integer)newValue);
//                System.out.println("SPINNER?!?! from: " + oldValue + " to " + newValue);
//            }
//        });
//
//        spinnerValue.addListener(new ChangeListener<Integer>() {
//            @Override
//            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
//                yearSlider.valueProperty().setValue(newValue);
//                System.out.println("Changing slider");
//            }
//        });
    }

    private void setupScale() {

    }
}
