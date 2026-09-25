-- Base de datos del laboratorio (paso 3 del procedimiento)
CREATE DATABASE IF NOT EXISTS laboratorio_api;
USE laboratorio_api;

-- La tabla 'producto' la crea automaticamente Hibernate (spring.jpa.hibernate.ddl-auto=update),
-- pero si se desea crearla manualmente:

-- CREATE TABLE producto (
--     id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
--     nombre VARCHAR(255) NOT NULL,
--     precio DOUBLE NOT NULL,
--     stock INT NOT NULL,
--     categoria VARCHAR(255) NOT NULL
-- );

-- Tabla de auditoria (paso 1 del laboratorio de AOP).
-- Hibernate tambien la crea automaticamente a partir de la entidad AuditoriaLog,
-- pero se deja el DDL de referencia del laboratorio.
CREATE TABLE IF NOT EXISTS auditoria_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    accion VARCHAR(50),
    metodo VARCHAR(100),
    fecha TIMESTAMP,
    detalle VARCHAR(255)
);

-- Consulta para verificar la actividad (paso 10 del laboratorio):
-- SELECT * FROM auditoria_log ORDER BY id DESC;