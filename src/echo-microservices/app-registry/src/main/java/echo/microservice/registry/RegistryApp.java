package echo.microservice.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Maximilian Irro
 */
@SpringBootApplication
public class RegistryApp {

  public static void main(String[] args) {
      SpringApplication.run(RegistryApp.class, args);
  }
}
