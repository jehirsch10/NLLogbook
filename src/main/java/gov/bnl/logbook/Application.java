package gov.bnl.logbook;

import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan(basePackages="gov.bnl.logbook")
@SpringBootApplication
public class Application {
    static Logger logger = Logger.getLogger(Application.class.getName());

    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}