package com.tecsup.aspect;

import com.tecsup.model.Producto;
import com.tecsup.service.AuditoriaService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Aspect
@Component
public class AuditoriaAspect {

    @Autowired
    private AuditoriaService auditoriaService;

    @AfterReturning("execution(* com.tecsup.service.ProductoService.guardar(..))")
    public void auditarGuardar(JoinPoint joinPoint) {
        auditoriaService.registrar(
                "CREAR",
                joinPoint.getSignature().getName(),
                "Se registró un producto"
        );
    }

    @AfterReturning("execution(* com.tecsup.service.ProductoService.actualizar(..))")
    public void auditarActualizar(JoinPoint joinPoint) {
        Long id = extraerId(joinPoint.getArgs());

        auditoriaService.registrar(
                "ACTUALIZAR",
                joinPoint.getSignature().getName(),
                "Se actualizó producto con ID: " + id
        );
    }

    @AfterReturning("execution(* com.tecsup.service.ProductoService.eliminar(..))")
    public void auditarEliminar(JoinPoint joinPoint) {
        Long id = extraerId(joinPoint.getArgs());

        auditoriaService.registrar(
                "ELIMINAR",
                joinPoint.getSignature().getName(),
                "Se eliminó producto con ID: " + id
        );
    }

    @AfterReturning(
            pointcut = "execution(* com.tecsup.service.ProductoService.listar(..))",
            returning = "resultado"
    )
    public void auditarListar(JoinPoint joinPoint, Object resultado) {
        int cantidad = 0;
        if (resultado instanceof Collection<?> coleccion) {
            cantidad = coleccion.size();
        }

        auditoriaService.registrar(
                "LISTAR",
                joinPoint.getSignature().getName(),
                "Se listaron " + cantidad + " producto(s)"
        );
    }

    private Long extraerId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long l) {
                return l;
            }
            if (arg instanceof Producto producto) {
                return producto.getId();
            }
        }
        return null;
    }
}