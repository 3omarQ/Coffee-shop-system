package com.example.escaperoom2;

import com.example.escaperoom2.controller.CommandeController;
import com.example.escaperoom2.controller.ConsumableController;
import com.example.escaperoom2.controller.SpecialConsumableController;
import com.example.escaperoom2.model.Commande;
import com.example.escaperoom2.model.Consumable;
import com.example.escaperoom2.model.ConsumableSummaryPrintable;
import com.example.escaperoom2.model.TotalPrintable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.print.PrintService;
import java.awt.Font;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class HelloController implements Initializable {
    @FXML
    private TextField searchBoissonInput;
    @FXML
    private TextField statusInputSearch;
    @FXML
    private ToggleGroup table;
    @FXML
    private GridPane grid;
    @FXML
    private GridPane gridCommandes;
    @FXML
    private Label commandeLabel;
    @FXML
    private TextField searchNumero;
    @FXML
    private TextField searchTable;
    private List<Consumable> consumables= new ArrayList<>();
    private Commande commandeCourante;
    private List<Commande> commandeList= new ArrayList<>();
    private MyListener myListener;
    private ModifierListener modifierListener;
    int row=1;
    private Map<Commande, AnchorPane> commandeAnchorPaneMap = new HashMap<>();
    private Map<Commande, CommandeController> commandeControllerMap = new HashMap<>();
    @FXML
    void fermerBtn(ActionEvent event) {
        showSummaryDialog();
    }

    @FXML
    void saveBtn(ActionEvent event) {
        ExcelWriter excelWriter=new ExcelWriter();
        try {
            excelWriter.addCommandesToExcel(commandeList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sauvegarde en Excel");
        alert.setHeaderText(null);
        alert.setContentText("TOUS LES COMMANDES ONT ETE SAUVEGARDEES AVEC SUCCES");
        alert.showAndWait();

    }

    // Method to handle printing
    private void showSummaryDialog() {
        double total = 0.0;
        StringBuilder summary = new StringBuilder();

        // Calculate the total for all consumables in commandeList
        for (Commande commande : commandeList) {
            total += commande.getTotalPrice(); // Assuming this method calculates total consumable price for the commande
            summary.append("Commande ID: ").append(commande.getId()).append("\n");
            for (Consumable consumable : commande.getCommandeConsumables()) {
                summary.append(" - ").append(consumable.getName())
                        .append(" x ").append(consumable.getQuantity()).append("\n");
            }
            summary.append(String.format("  Total =  %.2f dt \n\n", commande.getTotalPrice()));
        }

        // Create a TextFlow for rich text formatting
        TextFlow textFlow = new TextFlow();

        // Total
        Text totalText = new Text(String.format("Total des commandes: %.2f dt\n\n", total));
        totalText.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        textFlow.getChildren().add(totalText);

        // Summary
        String[] lines = summary.toString().split("\n");
        for (String line : lines) {
            Text lineText = new Text(line + "\n");
            if (line.startsWith("Commande ID: ")) {
                lineText.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            } else if (line.startsWith("  Total")) {
                lineText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            }else{
                lineText.setStyle("-fx-font-size: 14px;");
            }
            textFlow.getChildren().add(lineText);
        }

        // Create a ScrollPane and add the TextFlow to it
        ScrollPane scrollPane = new ScrollPane(textFlow);
        scrollPane.setPrefWidth(400);
        scrollPane.setFitToHeight(false);

        // Create a dialog to display the summary
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Résumé des Commandes");
        dialog.setHeaderText("Détails des consommables");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.setResizable(true);

        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType printButton = new ButtonType("Imprimer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(printButton, closeButton);
        dialog.setHeight(700);
        final double finalTotal = total;
        dialog.showAndWait().ifPresent(response -> {
            if (response == printButton) {
                PrinterJob job = PrinterJob.getPrinterJob();
                // Specify the name of the printer you want to use
                String desiredPrinterName = "OneNote (Desktop)";

                // Find the desired printer by name
                PrintService[] printServices = PrinterJob.lookupPrintServices();
                for (PrintService printService : printServices) {
                    if (printService.getName().equalsIgnoreCase(desiredPrinterName)) {
                        try {
                            job.setPrintService(printService);
                            break;
                        } catch (PrinterException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Create an instance of TotalPrintable with your list of commandes
                TotalPrintable totalPrintable = new TotalPrintable(commandeList);
                job.setPrintable(totalPrintable);

                // Show print dialog and print if user confirms
                boolean doPrint = job.printDialog();
                if (doPrint) {
                    try {
                        job.print();
                    } catch (PrinterException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void showSummaryByConsumable() {
        Map<String, Integer> consumableCountMap = new HashMap<>();
        Map<String, Double> consumableEarningsMap = new HashMap<>();

        // Calculate the total quantity and earnings for each consumable
        for (Commande commande : commandeList) {
            for (Consumable consumable : commande.getCommandeConsumables()) {
                String consumableName = consumable.getName();
                int quantity = consumable.getQuantity();

                double totalPrice;
                if (consumable.getName().equals("Jeu")) {
                    totalPrice = commande.calculateJeuPrice(consumable);
                } else {
                    totalPrice = quantity * consumable.getPrice();
                }

                consumableCountMap.put(consumableName, consumableCountMap.getOrDefault(consumableName, 0) + quantity);
                consumableEarningsMap.put(consumableName, consumableEarningsMap.getOrDefault(consumableName, 0.0) + totalPrice);
            }
        }

        // Sort the consumables map so that "Jeu" comes first, followed by "Personnes", and the rest after
        List<String> sortedConsumables = new ArrayList<>(consumableCountMap.keySet());

        sortedConsumables.sort((a, b) -> {
            // Prioritize "Jeu"
            if (a.equals("Jeu")) return -1;
            if (b.equals("Jeu")) return 1;

            // Prioritize consumables containing "Personnes"
            if (a.contains("Personnes") && !b.contains("Personnes")) return -1;
            if (b.contains("Personnes") && !a.contains("Personnes")) return 1;

            // Sort the remaining consumables by their count in descending order
            int countComparison = Integer.compare(consumableCountMap.get(b), consumableCountMap.get(a));
            if (countComparison != 0) {
                return countComparison;
            }

            // If counts are equal, sort alphabetically as a tiebreaker
            return a.compareTo(b);
        });

        // Build the summary string
        StringBuilder summary = new StringBuilder();
        double totalEarnings = 0.0;

        for (String consumableName : sortedConsumables) {
            int totalQuantity = consumableCountMap.get(consumableName);
            double earnings = consumableEarningsMap.get(consumableName);
            totalEarnings += earnings;

            summary.append(String.format("%-22s x %-2d : %.2f dt", consumableName, totalQuantity, earnings))
                    .append("\n");
        }

        // Create a TextFlow for rich text formatting
        TextFlow textFlow = new TextFlow();

        // Total earnings
        Text totalText = new Text(String.format("Total des gains: %.2f dt\n\n", totalEarnings));
        totalText.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 18px;");
        textFlow.getChildren().add(totalText);

        // Summary of consumables
        String[] lines = summary.toString().split("\n");
        for (String line : lines) {
            Text lineText = new Text(line + "\n");
            lineText.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px;");
            textFlow.getChildren().add(lineText);
        }

        // Create a ScrollPane and add the TextFlow to it
        ScrollPane scrollPane = new ScrollPane(textFlow);
        scrollPane.setPrefWidth(400);
        scrollPane.setFitToHeight(false);

        // Create a dialog to display the summary
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Résumé des Consommables");
        dialog.setHeaderText("Détails des consommables");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.setResizable(true);

        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType printButton = new ButtonType("Imprimer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(printButton, closeButton);
        dialog.setHeight(700);

        dialog.showAndWait().ifPresent(response -> {
            if (response == printButton) {
                PrinterJob job = PrinterJob.getPrinterJob();
                // Specify the name of the printer you want to use
                String desiredPrinterName = "OneNote (Desktop)";

                // Find the desired printer by name
                PrintService[] printServices = PrinterJob.lookupPrintServices();
                for (PrintService printService : printServices) {
                    if (printService.getName().equalsIgnoreCase(desiredPrinterName)) {
                        try {
                            job.setPrintService(printService);
                            break;
                        } catch (PrinterException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Create an instance of a Printable class for the summary
                ConsumableSummaryPrintable consumableSummaryPrintable = new ConsumableSummaryPrintable(consumableCountMap, consumableEarningsMap);
                job.setPrintable(consumableSummaryPrintable);

                // Show print dialog and print if user confirms
                boolean doPrint = job.printDialog();
                if (doPrint) {
                    try {
                        job.print();
                    } catch (PrinterException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @FXML
    void parBoisson(ActionEvent event) {
        showSummaryByConsumable();
    }
    @FXML
    void resetBtn(ActionEvent event) {
        System.out.println("Reset button clicked");
        commandeCourante.setCommandeConsumables(new ArrayList<>());
        commandeLabel.setText(commandeCourante.toString());
    }
    private List<Consumable> readConsumablesFromCSV(String filePath) {
        List<Consumable> consumables = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                // Assuming the CSV file has two columns: name and price
                String name = values[0];
                double price = Double.parseDouble(values[1]);
                Consumable consumable = new Consumable(name, price);
                consumables.add(consumable);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return consumables;
    }
    public void sortCommandeList() {

        // Assuming you have a list of commandes
        commandeList.sort(Comparator.comparingInt(Commande::getId));

    }



    @FXML
    void validerBtn(ActionEvent event) {
        System.out.println("Valider button clicked");

        RadioButton selectedRadioButton = (RadioButton) table.getSelectedToggle();
        if (selectedRadioButton != null) {
            String selectedTable = selectedRadioButton.getText();
            commandeCourante.setTable(selectedTable);
        }
        if (commandeCourante.getTable() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setHeaderText(null);
            alert.setContentText("Table non selectionnée");
            alert.showAndWait();
            return;
        }

        if (!commandeCourante.getCommandeConsumables().isEmpty() && !commandeCourante.getTable().equals("Table non selectionnée")) {
            // Remove the greyed-out AnchorPane from the grid
            if (greyedOutAnchorPane != null) {
                gridCommandes.getChildren().remove(greyedOutAnchorPane);
                greyedOutAnchorPane = null; // Reset the reference
            }

            addToCommandeList(commandeCourante);

            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("command.fxml"));

                AnchorPane anchorPane = fxmlLoader.load();

                CommandeController commandeController = fxmlLoader.getController();
                commandeController.setData(commandeCourante);
                commandeController.setModifierListener(modifierListener);

                gridCommandes.add(anchorPane, 0, commandeCourante.getId());
                GridPane.setMargin(anchorPane, new Insets(8));

                commandeAnchorPaneMap.put(commandeCourante, anchorPane);
                commandeControllerMap.put(commandeCourante, commandeController);
                commandeController.notifEnabled = true;

            } catch (IOException e) {
                e.printStackTrace();
            }

            commandeCourante = new Commande();
            commandeLabel.setText("Commande vierge");
            table.selectToggle(null);
        }
        sortCommandeList();
    }



    public void addToCommandeList(Commande commande) {
        if (commande != null && !commande.getCommandeConsumables().isEmpty()) {
            commandeList.add(commande);
            System.out.println("Added new commande: " + commande);
        }
        saveCommandeList();
    }



    public void modifyFromCommande(Consumable consumable,Boolean isAdd){ //tekhou consumable w tzidou fi commandeCourante, w tbadel commandeLabel
        boolean found = false;


        for (Consumable c : commandeCourante.getCommandeConsumables()) {
            if (c.getName().equals(consumable.getName())) {
                if(isAdd){
                c.setQuantity(c.getQuantity()+1);

                }else{
                    c.setQuantity(c.getQuantity()-1);
                    if(c.getQuantity()==0){
                        commandeCourante.getCommandeConsumables().remove(c);
                    }
                }
                found = true;
                break;
            }
        }
        if (!found) {
            Consumable newConsumable = new Consumable(consumable.getName(), consumable.getPrice());
            commandeCourante.getCommandeConsumables().add(newConsumable);

        }
        commandeCourante.calculateTotalPrice();
        commandeLabel.setText(commandeCourante.toString());

    }


    private AnchorPane greyedOutAnchorPane;

    public void modifyCommand(Commande commandeAModifier) {
        if (!commandeLabel.getText().contains("NUMERO")) {
            Commande.numero -= 2; // Adjust ID for the copy

            // Store elapsed time of the current command
            int elapsedTime = commandeAModifier.getElapsedTime();

            // Grey out the AnchorPane and store the reference
            greyedOutAnchorPane = commandeAnchorPaneMap.get(commandeAModifier);
            if (greyedOutAnchorPane != null) {
                greyedOutAnchorPane.setStyle("-fx-background-color: lightgrey;");
            }

            commandeList.remove(commandeAModifier);

            // Create a copy of the command to modify
            commandeCourante = commandeAModifier.copierCommande();
            commandeCourante.setElapsedTime(elapsedTime);
            commandeCourante.setId(commandeAModifier.getId());

            // Update the command label
            commandeLabel.setText(commandeCourante.toString());
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setHeaderText(null);
            alert.setContentText("UNE COMMANDE EST DEJA EN COURS DE MODIFICATION");
            alert.showAndWait();
        }
    }

    private void filterConsumables(String query) {
        // Clear the grid
        grid.getChildren().clear();

        // Initialize column and row
        int column = 0;
        int row = 1;

        // Iterate over the consumables using an index-based loop
        for (int i = 0; i < consumables.size(); i++) {
            Consumable consumable = consumables.get(i);
            if (consumable.getName().toLowerCase().contains(query.toLowerCase())) {
                try {
                    FXMLLoader fxmlLoader;
                    AnchorPane anchorPane;

                    // Check if the consumable needs special handling
                    if (consumable.getName().startsWith("Musée") || consumable.getName().startsWith("Choufli")) {
                        fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getResource("cardProductSpecial.fxml"));
                        anchorPane = fxmlLoader.load();
                        SpecialConsumableController specialController = fxmlLoader.getController();
                        specialController.setData(consumable, myListener);
                        i += 3; // Skip the next 4 iterations
                    } else {
                        fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getResource("cardProduct.fxml"));
                        anchorPane = fxmlLoader.load();
                        ConsumableController consumableController = fxmlLoader.getController();
                        consumableController.setData(consumable, myListener);
                    }

                    // Add the anchorPane to the grid
                    if (column == 4) {
                        column = 0;
                        row++;
                    }
                    grid.add(anchorPane, column++, row);
                    GridPane.setMargin(anchorPane, new Insets(8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void filterCommandesByTable(String query) {
        // Clear the grid of commandes
        gridCommandes.getChildren().clear();

        // If the query is empty, restore the full list of commandes
        if (query == null || query.trim().isEmpty()) {
            for (Commande commande : commandeList) {
                AnchorPane anchorPane = commandeAnchorPaneMap.get(commande);
                if (anchorPane != null) {
                    gridCommandes.getChildren().add(anchorPane);
                }
            }
            return;
        }

        // Filter commandes based on the table number
        for (Commande commande : commandeList) {
            if (commande.getTable().equalsIgnoreCase(query)) {
                // Load the corresponding AnchorPane and add it to the grid
                AnchorPane anchorPane = commandeAnchorPaneMap.get(commande);
                if (anchorPane != null) {
                    gridCommandes.getChildren().add(anchorPane);
                }
            }
        }
    }

    // Create a method to filter commandes by numero
    private void filterCommandesByNumero(String query) {
        // Clear the grid of commandes
        gridCommandes.getChildren().clear();

        // If the query is empty, restore the full list of commandes
        if (query == null || query.trim().isEmpty()) {
            for (Commande commande : commandeList) {
                AnchorPane anchorPane = commandeAnchorPaneMap.get(commande);
                if (anchorPane != null) {
                    gridCommandes.getChildren().add(anchorPane);
                }
            }
            return;
        }

        // Filter commandes based on the numero (ID)
        for (Commande commande : commandeList) {
            if (String.valueOf(commande.getId()).equals(query)) {
                // Load the corresponding AnchorPane and add it to the grid
                AnchorPane anchorPane = commandeAnchorPaneMap.get(commande);
                if (anchorPane != null) {
                    gridCommandes.getChildren().add(anchorPane);
                }
            }
        }
    }
    private void filterCommandesByStatus(String query) {
        // Clear the grid of commandes
        gridCommandes.getChildren().clear();



        // Filter commandes based on the status
        for (Commande commande : commandeList) {

            String getStatus;
            if(commande.isPayed()){
                getStatus="payee";
            }else{
                getStatus="non";
            }
            if (getStatus.toLowerCase().contains(query.toLowerCase())) {
                // Load the corresponding AnchorPane and add it to the grid
                AnchorPane anchorPane = commandeAnchorPaneMap.get(commande);
                if (anchorPane != null) {
                    gridCommandes.getChildren().add(anchorPane);
                }
            }
        }
    }
    private void saveCommandeList() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getTodayFilePath()))) {
            oos.writeObject(commandeList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCommandeList() {
        File file = new File(getTodayFilePath());
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                commandeList = (List<Commande>) ois.readObject();
                Commande.numero+=commandeList.size();
                sortCommandeList();
                restoreCommandesToGrid();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        System.out.println(commandeList);
    }

    private String getTodayFilePath() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return "data/commandes_" + today.format(formatter) + ".dat";
    }

    private void restoreCommandesToGrid() {
        for (Commande commande : commandeList) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("command.fxml"));

                AnchorPane anchorPane = fxmlLoader.load();
                CommandeController commandeController = fxmlLoader.getController();
                commandeController.setData(commande);
                commandeController.setModifierListener(modifierListener);

                gridCommandes.add(anchorPane, 0, row++);
                GridPane.setMargin(anchorPane, new Insets(8));
                commandeAnchorPaneMap.put(commande, anchorPane);
                commandeControllerMap.put(commande, commandeController);
                commandeController.notifEnabled = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, Double> priceMap;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gridCommandes.getRowConstraints().get(0).setMinHeight(0);
        gridCommandes.getRowConstraints().get(0).setPrefHeight(0);
        gridCommandes.getRowConstraints().get(0).setMaxHeight(0);

        priceMap = new HashMap<>();
        int column = 0;
        int row = 1;

        myListener = new MyListener() {
            @Override
            public void onClickListenerAdd(Consumable consumable) {
                modifyFromCommande(consumable, true);
            }

            @Override
            public void onClickListenerMinus(Consumable consumable) {
                modifyFromCommande(consumable, false);
            }
        };
        modifierListener = new ModifierListener() {
            @Override
            public void onClickListener(Commande commande) {
                modifyCommand(commande);
            }
        };
        searchBoissonInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filterConsumables(newValue);
        });

        searchTable.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCommandesByTable(newValue);
        });

        // Add listener for searchNumero
        searchNumero.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCommandesByNumero(newValue);
        });
        statusInputSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCommandesByStatus(newValue);
        });

        try {
            String csvFilePath = "./menu.csv";
            consumables.addAll(readConsumablesFromCSV(csvFilePath));
            for (int i = 0; i < consumables.size(); i++) {
                FXMLLoader fxmlLoader;
                AnchorPane anchorPane;

                if (consumables.get(i).getName().startsWith("Musée") || consumables.get(i).getName().startsWith("Choufli")) {
                    fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("cardProductSpecial.fxml"));
                    anchorPane = fxmlLoader.load();
                    SpecialConsumableController specialController = fxmlLoader.getController();
                    specialController.setData(consumables.get(i), myListener);
                    i+=3;
                } else {
                    fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("cardProduct.fxml"));
                    anchorPane = fxmlLoader.load();
                    ConsumableController consumableController = fxmlLoader.getController();
                    consumableController.setData(consumables.get(i), myListener);

                }

                if (column == 4) {
                    column = 0;
                    row++;
                }
                grid.add(anchorPane, column++, row);
                GridPane.setMargin(anchorPane, new Insets(8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadCommandeList();
        sortCommandeList();
        Commande.numero=commandeList.size()+1;

        this.commandeCourante = new Commande();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::saveCommandeList, 1, 1, TimeUnit.MINUTES);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                }
            }
        }));

        // Register a close request handler for the primary stage
        Platform.runLater(() -> {
            Stage stage = (Stage) grid.getScene().getWindow();
            stage.setOnCloseRequest((WindowEvent event) -> {
                event.consume(); // consume the event to prevent the default behavior
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Exit");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to exit?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    if (scheduler != null && !scheduler.isShutdown()) {
                        scheduler.shutdown();
                        try {
                            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                                scheduler.shutdownNow();
                            }
                        } catch (InterruptedException e) {
                            scheduler.shutdownNow();
                        }
                    }
                    Platform.exit();
                }
            });
        });
    }


}