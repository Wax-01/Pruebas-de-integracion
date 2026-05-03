// src/test/java/edu/unisabana/tyvs/registry/delivery/rest/RegistryControllerIT.java
package edu.unisabana.tyvs.registry.delivery.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;

// src/test/java/.../RegistryControllerIT.java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistryControllerIT {

    @TestConfiguration
    static class TestBeans {
        @Bean
        public RegistryRepositoryPort registryRepositoryPort() throws Exception {
            String jdbc = "jdbc:h2:mem:regdb;DB_CLOSE_DELAY=-1";
            var repo = new edu.unisabana.tyvs.registry.infrastructure.persistence.RegistryRepository(jdbc);
            repo.initSchema();
            return repo;
        }

        @Bean
        public edu.unisabana.tyvs.registry.application.usecase.Registry registry(RegistryRepositoryPort port) {
            return new edu.unisabana.tyvs.registry.application.usecase.Registry(port);
        }
    }

    @Autowired
    private TestRestTemplate rest;

    @Test
    public void shouldRegisterValidPerson() {
        String json = "{\"name\":\"Ana\",\"id\":100,\"age\":30,\"gender\":\"FEMALE\",\"alive\":true}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = rest.postForEntity("/register", new HttpEntity<>(json, headers), String.class);

        assert resp.getStatusCode() == HttpStatus.OK;
        assert "VALID".equals(resp.getBody());
    }

    @Test
    public void shouldReturnDuplicatedWhenRegisteringSamePersonTwice() {
        String json = "{\"name\":\"Carlos\",\"id\":105,\"age\":40,\"gender\":\"MALE\",\"alive\":true}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // First register
        rest.postForEntity("/register", new HttpEntity<>(json, headers), String.class);
        
        // Second register
        ResponseEntity<String> resp2 = rest.postForEntity("/register", new HttpEntity<>(json, headers), String.class);

        assert resp2.getStatusCode() == HttpStatus.OK;
        assert "DUPLICATED".equals(resp2.getBody());
    }

    @Test
    public void shouldReturnUnderageForMinor() {
        String json = "{\"name\":\"Juanito\",\"id\":106,\"age\":15,\"gender\":\"MALE\",\"alive\":true}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ResponseEntity<String> resp = rest.postForEntity("/register", new HttpEntity<>(json, headers), String.class);

        assert resp.getStatusCode() == HttpStatus.OK;
        assert "UNDERAGE".equals(resp.getBody());
    }

    @Test
    public void shouldReturnDeadForDeadPerson() {
        String json = "{\"name\":\"Pedro\",\"id\":107,\"age\":50,\"gender\":\"MALE\",\"alive\":false}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ResponseEntity<String> resp = rest.postForEntity("/register", new HttpEntity<>(json, headers), String.class);

        assert resp.getStatusCode() == HttpStatus.OK;
        assert "DEAD".equals(resp.getBody());
    }

    @Test
    public void shouldReturnBadRequestForInvalidJson() {
        // Missing name and age
        String json = "{\"id\":-5,\"gender\":\"\",\"alive\":true}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ResponseEntity<String> resp = rest.postForEntity("/register", new HttpEntity<>(json, headers), String.class);

        assert resp.getStatusCode() == HttpStatus.BAD_REQUEST;
    }
}
