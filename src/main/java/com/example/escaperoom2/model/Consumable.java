package com.example.escaperoom2.model;

import java.io.Serializable;

public class Consumable implements Serializable {
    private String name;
    private int quantity;


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Consumable(String name, double price) {
        this.name = name;
        this.price = price;
        quantity=1;
    }
    public Consumable(Consumable other) {
        this.name = other.name;
        this.price = other.price;
        this.quantity = other.quantity; // You can choose to initialize this as 1 if needed
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    private double price;
}
