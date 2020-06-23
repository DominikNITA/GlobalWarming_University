package frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("view.fxml").openStream());
        root.getStylesheets().add(this.getClass().getResource("../resources/styles/styles.css").toExternalForm());
        primaryStage.setTitle("Global Warming by Dominik Nita");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 850, 874, false));
        primaryStage.show();
        Controller controller = (Controller)fxmlLoader.getController();
        controller.insertColorPaletteToPane();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
