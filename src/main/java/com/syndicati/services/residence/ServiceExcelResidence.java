package com.syndicati.services.residence;

import com.syndicati.models.residence.Appartement;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javafx.stage.FileChooser;
import java.io.File;

import org.json.JSONObject;

public class ServiceExcelResidence {
    public static void AppartementsParResidence(List<Appartement> appartements, String residenceName) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(residenceName);

        CellStyle headerStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        font.setColor(IndexedColors.WHITE.getIndex());

        String[] headers = {"Bloc", "Étage", "Numéro", "Parking Dispo", "A louer", "Superficie", "Prix Location"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
        int rowIdx = 1;

        for (Appartement a : appartements) {
            Row row = sheet.createRow(rowIdx++);
            JSONObject info = new JSONObject(a.getAppartement_info());
            row.createCell(0).setCellValue(info.optString("bloc", "-"));
            row.createCell(1).setCellValue(info.optString("floor", "-"));
            row.createCell(2).setCellValue(info.optString("number", "-"));
            row.createCell(3).setCellValue(info.optBoolean("parking") ? "Oui" : "Non");
            row.createCell(4).setCellValue(info.optBoolean("disponible") ? "Oui" : "Non");
            row.createCell(5).setCellValue(a.getSuperficie());
            row.createCell(6).setCellValue(a.getPrix_location());
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier Excel");
        fileChooser.setInitialFileName(residenceName + "_appartements.xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (FileOutputStream fos = new FileOutputStream(file)) {
            wb.write(fos);
        }
        wb.close();
    }
}
