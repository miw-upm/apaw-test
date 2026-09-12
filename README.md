## [Máster en Ingeniería Web por la Universidad Politécnica de Madrid (miw-upm)](http://miw.etsisi.upm.es)

## Arquitectura y Patrones para Aplicaciones Web (APAW)

> Este proyecto es un apoyo docente de la asignatura y un ejemplo práctico de test funcionales

### :gear: Instalación del proyecto

1. Clonar el repositorio en tu equipo, **mediante consola**:

```sh
> cd <folder path>
> git clone https://github.com/miw-upm/apaw-test
```

2. Importar el proyecto mediante **IntelliJ IDEA**
    * **Open**, y seleccionar la carpeta del proyecto.

### Pruebas funcionales de usuarios con OpenFeign

Arranca previamente todos los microservicios: `apaw-eureka`, `apaw-user`, `apaw-practice` y `apaw-gateway` con Docker.
Las pruebas realizan peticiones HTTP reales, usan los datos de `SeederForDev`.

```sh
mvn verify
```

La URL se configura en `src/test/resources/application-test.yml`.
`UserClient` declara las llamadas con `@FeignClient`.
Crear un paquete por cada api, y dentro del api `apaw-practice` un paquete por cada `Epic`.
