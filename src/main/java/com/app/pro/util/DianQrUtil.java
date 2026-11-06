package com.app.pro.util;

import com.app.pro.entity.ConfiguracionDian;
import com.app.pro.entity.DocumentoVenta;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
public class DianQrUtil {

    public byte[] generarQrFactura(DocumentoVenta venta, ConfiguracionDian config, int ancho, int alto) {
        try {
            String textoQr = construirTextoQrDian(venta, config);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(textoQr, BarcodeFormat.QR_CODE, ancho, alto);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el código QR de la DIAN: " + e.getMessage(), e);
        }
    }

    private String construirTextoQrDian(DocumentoVenta venta, ConfiguracionDian config) {
        DateTimeFormatter fechaFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter horaFormat = DateTimeFormatter.ofPattern("HH:mm:ss-05:00");

        String baseUrl = "https://catalogo-vpfe-hab.dian.gov.co/document/searchqr?documentkey=" + venta.getCufe();

        return String.format(
                "NumFac: %s\nFecFac: %s\nHorFac: %s\nNitFac: %s\nDocAdq: %s\nValFac: %.2f\nValIva: %.2f\nValTolFac: %.2f\nCUFE: %s\nURL: %s",
                venta.getConsecutivo(),
                venta.getFechaEmision().format(fechaFormat),
                venta.getFechaEmision().format(horaFormat),
                config.getNitEmpresa(),
                venta.getCliente().getNumeroDocumento(),
                venta.getSubtotalGobal(),
                venta.getTotalIva(),
                venta.getTotalFinal(),
                venta.getCufe(),
                baseUrl
        );
    }
}