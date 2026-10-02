package com.example.metro;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@MapperScan("com.example.metro.mapper")
@ConfigurationPropertiesScan
@SpringBootApplication
public class MetroTicketReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetroTicketReservationApplication.class, args);
    }
}
