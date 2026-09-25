package com.tecsup;

import com.jayway.jsonpath.JsonPath;
import com.tecsup.model.AuditoriaLog;
import com.tecsup.repository.AuditoriaRepository;
import com.tecsup.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditoriaAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void limpiar() {
        auditoriaRepository.deleteAll();
        productoRepository.deleteAll();
    }

    private String json(String nombre, double precio, int stock, String categoria) {
        return "{"
                + "\"nombre\":\"" + nombre + "\","
                + "\"precio\":" + precio + ","
                + "\"stock\":" + stock + ","
                + "\"categoria\":\"" + categoria + "\""
                + "}";
    }

    private MockHttpServletRequestBuilder conAdmin(MockHttpServletRequestBuilder builder) {
        return builder.header("Usuario", "Ricardo").header("Rol", "ADMIN");
    }

    @Test
    void operacionesCRUDQuedanRegistradasEnAuditoriaConUsuario() throws Exception {

        MvcResult creado = mockMvc.perform(conAdmin(post("/api/productos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Disco Duro", 2500, 10, "Almacenamiento")))
                .andExpect(status().isCreated())
                .andReturn();

        long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(conAdmin(get("/api/productos"))).andExpect(status().isOk());

        mockMvc.perform(conAdmin(put("/api/productos/" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Disco SSD", 2600, 12, "Almacenamiento")))
                .andExpect(status().isOk());

        mockMvc.perform(conAdmin(delete("/api/productos/" + id))).andExpect(status().isOk());

        List<AuditoriaLog> registros = auditoriaRepository.findAll();

        assertThat(registros.stream().map(AuditoriaLog::getAccion))
                .contains("CREAR", "LISTAR", "ACTUALIZAR", "ELIMINAR");

        // Todos los registros deben tener el usuario autenticado
        assertThat(registros.stream().map(AuditoriaLog::getUsuario))
                .containsOnly("Ricardo (ADMIN)");

        AuditoriaLog eliminar = registros.stream()
                .filter(r -> r.getAccion().equals("ELIMINAR"))
                .findFirst()
                .orElseThrow();

        assertThat(eliminar.getDetalle()).contains("Se eliminó producto ID: " + id);

        AuditoriaLog listar = registros.stream()
                .filter(r -> r.getAccion().equals("LISTAR"))
                .findFirst()
                .orElseThrow();

        assertThat(listar.getDetalle()).contains("Se listaron 1 producto(s)");
    }
}