package br.com.oficina.oficina;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "jwt.secret=test-secret-minimo-64-chars-para-hmac-sha512-xxxxxxxxxxxxxxxx",
    "jwt.access-expiration-ms=900000",
    "jwt.refresh-expiration-ms=604800000"
})
class OficinaApplicationTests {

    @Test
    void contextLoads() {
    }
}
