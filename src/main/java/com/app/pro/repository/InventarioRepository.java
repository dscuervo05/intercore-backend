package com.app.pro.repository;

import com.app.pro.entity.Inventario;
import com.app.pro.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface InventarioRepository extends JpaRepository<Inventario, UUID> {
    Optional<Inventario> findFirstByProducto(Producto  producto);
}
