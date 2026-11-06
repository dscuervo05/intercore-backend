package com.app.pro.service;

import com.app.pro.config.DianSoapClient;
import com.app.pro.dian.wsdl.DianResponse;
import com.app.pro.dian.wsdl.SendTestSetAsyncResponse;
import com.app.pro.dian.wsdl.UploadDocumentResponse;
import com.app.pro.entity.ConfiguracionDian;
import com.app.pro.entity.DocumentoVenta;
import com.app.pro.repository.DocumentoVentaRepository;
import com.app.pro.util.DianCryptoUtil;
import com.app.pro.util.DianZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class NotaCreditoService {

    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;

    @Autowired
    private ConfiguracionDianService configuracionDianService;

    @Autowired
    private XmlDianService xmlDianService;

    @Autowired
    private FirmaDigitalService firmaDigitalService;

    @Autowired
    private DianCryptoUtil dianCryptoUtil;

    @Autowired
    private DianZipUtil dianZipUtil;

    @Autowired
    private DianSoapClient dianSoapClient;

    @Transactional
    public DocumentoVenta emitirNotaCreditoAnulacion(UUID idVentaOriginal, String idEmpresaTenant) {

        // 1. Obtener factura original validando que esté ACEPTADA por la DIAN
        DocumentoVenta ventaOriginal = documentoVentaRepository.findById(idVentaOriginal)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura."));

        if (!ventaOriginal.getIdEmpresaTenant().equals(idEmpresaTenant)) {
            throw new SecurityException("No tiene permisos para acceder a esta factura.");
        }
        if (ventaOriginal.getCufe() == null || !"ACEPTADA".equals(ventaOriginal.getEstadoDian())) {
            throw new IllegalStateException("Solo se pueden anular facturas previamente aceptadas por la DIAN.");
        }

        ConfiguracionDian config = configuracionDianService.obtenerConfiguracion(idEmpresaTenant);

        // 2. Parámetros de la Nota Crédito (NC)
        String consecutivoNC = "NC-" + ventaOriginal.getConsecutivo(); // Prefijo de prueba para la NC
        LocalDateTime fechaEmisionNC = LocalDateTime.now();
        String motivoAnulacion = "2"; // Código 2: Anulación de factura electrónica

        // 3. Generación del CUDE (SHA-384) para la Nota Crédito
        String cadenaCude = construirCadenaCude(consecutivoNC, fechaEmisionNC, ventaOriginal, config);
        String cudeGenerado = dianCryptoUtil.generarHashSha384(cadenaCude);

        // 4. Generación del XML UBL 2.1 (CreditNote)
        // Nota: Se requiere que xmlDianService tenga un método generarXmlNotaCredito
        String xmlUblSinFirmar = xmlDianService.generarXmlNotaCredito(ventaOriginal, config, consecutivoNC, cudeGenerado, fechaEmisionNC, motivoAnulacion);

        // 5. Firma Digital (XAdES)
        String xmlFirmado = xmlUblSinFirmar;
        if (config.getRutaCertificado() != null && !config.getRutaCertificado().isEmpty()) {
            try {
                xmlFirmado = firmaDigitalService.firmarXml(
                        xmlUblSinFirmar,
                        config.getRutaCertificado(),
                        config.getClaveCertificado()
                );
            } catch (Exception e) {
                throw new RuntimeException("Error al firmar la Nota Crédito: " + e.getMessage(), e);
            }
        }

        // 6. Empaquetado a ZIP
        String nombreZip = "z" + config.getNitEmpresa() + consecutivoNC.replace("-", "");
        String base64Zip;
        try {
            base64Zip = dianZipUtil.comprimirXmlABase64(xmlFirmado, nombreZip);
        } catch (Exception e) {
            throw new RuntimeException("Error al empaquetar la Nota Crédito en ZIP: " + e.getMessage(), e);
        }

        // 7. Transmisión SOAP a la DIAN (Mismo endpoint que la factura de prueba)
        try {
            SendTestSetAsyncResponse respuestaSoap = dianSoapClient.enviarSetDePruebas(
                    nombreZip,
                    base64Zip,
                    config.getTestSetId()
            );

            if (respuestaSoap != null && respuestaSoap.getSendTestSetAsyncResult() != null) {
                UploadDocumentResponse uploadDocResult = respuestaSoap.getSendTestSetAsyncResult().getValue();

                String zipKey = (uploadDocResult != null && uploadDocResult.getZipKey() != null)
                        ? uploadDocResult.getZipKey().getValue()
                        : null;

                if (zipKey != null && !zipKey.trim().isEmpty()) {
                    ventaOriginal.setEstadoDian("ANULADA_EN_PROCESO"); // Pendiente de GetStatus
                } else {
                    ventaOriginal.setEstadoDian("ERROR_NC");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al transmitir la Nota Crédito a la DIAN: " + e.getMessage(), e);
        }

        return documentoVentaRepository.save(ventaOriginal);
    }

    private String construirCadenaCude(String consecutivoNC, LocalDateTime fechaNC, DocumentoVenta venta, ConfiguracionDian config) {
        DateTimeFormatter fechaFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter horaFormat = DateTimeFormatter.ofPattern("HH:mm:ss-05:00");

        // La cadena CUDE mantiene la misma estructura pero usa los valores de la Nota y un PIN proporcionado por DIAN.
        // En ambiente de pruebas, la Clave Técnica suele reutilizarse para el cálculo del PIN/CUDE.
        return String.format("%s%s%s%.2f01%.2f020.00030.00%.2f%s%s%s%s",
                consecutivoNC,
                fechaNC.format(fechaFormat),
                fechaNC.format(horaFormat),
                venta.getSubtotalGobal(),
                venta.getTotalIva(),
                venta.getTotalFinal(),
                config.getNitEmpresa(),
                venta.getCliente().getNumeroDocumento(),
                config.getClaveTecnica(), // Para el CUDE se usa un PIN de software
                "2" // 2: Pruebas
        );
    }
}