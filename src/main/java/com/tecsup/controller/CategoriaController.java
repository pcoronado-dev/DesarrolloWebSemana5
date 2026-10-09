package com.tecsup.controller;

import com.tecsup.dto.CategoriaDTO;
import com.tecsup.model.Categoria;
import com.tecsup.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listar() {
        List<CategoriaDTO> categorias = categoriaService.listar()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        Categoria categoria = categoriaService.obtener(id);
        if (categoria == null) {
            return ResponseEntity.status(404).body("Categoría no encontrada");
        }
        return ResponseEntity.ok(convertirADTO(categoria));
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> guardar(@Valid @RequestBody CategoriaDTO dto) {
        Categoria categoria = convertirAEntidad(dto);
        Categoria guardada = categoriaService.guardar(categoria);
        return ResponseEntity.status(201).body(convertirADTO(guardada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody CategoriaDTO dto) {
        if (categoriaService.obtener(id) == null) {
            return ResponseEntity.status(404).body("Categoría no existe");
        }
        Categoria datos = convertirAEntidad(dto);
        Categoria actualizada = categoriaService.actualizar(id, datos);
        return ResponseEntity.ok(convertirADTO(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Categoria categoria = categoriaService.obtener(id);
        if (categoria == null) {
            return ResponseEntity.status(404).body("No existe");
        }
        categoriaService.eliminar(id);
        return ResponseEntity.ok("Eliminada correctamente");
    }

    private CategoriaDTO convertirADTO(Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        return dto;
    }

    private Categoria convertirAEntidad(CategoriaDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setId(dto.getId());
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        return categoria;
    }
}
