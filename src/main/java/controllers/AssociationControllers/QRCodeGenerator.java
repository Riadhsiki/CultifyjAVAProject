package controllers.associationcontrollers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class QRCodeGenerator {

    /**
     * Génère une image de code QR JavaFX à partir d'une URL
     *
     * @param url L'URL à encoder dans le code QR
     * @param width Largeur de l'image du code QR
     * @param height Hauteur de l'image du code QR
     * @return Une instance de javafx.scene.image.Image contenant le code QR
     */
    public static Image generateQRCodeImage(String url, int width, int height) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);
            byte[] imageData = outputStream.toByteArray();

            return new Image(new ByteArrayInputStream(imageData));
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}