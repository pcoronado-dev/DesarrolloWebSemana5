package com.tecsup.repository;

import com.tecsup.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Busqueda por nombre (Actividad del laboratorio)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}