package it.jacopocarlini.fff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class FffApplication {

    public static void main(String[] args) {
        SpringApplication.run(FffApplication.class, args);
    }


}
