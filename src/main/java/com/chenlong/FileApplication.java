package com.chenlong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Created by chenlong on 2017/8/17.
 */
@SpringBootApplication
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class,args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return  new RestTemplate();
    }
}
