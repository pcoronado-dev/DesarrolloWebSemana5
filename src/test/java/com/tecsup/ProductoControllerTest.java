package com.tecsup;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String json(String nombre, double precio, int stock, String categoria) {
        return "{"
                + "\"nombre\":\"" + nombre + "\","
                + "\"precio\":" + precio + ","
                + "\"stock\":" + stock + ","
                + "\"categoria\":\"" + categoria + "\""
                + "}";
    }

    private MockHttpServletRequestBuilder conCredenciales(
            MockHttpServletRequestBuilder builder) {
        return builder.header("Usuario", "Ricardo")
                .header("Rol", "ADMIN");
    }

    @Test
    void guardarProductoValidoDevuelve201() throws Exception {
        mockMvc.perform(conCredenciales(post("/api/productos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Monitor", 50, 20, "Perifericos")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Monitor"))
                .andExpect(jsonPath("$.categoria").value("Perifericos"));
    }

    @Test
    void guardarProductoInvalidoDevuelve400ConMensajes() throws Exception {
        mockMvc.perform(conCredenciales(post("/api/productos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", -5, -1, "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.precio").value("El precio debe ser mayor a 0"))
                .andExpect(jsonPath("$.stock").value("El stock no puede ser negativo"))
                .andExpect(jsonPath("$.categoria").value("La categoria es obligatoria"));
    }

    @Test
    void buscarPorNombreDevuelveCoincidencias() throws Exception {
        mockMvc.perform(conCredenciales(post("/api/productos"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("Teclado Mecanico", 120, 10, "Perifericos")));

        mockMvc.perform(conCredenciales(get("/api/productos/buscar")).param("nombre", "teclado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Teclado Mecanico"));
    }

    @Test
    void buscarPorNombreSinResultadosDevuelve404() throws Exception {
        mockMvc.perform(conCredenciales(get("/api/productos/buscar")).param("nombre", "NoExisteXYZ"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarProducto() throws Exception {
        MvcResult creado = mockMvc.perform(conCredenciales(post("/api/productos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Teclado", 150, 10, "Perifericos")))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(conCredenciales(put("/api/productos/{id}", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Teclado Mecánico", 200, 15, "Perifericos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Teclado Mecánico"))
                .andExpect(jsonPath("$.precio").value(200.0))
                .andExpect(jsonPath("$.stock").value(15));
    }

    @Test
    void testActualizarProductoNoExiste() throws Exception {
        mockMvc.perform(conCredenciales(put("/api/productos/{id}", 999L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Producto Inexistente", 100, 5, "Otros")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidacionActualizarDatosInvalidos() throws Exception {
        MvcResult creado = mockMvc.perform(conCredenciales(post("/api/productos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Mouse Pad", 50, 20, "Perifericos")))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(conCredenciales(put("/api/productos/{id}", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", -10, -5, "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.precio").value("El precio debe ser mayor a 0"))
                .andExpect(jsonPath("$.stock").value("El stock no puede ser negativo"))
                .andExpect(jsonPath("$.categoria").value("La categoria es obligatoria"));
    }

    @Test
    void testEliminarProducto() throws Exception {
        MvcResult creado = mockMvc.perform(conCredenciales(post("/api/productos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Audífonos", 120, 8, "Audio")))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(conCredenciales(delete("/api/productos/{id}", id)))
                .andExpect(status().isOk());
    }

    @Test
    void testEliminarProductoNoExiste() throws Exception {
        mockMvc.perform(conCredenciales(delete("/api/productos/{id}", 999L)))
                .andExpect(status().isNotFound());
    }
}
