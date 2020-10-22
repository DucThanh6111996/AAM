/*
 * Copyright 2011 Viettel Telecom. All rights reserved.
 * VIETTEL PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.viettel.it.util;

import com.viettel.util.Config;
import com.viettel.util.Constant;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.format.CellDateFormatter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Class help to manipulate excel file
 *
 * @author ThuanNHT
 * @version 1.0
 * @since: 1.0
 */
@Service(value = "excelUtil")
@Scope("session")
public class ExcelWriterUtils {

    private Workbook workbook;
    private SXSSFWorkbook SXSSFworkbook;
    private static final Logger logger = Logger.getLogger(ExcelWriterUtils.class);
    private FileOutputStream fileOut;

    public void createWorkbook() {
        SXSSFworkbook = new SXSSFWorkbook();
    }


    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public SXSSFWorkbook getSXSSFworkbook() {
        return SXSSFworkbook;
    }

    public void setSXSSFworkbook(SXSSFWorkbook SXSSFworkbook) {
        this.SXSSFworkbook = SXSSFworkbook;
    }

    public void setSheetSelectedSXSSF(int posSheet) {
        try {
            SXSSFworkbook.setActiveSheet(posSheet);
        } catch (IllegalArgumentException ex) {
            SXSSFworkbook.setActiveSheet(0);
            logger.error(ex.getMessage(), ex);
        }
    }

