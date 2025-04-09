package no.entur.nuska;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
class NuskaApplicationTests extends NuskaApplication {

  @Test
  void contextLoads() {}
}
