package com.tecsup.controller;

import java.util.List;

import com.tecsup.dto.ProductoDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tecsup.model.Producto;
import com.tecsup.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService service;

    // GET: listar todos
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    // POST: guardar producto (DTO + validacion)
    @PostMapping
    public ResponseEntity<?> guardar(@Valid @RequestBody ProductoDTO dto) {

        Producto p = new Producto();
        p.setNombre(dto.getNombre());
        p.setPrecio(dto.getPrecio());
        p.setStock(dto.getStock());
        p.setCategoria(dto.getCategoria());

        Producto guardado = service.guardar(p);

        return ResponseEntity.status(201).body(guardado);
    }

    // GET: buscar productos por nombre (Actividad del laboratorio)
    // Ejemplo: GET /api/productos/buscar?nombre=Monitor
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorNombre(@RequestParam String nombre) {
        List<Producto> resultados = service.buscarPorNombre(nombre);

        if (resultados.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron productos con ese nombre");
        }

        return ResponseEntity.ok(resultados);
    }

    // GET por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {

        Producto p = service.obtener(id);

        if (p == null) {
            return ResponseEntity.status(404).body("Producto no encontrado");
        }

        return ResponseEntity.ok(p);
    }

    // PUT: actualizar
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody ProductoDTO dto) {

        Producto existente = service.obtener(id);

        if (existente == null) {
            return ResponseEntity.status(404).body("Producto no existe");
        }

        existente.setNombre(dto.getNombre());
        existente.setPrecio(dto.getPrecio());
        existente.setStock(dto.getStock());
        existente.setCategoria(dto.getCategoria());

        return ResponseEntity.ok(service.guardar(existente));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {

        Producto p = service.obtener(id);

        if (p == null) {
            return ResponseEntity.status(404).body("No existe");
        }

        service.eliminar(id);

        return ResponseEntity.ok("Eliminado correctamente");
    }
}