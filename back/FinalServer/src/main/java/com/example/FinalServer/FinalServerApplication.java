package com.example.FinalServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinalServerApplication {

  private static Runnable StringSpringApplication;

  //  인텔리 제이에 내가 실행할 프로젝트의 설정에 환경변수로 지피티 인증키를 넣을 것

  public static void main(String[] args) {
    SpringApplication.run(FinalServerApplication.class, args);
  }
}

