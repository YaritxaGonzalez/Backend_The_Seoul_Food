CREATE TABLE usuarios (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    correo    VARCHAR(150) NOT NULL UNIQUE,
    telefono  VARCHAR(20),
    password  VARCHAR(255) NOT NULL,
    rol       VARCHAR(20) NOT NULL
);

CREATE TABLE productos (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    imagen      VARCHAR(255),
    precio      INT NOT NULL,
    descripcion VARCHAR(500),
    stock       INT NOT NULL
);

CREATE TABLE pedidos (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    total      INT NOT NULL,
    estado     VARCHAR(20) NOT NULL,
    fecha      DATETIME NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE pedido_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id       BIGINT NOT NULL,
    producto_id     BIGINT NOT NULL,
    cantidad        INT NOT NULL,
    precio_unitario INT NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);