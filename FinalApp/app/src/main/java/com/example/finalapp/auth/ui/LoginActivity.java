package com.example.finalapp.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalapp.R;
import com.example.finalapp.auth.data.AuthRepository;
import com.example.finalapp.auth.dto.LoginRequest;
import com.example.finalapp.auth.dto.TokenResponse;
import com.example.finalapp.core.network.ApiService;
import com.example.finalapp.core.network.RetrofitClient;
import com.example.finalapp.core.storage.TokenStore;
import com.example.finalapp.user.ui.MeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

  private EditText etEmail, etPassword;
  private Button btnLogin, btnGotoSignup;
  private AuthRepository authRepo;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    TokenStore.init(getApplicationContext()); // 토큰 저장소 초기화

    // 이미 토큰 있으면 바로 이동
    if (TokenStore.getAccessToken() != null) {
      startActivity(new Intent(this, MeActivity.class));
      finish();
      return;
    }

    ApiService api = RetrofitClient.getClient().create(ApiService.class);
    authRepo = new AuthRepository(api);

    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    btnLogin = findViewById(R.id.btnLogin);
    btnGotoSignup = findViewById(R.id.btnGotoSignup);

    btnLogin.setOnClickListener(v -> {
      String email = etEmail.getText().toString().trim();
      String pw = etPassword.getText().toString().trim();
      if (email.isEmpty() || pw.isEmpty()) {
        Toast.makeText(this, "이메일/비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
        return;
      }
      btnLogin.setEnabled(false);
      authRepo.login(new LoginRequest(email, pw), new Callback<TokenResponse>() {
        @Override public void onResponse(Call<TokenResponse> call, Response<TokenResponse> resp) {
          btnLogin.setEnabled(true);
          if (resp.isSuccessful()) {
            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MeActivity.class));
            finish();
          } else {
            Toast.makeText(LoginActivity.this, "로그인 실패(" + resp.code() + ")", Toast.LENGTH_SHORT).show();
          }
        }
        @Override public void onFailure(Call<TokenResponse> call, Throwable t) {
          btnLogin.setEnabled(true);
          Toast.makeText(LoginActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
        }
      });
    });

    btnGotoSignup.setOnClickListener(v ->
            startActivity(new Intent(this, SignupActivity.class)));
  }
}