package org.appxi.api.pieces;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@SpringBootApplication
public class ApiPiecesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiPiecesApplication.class, args);
    }

}
