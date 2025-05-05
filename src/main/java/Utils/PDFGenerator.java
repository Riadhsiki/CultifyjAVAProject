package Utils;

import Models.ContenuMultiMedia;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;


public class PDFGenerator {

    public static void generateContentPDF(ContenuMultiMedia content, String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            Paragraph title = new Paragraph(content.getTitre_media())
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            Paragraph category = new Paragraph("Catégorie : " + content.getCategorie_media())
                    .setFontSize(14)
                    .setItalic()
                    .setMarginTop(10);
            document.add(category);

            if (content.getPhoto_media() != null && !content.getPhoto_media().isEmpty()) {
                try {
                    ImageData imageData = ImageDataFactory.create(content.getPhoto_media());
                    Image pdfImage = new Image(imageData)
                            .setAutoScale(true)
                            .setHorizontalAlignment(HorizontalAlignment.CENTER);
                    document.add(pdfImage);
                } catch (MalformedURLException e) {
                    System.err.println("Erreur de chargement de l'image : " + e.getMessage());
                }
            }

            Paragraph contentText = new Paragraph(content.getText_media())
                    .setFontSize(12)
                    .setMarginTop(20)
                    .setTextAlignment(TextAlignment.JUSTIFIED);
            document.add(contentText);

            Paragraph footer = new Paragraph("Généré le : " + new java.util.Date())
                    .setFontSize(10)
                    .setItalic()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(30);
            document.add(footer);
        }
    }

    public static void generateContentsListPDF(List<ContenuMultiMedia> contents, String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            Paragraph title = new Paragraph("Rapport des Contenus Multimédia")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            float[] columnWidths = {2, 2, 2, 4};
            Table table = new Table(columnWidths).setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(20);

            // En-têtes
            table.addHeaderCell(createHeaderCell("Titre"));
            table.addHeaderCell(createHeaderCell("Catégorie"));
            table.addHeaderCell(createHeaderCell("Date"));
            table.addHeaderCell(createHeaderCell("Extrait"));

            // Données
            for (ContenuMultiMedia content : contents) {
                table.addCell(createContentCell(content.getTitre_media()));
                table.addCell(createContentCell(content.getCategorie_media()));
                table.addCell(createContentCell(content.getDate_media().toString()));
                String excerpt = content.getText_media().length() > 100 ?
                        content.getText_media().substring(0, 100) + "..." :
                        content.getText_media();
                table.addCell(createContentCell(excerpt));
            }

            document.add(table);

            Paragraph stats = new Paragraph("Total des contenus : " + contents.size())
                    .setFontSize(12)
                    .setItalic()
                    .setMarginTop(20);
            document.add(stats);
        }
    }

    private static Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.DARK_GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                )
                .setPadding(8)
                .setWidth(UnitValue.createPercentValue(25));
    }

    private static Cell createContentCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(5)
                .setTextAlignment(TextAlignment.LEFT);
    }

    public static void exportCurrentImageView(ImageView imageView, String filePath) throws IOException {
        throw new UnsupportedOperationException("Fonctionnalité désactivée - Dépend de SwingFXUtils");
        /*
        if (imageView.getImage() != null) {
            ImageIO.write(
                    SwingFXUtils.fromFXImage(imageView.getImage(), null),
                    "png",
                    new File(filePath)
            );
        }
        */
    }
}
