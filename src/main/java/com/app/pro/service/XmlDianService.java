package com.app.pro.service;

import com.app.pro.entity.ConfiguracionDian;
import com.app.pro.entity.DocumentoVenta;
import com.app.pro.entity.DetalleVenta;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class XmlDianService {

    /**
     * Construye la estructura XML oficial UBL 2.1 exigida por la DIAN
     */
    public String generarXmlUbl21(DocumentoVenta venta, ConfiguracionDian config) {
        StringBuilder xml = new StringBuilder();

        DateTimeFormatter fechaFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter horaFormat = DateTimeFormatter.ofPattern("HH:mm:ss-05:00");

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
                .append("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\"\n")
                .append("         xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"\n")
                .append("         xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\"\n")
                .append("         xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\">\n")

                // Encabezado de la Factura
                .append("    <cbc:UBLVersionID>UBL 2.1</cbc:UBLVersionID>\n")
                .append("    <cbc:CustomizationID>10</cbc:CustomizationID>\n")
                .append("    <cbc:ProfileExecutionID>1</cbc:ProfileExecutionID>\n") // 1: Producción, 2: Pruebas
                .append("    <cbc:ID>").append(venta.getConsecutivo()).append("</cbc:ID>\n")
                .append("    <cbc:UUID schemeName=\"CUFE-SHA384\">").append(venta.getCufe()).append("</cbc:UUID>\n")
                .append("    <cbc:IssueDate>").append(venta.getFechaEmision().format(fechaFormat)).append("</cbc:IssueDate>\n")
                .append("    <cbc:IssueTime>").append(venta.getFechaEmision().format(horaFormat)).append("</cbc:IssueTime>\n")
                .append("    <cbc:InvoiceTypeCode>01</cbc:InvoiceTypeCode>\n") // 01: Factura de Venta
                .append("    <cbc:DocumentCurrencyCode>COP</cbc:DocumentCurrencyCode>\n")

                // Emisor (Tu Empresa)
                .append("    <cac:AccountingSupplierParty>\n")
                .append("        <cac:Party>\n")
                .append("            <cac:PartyTaxScheme>\n")
                .append("                <cbc:RegistrationName>Empresa SaaS</cbc:RegistrationName>\n")
                .append("                <cbc:CompanyID schemeAgencyID=\"195\" schemeID=\"4\">").append(config.getNitEmpresa()).append("</cbc:CompanyID>\n")
                .append("            </cac:PartyTaxScheme>\n")
                .append("        </cac:Party>\n")
                .append("    </cac:AccountingSupplierParty>\n")

                // Adquirente (Cliente)
                .append("    <cac:AccountingCustomerParty>\n")
                .append("        <cac:Party>\n")
                .append("            <cac:PartyTaxScheme>\n")
                .append("                <cbc:RegistrationName>").append(venta.getCliente().getNombreCompleto()).append("</cbc:RegistrationName>\n")
                .append("                <cbc:CompanyID schemeAgencyID=\"195\">").append(venta.getCliente().getNumeroDocumento()).append("</cbc:CompanyID>\n")
                .append("            </cac:PartyTaxScheme>\n")
                .append("        </cac:Party>\n")
                .append("    </cac:AccountingCustomerParty>\n")

                // Totales Monetarios
                .append("    <cac:LegalMonetaryTotal>\n")
                .append("        <cbc:LineExtensionAmount currencyID=\"COP\">").append(venta.getSubtotalGobal()).append("</cbc:LineExtensionAmount>\n")
                .append("        <cbc:TaxExclusiveAmount currencyID=\"COP\">").append(venta.getTotalIva()).append("</cbc:TaxExclusiveAmount>\n")
                .append("        <cbc:PayableAmount currencyID=\"COP\">").append(venta.getTotalFinal()).append("</cbc:PayableAmount>\n")
                .append("    </cac:LegalMonetaryTotal>\n");

        // Líneas de Detalle de Productos
        int contadorLinea = 1;
        for (DetalleVenta detalle : venta.getDetalles()) {
            xml.append("    <cac:InvoiceLine>\n")
                    .append("        <cbc:ID>").append(contadorLinea++).append("</cbc:ID>\n")
                    .append("        <cbc:InvoicedQuantity unitCode=\"EA\">").append(detalle.getCantidad()).append("</cbc:InvoicedQuantity>\n")
                    .append("        <cbc:LineExtensionAmount currencyID=\"COP\">").append(detalle.getSubtotalNeto()).append("</cbc:LineExtensionAmount>\n")
                    .append("        <cac:Item>\n")
                    .append("            <cbc:Description>").append(detalle.getProducto().getNombre()).append("</cbc:Description>\n")
                    .append("        </cac:Item>\n")
                    .append("        <cac:Price>\n")
                    .append("            <cbc:PriceAmount currencyID=\"COP\">").append(detalle.getPrecioUnitario()).append("</cbc:PriceAmount>\n")
                    .append("        </cac:Price>\n")
                    .append("    </cac:InvoiceLine>\n");
        }

        xml.append("</Invoice>");

        return xml.toString();
    }
}