package com.example.finalapp.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalapp.R;
import com.example.finalapp.auth.data.AuthRepository;
import com.example.finalapp.auth.ui.LoginActivity;
import com.example.finalapp.core.network.ApiService;
import com.example.finalapp.core.network.RetrofitClient;
import com.example.finalapp.core.storage.TokenStore;
import com.example.finalapp.user.data.UserRepository;
import com.example.finalapp.user.dto.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeActivity extends AppCompatActivity {

  private TextView tvInfo;
  private Button btnLogout;
  private UserRepository userRepo;
  private AuthRepository authRepo;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_me);

    ApiService api = RetrofitClient.getClient().create(ApiService.class);
    userRepo = new UserRepository(api);
    authRepo = new AuthRepository(api);

    tvInfo = findViewById(R.id.tvInfo);
    btnLogout = findViewById(R.id.btnLogout);

    // me 호출
    userRepo.me(new Callback<UserProfileResponse>() {
      @Override public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> resp) {
        if (resp.isSuccessful() && resp.body() != null) {
          UserProfileResponse u = resp.body();
          tvInfo.setText("ID: " + u.getId() + "\nEmail: " + u.getEmail());
        } else {
          tvInfo.setText("불러오기 실패(" + resp.code() + ")");
        }
      }
      @Override public void onFailure(Call<UserProfileResponse> call, Throwable t) {
        tvInfo.setText("네트워크 오류");
      }
    });

    btnLogout.setOnClickListener(v -> {
      String refresh = TokenStore.getRefreshToken();
      if (refresh == null) {
        TokenStore.clear();
        gotoLogin();
        return;
      }
      authRepo.logout(refresh, new Callback<Void>() {
        @Override public void onResponse(Call<Void> call, Response<Void> resp) {
          if (resp.isSuccessful()) {
            Toast.makeText(MeActivity.this, "로그아웃", Toast.LENGTH_SHORT).show();
          } else {
            Toast.makeText(MeActivity.this, "로그아웃 실패(" + resp.code() + ")", Toast.LENGTH_SHORT).show();
          }
          gotoLogin();
        }
        @Override public void onFailure(Call<Void> call, Throwable t) {
          Toast.makeText(MeActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
          gotoLogin();
        }
      });
    });

  }

  private void gotoLogin() {
    TokenStore.clear();
    Intent i = new Intent(this, LoginActivity.class);
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
    finish();
  }
}