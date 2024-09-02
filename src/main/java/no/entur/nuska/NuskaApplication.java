package no.entur.nuska;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// TODO: Do we need that exclude? Should be automatically excluded?
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class NuskaApplication {

  public static void main(String[] args) {
    SpringApplication.run(NuskaApplication.class, args);
  }
}
