package com.example.finalapp.appui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalapp.R;
import com.example.finalapp.ledger.camera.LedgerCameraActivity;
import com.example.finalapp.ledger.parse.ui.OcrInputActivity;


public class OcrFragment extends Fragment {

    private ActivityResultLauncher<Intent> cameraLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ocr, container, false);

        // 결과 받기용 런처 등록
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            // OcrInputActivity로 전송
                            Intent intent = new Intent(requireContext(), OcrInputActivity.class);
                            intent.putExtra(OcrInputActivity.EXTRA_IMAGE_URI, imageUri.toString());
                            startActivity(intent);
                        }
                    }
                });

        // 촬영 버튼
        Button btnCamera = root.findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LedgerCameraActivity.class);
            cameraLauncher.launch(intent);
        });

        return root;
    }
}