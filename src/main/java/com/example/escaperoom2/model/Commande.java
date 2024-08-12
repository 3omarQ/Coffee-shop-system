package com.example.escaperoom2.model;

import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.abs;

public class Commande implements Serializable {
    // Static field to keep track of the last used number
    public static int numero=1;

    private int id;
    private List<Consumable> commandeConsumables;
    private LocalDateTime dateTime; // Field to store date and time
    private double totalPrice; // Field to store total price
    private String table;
    private boolean isPayed=false;

    private int elapsedTime; // in seconds

    private boolean isPaused;

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getId() {
        return abs(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPayed() {
        return isPayed;
    }

    public void setPayed(boolean isPayed) {
        this.isPayed = isPayed;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<Consumable> getCommandeConsumables() {
        return commandeConsumables;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }



    public void setCommandeConsumables(List<Consumable> commandeConsumables) {
        this.commandeConsumables = commandeConsumables;
        calculateTotalPrice(); // Recalculate total price when setting consumables
    }

    public Commande(List<Consumable> commandeConsumables) {
        this.commandeConsumables = commandeConsumables;
        this.dateTime = LocalDateTime.now(); // Initialize with the current date and time
        calculateTotalPrice(); // Calculate total price
    }

    public Commande() {
        isPayed=false;
        this.commandeConsumables = new ArrayList<>();
        this.dateTime = LocalDateTime.now(); // Initialize with the current date and time
        this.totalPrice = 0.0; // Initialize total price
        this.id = numero++;
    }

    public Commande copierCommande() {
        Commande copy = new Commande();
        copy.commandeConsumables = new ArrayList<>(this.commandeConsumables);
        copy.dateTime = this.dateTime;
        copy.totalPrice = this.totalPrice;
        copy.table = this.table;
        copy.isPayed = this.isPayed;
        copy.isPaused=this.isPaused;
        copy.id = this.id;
        copy.elapsedTime=this.elapsedTime;
        return copy;
    }
    private double getJeuRatePerHour() {
        String line;
        String csvSplitBy = ",";
        try (BufferedReader br = new BufferedReader(new FileReader("./menu.csv"))) {
            while ((line = br.readLine()) != null) {
                String[] consumable = line.split(csvSplitBy);
                if ("jeu".equalsIgnoreCase(consumable[0])) {
                    return Double.parseDouble(consumable[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1.0; // Return 0 if "jeu" is not found
    }
    public double calculateJeuPrice(Consumable jeuConsumable) {
        double ratePerHour = getJeuRatePerHour();
        double ratePerMinute = ratePerHour / 60.0;
        return (elapsedTime / 60.0) * ratePerMinute * jeuConsumable.getQuantity();
    }

    public void calculateTotalPrice() {
        totalPrice = 0.0;
        for (Consumable consumable : commandeConsumables) {
            if ("Jeu".equalsIgnoreCase(consumable.getName())) {
                totalPrice += calculateJeuPrice(consumable);
            } else {
                totalPrice += consumable.getPrice() * consumable.getQuantity();
            }
        }
    }

    public double getTotalPrice() {
        calculateTotalPrice();
        return totalPrice;
    }

    public String getFormattedDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String FORMAT = " %-24s %6s %2s\n";

        sb.append("+===================================+\n");

        // Emphasize Numero and Table
        sb.append(String.format("%-10s : %d", "# NUMERO ᵒ", getId())).append("\n");
        sb.append(String.format("%-10s : %s", "TABLE", getTable())).append("\n");

        // Divider
        sb.append("+----------------------------------+\n");
        sb.append(String.format(FORMAT,"_Produit_","PU","Q"));

        // Consumables
        for (Consumable consumable : commandeConsumables) {
            if (consumable.getName().equalsIgnoreCase("Jeu")) {
                sb.append(String.format(FORMAT,
                        consumable.getName(),
                        String.format("%.2f", calculateJeuPrice(consumable)),
                        consumable.getQuantity()));
            } else {
                sb.append(String.format(FORMAT,
                        consumable.getName(),
                        String.format("%.2f", consumable.getPrice()),
                        consumable.getQuantity()));
            }
        }

        // Divider
        sb.append("+----------------------------------+\n");

        // Total Price
        sb.append(String.format("TOTAL: %.2f DT\n", getTotalPrice()));

        // Final Divider
        sb.append("+===================================+");

        return sb.toString().toUpperCase();
    }
}

