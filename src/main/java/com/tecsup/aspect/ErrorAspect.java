package com.tecsup.aspect;

import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ErrorAspect {

    @AfterThrowing(
            pointcut = "execution(* com.tecsup.service.ProductoService.*(..))",
            throwing = "ex"
    )
    public void capturarError(Exception ex) {
        System.out.println("❌ ERROR AOP: " + ex.getMessage());
    }
}