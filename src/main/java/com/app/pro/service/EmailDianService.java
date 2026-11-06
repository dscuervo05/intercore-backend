package com.app.pro.service;

import com.app.pro.entity.DocumentoVenta;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailDianService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private PdfFacturaService pdfFacturaService;

    @Autowired
    private AttachedDocumentService attachedDocumentService;

    @Autowired
    private ConfiguracionDianService configuracionDianService;

    public void enviarFacturaPorCorreo(DocumentoVenta venta, String idEmpresaTenant, String correoDestinatario) {
        if (mailSender == null) {
            throw new IllegalStateException("El servicio de correo JavaMailSender no está configurado en application.properties.");
        }

        try {
            var config = configuracionDianService.obtenerConfiguracion(idEmpresaTenant);

            // Pasar la entidad venta directamente al generador de PDF
            byte[] pdfBytes = pdfFacturaService.generarPdfFactura(venta, idEmpresaTenant);

            // Generación segura del XML (puede retornar null si no hay referencia)
            String xmlAttached = null;
            try {
                xmlAttached = attachedDocumentService.generarXmlAttachedDocument(venta, config);
            } catch (Exception e) {
                // Si la factura no tiene XML previo, se omite el adjunto XML sin detener el envío del PDF
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(correoDestinatario);
            helper.setSubject("Factura Electrónica No. " + venta.getConsecutivo() + " - EMPRESA FACTURADORA S.A.S.");

            String mensajeHtml = "<h3>Estimado(a) " + venta.getCliente().getNombreCompleto() + ",</h3>"
                    + "<p>Adjunto a este correo encontrará su Factura Electrónica de Venta No. <b>"
                    + venta.getConsecutivo() + "</b> en formato PDF y su contenedor XML.</p>"
                    + "<p>CUFE: <code>" + (venta.getCufe() != null ? venta.getCufe() : "N/A") + "</code></p>"
                    + "<br/><p>Gracias por su compra.</p>";

            helper.setText(mensajeHtml, true);

            // 1. Adjuntar PDF
            helper.addAttachment("Factura-" + venta.getConsecutivo() + ".pdf", new ByteArrayResource(pdfBytes));

            // 2. Adjuntar XML Contenedor sólo si existe y no está vacío (Protección defensiva contra NPE)
            if (xmlAttached != null && !xmlAttached.isBlank()) {
                helper.addAttachment("AttachedDocument-" + venta.getConsecutivo() + ".xml",
                        new ByteArrayResource(xmlAttached.getBytes(StandardCharsets.UTF_8)));
            }

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo electrónico de la factura: " + e.getMessage(), e);
        }
    }
}