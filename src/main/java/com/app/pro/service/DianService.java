package com.app.pro.service;

import com.app.pro.config.DianSoapClient;
import com.app.pro.dian.wsdl.DianResponse;
import com.app.pro.dian.wsdl.GetStatusResponse;
import com.app.pro.dian.wsdl.SendTestSetAsyncResponse;
import com.app.pro.dian.wsdl.UploadDocumentResponse;
import com.app.pro.entity.ConfiguracionDian;
import com.app.pro.entity.DocumentoVenta;
import com.app.pro.repository.DocumentoVentaRepository;
import com.app.pro.util.DianCryptoUtil;
import com.app.pro.util.DianZipUtil;
import jakarta.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class DianService {

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
    public DocumentoVenta procesarYTransmitirFactura(UUID idVenta, String idEmpresaTenant) {

        // 1. Obtener factura y configuración del Tenant
        DocumentoVenta venta = documentoVentaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura especificada."));

        if (!venta.getIdEmpresaTenant().equals(idEmpresaTenant)) {
            throw new SecurityException("No tiene permisos para acceder a esta factura.");
        }

        ConfiguracionDian config = configuracionDianService.obtenerConfiguracion(idEmpresaTenant);

        // 2. Generación de CUFE (SHA-384)
        String cadenaCufe = construirCadenaCufe(venta, config);
        String cufeGenerado = dianCryptoUtil.generarHashSha384(cadenaCufe);
        venta.setCufe(cufeGenerado);

        // 3. Generación del XML UBL 2.1
        String xmlUblSinFirmar = xmlDianService.generarXmlUbl21(venta, config);

        // 4. Firma Digital (XAdES)
        String xmlFirmado = xmlUblSinFirmar;
        if (config.getRutaCertificado() != null && !config.getRutaCertificado().isEmpty()) {
            try {
                xmlFirmado = firmaDigitalService.firmarXml(
                        xmlUblSinFirmar,
                        config.getRutaCertificado(),
                        config.getClaveCertificado()
                );
            } catch (Exception e) {
                throw new RuntimeException("Error durante la firma digital: " + e.getMessage(), e);
            }
        }
        venta.setXmlReferencia(xmlFirmado);

        // 5. Empaquetado a ZIP y conversión a Base64
        String nombreZip = "z" + config.getNitEmpresa() + venta.getConsecutivo();
        String base64Zip;
        try {
            base64Zip = dianZipUtil.comprimirXmlABase64(xmlFirmado, nombreZip);
        } catch (Exception e) {
            throw new RuntimeException("Error al empaquetar el XML en ZIP: " + e.getMessage(), e);
        }

        // 6. Transmisión SOAP a los servidores de la DIAN
        try {
            SendTestSetAsyncResponse respuestaSoap = dianSoapClient.enviarSetDePruebas(
                    nombreZip,
                    base64Zip,
                    config.getTestSetId()
            );

            // 7. Evaluar respuesta desenvolviendo el ZipKey de UploadDocumentResponse
            if (respuestaSoap != null && respuestaSoap.getSendTestSetAsyncResult() != null) {
                UploadDocumentResponse uploadDocResult = respuestaSoap.getSendTestSetAsyncResult().getValue();

                if (uploadDocResult != null) {
                    // Extraer el zipKey devuelto por la DIAN
                    String zipKey = (uploadDocResult.getZipKey() != null)
                            ? uploadDocResult.getZipKey().getValue()
                            : null;

                    if (zipKey != null && !zipKey.trim().isEmpty()) {
                        // La DIAN recibió y procesó el ZIP exitosamente en el set de pruebas
                        venta.setEstadoDian("ACEPTADA");
                    } else {
                        venta.setEstadoDian("RECHAZADA");
                    }
                } else {
                    venta.setEstadoDian("PROCESANDO");
                }
            } else {
                venta.setEstadoDian("PROCESANDO");
            }

        } catch (Exception e) {
            venta.setEstadoDian("ERROR_TRANSMISION");
            throw new RuntimeException("Error al transmitir a los servidores de la DIAN: " + e.getMessage(), e);
        }

        return documentoVentaRepository.save(venta);
    }

    @Transactional
    public DocumentoVenta consultarEstadoDian(UUID idVenta, String trackId, String idEmpresaTenant) {
        DocumentoVenta venta = documentoVentaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura especificada."));

        if (!venta.getIdEmpresaTenant().equals(idEmpresaTenant)) {
            throw new SecurityException("No tiene permisos para acceder a esta factura.");
        }

        try {
            GetStatusResponse respuestaSoap = dianSoapClient.consultaEstadoDocumento(trackId);

            if (respuestaSoap != null && respuestaSoap.getGetStatusResult() != null) {
                DianResponse dianResponse = respuestaSoap.getGetStatusResult().getValue();

                if (dianResponse != null) {
                    boolean esValida = Boolean.TRUE.equals(dianResponse.isIsValid());
                    String codigoEstado = (dianResponse.getStatusCode() != null)
                            ? dianResponse.getStatusCode().getValue()
                            : "";

                    if (esValida || "00".equals(codigoEstado)) {
                        venta.setEstadoDian("ACEPTADA");
                    } else if ("09".equals(codigoEstado)) {
                        venta.setEstadoDian("EN_PROCESO");
                    } else {
                        venta.setEstadoDian("RECHAZADA");
                    }

                    if (dianResponse.getXmlBase64Bytes() != null && dianResponse.getXmlBase64Bytes().getValue() != null) {
                        byte[] xmlBytes = dianResponse.getXmlBase64Bytes().getValue();
                        String applicationResponseXml = new String(xmlBytes, java.nio.charset.StandardCharsets.UTF_8);
                        venta.setXmlReferencia(applicationResponseXml);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar el estado en los servidores de la DIAN: " + e.getMessage(), e);
        }

        return documentoVentaRepository.save(venta);
    }

    private String construirCadenaCufe(DocumentoVenta venta, ConfiguracionDian config) {
        DateTimeFormatter fechaFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter horaFormat = DateTimeFormatter.ofPattern("HH:mm:ss-05:00");

        return String.format("%s%s%s%.2f01%.2f020.00030.00%.2f%s%s%s%s",
                venta.getConsecutivo(),
                venta.getFechaEmision().format(fechaFormat),
                venta.getFechaEmision().format(horaFormat),
                venta.getSubtotalGobal(),
                venta.getTotalIva(),
                venta.getTotalFinal(),
                config.getNitEmpresa(),
                venta.getCliente().getNumeroDocumento(),
                config.getClaveTecnica(),
                "2" // 1: Producción, 2: Pruebas/Habilitación
        );
    }
}