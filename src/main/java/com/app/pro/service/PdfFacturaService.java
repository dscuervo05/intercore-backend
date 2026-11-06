package com.app.pro.service;

import com.app.pro.entity.ConfiguracionDian;
import com.app.pro.entity.DetalleVenta;
import com.app.pro.entity.DocumentoVenta;
import com.app.pro.repository.DocumentoVentaRepository;
import com.app.pro.util.DianQrUtil;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PdfFacturaService {

    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;

    @Autowired
    private ConfiguracionDianService configuracionDianService;

    @Autowired
    private DianQrUtil dianQrUtil;

    public byte[] generarPdfFactura(UUID idVenta, String idEmpresaTenant) {
        DocumentoVenta venta = documentoVentaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura especificada."));
        return generarPdfFactura(venta, idEmpresaTenant);
    }

    public byte[] generarPdfFactura(DocumentoVenta venta, String idEmpresaTenant) {
        if (!venta.getIdEmpresaTenant().equals(idEmpresaTenant)) {
            throw new SecurityException("No tiene permisos para acceder a esta factura.");
        }

        ConfiguracionDian config = configuracionDianService.obtenerConfiguracion(idEmpresaTenant);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fuentes
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font subTituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font textoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font textoNormal = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font cufeFont = FontFactory.getFont(FontFactory.COURIER, 7, Color.DARK_GRAY);

            // 1. Encabezado Emisor
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60f, 40f});

            PdfPCell cellEmisor = new PdfPCell();
            cellEmisor.setBorder(Rectangle.NO_BORDER);
            cellEmisor.addElement(new Paragraph("EMPRESA FACTURADORA S.A.S.", tituloFont));
            cellEmisor.addElement(new Paragraph("NIT: " + config.getNitEmpresa(), subTituloFont));
            cellEmisor.addElement(new Paragraph("Responsable de IVA", textoNormal));
            headerTable.addCell(cellEmisor);

            PdfPCell cellFactura = new PdfPCell();
            cellFactura.setBorder(Rectangle.BOX);
            cellFactura.setBackgroundColor(new Color(245, 245, 245));
            cellFactura.setPadding(8);
            cellFactura.addElement(new Paragraph("FACTURA ELECTRÓNICA DE VENTA", subTituloFont));
            cellFactura.addElement(new Paragraph("No. " + venta.getConsecutivo(), tituloFont));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            cellFactura.addElement(new Paragraph("Fecha: " + venta.getFechaEmision().format(formatter), textoNormal));
            headerTable.addCell(cellFactura);

            document.add(headerTable);
            document.add(Chunk.NEWLINE);

            // 2. Información Adquirente
            PdfPTable clienteTable = new PdfPTable(2);
            clienteTable.setWidthPercentage(100);
            clienteTable.setWidths(new float[]{50f, 50f});

            PdfPCell cellCli1 = new PdfPCell();
            cellCli1.setPadding(5);
            cellCli1.addElement(new Paragraph("ADQUIRENTE (CLIENTE)", textoBold));
            cellCli1.addElement(new Paragraph("Nombre: " + venta.getCliente().getNombreCompleto(), textoNormal));
            cellCli1.addElement(new Paragraph("NIT/Cédula: " + venta.getCliente().getNumeroDocumento(), textoNormal));
            clienteTable.addCell(cellCli1);

            PdfPCell cellCli2 = new PdfPCell();
            cellCli2.setPadding(5);
            cellCli2.addElement(new Paragraph("DETALLES DE PAGO", textoBold));
            cellCli2.addElement(new Paragraph("Forma de Pago: Contado", textoNormal));
            cellCli2.addElement(new Paragraph("Medio de Pago: Efectivo / Transferencia", textoNormal));
            clienteTable.addCell(cellCli2);

            document.add(clienteTable);
            document.add(Chunk.NEWLINE);

            // 3. Tabla de Productos
            PdfPTable itemsTable = new PdfPTable(5);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{40f, 12f, 16f, 16f, 16f});

            String[] headers = {"Descripción", "Cant.", "Val. Unitario", "IVA", "Total"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Paragraph(h, textoBold));
                c.setBackgroundColor(new Color(220, 220, 220));
                c.setPadding(5);
                itemsTable.addCell(c);
            }

            if (venta.getDetalles() != null) {
                for (DetalleVenta d : venta.getDetalles()) {
                    String nombreProducto = (d.getProducto() != null) ? d.getProducto().getNombre() : "Producto";

                    BigDecimal cantidad = BigDecimal.valueOf(d.getCantidad() != null ? d.getCantidad() : 0);
                    BigDecimal precioUnitario = d.getPrecioUnitario() != null ? d.getPrecioUnitario() : BigDecimal.ZERO;
                    BigDecimal valorIva = d.getValorIva() != null ? d.getValorIva() : BigDecimal.ZERO;
                    BigDecimal subtotalItem = cantidad.multiply(precioUnitario);

                    itemsTable.addCell(new PdfPCell(new Paragraph(nombreProducto, textoNormal)));
                    itemsTable.addCell(new PdfPCell(new Paragraph(String.valueOf(d.getCantidad()), textoNormal)));
                    itemsTable.addCell(new PdfPCell(new Paragraph(String.format("$%.2f", precioUnitario), textoNormal)));
                    itemsTable.addCell(new PdfPCell(new Paragraph(String.format("$%.2f", valorIva), textoNormal)));
                    itemsTable.addCell(new PdfPCell(new Paragraph(String.format("$%.2f", subtotalItem), textoNormal)));
                }
            }

            document.add(itemsTable);
            document.add(Chunk.NEWLINE);

            // 4. Totales y QR
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{40f, 60f});

            byte[] qrBytes = dianQrUtil.generarQrFactura(venta, config, 130, 130);
            Image qrImg = Image.getInstance(qrBytes);
            PdfPCell cellQr = new PdfPCell(qrImg);
            cellQr.setBorder(Rectangle.NO_BORDER);
            cellQr.setHorizontalAlignment(Element.ALIGN_CENTER);
            footerTable.addCell(cellQr);

            PdfPTable totalesInner = new PdfPTable(2);
            totalesInner.setWidthPercentage(100);

            totalesInner.addCell(new PdfPCell(new Paragraph("Subtotal:", textoBold)));
            totalesInner.addCell(new PdfPCell(new Paragraph(String.format("$%.2f", venta.getSubtotalGobal()), textoNormal)));

            totalesInner.addCell(new PdfPCell(new Paragraph("Total IVA:", textoBold)));
            totalesInner.addCell(new PdfPCell(new Paragraph(String.format("$%.2f", venta.getTotalIva()), textoNormal)));

            totalesInner.addCell(new PdfPCell(new Paragraph("TOTAL FACTURA:", tituloFont)));
            totalesInner.addCell(new PdfPCell(new Paragraph(String.format("$%.2f", venta.getTotalFinal()), tituloFont)));

            PdfPCell cellTotales = new PdfPCell(totalesInner);
            cellTotales.setBorder(Rectangle.NO_BORDER);
            footerTable.addCell(cellTotales);

            document.add(footerTable);
            document.add(Chunk.NEWLINE);

            // 5. CUFE
            Paragraph cufePara = new Paragraph();
            cufePara.add(new Chunk("CUFE: ", textoBold));
            cufePara.add(new Chunk(venta.getCufe() != null ? venta.getCufe() : "PENDIENTE DE GENERACION", cufeFont));
            document.add(cufePara);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de la factura: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }
}