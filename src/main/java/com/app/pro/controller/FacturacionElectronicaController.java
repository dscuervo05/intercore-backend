package com.app.pro.controller;

import com.app.pro.entity.DocumentoVenta;
import com.app.pro.repository.DocumentoVentaRepository;
import com.app.pro.security.JwtUtil;
import com.app.pro.service.DianService;
import com.app.pro.service.EmailDianService;
import com.app.pro.service.NotaCreditoService;
import com.app.pro.service.PdfFacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/facturacion-electronica")
public class FacturacionElectronicaController {

    @Autowired
    private DianService dianService;

    @Autowired
    private PdfFacturaService pdfFacturaService;

    @Autowired
    private NotaCreditoService notaCreditoService;

    @Autowired
    private EmailDianService emailDianService;

    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/{idVenta}/transmitir")
    public ResponseEntity<?> transmitirFactura(
            @PathVariable UUID idVenta,
            @RequestHeader("Authorization") String tokenHeader) {

        try {
            String token = tokenHeader.substring(7);
            String idEmpresaTenant = jwtUtil.extractIdEmpresa(token);

            DocumentoVenta ventaProcesada = dianService.procesarYTransmitirFactura(idVenta, idEmpresaTenant);
            return ResponseEntity.ok(ventaProcesada);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error durante la emisión: " + e.getMessage());
        }
    }

    @GetMapping("/{idVenta}/estado/{trackId}")
    public ResponseEntity<?> consultarEstadoDian(
            @PathVariable UUID idVenta,
            @PathVariable String trackId,
            @RequestHeader("Authorization") String tokenHeader) {

        try {
            String token = tokenHeader.substring(7);
            String idEmpresaTenant = jwtUtil.extractIdEmpresa(token);

            DocumentoVenta ventaActualizada = dianService.consultarEstadoDian(idVenta, trackId, idEmpresaTenant);
            return ResponseEntity.ok(ventaActualizada);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al consultar estado DIAN: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{idVenta}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> obtenerPdfFactura(
            @PathVariable UUID idVenta,
            @RequestHeader("Authorization") String tokenHeader) {

        String token = tokenHeader.substring(7);
        String idEmpresaTenant = jwtUtil.extractIdEmpresa(token);

        byte[] pdfBytes = pdfFacturaService.generarPdfFactura(idVenta, idEmpresaTenant);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=factura-" + idVenta + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/{idVenta}/anular")
    public ResponseEntity<?> anularFacturaConNotaCredito(
            @PathVariable UUID idVenta,
            @RequestHeader("Authorization") String tokenHeader) {

        try {
            String token = tokenHeader.substring(7);
            String idEmpresaTenant = jwtUtil.extractIdEmpresa(token);

            DocumentoVenta ventaAnulada = notaCreditoService.emitirNotaCreditoAnulacion(idVenta, idEmpresaTenant);
            return ResponseEntity.ok(ventaAnulada);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al generar la Nota Crédito: " + e.getMessage());
        }
    }

    @PostMapping("/{idVenta}/enviar-correo")
    public ResponseEntity<?> enviarFacturaPorCorreo(
            @PathVariable UUID idVenta,
            @RequestParam String correoDestinatario,
            @RequestHeader("Authorization") String tokenHeader) {

        try {
            String token = tokenHeader.substring(7);
            String idEmpresaTenant = jwtUtil.extractIdEmpresa(token);

            DocumentoVenta venta = documentoVentaRepository.findById(idVenta)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura."));

            emailDianService.enviarFacturaPorCorreo(venta, idEmpresaTenant, correoDestinatario);
            return ResponseEntity.ok("Factura enviada exitosamente a: " + correoDestinatario);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al enviar el correo: " + e.getMessage());
        }
    }
}