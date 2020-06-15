package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Controller controller = new Controller();
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        root.getStylesheets().add(this.getClass().getResource("../resources/styles/styles.css").toExternalForm());
        primaryStage.setTitle("Global Warming by Dominik Nita");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 850, 874));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
