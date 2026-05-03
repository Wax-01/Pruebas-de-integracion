# Taller de Pruebas de Integración y Sistema - Registraduría

Bienvenido a la documentación oficial del taller de pruebas de integración y sistema de la aplicación **Registraduría**.
Este sistema ha sido diseñado aplicando **Arquitectura Limpia** (Clean Architecture) para el manejo y persistencia del registro de votantes.

## 🧑‍💻 Integrantes
- Julia - Julia@unisabana.edu.co

---

## 🏗️ Arquitectura Limpia (Clean Architecture)

El proyecto organiza sus componentes en cuatro capas principales que garantizan el desacoplamiento:

1. **Domain**: Contiene las reglas del negocio (`Person`, `RegisterResult`).
2. **Application**: Orquesta los casos de uso (`Registry`) interactuando con los puertos.
3. **Infrastructure**: Implementa los detalles técnicos como la persistencia en base de datos (`RegistryRepository` usando JDBC H2).
4. **Delivery**: Expone la aplicación al mundo exterior (`RegistryController` vía API REST HTTP).

---

## 🧪 Tipos de Pruebas

Para garantizar la fiabilidad del sistema, aplicamos tres niveles de pruebas:

| Nivel de Prueba | Objetivo | Herramientas Utilizadas | Ejemplo en el Código |
|-----------------|----------|-------------------------|----------------------|
| **Integración (H2)** | Validar la comunicación real entre el caso de uso (`Registry`) y el repositorio (`RegistryRepository`) con una BD en memoria. | JUnit, H2 Database | [RegistryTest.java](registraduria/src/test/java/edu/unisabana/tyvs/registry/application/usecase/RegistryTest.java) |
| **Mocks (Aisladas)** | Aislar el caso de uso simulando el comportamiento del repositorio. | JUnit, Mockito | [RegistryWithMockTest.java](registraduria/src/test/java/edu/unisabana/tyvs/registry/application/usecase/RegistryWithMockTest.java) |
| **Sistema (Caja Negra)** | Validar los endpoints REST simulando peticiones HTTP externas. | Spring Boot Test, TestRestTemplate | [RegistryControllerIT.java](registraduria/src/test/java/edu/unisabana/tyvs/registry/delivery/rest/RegistryControllerIT.java) |

---

## 📋 Matriz de Pruebas de Integración y Sistema

| Caso | Entrada (JSON/Objeto) | Resultado Esperado | Tipo de Prueba | Método de Test |
|------|----------|--------------------|------|------|
| **Persona válida** | ID=100, edad=30 | `VALID` | Integración (H2) | `shouldRegisterValidPerson()` |
| **Persona duplicada** | ID=100 (ya registrado) | `DUPLICATED` | Integración (H2) | `shouldPersistValidVoterAndRejectDuplicates()` |
| **Persona menor de edad** | ID=101, edad=17 | `UNDERAGE` | Integración (H2) | `shouldReturnUnderageWhenAgeIsLessThan18()` |
| **Persona fallecida** | ID=102, alive=false | `DEAD` | Integración (H2) | `shouldReturnDeadWhenPersonIsNotAlive()` |
| **Persona con ID inválido** | ID=-1 | `INVALID` | Integración (H2) | `shouldReturnInvalidWhenIdIsZeroOrNegative()` |
| **Validar guardado con Mock** | ID=8 (no existe) | `VALID` + `verify().save()` | Mockito | `shouldRegisterValidPersonAndCallSave()` |
| **Simular Excepción en DB** | Exception lanzada en save() | `IllegalStateException` | Mockito | `shouldHandleDatabaseException()` |
| **HTTP Persona válida** | JSON válido | `200 OK` ("VALID") | Sistema (HTTP) | `shouldRegisterValidPerson()` |
| **HTTP Persona duplicada** | JSON duplicado | `200 OK` ("DUPLICATED") | Sistema (HTTP) | `shouldReturnDuplicatedWhenRegisteringSamePersonTwice()` |
| **HTTP Menor de edad** | JSON edad=15 | `200 OK` ("UNDERAGE") | Sistema (HTTP) | `shouldReturnUnderageForMinor()` |
| **HTTP Fallecido** | JSON alive=false | `200 OK` ("DEAD") | Sistema (HTTP) | `shouldReturnDeadForDeadPerson()` |
| **HTTP Request Inválido** | JSON sin nombre y edad negativa | `400 Bad Request` | Sistema (HTTP) | `shouldReturnBadRequestForInvalidJson()` |

---

## 🎯 Ejemplos de Pruebas

### Pruebas de Integración con H2
Usamos el patrón AAA (Arrange - Act - Assert) para preparar la BD en memoria y verificar los datos insertados:
```java
@Test
public void shouldReturnUnderageWhenAgeIsLessThan18() throws Exception {
    // Arrange
    Person p = new Person("Juan", 101, 17, Gender.MALE, true);
    // Act
    RegisterResult result = registry.registerVoter(p);
    // Assert
    assertEquals(RegisterResult.UNDERAGE, result);
    assertFalse(repo.existsById(101));
}
```

### Pruebas con Mockito
Simulamos el repositorio para asegurar que no dependemos de la base de datos para ciertas pruebas lógicas:
```java
@Test
public void shouldRegisterValidPersonAndCallSave() throws Exception {
    when(repo.existsById(8)).thenReturn(false);
    Person p = new Person("Carlos", 8, 30, Gender.MALE, true);
    RegisterResult result = registry.registerVoter(p);
    assertEquals(RegisterResult.VALID, result);
    verify(repo).save(8, "Carlos", 30, true);
}
```

---

## 📊 Reporte de Cobertura (JaCoCo)

Ejecutamos `mvn clean verify` obteniendo los siguientes resultados de cobertura de JaCoCo (`target/site/jacoco/index.html`):

- **Application (`Registry`)**: ~87% de instrucciones cubiertas.
- **Delivery (`RegistryController`)**: 100% de instrucciones cubiertas.

Se logró exitosamente la meta de más del 70% en las capas de `application` y `delivery`. Algunas líneas excluidas se deben a constructores por defecto vacíos o código legado inalcanzable.

---

## 🧠 Conclusiones y Reflexión Final

- **¿Qué capas fueron más difíciles de probar y por qué?**
La capa de infraestructura requiere configuración adicional (como levantar H2, preparar los esquemas, etc.), lo cual la hace ligeramente más laboriosa de preparar que un test unitario normal. Las pruebas de sistema (HTTP) también requieren levantar el contexto de Spring, lo cual consume más tiempo de ejecución.

- **¿Qué beneficios observas en usar mocks frente a H2 o base real?**
Los Mocks permiten simular condiciones que son difíciles de reproducir en una base de datos real (por ejemplo, forzar una `SQLException` en un momento exacto). Además, son mucho más rápidos ya que no implican levantar una base de datos ni acceder a disco.

- **¿Cómo mejorarías el diseño de `RegistryController` o `RegistryRepository` para facilitar las pruebas automáticas?**
El `RegistryController` se mejoró agregando `@Valid` y un `@ExceptionHandler` para retornar un 400 limpio cuando el JSON no cumple con la estructura de `PersonDTO`.

- **¿Qué aprendiste sobre integración continua (CI) al ejecutar tus pruebas con Maven y JaCoCo?**
Se demostró que automatizar estas pruebas en la fase `verify` de Maven nos permite obtener métricas de cobertura inmediatas (JaCoCo) antes de realizar cualquier integración, lo que ayuda a prevenir que código sin probar llegue a producción.
