# 00 - Introducción

## 1. Propósito

Este **Software Design Document (SDD)** describe el diseño, la arquitectura y las decisiones
técnicas de **TradeHub**, una plataforma de marketplace construida con Java 17, Spring Boot 3 y
Arquitectura Hexagonal (Ports & Adapters).

El documento tiene cuatro objetivos:

1. Explicar cómo se organiza el sistema y por qué se eligió esa organización.
2. Servir de referencia para el mantenimiento y la evolución del código.
3. Documentar las reglas de negocio que el dominio protege de forma explícita.
4. Demostrar, con código real, cómo se aplican los principios SOLID.

## 2. Alcance

El alcance cubre la versión actual del repositorio:

- Registro e inicio de sesión de compradores.
- Registro y aprobación de vendedores por parte del administrador.
- Gestión de almacenes del marketplace y de vendedores.
- Catálogo de productos físicos y digitales.
- Inventario disponible, reservado y dañado.
- Carrito de compras.
- Ciclo de vida completo de la orden y operaciones logísticas.

Quedan fuera del alcance de esta versión: pasarela de pagos real, facturación electrónica,
notificaciones, integración con transportistas y panel de administración web.

## 3. Audiencia

| Audiencia | Uso del documento |
|-----------|-------------------|
| Docente / evaluador | Verificar arquitectura, reglas de negocio y cumplimiento SOLID |
| Desarrolladores | Incorporarse al proyecto y mantener la separación de capas |
| Arquitectos | Revisar decisiones, dependencias y puntos de extensión |
| Operación / DevOps | Entender perfiles, persistencia y despliegue |

## 4. Cómo navegar este SDD

| Documento | Contenido |
|-----------|-----------|
| [01 - Requisitos](01-Requisitos.md) | Requisitos funcionales, no funcionales y trazabilidad |
| [02 - Arquitectura](02-Arquitectura.md) | Arquitectura hexagonal, diagrama de componentes y flujo de una petición |
| [03 - Dominio](03-Dominio.md) | Entidades, invariantes, máquinas de estado y excepciones |
| [04 - API](04-API.md) | Endpoints, roles, ejemplos y contrato de errores |
| [05 - SOLID](05-SOLID.md) | Evidencia concreta de SRP, OCP, LSP, ISP y DIP |

## 5. Glosario

| Término | Definición |
|---------|------------|
| **Dominio** | Núcleo del sistema con las entidades y reglas de negocio. No depende de Spring, JPA ni HTTP. |
| **Puerto de entrada** | Interfaz que declara una operación que el exterior puede solicitar al dominio. Es implementada por un servicio de aplicación. |
| **Puerto de salida** | Interfaz que declara una necesidad del dominio hacia el exterior (por ejemplo, persistencia). Es implementada por un adaptador. |
| **Adaptador** | Traduce entre el mundo externo (HTTP, JPA, JWT) y los puertos del dominio. |
| **Caso de uso** | Operación atómica del negocio, representada por un puerto de entrada. |
| **Servicio de aplicación** | Implementación de un caso de uso: orquesta entidades y puertos, sin contener reglas de negocio duplicadas. |
| **Entidad** | Objeto del dominio con identidad propia y ciclo de vida (por ejemplo, `Order`). |
| **Objeto de valor** | Objeto sin identidad propia que describe una característica (por ejemplo, una dirección o un precio). |
| **Invariante** | Condición que siempre debe cumplirse en el dominio. |
| **Buyer** | Comprador registrado, con estado comercial y direcciones. |
| **Seller** | Vendedor registrado por el administrador; requiere aprobación para vender. |
| **Product** | Artículo del catálogo, físico o digital, con precio y estado de publicación. |
| **InventoryItem** | Existencias de un producto en un almacén: disponible, reservado y dañado. |
| **Order** | Carrito u orden de compra; su estado evoluciona mediante transiciones controladas. |
| **DTO** | Objeto de transferencia usado por la capa REST para desacoplar el contrato HTTP del modelo de dominio. |
| **JWT** | Token firmado que identifica al usuario y su rol en cada petición. |
| **Perfil** | Configuración de Spring (`dev` o `prod`) que selecciona H2 o MySQL. |

## 6. Convenciones

- Los nombres de clases y métodos están en inglés; la documentación está en español.
- Las reglas de negocio viven en `domain/models` y se expresan con excepciones de dominio.
- Los controladores no contienen reglas de negocio: resuelven identidad, invocan un caso de uso y
  transforman el resultado a JSON.
- Cada caso de uso tiene una única interfaz de entrada y una única implementación.
- Los errores esperados se comunican con excepciones específicas, nunca con `RuntimeException`
  genérica.

## 7. Resumen ejecutivo

TradeHub aplica el patrón **Ports & Adapters** con tres zonas claramente separadas:

```text
Adaptadores de entrada (REST + seguridad)
            │  dependen de
            ▼
Puertos de entrada + servicios de aplicación
            │  usan
            ▼
Modelo de dominio + puertos de salida
            ▲  implementados por
            │
Adaptadores de salida (JPA)
```

La dirección de las dependencias apunta siempre hacia el dominio. Esto permite cambiar la base de
datos, el mecanismo de autenticación o el protocolo de entrada sin reescribir las reglas de negocio.

La versión documentada expone **24 puertos de entrada atómicos**, 7 puertos de salida de
persistencia, 6 controladores REST y una jerarquía de excepciones de dominio con códigos HTTP
diferenciados.
