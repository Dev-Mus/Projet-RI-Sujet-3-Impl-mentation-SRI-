package project.controller;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSpinner;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import project.Main;
import project.model.DocFx;
import project.model.Querying;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static project.controller.Home.eventShow;
import static project.controller.Home.pagination;
import static project.controller.Home.showPane;

public class Query implements Initializable {
    @FXML
    HBox vBox;

    @FXML
    Button btn;

    static JFXSpinner spinner;
    public static  JFXListView<DocFx> listView;
    @FXML
    TextField textField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createContent();

    }

    public void createContent() {

        listView = new JFXListView<>();
        listView.setBorder(Border.EMPTY);
        listView.setExpanded(true);
        listView.setFocusTraversable( false );
        listView.setPadding(new Insets(0,5,0,0));
        listView.setPrefWidth(Double.valueOf(1000));
        spinner = new JFXSpinner();

        btn.setOnAction(e-> {
            if(!Home.bool){
                querying(textField.getText(),vBox);Home.bool = true;
            }
        });
        textField.setOnKeyPressed(ke -> {
            if(!Home.bool){
                if (ke.getCode().equals(KeyCode.ENTER))
                {
                    querying(textField.getText(),vBox);
                    Home.bool = true;
                }
            }
        });

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



    public static void querying(String str, HBox vBox){

        listView.getItems().clear();
        LoadFriendsTask loadFriendsTask = new LoadFriendsTask(str);
        loadFriendsTask.setOnSucceeded(e->{
            Home.bool = false;
            vBox.getChildren().remove(spinner);
            vBox.setAlignment(Pos.CENTER_LEFT);
//            System.out.println(listView.getItems().size());
//            pagination = new Pagination(5, 0);
//            pagination.setPageFactory((Integer pageIndex) -> {
//                VBox vbox = createPage(pageIndex);
//                vbox.setOnMouseClicked(event->{
//                    DocFx docFx = (DocFx) vbox.getChildren().get(pageIndex);
//                    eventShow(docFx.getFullTile(),docFx.getFullAbstract());
//                });
//                return vbox;
//            });
//
//            vBox.getChildren().add(Home.pagination);
            vBox.getChildren().add(listView);
        });
        new Thread(loadFriendsTask).start();
        vBox.getChildren().clear();
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(spinner);
    }

    protected static class LoadFriendsTask extends Task<List<DocFx>>
    {
        String query;
        public LoadFriendsTask(String query) {
            this.query = query;
        }

        @Override
        protected List<DocFx> call() {
            List<DocFx> result = new Querying(query).getDocFx();
            return result;
        }
        @Override
        protected void succeeded() {
            listView.getItems().setAll(getValue());
        }

    }
}
