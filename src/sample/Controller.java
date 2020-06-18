package sample;

import application.Application;
import application.Zone;
import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.animation.AnimationTimer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.w3c.dom.ls.LSOutput;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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

    ViewMode viewMode;
    Group dataVisualization;

    private final int scaleCount = 10;
    private float stepLength;
    private PhongMaterial[] materials = new PhongMaterial[scaleCount];
    private PhongMaterial NaNMaterial;

    AnimationTimer animationTimer;
    boolean isPlaying = false;
    float timeElapsed = 0f;
    Long lastTime = null;
    FloatProperty animationSpeed;

    private static final float TEXTURE_LAT_OFFSET = -0.2f;
    private static final float TEXTURE_LON_OFFSET = 2.8f;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setup3DScene();
        application = new Application();
        model = new Model(application);
        setupScale();
        modeDropdown.setItems(FXCollections.observableArrayList(ViewMode.values()));
        modeDropdown.valueProperty().setValue(ViewMode.Quadrilateral);
        setupListeners();
        setupBindings();
        setupAnimation();
    }

    private void setup3DScene() {
        Group root3D = new Group();
        // Load geometry
        ObjModelImporter objImporter = new ObjModelImporter();
        try {
            URL modelUrl = this.getClass().getResource("../resources/earth.obj");
            objImporter.read(modelUrl);
        } catch (ImportException e) {
            System.out.println(e.getMessage());
        }
        MeshView[] meshViews = objImporter.getImport();
        for (MeshView meshView : meshViews) {
            //meshView.setCullFace(CullFace.NONE);
        }
        Group earth = new Group(meshViews);
        dataVisualization = new Group();

        Group earthWithTowns = new Group(earth);
        earthWithTowns.getChildren().add(dataVisualization);
        root3D.getChildren().add(earthWithTowns);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        new CameraManager(camera, pane3D, root3D);

//        PointLight light = new PointLight(Color.WHITE);
//        light.setTranslateX(-180);
//        light.setTranslateY(-90);
//        light.setTranslateZ(-120);
//        light.getScope().addAll(root3D);
//        root3D.getChildren().add(light);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        ambientLight.getScope().addAll(root3D);
        root3D.getChildren().add(ambientLight);

        SubScene subScene = new SubScene(root3D, 850, 800, false, SceneAntialiasing.BALANCED);
        subScene.setViewOrder(10f);
        subScene.setCamera(camera);
        subScene.setFill(Color.GREY);
        pane3D.getChildren().addAll(subScene);
    }

    private void setupScale() {
        float opacity = 0.01f;
        NaNMaterial = new PhongMaterial();
        NaNMaterial.setDiffuseColor(new Color(0.3, 0.3, 0.3, opacity));

        float range = model.getMax() - model.getMin();
        stepLength = range / (scaleCount - 1);
        System.out.println(range);
        System.out.println(stepLength);
        for (int i = 0; i < scaleCount; i++) {
            float currentTemp = model.getMin() +  i * stepLength;
            PhongMaterial material = new PhongMaterial();
            if (currentTemp >= 0) {
                System.out.println(currentTemp);
                System.out.println(model.getMin());
                System.out.println(model.getMax());
                System.out.println(Math.min(currentTemp / model.getMax(), 1f) + " " + (1.00001 - (currentTemp / (model.getMax()))) + " " + (1.00001 - (currentTemp / (model.getMax()))));
                material.setDiffuseColor(new Color(Math.min(currentTemp / model.getMax(), 1f),
                        1.00001 - (currentTemp / model.getMax()),
                        1.00001 - (currentTemp / model.getMax()), opacity));
            } else {
                material.setDiffuseColor(new Color(1.00001 - (currentTemp / model.getMin()), 1.00001 - (currentTemp / model.getMin()),
                        currentTemp / model.getMin(),
                        opacity));
            }
            materials[i] = material;
        }
    }

    private void setupListeners() {
        model.zonesWithAnomalyProperty().addListener(new ChangeListener<ObservableMap<Zone, Float>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableMap<Zone, Float>> observable, ObservableMap<Zone, Float> oldValue, ObservableMap<Zone, Float> newValue) {
                dataVisualization.getChildren().removeAll();
                dataVisualization.getChildren().clear();
                switch ((ViewMode) modeDropdown.getValue()) {
                    case Quadrilateral:
                        setupQuadrilateralMode(newValue);
                        break;
                    case Histrogramme:
                        break;
                    default:
                        System.out.println("Invalid state");
                        break;
                }
            }
        });
    }

    private void setupQuadrilateralMode(Map<Zone, Float> data) {
        float degreeSize = 4f;
        float scale = 1.04f;
        long startTime = System.nanoTime();
        for (Map.Entry<Zone, Float> entry : data.entrySet()) {
            int latitude = entry.getKey().getLatitude();
            int longitude = entry.getKey().getLongitude();
            Point3D topLeft = geoCoordTo3dCoord(latitude - degreeSize/2, longitude - degreeSize/2).multiply(scale);
            Point3D topRight = geoCoordTo3dCoord(latitude - degreeSize/2, longitude + degreeSize/2).multiply(scale);
            Point3D bottomLeft = geoCoordTo3dCoord(latitude + degreeSize/2, longitude - degreeSize/2).multiply(scale);
            Point3D bottomRight = geoCoordTo3dCoord(latitude + degreeSize/2, longitude + degreeSize/2).multiply(scale);
            PhongMaterial material = getMaterial(entry.getValue());
            AddQuadrilateral(dataVisualization, topRight, bottomRight, bottomLeft, topLeft, material);
        }
        long stopTime = System.nanoTime();
        System.out.println("Time spent on generating quads: " + (stopTime - startTime) / 1000000 + "ms");
    }

    private PhongMaterial getMaterial(Float value) {
        if (value.equals(Float.NaN)) {
            return NaNMaterial;
        }
        //System.out.println(value +" "+ model.getMax() + " " + model.getMin());
        int materialIndex = (int) ((value - model.getMin()) / stepLength);
        if(materialIndex > 5) {
            System.out.println(materialIndex + " zone added");
        }
        return materials[materialIndex];
    }

    private static Point3D geoCoordTo3dCoord(float lat, float lon) {
        float lat_cor = lat + TEXTURE_LAT_OFFSET;
        float lon_cor = lon + TEXTURE_LON_OFFSET;
        return new Point3D(
                -java.lang.Math.sin(java.lang.Math.toRadians(lon_cor))
                        * java.lang.Math.cos(java.lang.Math.toRadians(lat_cor)),
                -java.lang.Math.sin(java.lang.Math.toRadians(lat_cor)),
                java.lang.Math.cos(java.lang.Math.toRadians(lon_cor))
                        * java.lang.Math.cos(java.lang.Math.toRadians(lat_cor)));
    }

    private void AddQuadrilateral(Group parent, Point3D topRight, Point3D bottomRight, Point3D bottomLeft,
                                  Point3D topLeft, PhongMaterial material) {
        final TriangleMesh triangleMesh = new TriangleMesh();

        final float[] points = {
                (float) topRight.getX(), (float) topRight.getY(), (float) topRight.getZ(),
                (float) topLeft.getX(), (float) topLeft.getY(), (float) topLeft.getZ(),
                (float) bottomLeft.getX(), (float) bottomLeft.getY(), (float) bottomLeft.getZ(),
                (float) bottomRight.getX(), (float) bottomRight.getY(), (float) bottomRight.getZ()
        };

        final float[] texCoords = {
                1, 1,
                1, 0,
                0, 1,
                0, 0
        };

        final int[] faces = {
                0, 1, 1, 0, 2, 2,
                0, 1, 2, 2, 3, 3
        };

        triangleMesh.getPoints().setAll(points);
        triangleMesh.getTexCoords().setAll(texCoords);
        triangleMesh.getFaces().setAll(faces);

        final MeshView meshView = new MeshView(triangleMesh);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.FRONT);
        meshView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            //TODO add zone selection
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Zone clicked!");
            }
        });
        parent.getChildren().addAll(meshView);
    }


    private void setupBindings() {
        model.currentYearProperty().bindBidirectional(yearSlider.valueProperty());
        model.currentYearProperty().bindBidirectional(yearSpinner.getValueFactory().valueProperty());

//        yearSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
//            //System.out.println(newValue);
//            System.out.println(model.currentYearProperty().toString());
//        });

        animationSpeed = new SimpleFloatProperty(1f);
        animationSpeed.bindBidirectional(timeSlider.valueProperty());
        animationSpeed.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("Animation speed " + newValue);
            }
        });

        playPauseButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(isPlaying){
                    animationTimer.stop();
                    playPauseButton.setText("Play");
                }
                else{
                    animationTimer.start();
                    playPauseButton.setText("Pause");
                }
                isPlaying = !isPlaying;
            }
        });

        stopButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(isPlaying || model.getCurrentYear() != application.getAvailableYears().get(0)){
                    isPlaying = false;
                    lastTime = null;
                    model.currentYearProperty().setValue(application.getAvailableYears().get(0));
                    stopAnimation();
                }
            }
        });
    }

    private void setupAnimation(){
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(lastTime == null){
                    lastTime = now;
                }
                if(timeElapsed >= 2.f){
                    timeElapsed = 0f;
                    lastTime = null;
                    if(model.getCurrentYear() >= application.getAvailableYears().get(application.getAvailableYears().size()-1)){
                        stopAnimation();
                        return;
                    }
                    model.IncrementCurrentYear();
                    return;
                }
                timeElapsed += animationSpeed.getValue() * (now-lastTime) / 1000000000;
            }
        };
    }

    private void stopAnimation(){
        animationTimer.stop();
        playPauseButton.setText("Play");
    }
}
