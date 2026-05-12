package com.example.finalapp.appui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.finalapp.R;
import com.example.finalapp.appui.home.HomePagerAdapter;
import com.example.finalapp.core.network.ApiService;
import com.example.finalapp.core.network.RetrofitClient;
import com.example.finalapp.core.storage.TokenStore;
import com.example.finalapp.user.data.UserRepository;
import com.example.finalapp.user.dto.UserProfileResponse;
import com.example.finalapp.user.ui.MeActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private HomePagerAdapter pagerAdapter;
    private TextView tvLoginStatus;

    private UserRepository userRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tvLoginStatus = findViewById(R.id.tvLoginStatus);

        pagerAdapter = new HomePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("촬영");
            else tab.setText("파일보기");
        }).attach();

        Button btnMyInfo = findViewById(R.id.btnMyInfo);
        Button btnLogout = findViewById(R.id.btnLogout);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        userRepo = new UserRepository(api);

        updateLoginStatus();

        btnMyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MeActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            TokenStore.clear();
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    /** 로그인 상태 확인 후 닉네임 표시 */
    private void updateLoginStatus() {
        String access = TokenStore.getAccessToken();
        if (access == null || access.isEmpty()) {
            tvLoginStatus.setText("로그인 필요");
            return;
        }

        // 서버에서 닉네임 조회
        userRepo.me(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    UserProfileResponse profile = resp.body();
                    String nick = profile.getNickname();
                    tvLoginStatus.setText((nick != null && !nick.isEmpty())
                            ? nick
                            : "로그인됨");
                } else {
                    tvLoginStatus.setText("로그인됨 (프로필 불러오기 실패)");
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                tvLoginStatus.setText("로그인됨 (서버 연결 실패)");
            }
        });
    }
}
