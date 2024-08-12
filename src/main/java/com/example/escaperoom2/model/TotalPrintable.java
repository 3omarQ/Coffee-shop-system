package com.example.escaperoom2.model;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TotalPrintable implements Printable {
    private List<Commande> commandsList;

    public TotalPrintable(List<Commande> commandsList) {
        this.commandsList = commandsList;
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

        // Print the title
        Font titleFont = new Font("Arial", Font.BOLD, 8);
        g2d.setFont(titleFont);
        drawCenteredString(g2d, "**TOTAL**", pageWidth, y);
        y += 20;

        // Print the date at the top
        Font headerFont = new Font("Arial", Font.BOLD, 11);
        g2d.setFont(headerFont);
        String dateText = "DATE: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        drawCenteredString(g2d, dateText, pageWidth, y);
        y += 30;

        // Calculate the totals
        double totalSum = 0.0;
        double totalDesJeux = 0.0;
        double totalEscapeRoom = 0.0;
        double totalBoissons = 0.0;

        for (Commande commande : commandsList) {
            totalSum += commande.getTotalPrice();

            for (Consumable consumable : commande.getCommandeConsumables()) {
                String consumableName = consumable.getName().toLowerCase();

                if ("jeu".equalsIgnoreCase(consumableName)) {
                    totalDesJeux += commande.calculateJeuPrice(consumable);
                } else if (consumableName.contains("personnes")) {
                    totalEscapeRoom += consumable.getPrice();
                } else {
                    totalBoissons += consumable.getPrice();
                }
            }
        }

        // Print the totals
        String totalSumText = String.format("TOTAL DU JOUR: %.2f dt", totalSum);
        g2d.setFont(headerFont);
        drawLeftAlignedString(g2d, totalSumText, 5, y);
        y += 15;

        drawLeftAlignedString(g2d, "----------------------------", 5, y);
        y += 15;

        String totalJeuText = String.format("TOTAL LUDOTHEQUE: %.2f dt", totalDesJeux);
        drawLeftAlignedString(g2d, totalJeuText, 5, y);
        y += 15;

        String totalEscapeRoomText = String.format("TOTAL ESCAPE-ROOM: %.2f dt", totalEscapeRoom);
        drawLeftAlignedString(g2d, totalEscapeRoomText, 5, y);
        y += 15;

        String totalBoissonsText = String.format("TOTAL BOISSONS: %.2f dt", totalBoissons);
        drawLeftAlignedString(g2d, totalBoissonsText, 5, y);
        y += 15;

        drawLeftAlignedString(g2d, "----------------------------", 5, y);
        y += 20;

        // Print each command
        for (Commande commande : commandsList) {
            Font subHeaderFont = new Font("Arial", Font.PLAIN, 10);
            g2d.setFont(subHeaderFont);
            String commandIDText = String.format("+ Commande n° %d : %.2f dt", commande.getId(), commande.getTotalPrice());
            drawLeftAlignedString(g2d, commandIDText, 5, y);
            y += 12;
        }

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
