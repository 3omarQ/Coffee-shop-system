package com.example.escaperoom2.controller;

import com.example.escaperoom2.MyListener;
import com.example.escaperoom2.PriceChangeListener;
import com.example.escaperoom2.model.Consumable;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecialConsumableController {
    @FXML
    private Label prod_name;
    @FXML
    private Label prod_price;
    @FXML
    private Button prod_addBtn;
    @FXML
    private ChoiceBox<String> dropdownEscape;
    private Map<String, Double> priceMap = new HashMap<>();
    private MyListener myListener;
    private PriceChangeListener priceChangeListener;
    private Consumable consumable;
    private List<Consumable> allConsumables;

    public void setData(Consumable consumable, MyListener myListener) {
        this.consumable = consumable;
        this.myListener = myListener;

        prod_name.setText(consumable.getName().split("-")[0]);
        prod_price.setText(consumable.getPrice() + " dt");

        // Read consumables from CSV and populate priceMap


        dropdownEscape.setItems(FXCollections.observableArrayList("2Personnes", "3Personnes", "4Personnes", "5Personnes"));
        dropdownEscape.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            List<Consumable> escapeConsumablesFromCSV = readConsumablesFromCSV("menu.csv");
            for (Consumable c : escapeConsumablesFromCSV) {
                priceMap.put(c.getName(), c.getPrice());
                System.out.println(c.getName());
            }
            String key = (consumable.getName().split("-")[0] + "-" + newValue.toString()).replace(" ","");
            System.out.println("key :"+key);
            System.out.println(consumable.getName() + " prix " + consumable.getPrice());
            if (priceMap.containsKey(key)) {
                double price = priceMap.get(key);
                String newConsumableName = consumable.getName().split("-")[0] + "- " + newValue;

                consumable.setPrice(price);
                consumable.setName(newConsumableName);
                prod_price.setText(price + " dt");
                prod_name.setText(newConsumableName.split("-")[0]);
                System.out.println(consumable.getName() + " prix " + consumable.getPrice());
            }
        });

        // Set default selection
        dropdownEscape.getSelectionModel().selectFirst();
    }

    private List<Consumable> readConsumablesFromCSV(String filePath) {
        List<Consumable> consumables = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                // Assuming the CSV file has two columns: name and price
                String name = values[0];
                double price = Double.parseDouble(values[1]);
                if (name.contains("Musée") || name.contains("ChoufliHal")) {
                    Consumable consumable = new Consumable(name, price);
                    consumables.add(consumable);
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return consumables;
    }

    @FXML
    private void addBtn() {
        myListener.onClickListenerAdd(consumable);
    }
}