    public void saveToFileExcelSXSSF(String filePathName) {
        try {
            fileOut = new FileOutputStream(filePathName);
            SXSSFworkbook.write(fileOut);
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            try {
                fileOut.close();
                SXSSFworkbook = null;
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
    }

    public void createWorkBook(String filePathName) throws IOException {
        if (filePathName.endsWith(".xls") || filePathName.endsWith(".XLS")) {
            workbook = new HSSFWorkbook(new FileInputStream(filePathName));
        } else if (filePathName.endsWith(".xlsx") || filePathName.endsWith(".XLSX")) {
            workbook = new XSSFWorkbook(new FileInputStream(filePathName));
        }
    }

    /**
     * Method to create a new excel(xls,xlsx) file with file Name
     *
     * @param filePathName ThuanNHT
     */
    public void saveToFileExcel(String filePathName) {
        try {
            fileOut = new FileOutputStream(filePathName);
            workbook.write(fileOut);
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            try {
                fileOut.close();
                workbook = null;
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
    }

    /**
     * method to create a sheet
     *
     * @param sheetName ThuanNHT
     */
    public Sheet createSheet(String sheetName) {
        String temp = WorkbookUtil.createSafeSheetName(sheetName);
        return workbook.createSheet(temp);
    }

    /**
     * method t create a row
     *
     * @param r
     * @return ThuanNHT
     */
    public Row createRow(Sheet sheet, int r) {
        return sheet.createRow(r);
    }

    /**
     * method to create a cell with value
     *
     * @param cellValue ThuanNHT
     */
    public Cell createCell(Row row, int column, String cellValue) {
        // Create a cell and put a value in it.
        Cell cell = row.createCell(column);
        cell.setCellValue(cellValue);
        return cell;
    }

    /**
     * method to create a cell with value
     *
     * @param cellValue ThuanNHT
     */
    public Cell createCell(Sheet sheet, int c, int r, String cellValue) {
        Row row = sheet.getRow(r);
        if (row == null) {
            row = sheet.createRow(r);
        }
        // Create a cell and put a value in it.
        Cell cell = row.createCell(c);
        cell.setCellValue(cellValue);
        return cell;
    }
    //hoanm1_25-12-2013_start

    public Cell createCellNumeric(Sheet sheet, int c, int r, String cellValue) {
        Row row = sheet.getRow(r);
        if (row == null) {
            row = sheet.createRow(r);
        }
        Cell cell = row.createCell(c);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue(Long.parseLong(cellValue));
        return cell;
    }

    public Cell createCellNumSTP(Sheet sheet, int c, int r, Double cellValue) {
        Row row = sheet.getRow(r);
        if (row == null) {
            row = sheet.createRow(r);
        }
        Cell cell = row.createCell(c);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue(cellValue);
        return cell;
    }
    //hoanm1_25-12-2013_end

    public Cell createCell1(Sheet sheet, int c, int r, double cellValue) {
        Row row = sheet.getRow(r);
        if (row == null) {
            row = sheet.createRow(r);
        }
        // Create a cell and put a value in it.
        Cell cell = row.createCell(c);
        cell.setCellValue(cellValue);
        return cell;
    }

    /**
     * method to create a cell with value with style
     *
     * @param cellValue ThuanNHT
     */
    public Cell createCell(Sheet sheet, int c, int r, String cellValue, CellStyle style) {
        Row row = sheet.getRow(r);
        if (row == null) {
            row = sheet.createRow(r);
        }
        // Create a cell and put a value in it.
        Cell cell = row.createCell(c);
        cell.setCellValue(cellValue);
        cell.setCellStyle(style);
        return cell;
    }
    //R5853_Hoanm1_Start

    public Cell createCellFloat(Sheet sheet, int c, int r, Double cellValue) {
        Row row = sheet.getRow(r);
        if (row == null) {
            row = sheet.createRow(r);
        }
        Cell cell = row.createCell(c);
        try {
            if (cellValue != null) {
                DecimalFormat df = new DecimalFormat("#.#####");
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                String cellValueNew = df.format(Double.parseDouble(cellValue.toString()));
                cell.setCellValue(Double.parseDouble(cellValueNew));
            } else {
                cell.setCellValue("");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return cell;
    }
    //R5853_Hoanm1_End

    /**
     * Method get primitive content Of cell
     *
     * @param sheet
     * @param c
     * @param r
     * @return
     */
    private static Object getCellContent(Sheet sheet, int c, int r) {
        Cell cell = getCellOfSheet(r, c, sheet);
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().getString();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            default:
                return "";

        }
    }

    /**
     * Method set sheet is selected when is opened
     *
     * @param posSheet
     */
    public void setSheetSelected(int posSheet) {
        try {
            workbook.setActiveSheet(posSheet);
        } catch (IllegalArgumentException ex) {
            workbook.setActiveSheet(0);
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * method to merge cell
     *
     * @param sheet
     * @param firstRow based 0
     * @param lastRow  based 0
     * @param firstCol based 0
     * @param lastCol  based 0
     */
    private static void mergeCells(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(
                firstRow, //first row (0-based)
                lastRow, //last row  (0-based)
                firstCol, //first column (0-based)
                lastCol //last column  (0-based)
        ));
    }

    /**
     * method to fill color background for cell
     *
     * @param cell
     * @param colors:BLACK, WHITE, RED, BRIGHT_GREEN, BLUE, YELLOW, PINK,
     *                      TURQUOISE, DARK_RED, GREEN, DARK_BLUE, DARK_YELLOW, VIOLET, TEAL,
     *                      GREY_25_PERCENT, GREY_50_PERCENT, CORNFLOWER_BLUE, MAROON, LEMON_CHIFFON,
     *                      ORCHID, CORAL, ROYAL_BLUE, LIGHT_CORNFLOWER_BLUE, SKY_BLUE,
     *                      LIGHT_TURQUOISE, LIGHT_GREEN, LIGHT_YELLOW, PALE_BLUE, ROSE, LAVENDER,
     *                      TAN, LIGHT_BLUE, AQUA, LIME, GOLD, LIGHT_ORANGE, ORANGE, BLUE_GREY,
     *                      GREY_40_PERCENT, DARK_TEAL, SEA_GREEN, DARK_GREEN, OLIVE_GREEN, BROWN,
     *                      PLUM, INDIGO, GREY_80_PERCENT, AUTOMATIC;
     */
    public void fillAndColorCell(Cell cell, IndexedColors colors) {
        CellStyle style = workbook.createCellStyle();
        style.setFillBackgroundColor(colors.getIndex());
        cell.setCellStyle(style);
    }
    // datpk  lay object tu Row

    private static Object getCellContentRow(int c, Row row) {
        Cell cell = getCellOfSheetRow(c, row);
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().getString();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            default:
                return "";

        }
    }

    /**
     * Method get text content Of cell
     *
     * @param sheet
     * @param c
     * @param r
     * @return
     */
    public static String getCellStrContent(Sheet sheet, int c, int r) {
        Cell cell = getCellOfSheet(r, c, sheet);
        if (cell == null) {
            return "";
        }
        String temp = getCellContent(sheet, c, r).toString().trim();
        if (temp.endsWith(".0")) {
            return temp.substring(0, temp.length() - 2);
        }
        return temp;
    }
    // datpk getStringconten tu Row

    public static String getCellStrContentRow(int c, Row row) {
        Cell cell = getCellOfSheetRow(c, row);
        if (cell == null) {
            return "";
        }
        String temp = getCellContentRow(c, row).toString().trim();
        if (temp.endsWith(".0")) {
            return temp.substring(0, temp.length() - 2);
        }
        return temp;
    }

    /**
     * method to create validation from array String.But String do not exceed
     * 255 characters
     *
     * @param arrValidate * ThuanNHT
     */
    public void createDropDownlistValidateFromArr(Sheet sheet, String[] arrValidate, int firstRow, int lastRow, int firstCol, int lastCol) {
        CellRangeAddressList addressList = new CellRangeAddressList(
                firstRow, lastRow, firstCol, lastCol);
        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint(arrValidate);
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
        dataValidation.setSuppressDropDownArrow(false);
        HSSFSheet sh = (HSSFSheet) sheet;
        sh.addValidationData(dataValidation);
    }

    /**
     * Method to create validation from spread sheet via range
     *
     * @param range
     * @param firstRow
     * @param lastRow
     * @param firstCol
     * @param lastCol  * ThuanNHT
     */
    public void createDropDownListValidateFromSpreadSheet(String range, int firstRow, int lastRow, int firstCol, int lastCol, Sheet shet) {
        Name namedRange = workbook.createName();
        Random rd = new Random();
        String refName = ("List" + rd.nextInt()).replace("-", "");
        namedRange.setNameName(refName);
//        namedRange.setRefersToFormula("'Sheet1'!$A$1:$A$3");
        namedRange.setRefersToFormula(range);
        DVConstraint dvConstraint = DVConstraint.createFormulaListConstraint(refName);
        CellRangeAddressList addressList = new CellRangeAddressList(
                firstRow, lastRow, firstCol, lastCol);
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
        dataValidation.setSuppressDropDownArrow(false);
        HSSFSheet sh = (HSSFSheet) shet;
        sh.addValidationData(dataValidation);
    }

    public void createDropDownListValidateFromSpreadSheet(String sheetName, String columnRangeName,
                                                          int rowRangeStart, int rowRangeEnd, int firstRow, int lastRow, int firstCol, int lastCol, Sheet shet) {
        String range = "'" + sheetName + "'!$" + columnRangeName + "$" + rowRangeStart + ":" + "$" + columnRangeName + "$" + rowRangeEnd;
        Name namedRange = workbook.createName();
        Random rd = new Random();
        String refName = ("List" + rd.nextInt()).replace("-", "");
        namedRange.setNameName(refName);
//        namedRange.setRefersToFormula("'Sheet1'!$A$1:$A$3");
        namedRange.setRefersToFormula(range);
        DVConstraint dvConstraint = DVConstraint.createFormulaListConstraint(refName);
        CellRangeAddressList addressList = new CellRangeAddressList(
                firstRow, lastRow, firstCol, lastCol);
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
        dataValidation.setSuppressDropDownArrow(false);
        HSSFSheet sh = (HSSFSheet) shet;
        sh.addValidationData(dataValidation);
    }

    public Sheet getSheetAt(int pos) {
        return workbook.getSheetAt(pos);
    }

    public Sheet getSheet(String name) {
        return workbook.getSheet(name);
    }

    /**
     * Method to read an excel file
     *
     * @param filePathName
     * @return * ThuanNHT
     */
    public Workbook readFileExcel(String filePathName) {
        InputStream inp = null;
        try {
            inp = new FileInputStream(filePathName);
            workbook = WorkbookFactory.create(inp);
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            try {
                if (inp != null) {
                    inp.close();
                }
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
        return workbook;
    }

    public Workbook readFileExcel(InputStream inp) {
        try {
            workbook = WorkbookFactory.create(inp);
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            try {
                inp.close();
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
        return workbook;
    }

    /**
     * * ThuanNHT
     *
     * @param r
     * @param c
     * @param sheet
     * @return
     */
    private static Cell getCellOfSheet(int r, int c, Sheet sheet) {
        try {
            Row row = sheet.getRow(r);
            if (row == null) {
                return null;
            }
            return row.getCell(c);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * set style for cell
     *
     * @param cell
     * @param halign
     * @param valign
     * @param border
     * @param borderColor
     */
    private void setCellStyle(Cell cell, short halign, short valign, short border, short borderColor, int fontHeight) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) fontHeight);
        font.setFontName("Times New Roman");
        style.setAlignment(halign);
        style.setVerticalAlignment(valign);
        style.setBorderBottom(border);
        style.setBottomBorderColor(borderColor);
        style.setBorderLeft(border);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(border);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(border);
        style.setTopBorderColor(borderColor);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    /**
     * Method to draw an image on excel file
     *
     * @param imgSrc
     * @param sheet
     * @param colCorner
     * @param rowCorner
     * @throws IOException
     */
    public void drawImageOnSheet(String imgSrc, Sheet sheet, int colCorner, int rowCorner) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(imgSrc);
            byte[] bytes = IOUtils.toByteArray(is);
            int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
            if (imgSrc.endsWith(".jpg") || imgSrc.endsWith(".JPG") || imgSrc.endsWith(".jpeg") || imgSrc.endsWith(".JPEG")) {
                pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
            } else if (imgSrc.endsWith(".png") || imgSrc.endsWith(".PNG")) {
                pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
            }

            CreationHelper helper = workbook.getCreationHelper();
            // Create the drawing patriarch.  This is the top level container for all shapes.
            Drawing drawing = sheet.createDrawingPatriarch();
            //add a picture shape
            ClientAnchor anchor = helper.createClientAnchor();
            //set top-left corner of the picture,
            //subsequent call of Picture#resize() will operate relative to it
            anchor.setCol1(colCorner);
            anchor.setRow1(rowCorner);
            Picture pict = drawing.createPicture(anchor, pictureIdx);

            //auto-size picture relative to its top-left corner
            pict.resize();
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (is != null) {
                is.close();
            }
        }

    }

    public void setStandardCellStyle(Cell cell) {
        setCellStyle(cell, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER,
                CellStyle.BORDER_THIN, IndexedColors.BLACK.getIndex(), 12);
    }

    // datpk: lay cell tu Row
    private static Cell getCellOfSheetRow(int c, Row row) {
        try {
            if (row == null) {
                return null;
            }
            return row.getCell(c);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    public static void main(String[] arg) {
    }

    public Row getOrCreateRow(Sheet sheet, int rowIndex) {
        if (sheet.getRow(rowIndex) == null) {
            return sheet.createRow(rowIndex);
        }
        return sheet.getRow(rowIndex);
    }

    public XSSFRow getOrCreateRow(XSSFSheet sheet, int rowIndex) {
        if (sheet.getRow(rowIndex) == null) {
            return sheet.createRow(rowIndex);
        }
        return sheet.getRow(rowIndex);
    }

    //getCellValue not null
    public String getCellValue(Cell cell) {
        String result = "";
        if (cell == null) {
            return result;
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                result = String.valueOf(cell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                result = String.valueOf(cell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                switch (cell.getCachedFormulaResultType()) {
                    case Cell.CELL_TYPE_NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
//                            Date date = cell.getDateCellValue();
//                            String dateFmt = cell.getCellStyle().getDataFormatString();
//                            result = new CellDateFormatter(dateFmt).format(date);
//                            result = DateTimeUtils.format(date, "dd/MM/yyyy HH:mm:ss");
//                            result = date.toGMTString();
                        } else {
                            Double number = cell.getNumericCellValue();
                            if (Math.round(number) == number) {
                                result = String.valueOf(Math.round(number));
                            } else {
                                result = String.valueOf(cell.getNumericCellValue());
                            }
                        }
                        break;
                    case Cell.CELL_TYPE_STRING:
                        result = String.valueOf(cell.getRichStringCellValue());
                        break;
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    String dateFmt = cell.getCellStyle().getDataFormatString();
                    result = new CellDateFormatter(dateFmt).format(date);
                } else {
                    Double number = cell.getNumericCellValue();
                    if (Math.round(number) == number) {
                        result = String.valueOf(Math.round(number));
                    } else {
                        result = String.valueOf(cell.getNumericCellValue());
                    }
                }
                break;
            case Cell.CELL_TYPE_BLANK:
                result = "";
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                result = String.valueOf(cell.getBooleanCellValue());
                break;
        }
        return result.trim();
    }

    public void setCellValue(Cell cell, Object obj) {
        if (obj == null) {
            cell.setCellValue("");
        } else {
            if (obj instanceof Date) {
                cell.setCellValue((Date) obj);
            } else if (obj instanceof Calendar) {
                cell.setCellValue((Calendar) obj);
            } else if (obj instanceof String) {
                cell.setCellValue((String) obj);
//            } else if (obj instanceof RichTextString) {
//                cell.setCellValue((RichTextString) obj);
            } else if (obj instanceof Double) {
                cell.setCellValue((Double) obj);
            } else if (obj instanceof Long) {
                cell.setCellValue(Double.valueOf((Long) obj));
            } else if (obj instanceof Integer) {
                cell.setCellValue(Double.valueOf((Integer) obj));
            } else if (obj instanceof Float) {
                cell.setCellValue(Double.valueOf((Float) obj));
            } else if (obj instanceof Short) {
                cell.setCellValue(Double.valueOf((Short) obj));
            } else if (obj instanceof Boolean) {
                cell.setCellValue((Boolean) obj);
            }
        }
    }

    public CellStyle getCsLeftBoder() {
        return cellStyle(CellStyle.ALIGN_LEFT, CellStyle.VERTICAL_BOTTOM, CellStyle.BORDER_THIN,
                IndexedColors.BLACK.getIndex(), -1, 11, -1, false);
    }

    public CellStyle getCsImportSucc() {
        return cellStyle(CellStyle.ALIGN_LEFT, CellStyle.VERTICAL_BOTTOM, CellStyle.BORDER_THIN,
                IndexedColors.BLACK.getIndex(), IndexedColors.GREEN.getIndex(), 11, -1, false);
    }

    public CellStyle getCsImportFail() {
        return cellStyle(CellStyle.ALIGN_LEFT, CellStyle.VERTICAL_BOTTOM, CellStyle.BORDER_THIN,
                IndexedColors.BLACK.getIndex(), IndexedColors.RED.getIndex(), 11, -1, false);
    }

    public CellStyle getCsRightBoder() {
        return cellStyle(CellStyle.ALIGN_RIGHT, CellStyle.VERTICAL_BOTTOM, CellStyle.BORDER_THIN,
                IndexedColors.BLACK.getIndex(), -1, 11, -1, true);
    }

    public CellStyle getCsCenterBoder() {
        return cellStyle(CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_BOTTOM, CellStyle.BORDER_THIN,
                IndexedColors.BLACK.getIndex(), -1, 11, -1, true);
    }

    public CellStyle getCsDateBoder() {
        CellStyle cellStyle =  cellStyle(CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_BOTTOM, CellStyle.BORDER_THIN,
                IndexedColors.BLACK.getIndex(), -1, 11, -1, true);
        cellStyle.setDataFormat(getSXSSFworkbook().createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));
        return cellStyle;
    }

    public CellStyle getCsCenterNoBoder() {
        return cellStyle(CellStyle.ALIGN_RIGHT, CellStyle.VERTICAL_BOTTOM, CellStyle.NO_FILL,
                IndexedColors.BLACK.getIndex(), -1, 11, -1, true);
    }

    public CellStyle getCsCenterNoboderBoldweight() {
        return cellStyle(CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_BOTTOM, CellStyle.NO_FILL,
                IndexedColors.BLACK.getIndex(), -1, 11, XSSFFont.BOLDWEIGHT_BOLD, false);
    }

    public CellStyle getCsColHeader() {
        return cellStyle(CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, CellStyle.BORDER_THIN,
                IndexedColors.BLACK.getIndex(), IndexedColors.SKY_BLUE.getIndex(),
                11, XSSFFont.BOLDWEIGHT_BOLD, true);
    }

    public CellStyle getCsTitle() {
        return cellStyle(CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_BOTTOM, CellStyle.NO_FILL,
                IndexedColors.BLACK.getIndex(), -1, 18, XSSFFont.BOLDWEIGHT_BOLD, true);
    }

    public CellStyle getCsSubTitle() {
        return cellStyle(CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_BOTTOM, CellStyle.NO_FILL,
                IndexedColors.BLACK.getIndex(), -1, 13, XSSFFont.BOLDWEIGHT_BOLD, true);
    }

    private CellStyle cellStyle(short halign, short valign, short border,
                                short borderColor, int foregroundColor,
                                int fontHeight, int fontWeight, boolean wraptext) {
        CellStyle style;
        Font font;
        if (workbook != null) {
            style = workbook.createCellStyle();
            font = workbook.createFont();

        } else {
            style = SXSSFworkbook.createCellStyle();
            font = SXSSFworkbook.createFont();
        }
        font.setFontHeightInPoints((short) fontHeight);
        font.setFontName("Times New Roman");
        if (fontWeight != -1) {
            font.setBoldweight((short) fontWeight);
        }
        style.setAlignment(halign);
        style.setVerticalAlignment(valign);
        style.setBorderBottom(border);
        style.setBottomBorderColor(borderColor);
        style.setBorderLeft(border);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(border);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(border);
        style.setTopBorderColor(borderColor);
        style.setFont(font);
        if (foregroundColor != -1) {
            style.setFillForegroundColor((short) foregroundColor);
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        }
        style.setWrapText(wraptext);
        return style;
    }

    public String getFolderSave() {
        String pathOut;
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        pathOut = ctx.getRealPath("/") + File.separator + ".." + File.separator + ".." + File.separator + "report_out" + File.separator;
        File folderOut = new File(pathOut);
        if (!folderOut.exists()) {
            folderOut.mkdirs();
        }
        return pathOut;
    }

    public String getReportName(String reportName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern("ddMMyyyy_HHmmss");
        String strCurTimeExp = dateFormat.format(new Date());
        return reportName + "_" + strCurTimeExp + ".xlsx";
    }
    public String getPathOut(String reportName) {
        return getFolderSave() + getReportName(reportName);
    }
    //dungvv8_start_31/03/2016
    public void creatTemplate(Sheet sheet, String title) {

//        createCellObject(sheet, 0, 0, LanguageBundleUtils.getString("common.export.corporation"), getCsSubTitle());
//        createCellObject(sheet, 0, 1, LanguageBundleUtils.getString("common.export.company"), getCsCenterNoboderBoldweight());
//        createCellObject(sheet, 5, 0, LanguageBundleUtils.getString("common.export.nation"), getCsSubTitle());
//        createCellObject(sheet, 5, 1, LanguageBundleUtils.getString("common.export.declare"), getCsCenterNoboderBoldweight());
        createCellObject(sheet, 1, 4, title, getCsTitle());

        ExcelWriterUtils.mergeCells(sheet, 0, 0, 0, 2);
        ExcelWriterUtils.mergeCells(sheet, 1, 1, 0, 2);
        ExcelWriterUtils.mergeCells(sheet, 0, 0, 5, 8);
        ExcelWriterUtils.mergeCells(sheet, 1, 1, 5, 8);
        ExcelWriterUtils.mergeCells(sheet, 4, 4, 1, 6);

        setRowHeight(sheet, 4, 630);
    }
    //dungvv8_end_31/03/2016

    public void setRowHeight(Sheet sheet, int rowIndex, int rowHeight) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        row.setHeight((short) rowHeight);
    }

    public Cell createCellObject(Sheet sheet, int c, int r, Object obj, CellStyle cs) {
        Row row = sheet.getRow(r);
        if (row == null) {
            row = sheet.createRow(r);
        }
        // Create a cell and put a value in it.
        Cell cell = row.createCell(c);
        if (obj != null) {
            if (obj instanceof String) {
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue((String) obj);
            } else if (obj instanceof Double) {
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue((Double) obj);
            } else if (obj instanceof Float) {
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue((Float) obj);
            } else if (obj instanceof Long) {
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue((Long) obj);
            } else if (obj instanceof Integer) {
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue((Integer) obj);
            } else if (obj instanceof Date) {
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue((Date) obj);
            }
        } else {
            cell.setCellType(Cell.CELL_TYPE_STRING);
            cell.setCellValue("");
        }
        // CellStyle
        if (cs != null) {
            cell.setCellStyle(cs);
//            if (obj instanceof Date) {
//                cs.setDataFormat(sheet.getWorkbook().
//                        createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));
//            }
        }
        return cell;
    }
    //R7007_dungvv8_end
}
