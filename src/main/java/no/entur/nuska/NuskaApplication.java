package no.entur.nuska;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
public class NuskaApplication {

  public static void main(String[] args) {
    SpringApplication.run(NuskaApplication.class, args);
  }
}
