package project.model;


import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DocFx extends VBox {

    private Label m_title = new Label();
    private Label m_abstract = new Label();

    public DocFx(String m_title, String m_abstract){
        super();
        this.m_title.setText(m_title);
        this.m_abstract.setText(m_abstract);
        this.m_title.setId("label_head");
        this.m_abstract.setId("label_content");
        this.getChildren().addAll(this.m_title,this.m_abstract);
    }
    public String getFullTile(){
        return m_title.getText();
    }

    public String getFullAbstract(){
        return m_abstract.getText();
    }


}
