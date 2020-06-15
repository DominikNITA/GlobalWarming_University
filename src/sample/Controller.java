package sample;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;

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
    private Spinner yearSpinner;

    @FXML
    private Button playPauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private ChoiceBox modeDropdown;


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
    }

    private void setupScale() {

    }
}
