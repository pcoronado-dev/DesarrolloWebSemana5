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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadAspectTest {

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

    private MockHttpServletRequestBuilder conRol(String usuario, String rol,
                                                 MockHttpServletRequestBuilder builder) {
        return builder.header("Usuario", usuario).header("Rol", rol);
    }

    @Test
    void sinCredencialesDevuelve401() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Debe enviar Usuario y Rol"));
    }

    @Test
    void rolNoCoincideDevuelve403() throws Exception {
        // Ricardo es ADMIN en el mapa centralizado; enviar otro rol es rechazado
        mockMvc.perform(conRol("Ricardo", "USER", get("/api/productos")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("No tiene permisos"));
    }

    @Test
    void usuarioConRolUSERPuedeCrearYListar() throws Exception {
        // Actividad: USER tambien puede crear productos
        mockMvc.perform(conRol("Ana", "USER", post("/api/productos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Teclado", 80, 15, "Perifericos")))
                .andExpect(status().isCreated());

        mockMvc.perform(conRol("Ana", "USER", get("/api/productos")))
                .andExpect(status().isOk());

        List<AuditoriaLog> registros = auditoriaRepository.findAll();
        assertThat(registros.stream().map(AuditoriaLog::getAccion))
                .contains("CREAR", "LISTAR");
        assertThat(registros.stream().map(AuditoriaLog::getUsuario))
                .containsOnly("Ana (USER)");
    }

    @Test
    void usuarioConRolUSERNoPuedeActualizarNiEliminar() throws Exception {

        MvcResult creado = mockMvc.perform(conRol("Ricardo", "ADMIN", post("/api/productos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Monitor", 50, 20, "Perifericos")))
                .andExpect(status().isCreated())
                .andReturn();

        long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(conRol("Ana", "USER", put("/api/productos/" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Monitor 2", 60, 20, "Perifericos")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acceso denegado"));

        mockMvc.perform(conRol("Ana", "USER", delete("/api/productos/" + id)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acceso denegado"));
    }
}