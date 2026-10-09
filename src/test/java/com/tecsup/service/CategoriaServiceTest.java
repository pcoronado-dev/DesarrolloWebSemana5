package com.tecsup.service;

import com.tecsup.model.Categoria;
import com.tecsup.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repo;

    @InjectMocks
    private CategoriaService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListarCategorias() {
        List<Categoria> lista = List.of(
                new Categoria(1L, "Electrónicos", "Dispositivos electrónicos"),
                new Categoria(2L, "Computadoras", "Laptops y PCs")
        );
        when(repo.findAll()).thenReturn(lista);
        List<Categoria> resultado = service.listar();
        assertEquals(2, resultado.size());
        verify(repo, times(1)).findAll();
    }

    @Test
    void testGuardarCategoria() {
        Categoria categoria = new Categoria(1L, "Accesorios", "Mouse, teclados");
        when(repo.save(categoria)).thenReturn(categoria);
        Categoria resultado = service.guardar(categoria);
        assertNotNull(resultado);
        assertEquals("Accesorios", resultado.getNombre());
        verify(repo).save(categoria);
    }

    @Test
    void testObtenerCategoria() {
        Categoria categoria = new Categoria(1L, "Audio", "Audífonos y parlantes");
        when(repo.findById(1L)).thenReturn(Optional.of(categoria));
        Categoria resultado = service.obtener(1L);
        assertNotNull(resultado);
        assertEquals("Audio", resultado.getNombre());
    }

    @Test
    void testObtenerCategoriaNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        Categoria resultado = service.obtener(99L);
        assertNull(resultado);
    }

    @Test
    void testActualizarCategoria() {
        Categoria existente = new Categoria(1L, "Gaming", "Productos gaming");
        Categoria actualizado = new Categoria(1L, "Gaming Pro", "Productos gaming premium");

        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Categoria.class))).thenReturn(actualizado);

        Categoria resultado = service.actualizar(1L, actualizado);
        assertNotNull(resultado);
        assertEquals("Gaming Pro", resultado.getNombre());
        verify(repo).save(any(Categoria.class));
    }

    @Test
    void testActualizarCategoriaNoExiste() {
        Categoria categoria = new Categoria(99L, "Test", "Test desc");
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.actualizar(99L, categoria));
        verify(repo, never()).save(any(Categoria.class));
    }

    @Test
    void testEliminarCategoria() {
        doNothing().when(repo).deleteById(1L);
        service.eliminar(1L);
        verify(repo, times(1)).deleteById(1L);
    }
}
