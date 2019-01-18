package project.controller;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import project.Main;
import project.model.DocFx;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Home implements Initializable {
    @FXML
    Button close,minimise,maximise;
    @FXML
    BorderPane holdPane;
    public static BorderPane searchPane,queryPane,showPane;
    static Pagination pagination = null;


    public static Boolean bool = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            searchPane = FXMLLoader.load(getClass().getResource("../view/search.fxml"));
            queryPane = FXMLLoader.load(getClass().getResource("../view/query.fxml"));
            showPane = FXMLLoader.load(getClass().getResource("../view/show.fxml"));

            setNode(searchPane);
        } catch (IOException ex) {
            Logger.getLogger(Home.class.getName()).log(Level.SEVERE, null, ex);
        }
        eventSearchPane();
        close.setOnAction(event -> System.exit(0));

        minimise.setOnAction(e -> Main.primaryStage.setIconified(true));
        maximise.setOnAction(e -> {
            if (!Main.primaryStage.isMaximized())
                 Main.primaryStage.setMaximized(true);
            else
                Main.primaryStage.setMaximized(false);
        });

        // result titles as clickListener

        Query.listView.setOnMouseClicked(event -> {
            DocFx docFx = Query.listView.getSelectionModel().getSelectedItem();
            eventShow(docFx.getFullTile(),docFx.getFullAbstract()); setNode(showPane);
        });
        eventShowPane();

    }

    public void setNode(BorderPane node1) {
        BorderPane node = new BorderPane(node1);
        holdPane.getChildren().clear();
        holdPane.setTop(node.getTop());
        holdPane.setBottom(node.getBottom());
        holdPane.setLeft(node.getLeft());
        holdPane.setRight(node.getRight());
        holdPane.setCenter(node.getCenter());
    }

    void eventSearchPane(){
        HBox hBox= (HBox) searchPane.getCenter();
        AnchorPane anchorPane = (AnchorPane) hBox.getChildren().get(0);
        Button btn = (Button) anchorPane.getChildren().get(1);
        TextField input = (TextField) anchorPane.getChildren().get(0);
        input.setOnKeyPressed(ke -> { if (ke.getCode().equals(KeyCode.ENTER)) {action(input.getText()); } });
        btn.setOnAction(e -> action(input.getText()));
    }

    private void action(String input) {
        if(!bool){
            eventQueryPane(input);
//                pagination = new Pagination(5, 0);
//                pagination.setPageFactory((Integer pageIndex) -> {
//                    VBox vbox = createPage(pageIndex);
//                    vbox.setOnMouseClicked(e->{
//                        DocFx docFx = (DocFx) vbox.getChildren().get(pageIndex);
//                        eventShow(docFx.getFullTile(),docFx.getFullAbstract()); setNode(showPane);
//                    });
//                    return vbox;
//                });
            bool = true;
            setNode(queryPane);
        }
    }

    void eventQueryPane(String query){
        VBox hBox= (VBox) queryPane.getTop();
        AnchorPane anchorPane = (AnchorPane) hBox.getChildren().get(0);
        TextField input = (TextField) anchorPane.getChildren().get(0);
        input.setText(query);
        Query.querying(query, (HBox) queryPane.getCenter());
    }

    void eventShowPane(){
        VBox hBox= (VBox) showPane.getTop();
        VBox vBox = (VBox) hBox.getChildren().get(0);
        JFXButton back = (JFXButton) vBox.getChildren().get(0);
        back.setOnAction(e-> setNode(queryPane));
    }
     public static void eventShow(String titre,String abstrac){
        VBox vBo = (VBox) showPane.getTop();
        Label label = (Label) vBo.getChildren().get(1);
        label.setText(titre);
        BorderPane vBox = (BorderPane) showPane.getCenter();
        TextArea textArea = (TextArea) vBox.getTop();
        textArea.setEditable(false);
        textArea.setText(abstrac.replaceAll("^","\n "));
//        abtrac.addEventFilter(MouseEvent.ANY, t -> t.consume());
//        Platform.runLater(() -> abtrac.selectRange(13, 18));
    }

    public static int itemsPerPage() {
        return 11;
    }

    public static VBox createPage(int pageIndex) {
        VBox vBox = new VBox(5);
        int page = pageIndex * itemsPerPage();
        for (int i = page; i < page + itemsPerPage(); i++) {
            DocFx docFx = Query.listView.getItems().get(i);
            vBox.getChildren().add(docFx);
        }
        return vBox;
    }

}
