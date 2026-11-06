package com.app.pro.service;

import com.app.pro.entity.ConfiguracionDian;
import com.app.pro.entity.DocumentoVenta;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Service
public class AttachedDocumentService {

    public String generarXmlAttachedDocument(DocumentoVenta venta, ConfiguracionDian config) {
        DateTimeFormatter fechaFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter horaFormat = DateTimeFormatter.ofPattern("HH:mm:ss-05:00");

        String xmlFacturaBase64 = Base64.getEncoder().encodeToString(venta.getXmlReferencia().getBytes(StandardCharsets.UTF_8));

        // Si no se cuenta con el ApplicationResponse individual, se genera un contenedor simplificado
        String applicationResponseBase64 = xmlFacturaBase64;

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xml.append("<AttachedDocument xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:AttachedDocument-2\" ")
                .append("xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" ")
                .append("xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">\n");

        xml.append("  <cbc:UBLVersionID>UBL 2.1</cbc:UBLVersionID>\n");
        xml.append("  <cbc:CustomizationID>20</cbc:CustomizationID>\n");
        xml.append("  <cbc:ProfileExecutionID>2</cbc:ProfileExecutionID>\n");
        xml.append("  <cbc:ID>AT-").append(venta.getConsecutivo()).append("</cbc:ID>\n");
        xml.append("  <cbc:IssueDate>").append(venta.getFechaEmision().format(fechaFormat)).append("</cbc:IssueDate>\n");
        xml.append("  <cbc:IssueTime>").append(venta.getFechaEmision().format(horaFormat)).append("</cbc:IssueTime>\n");
        xml.append("  <cbc:DocumentTypeCode>ContenedorDeFacturaElectronica</cbc:DocumentTypeCode>\n");
        xml.append("  <cbc:ParentDocumentID>").append(venta.getConsecutivo()).append("</cbc:ParentDocumentID>\n");

        // Emisor del contenedor
        xml.append("  <cac:SenderParty>\n");
        xml.append("    <cac:PartyTaxScheme>\n");
        xml.append("      <cbc:RegistrationName>EMPRESA FACTURADORA S.A.S.</cbc:RegistrationName>\n");
        xml.append("      <cbc:CompanyID schemeID=\"4\" schemeName=\"31\">").append(config.getNitEmpresa()).append("</cbc:CompanyID>\n");
        xml.append("      <cac:TaxScheme><cbc:ID>01</cbc:ID><cbc:Name>IVA</cbc:Name></cac:TaxScheme>\n");
        xml.append("    </cac:PartyTaxScheme>\n");
        xml.append("  </cac:SenderParty>\n");

        // Receptor del contenedor
        xml.append("  <cac:ReceiverParty>\n");
        xml.append("    <cac:PartyTaxScheme>\n");
        xml.append("      <cbc:RegistrationName>").append(venta.getCliente().getNombreCompleto()).append("</cbc:RegistrationName>\n");
        xml.append("      <cbc:CompanyID schemeID=\"3\" schemeName=\"13\">").append(venta.getCliente().getNumeroDocumento()).append("</cbc:CompanyID>\n");
        xml.append("      <cac:TaxScheme><cbc:ID>01</cbc:ID><cbc:Name>IVA</cbc:Name></cac:TaxScheme>\n");
        xml.append("    </cac:PartyTaxScheme>\n");
        xml.append("  </cac:ReceiverParty>\n");

        // Adjunto de Factura Electrónica en Base64
        xml.append("  <cac:Attachment>\n");
        xml.append("    <cac:ExternalReference>\n");
        xml.append("      <cbc:MimeCode>text/xml</cbc:MimeCode>\n");
        xml.append("      <cbc:EncodingCode>UTF-8</cbc:EncodingCode>\n");
        xml.append("      <cbc:Description>").append(xmlFacturaBase64).append("</cbc:Description>\n");
        xml.append("    </cac:ExternalReference>\n");
        xml.append("  </cac:Attachment>\n");

        // Adjunto de Respuesta de la DIAN en Base64
        xml.append("  <cac:ParentDocumentLineReference>\n");
        xml.append("    <cbc:LineID>1</cbc:LineID>\n");
        xml.append("    <cac:DocumentReference>\n");
        xml.append("      <cbc:ID>").append(venta.getConsecutivo()).append("</cbc:ID>\n");
        xml.append("      <cbc:UUID schemeName=\"CUFE-SHA384\">").append(venta.getCufe()).append("</cbc:UUID>\n");
        xml.append("      <cac:Attachment>\n");
        xml.append("        <cac:ExternalReference>\n");
        xml.append("          <cbc:MimeCode>text/xml</cbc:MimeCode>\n");
        xml.append("          <cbc:EncodingCode>UTF-8</cbc:EncodingCode>\n");
        xml.append("          <cbc:Description>").append(applicationResponseBase64).append("</cbc:Description>\n");
        xml.append("        </cac:ExternalReference>\n");
        xml.append("      </cac:Attachment>\n");
        xml.append("    </cac:DocumentReference>\n");
        xml.append("  </cac:ParentDocumentLineReference>\n");

        xml.append("</AttachedDocument>");
        return xml.toString();
    }
}