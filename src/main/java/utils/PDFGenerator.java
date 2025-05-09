package Utils;

import Models.ContenuMultiMedia;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PDFGenerator {

    public static void generateContentPDF(ContenuMultiMedia content, String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // En-tête avec logo
            addHeader(document);

            // Titre du contenu
            Paragraph title = new Paragraph(content.getTitre_media())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(title);

            // Catégorie
            Paragraph category = new Paragraph("Catégorie : " + content.getCategorie_media())
                    .setFontSize(14)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(category);

            // Gestion de l'image avec fallback
            addImageToDocument(document, content);

            // Contenu texte
            Paragraph contentText = new Paragraph(content.getText_media())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMarginTop(20);
            document.add(contentText);

            // Pied de page
            addFooter(document);

        } catch (Exception e) {
            throw new IOException("Erreur lors de la génération du PDF : " + e.getMessage(), e);
        }
    }

    private static void addImageToDocument(Document document, ContenuMultiMedia content) {
        if (content.getPhoto_media() != null && !content.getPhoto_media().isEmpty()) {
            try {
                ImageData imageData;

                // Essayer de charger l'image depuis différents chemins
                if (content.getPhoto_media().startsWith("file:")) {
                    // Cas 1 : URL absolue (file:/...)
                    imageData = ImageDataFactory.create(content.getPhoto_media());
                } else if (Files.exists(Paths.get(content.getPhoto_media()))) {
                    // Cas 2 : Chemin relatif (uploads/image.jpg)
                    imageData = ImageDataFactory.create(content.getPhoto_media());
                } else {
                    // Cas 3 : Image dans les ressources
                    imageData = ImageDataFactory.create(
                            PDFGenerator.class.getResource("/images/default.png").toString());
                }

                Image pdfImage = new Image(imageData)
                        .setAutoScale(true)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                        .setMarginBottom(15);
                document.add(pdfImage);

            } catch (MalformedURLException e) {
                System.err.println("Erreur de chargement de l'image : " + e.getMessage());
                addDefaultImage(document);
            }
        } else {
            addDefaultImage(document);
        }
    }

    private static void addDefaultImage(Document document) {
        try {
            ImageData defaultImage = ImageDataFactory.create(
                    PDFGenerator.class.getResource("/images/default.png").toString());
            document.add(new Image(defaultImage)
                    .setAutoScale(true)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER));
        } catch (Exception e) {
            document.add(new Paragraph("[Image non disponible]")
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER));
        }
    }

    private static void addHeader(Document document) throws MalformedURLException {
        try {
            // Logo (optionnel)
            ImageData logoData = ImageDataFactory.create(
                    PDFGenerator.class.getResource("/images/logo.png").toString());
            Image logo = new Image(logoData)
                    .setWidth(100)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(logo);
        } catch (Exception e) {
            // Si le logo n'existe pas, on continue sans
        }

        document.add(new Paragraph("Cultify - Encyclopédie")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(16)
                .setMarginBottom(10));
    }

    private static void addFooter(Document document) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Paragraph footer = new Paragraph("Généré le " + sdf.format(new Date()))
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(30);
        document.add(footer);
    }

    public static void generateContentsListPDF(List<ContenuMultiMedia> contents, String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            addHeader(document);

            // Titre du document
            document.add(new Paragraph("Liste des contenus multimédias")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Tableau des contenus
            float[] columnWidths = {3, 2, 2, 5};
            Table table = new Table(UnitValue.createPercentArray(columnWidths))
                    .useAllAvailableWidth()
                    .setMarginTop(20);

            // En-têtes du tableau
            table.addHeaderCell(createHeaderCell("Titre"));
            table.addHeaderCell(createHeaderCell("Catégorie"));
            table.addHeaderCell(createHeaderCell("Date"));
            table.addHeaderCell(createHeaderCell("Extrait"));

            // Remplissage des données
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            for (ContenuMultiMedia content : contents) {
                table.addCell(createContentCell(content.getTitre_media()));
                table.addCell(createContentCell(content.getCategorie_media()));
                table.addCell(createContentCell(dateFormat.format(content.getDate_media())));

                String excerpt = content.getText_media().length() > 100 ?
                        content.getText_media().substring(0, 100) + "..." :
                        content.getText_media();
                table.addCell(createContentCell(excerpt));
            }

            document.add(table);

            // Statistiques
            document.add(new Paragraph("Nombre total de contenus : " + contents.size())
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setItalic()
                    .setMarginTop(10));

            addFooter(document);
        }
    }

    private static Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    private static Cell createContentCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(5)
                .setTextAlignment(TextAlignment.LEFT);
    }
}