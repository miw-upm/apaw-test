## [Máster en Ingeniería Web por la Universidad Politécnica de Madrid (miw-upm)](http://miw.etsisi.upm.es)

## Arquitectura y Patrones para Aplicaciones Web (APAW)
> Este proyecto es un apoyo docente de la asignatura y un ejemplo práctico del desarrollo de una aplicación Web siguiendo una Arquitectura por capas

Es un ejemplo de un API Rest completo, basado en Spring Boot, con una arquitectura de tres capas, y almacenamiento en
bases de datos con JPA soportado por Hibernate y Postgres.

## Tecnologías necesarias
`Java` `Maven` `GitHub` `GitHub Actions` `Sonarcloud` `Slack` `Spring-Boot` `GitHub Packages` `OpenAPI` `JPA` `PostgreSQL` `Docker` 

### :gear: Instalación del proyecto
1. Clonar el repositorio en tu equipo, **mediante consola**:
```sh
> cd <folder path>
> git clone https://github.com/miw-upm/apaw-user
```
2. Importar el proyecto mediante **IntelliJ IDEA**
   * **Open**, y seleccionar la carpeta del proyecto.

### Pruebas funcionales de usuarios con OpenFeign

Arranca previamente `apaw-user` y el gateway para que la API esté disponible en
`http://localhost:8080/api/apaw-user`. Las pruebas realizan peticiones HTTP reales,
crean usuarios de prueba y los eliminan al terminar; utiliza una base de datos de desarrollo.

```sh
mvn verify -Dit.test=UserResourceFT
```

La URL se configura en `src/test/resources/application-test.yml`. Se puede cambiar
con la variable `APAW_USER_URL` o mediante una propiedad de Maven:

```sh
mvn verify -Dit.test=UserResourceFT -Dtest.api.base-url=http://localhost:8081
```

`UserClient` declara las llamadas con `@FeignClient` y envía los filtros de
`UserFindCriteria` como parámetros de consulta con `@SpringQueryMap`.
Las lecturas individuales usan `/{id}` y `/{mobile}`, tal como están declaradas
en el controlador; el alta y la búsqueda usan `/users`, y el borrado `/users/{id}`.

`UserResourceFT` comprueba alta, lectura por UUID y móvil, búsqueda resumida,
filtros por móvil y estado, borrado, respuestas 404 y validaciones 400.
El contexto de Spring de las pruebas habilita Feign sin arrancar un servidor web.
`mvn test` no ejecuta estas pruebas externas; se ejecutan con Failsafe en `verify`.
