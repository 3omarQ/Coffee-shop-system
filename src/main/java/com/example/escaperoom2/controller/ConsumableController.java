package com.example.escaperoom2.controller;

import com.example.escaperoom2.MyListener;
import com.example.escaperoom2.model.Commande;
import com.example.escaperoom2.model.Consumable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ConsumableController implements Initializable {


    @FXML
    private AnchorPane card_form;

    @FXML
    private Button prod_addBtn;


    @FXML
    private Label prod_name;

    @FXML
    private Label prod_price;

    @FXML
    public Commande commandeCourante;
    public Consumable consumable;

    private MyListener myListener;

    @FXML
    void addBtn(ActionEvent event) {
        myListener.onClickListenerAdd(consumable);
    }
    @FXML
    void minusBtn(ActionEvent event) {
        myListener.onClickListenerMinus(consumable);
    }
    public void setData(Consumable consumable, MyListener myListener){
        this.consumable=consumable;
        this.myListener=myListener;

        prod_name.setText(consumable.getName());
        prod_price.setText(""+consumable.getPrice() + " dt");

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


    }
}
