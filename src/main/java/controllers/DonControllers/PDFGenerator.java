package controllers.DonControllers;

import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFGenerator {

    /**
     * Génère un PDF à partir d'un template HTML avec des données spécifiques
     *
     * @param donorName       Nom du donateur
     * @param donorEmail      Email du donateur
     * @param donId           ID du don
     * @param associationName Nom de l'association
     * @param amount          Montant du don
     * @return Bytes du PDF généré
     */
    public static byte[] generateDonConfirmationPDF(String donorName, String donorEmail,
                                                    int donId, String associationName,
                                                    double amount) throws IOException, DocumentException {

        // Date actuelle formattée
        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String currentYear = new SimpleDateFormat("yyyy").format(new Date());

        // Construire le contenu HTML
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\" />\n" +
                "    <title>Confirmation de Don - Cultify</title>\n" +
                "    <style>\n" +
                "        /* Style général */\n" +
                "        body {\n" +
                "            font-family: 'Arial', sans-serif;\n" +
                "            margin: 0;\n" +
                "            padding: 20px;\n" +
                "            background-color: rgb(204, 225, 228);\n" +
                "            color: #333;\n" +
                "        }\n" +
                "\n" +
                "        /* Conteneur principal */\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            background-color: #fff;\n" +
                "            padding: 30px;\n" +
                "            border-radius: 10px;\n" +
                "            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);\n" +
                "        }\n" +
                "\n" +
                "        /* En-tête */\n" +
                "        .header {\n" +
                "            text-align: center;\n" +
                "            margin-bottom: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .header h1 {\n" +
                "            color: rgb(149, 110, 73);\n" +
                "            font-size: 28px;\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "\n" +
                "        .header p {\n" +
                "            color: #777;\n" +
                "            font-size: 16px;\n" +
                "            margin: 5px 0 0;\n" +
                "        }\n" +
                "\n" +
                "        /* Informations de l'utilisateur */\n" +
                "        .user-info {\n" +
                "            margin-bottom: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .user-info p {\n" +
                "            margin: 5px 0;\n" +
                "        }\n" +
                "\n" +
                "        /* Tableau des dons */\n" +
                "        table {\n" +
                "            width: 100%;\n" +
                "            border-collapse: collapse;\n" +
                "            margin-bottom: 30px;\n" +
                "        }\n" +
                "\n" +
                "        table th, table td {\n" +
                "            padding: 10px;\n" +
                "            border: 1px solid #ddd;\n" +
                "            text-align: left;\n" +
                "        }\n" +
                "\n" +
                "        table th {\n" +
                "            background-color: rgb(149, 110, 73);\n" +
                "            color: #fff;\n" +
                "        }\n" +
                "\n" +
                "        table tr:nth-child(even) {\n" +
                "            background-color: #f9f9f9;\n" +
                "        }\n" +
                "\n" +
                "        /* Total */\n" +
                "        .total {\n" +
                "            text-align: right;\n" +
                "            font-size: 18px;\n" +
                "            font-weight: bold;\n" +
                "            margin-bottom: 30px;\n" +
                "            color: rgb(149, 110, 73);\n" +
                "        }\n" +
                "\n" +
                "        /* Pied de page */\n" +
                "        .footer {\n" +
                "            margin-top: 30px;\n" +
                "            text-align: center;\n" +
                "            color: #777;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "\n" +
                "        .footer a {\n" +
                "            color: rgb(149, 110, 73);\n" +
                "            text-decoration: none;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <!-- En-tête avec logo -->\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Confirmation de Don</h1>\n" +
                "            <p>Merci pour votre générosité !</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <!-- Informations de l'utilisateur -->\n" +
                "        <div class=\"user-info\">\n" +
                "            <p><strong>Nom du donateur:</strong> " + donorName + "</p>\n" +
                "            <p><strong>Email:</strong> " + donorEmail + "</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <!-- Tableau des dons -->\n" +
                "        <table>\n" +
                "            <thead>\n" +
                "                <tr>\n" +
                "                    <th>ID du Don</th>\n" +
                "                    <th>Association</th>\n" +
                "                    <th>Montant</th>\n" +
                "                    <th>Date</th>\n" +
                "                </tr>\n" +
                "            </thead>\n" +
                "            <tbody>\n" +
                "                <tr>\n" +
                "                    <td>" + donId + "</td>\n" +
                "                    <td>" + associationName + "</td>\n" +
                "                    <td>" + amount + " TND</td>\n" +
                "                    <td>" + currentDate + "</td>\n" +
                "                </tr>\n" +
                "            </tbody>\n" +
                "        </table>\n" +
                "\n" +
                "        <!-- Total -->\n" +
                "        <div class=\"total\">\n" +
                "            <p><strong>Total:</strong> " + amount + " TND</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <!-- Pied de page -->\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Pour toute question, contactez-nous à <a href=\"mailto:support@cultify.com\">support@cultify.com</a>.</p>\n" +
                "            <p>" + currentYear + " Cultify. Tous droits réservés.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        // Créer le PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }

    // Méthode de test pour sauvegarder un PDF généré localement
    public static void saveToFile(byte[] pdfBytes, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
            fos.write(pdfBytes);
        }
    }
}