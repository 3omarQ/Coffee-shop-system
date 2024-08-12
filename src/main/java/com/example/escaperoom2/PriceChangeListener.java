package com.example.escaperoom2;

import com.example.escaperoom2.model.Consumable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;


public interface PriceChangeListener {
    public void onChangeListener(Consumable consumable);
}
