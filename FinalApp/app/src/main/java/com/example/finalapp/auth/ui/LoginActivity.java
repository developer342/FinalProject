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
import com.example.finalapp.user.data.UserRepository;
import com.example.finalapp.user.dto.UserProfileResponse;
import com.example.finalapp.user.ui.MeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

  private EditText etEmail, etPassword;
  private Button btnLogin, btnGotoSignup;
  private AuthRepository authRepo;
  private UserRepository userRepo;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    TokenStore.init(getApplicationContext()); // 토큰 저장소

    // 토큰 있으면 바로 이동
    if (TokenStore.getAccessToken() != null) {
      startActivity(new Intent(this, MeActivity.class));
      finish();
      return;
    }

    ApiService api = RetrofitClient.getClient().create(ApiService.class);
    authRepo = new AuthRepository(api);
    userRepo  = new UserRepository(api);

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
          if (!resp.isSuccessful()) {
            btnLogin.setEnabled(true);
            Toast.makeText(LoginActivity.this, "로그인 실패(" + resp.code() + ")", Toast.LENGTH_SHORT).show();
            return;
          }
          // 로그인 성공 → me 호출로 닉네임 확인
          userRepo.me(new Callback<UserProfileResponse>() {
            @Override public void onResponse(Call<UserProfileResponse> c, Response<UserProfileResponse> r) {
              btnLogin.setEnabled(true);
              if (r.isSuccessful() && r.body() != null) {
                String nick = r.body().getNickname();
                Toast.makeText(LoginActivity.this,
                        (nick != null && !nick.isEmpty()) ? ("환영합니다, " + nick) : "로그인 성공",
                        Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
              }
              startActivity(new Intent(LoginActivity.this, MeActivity.class));
              finish();
            }
            @Override public void onFailure(Call<UserProfileResponse> c, Throwable t) {
              btnLogin.setEnabled(true);
              Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
              startActivity(new Intent(LoginActivity.this, MeActivity.class));
              finish();
            }
          });
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