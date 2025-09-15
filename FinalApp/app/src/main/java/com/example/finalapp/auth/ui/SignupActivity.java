package com.example.finalapp.auth.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalapp.R;
import com.example.finalapp.auth.data.AuthRepository;
import com.example.finalapp.auth.dto.SignupRequest;
import com.example.finalapp.core.network.ApiService;
import com.example.finalapp.core.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

  private EditText etEmail, etPassword;
  private Button btnSignup;
  private AuthRepository authRepo;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signup);

    ApiService api = RetrofitClient.getClient().create(ApiService.class);
    authRepo = new AuthRepository(api);

    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    btnSignup = findViewById(R.id.btnSignup);

    btnSignup.setOnClickListener(v -> {
      String email = etEmail.getText().toString().trim();
      String pw = etPassword.getText().toString().trim();
      if (email.isEmpty() || pw.isEmpty()) {
        Toast.makeText(this, "이메일/비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
        return;
      }
      btnSignup.setEnabled(false);
      authRepo.signup(new SignupRequest(email, pw), new Callback<Void>() {
        @Override public void onResponse(Call<Void> call, Response<Void> resp) {
          btnSignup.setEnabled(true);
          if (resp.isSuccessful()) {
            Toast.makeText(SignupActivity.this, "회원가입 완료", Toast.LENGTH_SHORT).show();
            finish(); // 로그인 화면으로
          } else {
            Toast.makeText(SignupActivity.this, "실패(" + resp.code() + ")", Toast.LENGTH_SHORT).show();
          }
        }
        @Override public void onFailure(Call<Void> call, Throwable t) {
          btnSignup.setEnabled(true);
          Toast.makeText(SignupActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
        }
      });
    });
  }
}