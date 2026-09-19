package com.tecsup;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void guardarProductoValidoDevuelve201() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Monitor", 50, 20, "Perifericos")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Monitor"))
                .andExpect(jsonPath("$.categoria").value("Perifericos"));
    }

    @Test
    void guardarProductoInvalidoDevuelve400ConMensajes() throws Exception {
        mockMvc.perform(post("/api/productos")
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
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("Teclado Mecanico", 120, 10, "Perifericos")));

        mockMvc.perform(get("/api/productos/buscar").param("nombre", "teclado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Teclado Mecanico"));
    }

    @Test
    void buscarPorNombreSinResultadosDevuelve404() throws Exception {
        mockMvc.perform(get("/api/productos/buscar").param("nombre", "NoExisteXYZ"))
                .andExpect(status().isNotFound());
    }
}