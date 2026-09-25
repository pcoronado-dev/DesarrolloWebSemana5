package com.tecsup.aspect;

import com.tecsup.service.AuditoriaService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ErrorAspect {

    @Autowired
    private AuditoriaService auditoriaService;

    @AfterThrowing(
            pointcut = "execution(* com.tecsup.service.ProductoService.*(..))",
            throwing = "ex"
    )
    public void capturarError(JoinPoint joinPoint, Exception ex) {
        System.out.println("ERROR AOP: " + ex.getMessage());

        String detalle = ex.getMessage() != null
                ? ex.getMessage()
                : "Excepción de tipo " + ex.getClass().getSimpleName();

        auditoriaService.registrar(
                "ERROR",
                joinPoint.getSignature().getName(),
                detalle
        );
    }
}