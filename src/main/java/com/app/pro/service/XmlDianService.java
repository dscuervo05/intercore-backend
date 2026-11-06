package com.app.pro.service;

import com.app.pro.entity.ConfiguracionDian;
import com.app.pro.entity.DocumentoVenta;
import com.app.pro.entity.DetalleVenta;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    public String generarXmlNotaCredito(DocumentoVenta ventaOriginal, ConfiguracionDian config,
                                        String consecutivoNC, String cude,
                                        LocalDateTime fechaNC, String conceptoCorreccion) {

        DateTimeFormatter fechaFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter horaFormat = DateTimeFormatter.ofPattern("HH:mm:ss-05:00");

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xml.append("<CreditNote xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2\" ")
                .append("xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" ")
                .append("xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" ")
                .append("xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n");

        xml.append("  <cbc:UBLVersionID>UBL 2.1</cbc:UBLVersionID>\n");
        xml.append("  <cbc:CustomizationID>20</cbc:CustomizationID>\n");
        xml.append("  <cbc:ProfileExecutionID>2</cbc:ProfileExecutionID>\n"); // 1: Prod, 2: Pruebas
        xml.append("  <cbc:ID>").append(consecutivoNC).append("</cbc:ID>\n");
        xml.append("  <cbc:UUID schemeID=\"2\" schemeName=\"CUDE-SHA384\">").append(cude).append("</cbc:UUID>\n");
        xml.append("  <cbc:IssueDate>").append(fechaNC.format(fechaFormat)).append("</cbc:IssueDate>\n");
        xml.append("  <cbc:IssueTime>").append(fechaNC.format(horaFormat)).append("</cbc:IssueTime>\n");
        xml.append("  <cbc:CreditNoteTypeCode>91</cbc:CreditNoteTypeCode>\n");
        xml.append("  <cbc:DocumentCurrencyCode>COP</cbc:DocumentCurrencyCode>\n");
        xml.append("  <cbc:LineCountNumeric>").append(ventaOriginal.getDetalles() != null ? ventaOriginal.getDetalles().size() : 0).append("</cbc:LineCountNumeric>\n");

        // Motivo de Corrección (DiscrepancyResponse)
        xml.append("  <cac:DiscrepancyResponse>\n");
        xml.append("    <cbc:ReferenceID>").append(ventaOriginal.getConsecutivo()).append("</cbc:ReferenceID>\n");
        xml.append("    <cbc:ResponseCode>").append(conceptoCorreccion).append("</cbc:ResponseCode>\n");
        xml.append("    <cbc:Description>Anulacion de factura electronica de venta</cbc:Description>\n");
        xml.append("  </cac:DiscrepancyResponse>\n");

        // Referencia a la Factura de Venta Original
        xml.append("  <cac:BillingReference>\n");
        xml.append("    <cac:InvoiceDocumentReference>\n");
        xml.append("      <cbc:ID>").append(ventaOriginal.getConsecutivo()).append("</cbc:ID>\n");
        xml.append("      <cbc:UUID schemeName=\"CUFE-SHA384\">").append(ventaOriginal.getCufe()).append("</cbc:UUID>\n");
        xml.append("      <cbc:IssueDate>").append(ventaOriginal.getFechaEmision().format(fechaFormat)).append("</cbc:IssueDate>\n");
        xml.append("    </cac:InvoiceDocumentReference>\n");
        xml.append("  </cac:BillingReference>\n");

        // Emisor (AccountingSupplierParty)
        xml.append("  <cac:AccountingSupplierParty>\n");
        xml.append("    <cbc:AdditionalAccountID>2</cbc:AdditionalAccountID>\n");
        xml.append("    <cac:Party>\n");
        xml.append("      <cac:PartyTaxScheme>\n");
        xml.append("        <cbc:RegistrationName>EMPRESA FACTURADORA S.A.S.</cbc:RegistrationName>\n");
        xml.append("        <cbc:CompanyID schemeID=\"4\" schemeName=\"31\">").append(config.getNitEmpresa()).append("</cbc:CompanyID>\n");
        xml.append("        <cac:TaxScheme><cbc:ID>01</cbc:ID><cbc:Name>IVA</cbc:Name></cac:TaxScheme>\n");
        xml.append("      </cac:PartyTaxScheme>\n");
        xml.append("    </cac:Party>\n");
        xml.append("  </cac:AccountingSupplierParty>\n");

        // Adquirente (AccountingCustomerParty)
        xml.append("  <cac:AccountingCustomerParty>\n");
        xml.append("    <cbc:AdditionalAccountID>2</cbc:AdditionalAccountID>\n");
        xml.append("    <cac:Party>\n");
        xml.append("      <cac:PartyTaxScheme>\n");
        xml.append("        <cbc:RegistrationName>").append(ventaOriginal.getCliente().getNombreCompleto()).append("</cbc:RegistrationName>\n");
        xml.append("        <cbc:CompanyID schemeID=\"3\" schemeName=\"13\">").append(ventaOriginal.getCliente().getNumeroDocumento()).append("</cbc:CompanyID>\n");
        xml.append("        <cac:TaxScheme><cbc:ID>01</cbc:ID><cbc:Name>IVA</cbc:Name></cac:TaxScheme>\n");
        xml.append("      </cac:PartyTaxScheme>\n");
        xml.append("    </cac:Party>\n");
        xml.append("  </cac:AccountingCustomerParty>\n");

        // Totales de Impuestos y Monentarios
        xml.append("  <cac:TaxTotal>\n");
        xml.append("    <cbc:TaxAmount currencyID=\"COP\">").append(String.format("%.2f", ventaOriginal.getTotalIva())).append("</cbc:TaxAmount>\n");
        xml.append("  </cac:TaxTotal>\n");

        xml.append("  <cac:LegalMonetaryTotal>\n");
        xml.append("    <cbc:LineExtensionAmount currencyID=\"COP\">").append(String.format("%.2f", ventaOriginal.getSubtotalGobal())).append("</cbc:LineExtensionAmount>\n");
        xml.append("    <cbc:TaxExclusiveAmount currencyID=\"COP\">").append(String.format("%.2f", ventaOriginal.getSubtotalGobal())).append("</cbc:TaxExclusiveAmount>\n");
        xml.append("    <cbc:TaxInclusiveAmount currencyID=\"COP\">").append(String.format("%.2f", ventaOriginal.getTotalFinal())).append("</cbc:TaxInclusiveAmount>\n");
        xml.append("    <cbc:PayableAmount currencyID=\"COP\">").append(String.format("%.2f", ventaOriginal.getTotalFinal())).append("</cbc:PayableAmount>\n");
        xml.append("  </cac:LegalMonetaryTotal>\n");

        // Detalle de Ítems (CreditNoteLine)
        if (ventaOriginal.getDetalles() != null) {
            int numeroLinea = 1;
            for (DetalleVenta d : ventaOriginal.getDetalles()) {
                BigDecimal cantidad = BigDecimal.valueOf(d.getCantidad() != null ? d.getCantidad() : 0);
                BigDecimal precioUnitario = d.getPrecioUnitario() != null ? d.getPrecioUnitario() : BigDecimal.ZERO;
                BigDecimal subtotalItem = cantidad.multiply(precioUnitario);

                xml.append("  <cac:CreditNoteLine>\n");
                xml.append("    <cbc:ID>").append(numeroLinea++).append("</cbc:ID>\n");
                xml.append("    <cbc:CreditedQuantity unitCode=\"EA\">").append(cantidad).append("</cbc:CreditedQuantity>\n");
                xml.append("    <cbc:LineExtensionAmount currencyID=\"COP\">").append(String.format("%.2f", subtotalItem)).append("</cbc:LineExtensionAmount>\n");
                xml.append("    <cac:Item>\n");
                xml.append("      <cbc:Description>").append(d.getProducto() != null ? d.getProducto().getNombre() : "Producto").append("</cbc:Description>\n");
                xml.append("    </cac:Item>\n");
                xml.append("    <cac:Price>\n");
                xml.append("      <cbc:PriceAmount currencyID=\"COP\">").append(String.format("%.2f", precioUnitario)).append("</cbc:PriceAmount>\n");
                xml.append("    </cac:Price>\n");
                xml.append("  </cac:CreditNoteLine>\n");
            }
        }

        xml.append("</CreditNote>");
        return xml.toString();
    }
}