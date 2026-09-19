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