package com.app.pro.controller;

import com.app.pro.entity.Producto;
import com.app.pro.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    // 1. LEER TODOS (El que ya usaste)
    @GetMapping
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // 2. LEER UNO SOLO (Búsqueda por ID)
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable UUID id) {
        Optional<Producto> producto = productoRepository.findById(id);
        return producto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. CREAR NUEVO PRODUCTO
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        // Aseguramos que la fecha de creación sea la actual si no viene en el JSON
        if (producto.getCredadoEn() == null) {
            producto.setCredadoEn(LocalDateTime.now());
        }
        Producto nuevoProducto = productoRepository.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    // 4. ACTUALIZAR PRODUCTO EXISTENTE
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable UUID id, @RequestBody Producto detallesProducto) {
        Optional<Producto> productoExistente = productoRepository.findById(id);

        if (productoExistente.isPresent()) {
            Producto productoActualizado = productoExistente.get();
            
            // Actualizamos los campos
            productoActualizado.setNombre(detallesProducto.getNombre());
            productoActualizado.setDescripcion(detallesProducto.getDescripcion());
            productoActualizado.setPrecio_venta(detallesProducto.getPrecio_venta());
            productoActualizado.setCosto(detallesProducto.getCosto());
            // Agrega aquí otros campos como el código de barras si lo necesitas
            // productoActualizado.setCodigoBarrasQR(detallesProducto.getCodigoBarrasQR());

            productoRepository.save(productoActualizado);
            return ResponseEntity.ok(productoActualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. ELIMINAR PRODUCTO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable UUID id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}