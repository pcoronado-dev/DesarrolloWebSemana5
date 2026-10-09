package com.tecsup.service;

import com.tecsup.model.Producto;
import com.tecsup.repository.ProductoRepository;
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

public class ProductoServiceTest {

    @Mock
    private ProductoRepository repo;

    @InjectMocks
    private ProductoService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // TEST LISTAR
    @Test
    void testListarProductos() {
        List<Producto> lista = List.of(
                new Producto(1L, "Monitor", 500.0, 10, "Perifericos")
        );
        when(repo.findAll()).thenReturn(lista);
        List<Producto> resultado = service.listar();
        assertEquals(1, resultado.size());
        assertEquals("Monitor", resultado.get(0).getNombre());
        verify(repo, times(1)).findAll();
    }

    // TEST GUARDAR
    @Test
    void testGuardarProducto() {
        Producto producto = new Producto(1L, "Laptop", 3000.0, 5, "Computacion");
        when(repo.save(producto)).thenReturn(producto);
        Producto resultado = service.guardar(producto);
        assertNotNull(resultado);
        assertEquals("Laptop", resultado.getNombre());
        verify(repo).save(producto);
    }

    // TEST OBTENER
    @Test
    void testObtenerProducto() {
        Producto producto = new Producto(1L, "Teclado", 100.0, 20, "Perifericos");
        when(repo.findById(1L)).thenReturn(Optional.of(producto));
        Producto resultado = service.obtener(1L);
        assertNotNull(resultado);
        assertEquals("Teclado", resultado.getNombre());
        verify(repo).findById(1L);
    }

    // TEST OBTENER CUANDO NO EXISTE
    @Test
    void testObtenerProductoNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        Producto resultado = service.obtener(99L);
        assertNull(resultado);
        verify(repo).findById(99L);
    }

    // TEST ACTUALIZAR
    @Test
    void testActualizarProducto() {
        Producto productoExistente = new Producto(1L, "Monitor", 500.0, 10, "Perifericos");
        Producto productoActualizado = new Producto(1L, "Monitor 24\"", 550.0, 15, "Perifericos");

        when(repo.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(repo.save(any(Producto.class))).thenReturn(productoActualizado);

        Producto resultado = service.actualizar(1L, productoActualizado);

        assertNotNull(resultado);
        assertEquals("Monitor 24\"", resultado.getNombre());
        assertEquals(550.0, resultado.getPrecio());
        assertEquals(15, resultado.getStock());
        verify(repo).findById(1L);
        verify(repo).save(any(Producto.class));
    }

    // TEST ACTUALIZAR CUANDO NO EXISTE
    @Test
    void testActualizarProductoNoExiste() {
        Producto productoActualizado = new Producto(99L, "Test", 100.0, 5, "Test");
        when(repo.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.actualizar(99L, productoActualizado));

        assertTrue(exception.getMessage().contains("No se encontr"));
        verify(repo).findById(99L);
        verify(repo, never()).save(any(Producto.class));
    }

    // TEST ELIMINAR
    @Test
    void testEliminarProducto() {
        doNothing().when(repo).deleteById(1L);
        service.eliminar(1L);
        verify(repo, times(1)).deleteById(1L);
    }
}
