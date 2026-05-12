package com.example.finalapp.appui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalapp.R;
import com.example.finalapp.auth.ui.LoginActivity;
import com.example.finalapp.auth.ui.SignupActivity;
import com.example.finalapp.core.network.RetrofitClient;

public class MainActivity extends AppCompatActivity {

  //테스트용 로그인 계정
  //test3@example.com
  //1234

//  메인의 클라이언트 주소를 실제 ip로 교체하기
//  가계부 쪽 클라이언트 주소도 같이 확인하기

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //실기기에서 테스트할 때 PC IP로 교체
    RetrofitClient.setBaseUrlForDevice("http://192.168.1.151:8080/api/");  //실기기(집 노트북)
//    RetrofitClient.setBaseUrlForDevice("http://192.168.1.123:8080/api/");  //실기기(집 데탑)
//    RetrofitClient.setBaseUrlForDevice("http://192.168.0.60:8080/api/");  //실기기(학교)
//    RetrofitClient.setBaseUrlForDevice("http://10.0.2.2:8080/api/");    //에뮬
    setContentView(R.layout.activity_main);

    Button btnGoLogin = findViewById(R.id.btnGoLogin);
    Button btnGoSignup = findViewById(R.id.btnGoSignup);

    // 로그인 화면으로 이동
    btnGoLogin.setOnClickListener(v -> {
      Intent intent = new Intent(MainActivity.this, LoginActivity.class);
      startActivity(intent);
    });

    // 회원가입 화면으로 이동
    btnGoSignup.setOnClickListener(v -> {
      Intent intent = new Intent(MainActivity.this, SignupActivity.class);
      startActivity(intent);
    });
  }
}

