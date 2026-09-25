package com.tecsup;

import com.jayway.jsonpath.JsonPath;
import com.tecsup.model.AuditoriaLog;
import com.tecsup.model.Producto;
import com.tecsup.repository.AuditoriaRepository;
import com.tecsup.repository.ProductoRepository;
import com.tecsup.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Autowired
    private ProductoService productoServicio;

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

    @Test
    void operacionesCRUDQuedanRegistradasEnAuditoria() throws Exception {

        MvcResult creado = mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Disco Duro", 2500, 10, "Almacenamiento")))
                .andExpect(status().isCreated())
                .andReturn();

        long id = ((Number) JsonPath.read(creado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());

        mockMvc.perform(put("/api/productos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Disco SSD", 2600, 12, "Almacenamiento")))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/productos/" + id)).andExpect(status().isOk());

        List<AuditoriaLog> registros = auditoriaRepository.findAll();

        assertThat(registros.stream().map(AuditoriaLog::getAccion))
                .contains("CREAR", "LISTAR", "ACTUALIZAR", "ELIMINAR");

        AuditoriaLog actualizar = registros.stream()
                .filter(r -> r.getAccion().equals("ACTUALIZAR"))
                .findFirst()
                .orElseThrow();

        assertThat(actualizar.getDetalle()).contains("Se actualizó producto con ID: " + id);
    }

    @Test
    void erroresDelServicioQuedanRegistradosEnAuditoria() {

        Producto p = new Producto();
        p.setNombre("Inexistente");
        p.setPrecio(10);
        p.setStock(5);
        p.setCategoria("X");

        assertThatThrownBy(() -> productoServicio.actualizar(999L, p))
                .isInstanceOf(RuntimeException.class);

        List<AuditoriaLog> registros = auditoriaRepository.findAll();

        assertThat(registros.stream().map(AuditoriaLog::getAccion)).contains("ERROR");
    }
}