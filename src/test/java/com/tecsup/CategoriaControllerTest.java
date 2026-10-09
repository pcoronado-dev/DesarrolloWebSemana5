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
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String jsonCategoria(String nombre, String descripcion) {
        return "{"
                + "\"nombre\":\"" + nombre + "\","
                + "\"descripcion\":\"" + descripcion + "\""
                + "}";
    }

    private MockHttpServletRequestBuilder conCredenciales(MockHttpServletRequestBuilder builder) {
        return builder.header("Usuario", "Ricardo")
                .header("Rol", "ADMIN");
    }

    @Test
    void testGuardarCategoria() throws Exception {
        mockMvc.perform(conCredenciales(post("/api/categorias"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCategoria("Electrónicos", "Dispositivos electrónicos")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Electrónicos"));
    }

    @Test
    void testListarCategorias() throws Exception {
        mockMvc.perform(conCredenciales(get("/api/categorias")))
                .andExpect(status().isOk());
    }

    @Test
    void testObtenerCategoria() throws Exception {
        MvcResult creado = mockMvc.perform(conCredenciales(post("/api/categorias"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCategoria("Computadoras", "Laptops y PCs")))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(conCredenciales(get("/api/categorias/{id}", id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Computadoras"));
    }

    @Test
    void testObtenerCategoriaNoExiste() throws Exception {
        mockMvc.perform(conCredenciales(get("/api/categorias/{id}", 999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidacionNombreVacioCategoria() throws Exception {
        mockMvc.perform(conCredenciales(post("/api/categorias"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCategoria("", "Descripción")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").value("El nombre es obligatorio"));
    }

    @Test
    void testActualizarCategoria() throws Exception {
        MvcResult creado = mockMvc.perform(conCredenciales(post("/api/categorias"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCategoria("Audio", "Audífonos")))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(conCredenciales(put("/api/categorias/{id}", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCategoria("Audio Profesional", "Audífonos y parlantes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Audio Profesional"));
    }

    @Test
    void testActualizarCategoriaNoExiste() throws Exception {
        mockMvc.perform(conCredenciales(put("/api/categorias/{id}", 999L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCategoria("Inexistente", "Desc")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarCategoria() throws Exception {
        MvcResult creado = mockMvc.perform(conCredenciales(post("/api/categorias"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCategoria("Accesorios", "Accesorios varios")))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(conCredenciales(delete("/api/categorias/{id}", id)))
                .andExpect(status().isOk());
    }

    @Test
    void testEliminarCategoriaNoExiste() throws Exception {
        mockMvc.perform(conCredenciales(delete("/api/categorias/{id}", 999L)))
                .andExpect(status().isNotFound());
    }
}
