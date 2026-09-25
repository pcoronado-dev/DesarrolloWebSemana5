package com.tecsup.service;

import com.tecsup.model.Producto;
import com.tecsup.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repo;

    public List<Producto> listar() {
        return repo.findAll();
    }

    public Producto guardar(Producto p) {
        return repo.save(p);
    }

    public Producto obtener(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    // Actualizar un producto existente (auditado por AuditoriaAspect)
    public Producto actualizar(Long id, Producto datos) {
        Producto existente = repo.findById(id).orElse(null);

        if (existente == null) {
            throw new RuntimeException("No se encontró producto con ID: " + id);
        }

        existente.setNombre(datos.getNombre());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        existente.setCategoria(datos.getCategoria());

        return repo.save(existente);
    }

    // Busqueda por nombre (Actividad del laboratorio)
    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return repo.findAll();
        }
        return repo.findByNombreContainingIgnoreCase(nombre.trim());
    }
}