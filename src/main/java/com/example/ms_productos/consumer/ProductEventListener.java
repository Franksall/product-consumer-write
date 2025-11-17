package com.example.ms_productos.consumer;

import com.example.ms_productos.model.Producto;
import com.example.ms_productos.repository.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductEventListener {

    @Autowired
    private ProductoRepository productRepository;

    private static final String TOPIC_NAME = "topic-productos-write";


    @KafkaListener(topics = TOPIC_NAME, groupId = "product-consumer-group")
    @Transactional //  operaciones de BD sean seguras
    public void handleProductWrite(Producto producto) {

        System.out.println("==> (CONSUMER) RECIBIDO: " + producto.toString());

        try {
            //  CASO 1: EVENTO DE "ELIMINAR"
            if ("DELETE_EVENT".equals(producto.getNombre())) {

                System.out.println("==> (CONSUMER) Procesando DELETE para ID: " + producto.getId());
                productRepository.deleteById(producto.getId());
                System.out.println("==> (CONSUMER) Producto eliminado.");

                //  CASO 2: EVENTO DE "ACTUALIZAR STOCK"
            } else if ("STOCK_UPDATE_EVENT".equals(producto.getNombre())) {

                System.out.println("==> (CONSUMER) Procesando STOCK UPDATE para ID: " + producto.getId());
                // Llamamos al metodo nativo que acabamos de crear en el repositorio
                productRepository.actualizarStockNativo(producto.getId(), producto.getStock());
                System.out.println("==> (CONSUMER) Stock actualizado.");

                //  CASO 3: ES UN "CREAR" O "ACTUALIZAR" NORMAL
            } else {

                // Si el ID es nulo, es un producto NUEVO
                if (producto.getId() == null) {
                    System.out.println("==> (CONSUMER) Procesando CREATE para: " + producto.getNombre());
                    // Replicamos la lógica de negocio (poner activo y fecha)
                    producto.setActivo(true);
                    producto.setFechaCreacion(java.time.LocalDateTime.now());
                    productRepository.save(producto);
                    System.out.println("==> (CONSUMER) Producto NUEVO guardado.");

                    // Si el ID NO es nulo, es una ACTUALIZACIÓN
                } else {
                    System.out.println("==> (CONSUMER) Procesando UPDATE para ID: " + producto.getId());

                    productRepository.findById(producto.getId()).ifPresent(existingProducto -> {
                        existingProducto.setNombre(producto.getNombre());
                        existingProducto.setDescripcion(producto.getDescripcion());
                        existingProducto.setPrecio(producto.getPrecio());
                        existingProducto.setStock(producto.getStock());
                        existingProducto.setActivo(producto.getActivo());

                        productRepository.save(existingProducto);
                        System.out.println("==> (CONSUMER) Producto existente ACTUALIZADO.");
                    });
                }
            }

        } catch (Exception e) {
            System.err.println("==> (CONSUMER) ERROR FATAL al procesar evento: " + e.getMessage());
        }
    }

}