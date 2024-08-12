package com.example.escaperoom2.model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;



public class CommandePasserPrintable implements Printable {
    private Commande commande;

    public CommandePasserPrintable(Commande commande) {
        this.commande = commande;
    }

    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        int pageWidth = (int) pf.getImageableWidth();
        int pageHeight = (int) pf.getImageableHeight();

        // Adjust the y position
        int y = 20;

        // Main attributes
        Font headerFont = new Font("Arial", Font.BOLD, 16);
        g2d.setFont(headerFont);
        drawCenteredString(g2d, "TABLE: " + commande.getTable(), pageWidth, y);
        y += 30;

        // Table and date-time
        Font subHeaderFont = new Font("Arial", Font.BOLD, 12);
        g2d.setFont(subHeaderFont);
        String tableText = "N°: " + commande.getId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String dateTimeText = "DATE: " + commande.getDateTime().format(formatter).toUpperCase();
        g2d.drawString(tableText, 5, y);
        g2d.drawString(dateTimeText, pageWidth - g2d.getFontMetrics().stringWidth(dateTimeText) - 5, y);
        y += 30;

        // Consumables
        Font itemFont = new Font("Courier New", Font.BOLD, 12);
        g2d.setFont(itemFont);
        List<Consumable> consumables = commande.getCommandeConsumables();
        for (Consumable consumable : consumables) {
            if(!consumable.getName().contains("Jeu")){

            String line = String.format("%-22s x %2d", consumable.getName().toUpperCase(), consumable.getQuantity());
            drawLeftAlignedString(g2d, line, 5, y);
            y += 20;
            }
        }
        y+=20;
        drawLeftAlignedString(g2d, "------------------------", 5, y);

        return PAGE_EXISTS;
    }

    private void drawCenteredString(Graphics2D g2d, String text, int pageWidth, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (pageWidth - metrics.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }

    private void drawLeftAlignedString(Graphics2D g2d, String text, int x, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        g2d.drawString(text, x, y + metrics.getAscent());
    }


}