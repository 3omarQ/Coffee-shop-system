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



public class CommandePrintable implements Printable {
    private Commande commande;

    public CommandePrintable(Commande commande) {
        this.commande = commande;
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        int pageWidth = (int) pf.getImageableWidth();
        int pageHeight = (int) pf.getImageableHeight();

        // Load the logo image
        BufferedImage logo;
        try {
            logo = ImageIO.read(new File("./img.png"));
        } catch (IOException e) {
            e.printStackTrace();
            return NO_SUCH_PAGE;
        }

        int logoWidth = pageWidth;  // Scale the logo width to match the page width
        int logoHeight = (int) (logo.getHeight() * ((double) logoWidth / logo.getWidth()));
        Image scaledLogo = logo.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);

        // Draw the logo image at the very top center
        int logoX = 0;  // Center the logo
        int logoY = 0;
        g2d.drawImage(scaledLogo, logoX, logoY, null);

        // Adjust the y position
        int y = logoY + logoHeight + 10;

        // Sub-header with address
        Font subHeaderFont = new Font("Arial", Font.PLAIN, 8);
        g2d.setFont(subHeaderFont);
        drawCenteredString(g2d, "Avenue Zakaria Ahmed, Sahloul 2, Sousse", pageWidth, y);
        y += 20;

        // Main attributes
        Font numeroFont = new Font("Arial", Font.BOLD, 10);
        g2d.setFont(numeroFont);
        drawCenteredString(g2d, "NUMERO: " + commande.getId(), pageWidth, y);
        y += 15;

        Font boldLargeFont = new Font("Arial", Font.BOLD, 12);
        g2d.setFont(boldLargeFont);
        drawCenteredString(g2d, "-----------------------", pageWidth, y);
        y += 15;
        drawCenteredString(g2d, "▓▓ TABLE: " + commande.getTable() + " ▓▓", pageWidth, y);
        y += 15;
        drawCenteredString(g2d, "-----------------------", pageWidth, y);
        y += 20;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        g2d.setFont(new Font("Arial", Font.PLAIN, 8));
        drawCenteredString(g2d, "DATE: " + commande.getDateTime().format(formatter).toUpperCase(), pageWidth, y);
        y += 15;

        // Consumables
        Font tableFont = new Font("Courier New", Font.BOLD, 7);
        g2d.setFont(tableFont);
        FontMetrics metrics = g.getFontMetrics(tableFont);
        int lineHeight = metrics.getHeight();
        List<Consumable> consumables = commande.getCommandeConsumables();

        int tableStartY = y;
        for (Consumable consumable : consumables) {
            if ("Jeu".equalsIgnoreCase(consumable.getName())) {
                // Calculate the elapsed time in hh:mm format
                int hours = commande.getElapsedTime() / 3600;
                int minutes = (commande.getElapsedTime() % 3600) / 60;
                String elapsedTimeFormatted = String.format("%02dh%02dmn", hours, minutes);
                String line = String.format("%-3s %3.2f DT/H  x%2d Pers. x %-7s: %2.2f DT",
                        consumable.getName().toUpperCase(), consumable.getPrice(),
                        consumable.getQuantity(), elapsedTimeFormatted,
                        commande.calculateJeuPrice(consumable));
                drawLeftAlignedString(g2d, line, 5, y);
                y += lineHeight;
            } else if (consumable.getName().contains("Choufli") || consumable.getName().contains("Musée")) {
                String line = String.format("%-22s %3.2f DT  : %7.2f DT",
                        consumable.getName().toUpperCase(), consumable.getPrice(),
                        consumable.getPrice());
                drawLeftAlignedString(g2d, line, 5, y);
                y += lineHeight;
            } else {
                String line = String.format("%-22s %3.2f x %1d : %7.2f DT",
                        consumable.getName().toUpperCase(), consumable.getPrice(),
                        consumable.getQuantity(), consumable.getPrice() * consumable.getQuantity());
                drawLeftAlignedString(g2d, line, 5, y);
                y += lineHeight;
            }
        }
        y += lineHeight;

        int tableEndY = y;

        // Check remaining height for total price and thank you message
        int remainingHeight = pageHeight - y - 40; // Space needed for the total price and thank you message

        if (remainingHeight < 40) {
            return NO_SUCH_PAGE; // If not enough space, do not print this page
        }

        // Total price
        y += 20; // Leave some space before the total price
        g2d.setFont(boldLargeFont);
        drawCenteredString(g2d, "TOTAL: " + String.format("%.2f DT", commande.getTotalPrice()).toUpperCase(), pageWidth, y);

        // Thank you message
        y += 20;
        Font thankYouFont = new Font("Arial", Font.BOLD, 8);
        g2d.setFont(thankYouFont);
        drawCenteredString(g2d, "MERCI POUR VOTRE VISITE", pageWidth, y);

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