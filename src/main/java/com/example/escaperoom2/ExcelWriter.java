package com.example.escaperoom2;

import com.example.escaperoom2.model.Commande;
import com.example.escaperoom2.model.Consumable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWriter {

    private static final String BASE_DIR = "./Archive des commandes/";

    private static final String[] HEADER_TITLES = {
            "ID", "Temps", "Prix total", "Table", "Est Payé",
            "Consommables"
    };

    public void addCommandesToExcel(List<Commande> commandes) throws IOException {
        LocalDate today = LocalDate.now();
        String monthYear = today.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        String day = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        String directoryPath = BASE_DIR + monthYear;
        Files.createDirectories(Paths.get(directoryPath));

        String excelFilePath = directoryPath + "/" + day + ".xlsx";
        File excelFile = new File(excelFilePath);

        Workbook workbook;

        // If the file exists, delete it to create a new one
        if (excelFile.exists()) {
            if (!excelFile.delete()) {
                throw new IOException("Failed to delete existing file: " + excelFilePath);
            }
        }

        // Create a new workbook
        workbook = new XSSFWorkbook();

        // Create a new sheet with the name of the current day
        Sheet sheet = workbook.createSheet(day);
        prepareHeaderColumn(sheet, workbook);

        for (Commande commande : commandes) {
            int columnIndex = commande.getId(); // Assuming ID is the column index
            int rowIndex = 1;

            sheet.setColumnWidth(columnIndex, 35 * 256);

            CellStyle centeredStyle = workbook.createCellStyle();
            centeredStyle.setAlignment(HorizontalAlignment.CENTER);
            centeredStyle.setBorderRight(BorderStyle.MEDIUM);
            centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Row row = sheet.getRow(rowIndex++);
            if (row == null) {
                row = sheet.createRow(rowIndex - 1);
            }
            Cell cell = row.createCell(columnIndex);
            cell.setCellValue(commande.getId());
            cell.setCellStyle(centeredStyle);

            row = sheet.getRow(rowIndex++);
            if (row == null) {
                row = sheet.createRow(rowIndex - 1);
            }
            cell = row.createCell(columnIndex);
            cell.setCellValue(commande.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            cell.setCellStyle(centeredStyle);

            row = sheet.getRow(rowIndex++);
            if (row == null) {
                row = sheet.createRow(rowIndex - 1);
            }
            cell = row.createCell(columnIndex);
            cell.setCellValue(String.format("%.2f dt", commande.getTotalPrice()));
            cell.setCellStyle(centeredStyle);

            row = sheet.getRow(rowIndex++);
            if (row == null) {
                row = sheet.createRow(rowIndex - 1);
            }
            cell = row.createCell(columnIndex);
            cell.setCellValue(commande.getTable());
            cell.setCellStyle(centeredStyle);

            row = sheet.getRow(rowIndex++);
            if (row == null) {
                row = sheet.createRow(rowIndex - 1);
            }
            cell = row.createCell(columnIndex);
            cell.setCellValue(commande.isPayed());
            cell.setCellStyle(centeredStyle);

            for (Consumable consumable : commande.getCommandeConsumables()) {
                row = sheet.getRow(rowIndex++);
                if (row == null) {
                    row = sheet.createRow(rowIndex - 1);
                }
                cell = row.createCell(columnIndex);
                cell.setCellStyle(centeredStyle);

                if ("Jeu".equalsIgnoreCase(consumable.getName())) {
                    int hours = commande.getElapsedTime() / 3600;
                    int minutes = (commande.getElapsedTime() % 3600) / 60;
                    String elapsedTimeFormatted = String.format("%02dh%02dmn", hours, minutes);
                    String cellValue = String.format("%s %.2f DT/H X %d Pers. x %s = %.2f DT",
                            consumable.getName().toUpperCase(), consumable.getPrice(),
                            consumable.getQuantity(), elapsedTimeFormatted,
                            commande.calculateJeuPrice(consumable));
                    cell.setCellValue(cellValue);
                } else if (consumable.getName().contains("Choufli") || consumable.getName().contains("Musée")) {
                    String cellValue = String.format("%s %.2f DT = %.2f DT",
                            consumable.getName().toUpperCase(), consumable.getPrice(),
                            consumable.getPrice());
                    cell.setCellValue(cellValue);
                } else {
                    String cellValue = String.format("%s %.2f DT X %d = %.2f DT",
                            consumable.getName().toUpperCase(), consumable.getPrice(),
                            consumable.getQuantity(), consumable.getPrice() * consumable.getQuantity());
                    cell.setCellValue(cellValue);
                }
            }
        }

        // Write the workbook to the file
        try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void prepareHeaderColumn(Sheet sheet, Workbook workbook) {
        sheet.setColumnWidth(0, 15 * 256);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);

        headerStyle.setFont(headerFont);

        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        for (int i = 0; i < HEADER_TITLES.length; i++) {
            Row row = sheet.getRow(i + 1); // Start from row 1
            if (row == null) {
                row = sheet.createRow(i + 1);
            }
            row.setHeightInPoints(25); // Set row height

            Cell cell = row.createCell(0); // Header in the first column
            cell.setCellValue(HEADER_TITLES[i]);
            cell.setCellStyle(headerStyle);
        }
    }
}
