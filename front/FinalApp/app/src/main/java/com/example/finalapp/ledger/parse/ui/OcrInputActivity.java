package com.example.finalapp.ledger.parse.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalapp.R;
import com.example.finalapp.ledger.data.gpt.GptRepository;
import com.example.finalapp.ledger.data.repo.EntryRepository;
import com.example.finalapp.ledger.dto.EntryItemResponse;
import com.example.finalapp.ledger.dto.EntryResponse;
import com.example.finalapp.ledger.dto.GptResponse;
import com.example.finalapp.visionapi.api.VisionApiClient;
import com.example.finalapp.visionapi.api.VisionApiService;
import com.example.finalapp.visionapi.dto.VisionRequest;
import com.example.finalapp.visionapi.dto.VisionResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.NonNull;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Vision OCR → GPT 정제 → 결과 확인 후 버튼 클릭 시 서버 전송 → CSV/XLSX 선택 저장
 */
public class OcrInputActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri";

    private TextView tvAllText;
    private TextView tvSummary;
    private Button btnSendServer;
    private Button btnRefineText;   // GPT 정제 버튼
    private Button btnExportCsv;    // CSV 내보내기 버튼
    private Button btnExportXlsx;   // 엑셀 내보내기 버튼

    private String lastRefinedText;
    private File lastExportCache;
    private AlertDialog exportDialog;

    private EntryRepository entryRepo;
    private GptRepository gptRepo;

    private Call<EntryResponse> parseCall;
    private Call<ResponseBody> exportCall;

    private String currentFromDate;
    private String currentToDate;

    private final ActivityResultLauncher<Intent> createDocLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    tvSummary.append("\n[저장 취소]");
                    return;
                }

                Uri dest = result.getData().getData();
                if (dest == null || lastExportCache == null || !lastExportCache.exists()) {
                    tvSummary.append("\n[저장 실패: 파일 없음]");
                    return;
                }

                try (InputStream in = new FileInputStream(lastExportCache);
                     OutputStream out = getContentResolver().openOutputStream(dest, "w")) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                    out.flush();
                    tvSummary.append("\n[저장 완료] " + dest.getPath());
                } catch (Exception e) {
                    tvSummary.append("\n[저장 오류] " + e.getMessage());
                } finally {
                    if (lastExportCache != null && lastExportCache.exists()) {
                        lastExportCache.delete();
                        lastExportCache = null;
                    }
                }
            });

    /**
     * OCR 이미지 수신 및 Vision 분석 시작
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_input);

        tvAllText = findViewById(R.id.tvAllText);
        tvSummary = findViewById(R.id.tvSummary);
        btnSendServer = findViewById(R.id.btnSendToServer);
        btnRefineText = findViewById(R.id.btnRefineText);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        btnExportXlsx = findViewById(R.id.btnExportXlsx);

        entryRepo = new EntryRepository();
        gptRepo = new GptRepository();

        btnSendServer.setVisibility(android.view.View.GONE);
        btnRefineText.setVisibility(android.view.View.GONE);
        btnExportCsv.setVisibility(android.view.View.GONE);
        btnExportXlsx.setVisibility(android.view.View.GONE);

        // GPT 정제 버튼
        btnRefineText.setOnClickListener(v -> {
            String text = tvAllText.getText().toString();
            if (text == null || text.trim().isEmpty()) {
                Toast.makeText(this, "OCR 텍스트가 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }
            tvSummary.setText("GPT 정제 중...");
            requestGptRefine(text);
        });

        // 서버 전송 버튼
        btnSendServer.setOnClickListener(v -> {
            if (lastRefinedText != null && !lastRefinedText.trim().isEmpty()) {
                tvSummary.setText("서버 전송 중...");
                requestParse(lastRefinedText);
            } else {
                Toast.makeText(this, "정제된 내용이 없습니다", Toast.LENGTH_SHORT).show();
            }
        });

        // CSV / XLSX 버튼 클릭 리스너 (초기에는 숨김)
        btnExportCsv.setOnClickListener(v -> {
            btnExportCsv.setEnabled(false);
            tvSummary.append("\nCSV 파일 생성 중...");
            downloadCsv(currentFromDate, currentToDate);
        });

        btnExportXlsx.setOnClickListener(v -> {
            btnExportXlsx.setEnabled(false);
            tvSummary.append("\n엑셀 파일 생성 중...");
            downloadXlsx(currentFromDate, currentToDate);
        });

        String uriStr = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        if (uriStr == null) {
            tvSummary.setText("이미지 경로 없음");
            return;
        }

        runOcrThenRefine(Uri.parse(uriStr));
    }

    /**
     * Vision API 호출 후 OCR 결과 표시 (GPT 정제는 버튼으로 실행)
     */
    private void runOcrThenRefine(Uri uri) {
        tvSummary.setText("Vision API 분석 중...");
        final String apiKey = "AIzaSyDwEk2tf_NRdn8_piS43GUnKgpGwB0ppZA"; // Vision API 키

        callVisionOcrWithDocMode(uri, apiKey, new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> resp) {
                runOnUiThread(() -> {
                    String text = (resp != null && resp.body() != null) ? resp.body() : "";
                    tvAllText.setText(text.isEmpty() ? "텍스트 없음" : text);

                    if (text.isEmpty()) {
                        tvSummary.setText("텍스트 없음");
                        return;
                    }

                    tvSummary.setText("OCR 완료 — GPT 정제를 실행하려면 버튼을 누르세요");
                    btnRefineText.setVisibility(android.view.View.VISIBLE);
                });
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                runOnUiThread(() ->
                        tvSummary.setText("Vision API 실패: " + (t == null ? "unknown" : t.getMessage())));
            }
        });
    }

    /**
     * Vision API 호출
     */
    private void callVisionOcrWithDocMode(@NonNull Uri imageUri, @NonNull String apiKey, @NonNull Callback<String> cb) {
        try {
            byte[] bytes;
            try (InputStream in = getContentResolver().openInputStream(imageUri);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) bos.write(buf, 0, r);
                bytes = bos.toByteArray();
            }

            final String b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
            VisionRequest body = VisionRequest.fromBase64(b64, "DOCUMENT_TEXT_DETECTION",
                    java.util.Arrays.asList("ko", "en"));

            VisionApiClient client = VisionApiClient.getInstance();
            client.setApiKey(apiKey);
            VisionApiService svc = client.getService();

            svc.annotate(body).enqueue(new Callback<VisionResponse>() {
                @Override
                public void onResponse(Call<VisionResponse> call, Response<VisionResponse> resp) {
                    if (!resp.isSuccessful() || resp.body() == null) {
                        cb.onFailure(null, new RuntimeException("Vision 실패: " + resp.code()));
                        return;
                    }
                    cb.onResponse(null, Response.success(resp.body().getFullTextOrEmpty()));
                }

                @Override
                public void onFailure(Call<VisionResponse> call, Throwable t) {
                    cb.onFailure(null, t);
                }
            });
        } catch (Exception e) {
            cb.onFailure(null, e);
        }
    }

    /**
     * GPT로 OCR 텍스트 정제 (서버 호출 버전)
     */
    private void requestGptRefine(String rawText) {
        tvSummary.setText("GPT 정제 중...");

        gptRepo.refineWithServer(rawText).enqueue(new retrofit2.Callback<GptResponse>() {
            @Override
            public void onResponse(retrofit2.Call<GptResponse> call,
                                   retrofit2.Response<GptResponse> resp) {

                try {
                    Log.e("GPT", "서버 응답 코드 = " + resp.code());
                    if (resp.errorBody() != null) {
                        Log.e("GPT", "서버 응답 본문 = " + resp.errorBody().string());
                    }

                    if (!resp.isSuccessful() || resp.body() == null) {
                        tvSummary.setText("서버 GPT 응답 실패(" + resp.code() + ")");
                        return;
                    }

                    String refined = resp.body().getContentOrEmpty();
                    if (refined.isEmpty()) {
                        tvSummary.setText("GPT 결과 없음");
                        return;
                    }

                    lastRefinedText = refined;
                    tvAllText.setText(refined);
                    tvSummary.setText("GPT 정제 완료 — 결과 확인 후 서버 전송 가능");
                    btnSendServer.setVisibility(android.view.View.VISIBLE);
                    btnRefineText.setVisibility(android.view.View.GONE);

                } catch (Exception e) {
                    Log.e("GPT", "응답 처리 중 예외", e);
                    tvSummary.setText("서버 응답 파싱 실패");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<GptResponse> call, Throwable t) {
                tvSummary.setText("서버 GPT 요청 실패: " + (t == null ? "unknown" : t.getMessage()));
            }
        });
    }

    /**
     * 서버로 전송 후 CSV/XLSX 버튼 표시
     */
    private void requestParse(String refinedText) {
        parseCall = entryRepo.parseText(refinedText);
        parseCall.enqueue(new Callback<EntryResponse>() {
            @Override
            public void onResponse(Call<EntryResponse> call, Response<EntryResponse> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    int code = resp.code();
                    String msg = "서버 분석 실패(" + code + ")";
                    if (code == 401 || code == 403) msg += " - 로그인/토큰 필요";
                    tvSummary.setText(msg);
                    return;
                }

                EntryResponse b = resp.body();
                currentFromDate = b.getDate();
                currentToDate = b.getDate();

                StringBuilder sb = new StringBuilder();
                sb.append("서버 저장 완료\n")
                        .append("상호 = ").append(safe(b.getMerchant())).append("\n")
                        .append("날짜 = ").append(safeDate(b.getDate())).append("\n")
                        .append("결제 = ").append(safe(b.getPaymentMethod())).append("\n")
                        .append("카테고리 = ").append(safe(b.getCategory())).append("\n")
                        .append("총액 = ").append(safeNum(b.getTotal())).append("원\n\n");

                if (b.getItems() != null && !b.getItems().isEmpty()) {
                    sb.append("[품목 목록]\n");
                    for (EntryItemResponse item : b.getItems()) {
                        sb.append("- ").append(safe(item.getName()))
                                .append(" / 수량: ").append(safeNum(item.getQuantity()))
                                .append(" / 단가: ").append(safeNum(item.getPrice())).append("원\n")
                                .append(" / 금액: ").append(safeNum(item.getAmount()))
                                .append("원\n");
                    }
                } else {
                    sb.append("[품목 없음]");
                }

                tvSummary.setText(sb.toString());

                // ✅ 서버 전송 버튼 비활성화 (중복 방지)
                btnSendServer.setEnabled(false);
                btnSendServer.setText("전송 완료됨");
                btnSendServer.setAlpha(0.5f);

                // ✅ CSV / XLSX 버튼 표시
                btnExportCsv.setVisibility(android.view.View.VISIBLE);
                btnExportXlsx.setVisibility(android.view.View.VISIBLE);
                btnExportCsv.setEnabled(true);
                btnExportXlsx.setEnabled(true);
            }

            @Override
            public void onFailure(Call<EntryResponse> call, Throwable t) {
                tvSummary.setText("서버 전송 실패: " + (t == null ? "unknown" : t.getMessage()));
            }
        });
    }

    /**
     * CSV 다운로드
     */
    private void downloadCsv(String from, String to) {
        exportCall = entryRepo.exportCsv(from, to);
        exportCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    tvSummary.append("\nCSV 다운로드 실패 (" + resp.code() + ")");
                    return;
                }

                try {
                    String fileName = "ledger_items_" + from + "_" + to + ".csv";

                    // 문서(Documents) 폴더에 저장
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

                    Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                    if (uri == null) {
                        tvSummary.append("\nCSV 저장 실패 (파일 생성 오류)");
                        return;
                    }

                    try (OutputStream out = getContentResolver().openOutputStream(uri);
                         InputStream in = resp.body().byteStream()) {

                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                    }

                    tvSummary.append("\nCSV 저장 완료 (문서 폴더)");
                    btnExportCsv.setText("CSV 저장 완료");
                    btnExportCsv.setAlpha(0.5f);
                    btnExportCsv.setEnabled(false);

                } catch (Exception e) {
                    tvSummary.append("\nCSV 저장 실패: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                tvSummary.append("\nCSV 네트워크 오류");
            }
        });
    }


    /**
     * XLSX 다운로드
     */
    private void downloadXlsx(String from, String to) {
        exportCall = entryRepo.exportXlsx(from, to);
        exportCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    tvSummary.append("\n엑셀 다운로드 실패 (" + resp.code() + ")");
                    return;
                }

                try {
                    String fileName = "ledger_items_" + from + "_" + to + ".xlsx";

                    // 문서(Documents) 폴더에 저장
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE,
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

                    Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                    if (uri == null) {
                        tvSummary.append("\n엑셀 저장 실패 (파일 생성 오류)");
                        return;
                    }

                    try (OutputStream out = getContentResolver().openOutputStream(uri);
                         InputStream in = resp.body().byteStream()) {

                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                    }

                    tvSummary.append("\n엑셀 저장 완료 (문서 폴더)");
                    btnExportXlsx.setText("엑셀 저장 완료");
                    btnExportXlsx.setAlpha(0.5f);
                    btnExportXlsx.setEnabled(false);

                } catch (Exception e) {
                    tvSummary.append("\n엑셀 저장 실패: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                tvSummary.append("\n엑셀 네트워크 오류");
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (parseCall != null) parseCall.cancel();
        if (exportCall != null) exportCall.cancel();
        if (exportDialog != null && exportDialog.isShowing()) exportDialog.dismiss();
    }

    private String safe(String v) {
        return v == null ? "-" : v;
    }

    private String safeDate(String date) {
        return (date == null || date.isEmpty()) ? "" : date;
    }

    private String safeNum(Number n) {
        return n == null ? "-" : String.valueOf(n);
    }
}