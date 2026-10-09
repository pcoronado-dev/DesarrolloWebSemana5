package com.tecsup.service;

import com.tecsup.model.Categoria;
import com.tecsup.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public Categoria obtener(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    public Categoria actualizar(Long id, Categoria datos) {
        Categoria existente = categoriaRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new RuntimeException("No se encontró categoría con ID: " + id);
        }
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        return categoriaRepository.save(existente);
    }

    public void eliminar(Long id) {
        categoriaRepository.deleteById(id);
    }
}
