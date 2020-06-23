package frontend;

import backend.BackendLogic;
import backend.YearlyStatistics;
import backend.Zone;
import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import frontend.utility.CameraManager;
import frontend.utility.ColorPaletteTools;
import javafx.animation.AnimationTimer;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
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
    private ChoiceBox<ViewMode> modeDropdown;

    @FXML
    private Text selectedZoneText;
    private LineChart<Number, Number> lineChart;

    @FXML
    private Text yearStatsText;
    @FXML
    private Text minTempText;
    @FXML
    private Text maxTempText;
    @FXML
    private Text meanTempText;

    private Model model;
    private BackendLogic application;

    Group dataVisualization;

    Group quadrilateralGroup;
    HashMap<Zone, MeshView> quadrilateralMap;
    ColorPaletteTools quadrilateralPalette;

    Group histogramGroup;
    HashMap<Zone, Cylinder> histogramMap;
    ColorPaletteTools histogramPalette;

    AnimationTimer animationTimer;
    boolean isPlaying = false;
    float timeElapsed = 0f;
    Long lastTime = null;
    FloatProperty animationSpeed;

    private static final float TEXTURE_LAT_OFFSET = -0.2f;
    private static final float TEXTURE_LON_OFFSET = 2.8f;

    Zone selectedZone = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setup3DScene();
        application = new BackendLogic();
        model = new Model(application);
        setupChart();
        setupColorPalettes();
        setupQuadrilateralMode();
        setupHistogramMode();
        dataVisualization.getChildren().add(quadrilateralGroup);
        setupListeners();
        setupBindings();
        setupAnimation();
        model.selectedZoneProperty().setValue(new Zone(0, 2));
    }

    private void setupChart() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(application.getAvailableYears().get(0));
        xAxis.setUpperBound(application.getAvailableYears().get(application.getAvailableYears().size() - 1));
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(application.getMinAnomaly());
        yAxis.setUpperBound(application.getMaxAnomaly());
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLayoutX(110);
        lineChart.setPrefHeight(158);
        lineChart.setPrefWidth(318);
        //don't show points on the chart
        lineChart.setCreateSymbols(false);
        zoneStatisticsPane.getChildren().add(lineChart);
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
        Group earth = new Group(meshViews);
        dataVisualization = new Group();

        Group earthWithTowns = new Group(earth);
        earthWithTowns.getChildren().add(dataVisualization);
        //root3D.getChildren().add(earth);
        root3D.getChildren().add(earthWithTowns);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        new CameraManager(camera, pane3D, root3D);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        ambientLight.getScope().addAll(root3D);
        root3D.getChildren().add(ambientLight);

        SubScene subScene = new SubScene(root3D, 850, 800, true, SceneAntialiasing.BALANCED);
        subScene.setViewOrder(10f);
        subScene.setCamera(camera);
        subScene.setFill(Color.GREY);
        pane3D.getChildren().addAll(subScene);
    }

    public void setupColorPalettes() {
        quadrilateralPalette = new ColorPaletteTools(6, 0.01f, new PhongMaterial(new Color(0.2, 0.2, 0.2, 0.4)));
        histogramPalette = new ColorPaletteTools(2, 0.6f, new PhongMaterial(new Color(0, 0, 0, 0f)));
    }

    public void insertColorPaletteToPane() {
        quadrilateralPalette.prepareScaleLegend(scaleLegendBox);
    }

    private void setupListeners() {
        model.zonesWithAnomalyProperty().addListener(new ChangeListener<ObservableMap<Zone, Float>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableMap<Zone, Float>> observable, ObservableMap<Zone, Float> oldValue, ObservableMap<Zone, Float> newValue) {
                switch ((ViewMode) modeDropdown.getValue()) {
                    case Quadrilateral:
                        updateQuadrilateralGroup(newValue);
                        break;
                    case Histogram:
                        updateHistogramGroup(newValue);
                        break;
                    default:
                        System.out.println("Invalid state");
                        break;
                }
            }
        });

        modeDropdown.setItems(FXCollections.observableArrayList(ViewMode.values()));
        modeDropdown.valueProperty().setValue(ViewMode.Quadrilateral);
        modeDropdown.valueProperty().addListener(new ChangeListener<ViewMode>() {
            @Override
            public void changed(ObservableValue<? extends ViewMode> observable, ViewMode oldValue, ViewMode newValue) {
                dataVisualization.getChildren().clear();
                switch (newValue) {
                    case Quadrilateral:
                        updateQuadrilateralGroup(model.getZonesWithAnomaly());
                        dataVisualization.getChildren().add(quadrilateralGroup);
                        break;
                    case Histogram:
                        updateHistogramGroup(model.getZonesWithAnomaly());
                        dataVisualization.getChildren().add(histogramGroup);
                        break;
                    default:
                        System.out.println("Invalid view mode value!");
                        break;
                }
            }
        });

        model.selectedZoneProperty().addListener(new ChangeListener<Zone>() {
            @Override
            public void changed(ObservableValue<? extends Zone> observable, Zone oldValue, Zone newValue) {
                selectedZoneText.setText(newValue.getLatitude() + "°   " + newValue.getLongitude() + "°");
                updateChart(application.getYearlyAnomaliesForZone(newValue));
            }
        });

        model.currentYearProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                YearlyStatistics stats = application.getYearlyStatistics((int) newValue);
                yearStatsText.setText(newValue.toString());
                minTempText.setText((Math.round(stats.getMinTemperature() * 100f) / 100f) + "");
                maxTempText.setText((Math.round(stats.getMaxTemperature() * 100f) / 100f) + "");
                meanTempText.setText((Math.round(stats.getMeanTemperature() * 100f) / 100f) + "");

                meanTempText.getStyleClass().clear();
                if(stats.getMeanTemperature() >= 0){
                    meanTempText.getStyleClass().add("red-text");
                }
                else{
                    meanTempText.getStyleClass().add("blue-text");
                }
            }
        });

        playPauseButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isPlaying) {
                    animationTimer.stop();
                    playPauseButton.setText("Play");
                } else {
                    animationTimer.start();
                    playPauseButton.setText("Pause");
                }
                isPlaying = !isPlaying;
            }
        });

        stopButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isPlaying || model.getCurrentYear() != application.getAvailableYears().get(0)) {
                    isPlaying = false;
                    lastTime = null;
                    model.currentYearProperty().setValue(application.getAvailableYears().get(0));
                    stopAnimation();
                }
            }
        });
    }

    private void updateChart(List<Float> yValues) {
        lineChart.getData().clear();
        XYChart.Series series = new XYChart.Series<>();
        for (int i = 0; i < application.getAvailableYears().size(); i++) {
            if (!yValues.get(i).isNaN()) {
                series.getData().add(new XYChart.Data(application.getAvailableYears().get(i), yValues.get(i)));
            }
        }
        lineChart.getData().add(series);

    }

    private void updateQuadrilateralGroup(ObservableMap<Zone, Float> newValue) {
        for (Map.Entry<Zone, Float> entry : newValue.entrySet()) {
            quadrilateralMap.get(entry.getKey()).setMaterial(quadrilateralPalette.getMaterial(entry.getValue()));
        }
    }

    private void setupQuadrilateralMode() {
        Map<Zone, Float> data = application.getAnomaliesByYear(application.getAvailableYears().get(0));
        float degreeSize = 4f;
        float scale = 1.013f;
        quadrilateralGroup = new Group();
        quadrilateralMap = new HashMap<>();

        for (Map.Entry<Zone, Float> entry : data.entrySet()) {
            int latitude = entry.getKey().getLatitude();
            int longitude = entry.getKey().getLongitude();
            Point3D topLeft = geoCoordTo3dCoord(latitude - degreeSize / 2, longitude - degreeSize / 2).multiply(scale);
            Point3D topRight = geoCoordTo3dCoord(latitude - degreeSize / 2, longitude + degreeSize / 2).multiply(scale);
            Point3D bottomLeft = geoCoordTo3dCoord(latitude + degreeSize / 2, longitude - degreeSize / 2).multiply(scale);
            Point3D bottomRight = geoCoordTo3dCoord(latitude + degreeSize / 2, longitude + degreeSize / 2).multiply(scale);
            PhongMaterial material = quadrilateralPalette.getMaterial(entry.getValue());
            AddQuadrilateral(quadrilateralGroup, topRight, bottomRight, bottomLeft, topLeft, material, entry.getKey());
        }
    }

    private void updateHistogramGroup(ObservableMap<Zone, Float> newValue) {
        for (Map.Entry<Zone, Float> entry : newValue.entrySet()) {
            histogramMap.get(entry.getKey()).setMaterial(histogramPalette.getMaterial(entry.getValue()));
            histogramMap.get(entry.getKey()).setHeight(getHeightForAnomaly((float) Math.round(entry.getValue() * 100f) / 100f));
        }
    }

    private float getHeightForAnomaly(Float value) {
        return Math.min(Math.abs(value) / 5, 1.5f);
    }

    private void setupHistogramMode() {
        Map<Zone, Float> data = application.getAnomaliesByYear(application.getAvailableYears().get(0));
        histogramGroup = new Group();
        histogramMap = new HashMap<>();
        for (Map.Entry<Zone, Float> entry : data.entrySet()) {
            Point3D centerOnEarth = geoCoordTo3dCoord(entry.getKey().getLatitude(), entry.getKey().getLongitude());
            Point3D yAxis = new Point3D(0, 1, 0);
            Point3D miscPoint = centerOnEarth.multiply(1.05f);
            Point3D diff = miscPoint.subtract(centerOnEarth);
            double height = diff.magnitude();

            Point3D mid = miscPoint.midpoint(centerOnEarth);
            Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

            Point3D axisOfRotation = diff.crossProduct(yAxis);
            double angle = Math.acos(diff.normalize().dotProduct(yAxis));
            Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

            Cylinder line = new Cylinder(0.009f, height);

            line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
            line.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    model.selectedZoneProperty().setValue(entry.getKey());
                }
            });
            histogramGroup.getChildren().add(line);
            histogramMap.put(entry.getKey(), line);
        }
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
                                  Point3D topLeft, PhongMaterial material, Zone zone) {
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
            @Override
            public void handle(MouseEvent event) {
                model.selectedZoneProperty().setValue(zone);
            }
        });
        quadrilateralMap.put(zone, meshView);
        parent.getChildren().addAll(meshView);
    }


    private void setupBindings() {
        model.currentYearProperty().bindBidirectional(yearSlider.valueProperty());
        model.currentYearProperty().bindBidirectional(yearSpinner.getValueFactory().valueProperty());

        animationSpeed = new SimpleFloatProperty(1f);
        animationSpeed.bindBidirectional(timeSlider.valueProperty());
    }

    private void setupAnimation() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == null) {
                    lastTime = now;
                }
                if (timeElapsed >= 75f) {
                    timeElapsed = 0f;
                    lastTime = null;
                    if (model.getCurrentYear() >= application.getAvailableYears().get(application.getAvailableYears().size() - 1)) {
                        stopAnimation();
                        return;
                    }
                    model.IncrementCurrentYear();
                    return;
                }
                timeElapsed += animationSpeed.getValue() * (now - lastTime) / 1000000000;
            }
        };
    }

    private void stopAnimation() {
        animationTimer.stop();
        playPauseButton.setText("Play");
        isPlaying = false;
    }
}
