package com.example.escaperoom2.controller;

import com.example.escaperoom2.ExcelWriter;
import com.example.escaperoom2.ModifierListener;
import com.example.escaperoom2.model.Commande;
import com.example.escaperoom2.model.CommandePasserPrintable;
import com.example.escaperoom2.model.CommandePrintable;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javax.print.PrintService;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CommandeController {
    @FXML
    private Button additionButton;
    @FXML
    private Button passerButton;
    @FXML
    private Button modifierButton;

    @FXML
    private Button toggleHeightBtn;
    @FXML
    private ToggleButton statutButton;
    @FXML
    private Label compteurLabel;
    @FXML
    private Label detailLabel;

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ToggleButton pauseBtn;
    @FXML
    private ToggleButton notifButton;
    @FXML
    private VBox commandOptionsBox; // Ensure this is connected to your FXML
    private Commande commande;

    private String commandeText;

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    private ModifierListener modifierListener;
    private Timeline timer;

    private boolean isPaused = false;
    private boolean heightToggle=true;
    private int elapsedTime; // in seconds

    public boolean notifEnabled = false; // Add this line

    public void startTimer() {
        if (timer != null) {
            timer.stop();
        }
        elapsedTime = commande.getElapsedTime(); // Use the elapsed time from the commande
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            elapsedTime++;
            commande.setElapsedTime(elapsedTime); // Update the elapsed time in the commande
            updateCompteurLabel();
            if(heightToggle){
                detailLabel.setText(commande.toString());
            }else{

            detailLabel.setText(commandeText);
            }

            if ((elapsedTime %3600==0) && notifEnabled) {
                showAlert();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }


    public void stopTimer() {
        if (timer != null) {
            timer.stop();
            isPaused=true;
            commande.setPaused(true);
            pauseBtn.setText("▶");
        }
    }

    private void updateCompteurLabel() {
        int hours = elapsedTime / 3600; // Calculate hours
        int minutes = (elapsedTime % 3600) / 60; // Calculate remaining minutes
        int seconds = elapsedTime % 60; // Calculate remaining seconds
        compteurLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }
    private void showAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("ALERTE COMPTEUR - 60 MINUTES SONT PASSEES");
            alert.setHeaderText(null);

            Text text = new Text(commande.toString()+"\n\n 60 MINUTES SONT PASSEES");
            text.setFont(Font.font("Courier New", FontWeight.BOLD, 15));

            VBox dialogPaneContent = new VBox();
            dialogPaneContent.getChildren().add(text);

            alert.getDialogPane().setContent(dialogPaneContent);

            alert.showAndWait();
        });
    }
    @FXML
    void passerBtn(ActionEvent event) {
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

        job.setPrintable(new CommandePasserPrintable(commande));

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void additionBtn(ActionEvent event) {

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
        job.setPrintable(new CommandePrintable(commande));

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
        commande.setPayed(true);

        anchorPane.setStyle("-fx-background-color: #ffd3d3;");
        stopTimer();
        isPaused=true;
        commande.setPaused(true);
        pauseBtn.setText("▶");

    }
    @FXML
    void pauseBtn(ActionEvent event) {
        if (isPaused) {
            // Resume the timer
            startTimer();
            pauseBtn.setText("⏸");
            isPaused=false;
            commande.setPaused(false);// Change button text to indicate next action
        } else {
            // Pause the timer
            stopTimer();
            isPaused=true;
            commande.setPaused(true);
            pauseBtn.setText("▶"); // Change button text to indicate next action
        }

    }

    public void stopTimerWithNecessaryModifications(){
        stopTimer();
        isPaused=true;
        commande.setPaused(true);
        pauseBtn.setText("▶");
    }

    @FXML
    void notifToggle(ActionEvent event) {
        if(notifEnabled){
            notifEnabled=!notifEnabled;
            notifButton.setText("🔔❌");
        }else{
            notifEnabled = !notifEnabled;
            notifButton.setText("🔔");
        }
    }
    @FXML
    void toggleHeightBtn(ActionEvent event) {
        if (heightToggle) { // If heightToggle is true, it's currently in the full size, so we want to minimize it
            // Minimize height and hide commandOptionsBox

            commandOptionsBox.setVisible(false);

            // Update detailLabel with limited information
            StringBuilder sb = new StringBuilder();
            sb.append("+===================================+\n");
            sb.append(String.format("%-10s : %d", "# NUMERO ᵒ", commande.getId())).append("\n");
            sb.append(String.format("%-10s : %s", "TABLE", commande.getTable())).append("\n");
            sb.append("+----------------------------------+\n");
            commandeText=sb.toString();
            detailLabel.setText(commandeText);
            anchorPane.setPrefHeight(70);
            anchorPane.setMaxHeight(70);
            anchorPane.setMinHeight(70);

            // Set button to indicate it will expand on next click
            toggleHeightBtn.setText("\uD83D\uDD3D");
        } else { // Otherwise, expand to full size
            // Reset height and show commandOptionsBox
            commandOptionsBox.setVisible(true);

            // Update detailLabel with full commande details
            commandeText=commande.toString();
            detailLabel.setText(commandeText);
            anchorPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
            anchorPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
            anchorPane.setMinHeight(Region.USE_COMPUTED_SIZE);

            // Set button to indicate it will minimize on next click
            toggleHeightBtn.setText("\uD83D\uDD3C");
        }

        // Toggle the state
        heightToggle = !heightToggle;
    }
    @FXML
    void modifierBtn(ActionEvent event) {
        modifierListener.onClickListener(commande);
    }

    public void setData(Commande commande) {
        this.commande = commande;
        startTimer();
        if (heightToggle) { // If expanded, show full details
            // Reset height and show commandOptionsBox
            commandOptionsBox.setVisible(true);

            // Update detailLabel with full commande details
            commandeText=commande.toString();
            detailLabel.setText(commandeText);
            anchorPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
            anchorPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
            anchorPane.setMinHeight(Region.USE_COMPUTED_SIZE);

            // Set button to indicate it will minimize on next click
            toggleHeightBtn.setText("\uD83D\uDD3C");
        } else { // If minimized, show limited details
            commandOptionsBox.setVisible(false);

            // Update detailLabel with limited information
            StringBuilder sb = new StringBuilder();
            sb.append("+===================================+\n");
            sb.append(String.format("%-10s : %d", "# NUMERO ᵒ", commande.getId())).append("\n");
            sb.append(String.format("%-10s : %s", "TABLE", commande.getTable())).append("\n");
            sb.append("+----------------------------------+\n");
            commandeText=sb.toString();
            detailLabel.setText(commandeText);
            anchorPane.setPrefHeight(70);
            anchorPane.setMaxHeight(70);
            anchorPane.setMinHeight(70);
        }
        if(commande.isPaused()){
            stopTimer();
            updateCompteurLabel();
        }
        updateStatus();
    }

    public void setModifierListener(ModifierListener modifierListener) {
        this.modifierListener = modifierListener; // Set the modifier listener
    }


    private double computeTextHeight(Label label) {
        Text text = new Text(label.getText());
        text.setFont(label.getFont());
        return text.getLayoutBounds().getHeight();
    }


    private void updateStatus() {

        if (commande != null) {
            if (commande.isPayed()) {
                anchorPane.setStyle("-fx-background-color: #ffd3d3;");
            } else {
                anchorPane.setStyle("-fx-background-color: #d6ffd6;");
            }
        }
    }
}
