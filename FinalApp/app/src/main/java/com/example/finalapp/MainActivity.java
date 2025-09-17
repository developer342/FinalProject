package com.example.finalapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalapp.auth.ui.LoginActivity;
import com.example.finalapp.auth.ui.SignupActivity;
import com.example.finalapp.core.network.RetrofitClient;

public class MainActivity extends AppCompatActivity {

  //테스트용 로그인 계정
  //test3@example.com
  //1234

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //실기기에서 테스트할 때 PC IP로 교체
    RetrofitClient.setBaseUrlForDevice("http://<자기 PC IP>:8080/api/");
    setContentView(R.layout.activity_main);

    Button btnGoLogin = findViewById(R.id.btnGoLogin);
    btnGoLogin.setOnClickListener(v -> {
      Intent i = new Intent(MainActivity.this, LoginActivity.class);
      startActivity(i);
    });

    Button btnGoSignup = findViewById(R.id.btnGoSignup);
    btnGoSignup.setOnClickListener(v -> {
      Intent i = new Intent(MainActivity.this, SignupActivity.class);
      startActivity(i);
    });
  }
}