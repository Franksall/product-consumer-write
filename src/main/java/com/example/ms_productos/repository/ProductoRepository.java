package com.example.ms_productos.repository;

import com.example.ms_productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Llama al procedimiento almacenado "actualizar_stock" de PostgreSQL.
     * Usamos @Modifying porque es una operación de escritura (UPDATE).
     * Usamos nativeQuery = true para ejecutar SQL nativo.
     */
    @Modifying
    @Query(value = "SELECT actualizar_stock(:productoId, :cantidad)", nativeQuery = true)
    void actualizarStockNativo(@Param("productoId") Long productoId, @Param("cantidad") Integer cantidad);


}