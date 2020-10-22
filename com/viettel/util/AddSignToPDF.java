package com.viettel.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.viettel.it.util.MessageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
//itext libraries to write PDF file
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author hophk
 * @Description AddSignToPDF.java
 * @since Sep 30, 2015
 */
public class AddSignToPDF {
    private static Logger logger = LogManager.getLogger(AddSignToPDF.class);

    public static void addSignPlaceHolder(String inputFile, String keySearch, String comment, int shiftX, int shiftY, int page) throws Exception {
        List<TextPosition> listTextPosition = findPositions(inputFile, keySearch);
        int size = listTextPosition.size();
        Rectangle rect;
        if (size > 1) {
            rect = new Rectangle(listTextPosition.get(size - 1).getX(), listTextPosition.get(size - 1).getY(), listTextPosition.get(size - 1).getX(), listTextPosition.get(size - 1).getY());
        } else {
            rect = new Rectangle(listTextPosition.get(0).getX(), listTextPosition.get(0).getY(), listTextPosition.get(0).getX(), listTextPosition.get(0).getY());
        }
        String outputFile = inputFile + "_temp";
        PdfReader reader = new PdfReader(inputFile);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputFile));
        PdfAnnotation annotation;
        float pageHeight = reader.getPageSize(reader.getNumberOfPages()).getHeight();
        Rectangle nRect = new Rectangle(rect.getLeft() + shiftX, pageHeight - rect.getTop() + shiftY, rect.getLeft() + shiftX - 20, pageHeight - rect.getTop() + shiftY + 20);
        annotation = PdfAnnotation.createText(stamper.getWriter(), nRect, comment, comment, true, null);
        stamper.addAnnotation(annotation, page);
        stamper.close();
        reader.close();
        // Thay the file cu
        (new File(inputFile)).delete();
        File oldFile = new File(inputFile);
        new File(outputFile).renameTo(oldFile);
    }

    public static void addSignPlaceHolderNew(String inputFile, String keySearch, String comment, int shiftX, int shiftY, Integer checkPage) throws Exception {
        List<TextPosition> listTextPosition = findPositions(inputFile, keySearch);
        int size = listTextPosition.size();
        Rectangle rect;
        if (size > 1) {
            rect = new Rectangle(listTextPosition.get(size - 1).getX(), listTextPosition.get(size - 1).getY(), listTextPosition.get(size - 1).getX(), listTextPosition.get(size - 1).getY());
        } else {
            rect = new Rectangle(listTextPosition.get(0).getX(), listTextPosition.get(0).getY(), listTextPosition.get(0).getX(), listTextPosition.get(0).getY());
        }
        String outputFile = inputFile + "_temp";
        PdfReader reader = new PdfReader(inputFile);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputFile));
        PdfAnnotation annotation;
        float pageHeight = reader.getPageSize(reader.getNumberOfPages()).getHeight();
        Rectangle nRect = new Rectangle(rect.getLeft() + shiftX, pageHeight - rect.getTop() + shiftY, rect.getLeft() + shiftX - 20, pageHeight - rect.getTop() + shiftY + 20);
        annotation = PdfAnnotation.createText(stamper.getWriter(), nRect, comment, comment, true, null);
        if (checkPage > 12) {
            stamper.addAnnotation(annotation, reader.getNumberOfPages());
        } else {
            stamper.addAnnotation(annotation, 1);
        }
        stamper.close();
        reader.close();
        // Thay the file cu
        (new File(inputFile)).delete();
        File oldFile = new File(inputFile);
        new File(outputFile).renameTo(oldFile);
    }

    public static List<TextPosition> findPositions(String pdfFile, final String key)
            throws IOException {
        PDDocument document = PDDocument.load(pdfFile);
        final StringBuffer extractedText = new StringBuffer();
        final List<TextPosition> positions = new ArrayList<>();
        PDFTextStripper textStripper = new PDFTextStripper() {
            @Override
            protected void processTextPosition(TextPosition text) {
                extractedText.append(text.getCharacter());
                if (extractedText.toString().endsWith(key)) {
                    positions.add(text);
                }
            }
        };
        List lstPage = document.getDocumentCatalog().getAllPages();
        for (int pageNum = 0; pageNum < document.getDocumentCatalog().getAllPages().size(); pageNum++) {
            PDPage page = (PDPage) lstPage.get(pageNum);
            textStripper.processStream(page, page.findResources(), page.getContents().getStream());
            extractedText.setLength(0);
        }
        document.close();
        return Collections.unmodifiableList(positions);
    }

    public static void main(String[] args) {
        try {
            addSignPlaceHolder("D:\\uctt\\TEMPLATE_UCTT.pdf", MessageUtil.getResourceBundleMessage("vice.director"), "1", -25, -30, 2);
            addSignPlaceHolder("D:\\uctt\\TEMPLATE_UCTT.pdf", MessageUtil.getResourceBundleMessage("deputy.general.manager"), "2", -50, -30, 1);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}