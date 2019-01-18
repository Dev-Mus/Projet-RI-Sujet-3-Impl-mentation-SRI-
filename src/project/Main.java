package project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import project.model.*;


public class Main extends Application {

    public static Stage primaryStage;
    private double xOffset = 0;
    private double yOffset = 0;
    Parent root;

    @Override
    public void start(Stage primaryStage) throws Exception{

        root = FXMLLoader.load(getClass().getResource("view/home.fxml"));
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        //move around here
        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.lcdtext", "true");
        this.primaryStage = primaryStage;
        this.primaryStage.initStyle(StageStyle.UNDECORATED);
        this.primaryStage.setTitle("Search Engine");
        this.primaryStage.setScene(new Scene(root, 1080, 850));
        this.primaryStage.show();
        new Parsing(); //calling parsers
    }

    public static void main(String[] args) {
        launch(args); // calling interfaces
//        new Parsing();
    }



}
