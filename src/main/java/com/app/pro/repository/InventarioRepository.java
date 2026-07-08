package com.app.pro.repository;

import com.app.pro.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, UUID> {

    // 1. Trae todo el inventario de una empresa (Para las alertas de stock)
    List<Inventario> findByIdEmpresaTenant(String idEmpresaTenant);

    // 2. Busca un producto específico de una empresa (Para descontar en la venta)
    // Usamos @Query para navegar por la relación del producto de forma segura
    @Query("SELECT i FROM Inventario i WHERE i.producto.idproducto = :idProducto AND i.idEmpresaTenant = :idEmpresa")
    Optional<Inventario> findByIdProductoAndIdEmpresaTenant(@Param("idProducto") UUID idProducto, @Param("idEmpresa") String idEmpresa);
}