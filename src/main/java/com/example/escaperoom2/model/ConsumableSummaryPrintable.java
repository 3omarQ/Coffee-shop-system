package com.example.escaperoom2.model;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsumableSummaryPrintable implements Printable {
    private Map<String, Integer> consumableCountMap;
    private Map<String, Double> consumableEarningsMap;
    private double totalEarnings;

    public ConsumableSummaryPrintable(Map<String, Integer> consumableCountMap, Map<String, Double> consumableEarningsMap) {
        this.consumableCountMap = consumableCountMap;
        this.consumableEarningsMap = consumableEarningsMap;
        double totalEarnings = 0.0;
        for (double earnings : consumableEarningsMap.values()) {
            totalEarnings += earnings;
        }
        this.totalEarnings = totalEarnings;
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        int pageWidth = (int) pf.getImageableWidth();

        // Adjust the y position
        int y = 20;

        // Print the date at the top
        Font titleFont = new Font("Arial", Font.BOLD, 8);
        g2d.setFont(titleFont);
        drawCenteredString(g2d, "**VENTES PAR PRODUIT**", pageWidth, y);
        y += 20;

        Font headerFont = new Font("Arial", Font.BOLD, 13);
        g2d.setFont(headerFont);
        String dateText = "DATE: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        drawCenteredString(g2d, dateText, pageWidth, y);
        y += 30;

        // Print the total earnings
        String totalEarningsText = String.format("TOTAL : %.2f dt", totalEarnings);
        g2d.setFont(headerFont);
        drawLeftAlignedString(g2d, totalEarningsText, 5, y);
        y += 20;
        drawLeftAlignedString(g2d, "----------------------------", 5, y);
        y += 20;

        // Print each consumable's summary
        Font subHeaderFont = new Font("Courier New", Font.PLAIN, 9);
        g2d.setFont(subHeaderFont);
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
        for (String consumableName : sortedConsumables) {
            int quantity = consumableCountMap.get(consumableName);
            double earnings = consumableEarningsMap.get(consumableName);

            // Check if the consumable name contains "Jeu" or "Personnes"
            boolean isBold = consumableName.equals("Jeu") || consumableName.contains("Personnes");

            // Set the font based on whether the text should be bold or not
            Font currentFont = g2d.getFont();
            Font boldFont = currentFont.deriveFont(Font.BOLD);

            if (isBold) {
                g2d.setFont(boldFont);
            }

            String consumableText = String.format("%-21s ˣ %-2d: %-5s", consumableName, quantity, String.format("%.2f dt", earnings));
            drawLeftAlignedString(g2d, consumableText, 5, y);
            y += 15;

            // Reset the font to normal after drawing the bold text
            if (isBold) {
                g2d.setFont(currentFont);
            }
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
