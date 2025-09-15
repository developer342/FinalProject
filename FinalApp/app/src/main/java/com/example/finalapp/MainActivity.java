package com.example.finalapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalapp.auth.ui.LoginActivity;

public class MainActivity extends AppCompatActivity {

  //테스트용 로그인 계정
  //test@example.com
  //1234

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button btnGoLogin = findViewById(R.id.btnGoLogin);
    btnGoLogin.setOnClickListener(v -> {
      Intent i = new Intent(MainActivity.this, LoginActivity.class);
      startActivity(i);
    });
  }
}