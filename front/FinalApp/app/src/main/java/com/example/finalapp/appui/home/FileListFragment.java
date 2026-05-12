package com.example.finalapp.appui.home;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileListFragment extends Fragment {

    private RecyclerView recyclerFiles;
    private TextView tvEmpty;
    private Button btnCsvTab, btnXlsxTab;
    private FileAdapter adapter;

    private enum Mode { CSV, XLSX }
    private Mode currentMode = Mode.CSV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_file_list, container, false);

        recyclerFiles = root.findViewById(R.id.recyclerFiles);
        tvEmpty = root.findViewById(R.id.tvEmpty);
        btnCsvTab = root.findViewById(R.id.btnCsvTab);
        btnXlsxTab = root.findViewById(R.id.btnXlsxTab);

        recyclerFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FileAdapter();
        recyclerFiles.setAdapter(adapter);

        btnCsvTab.setOnClickListener(v -> switchMode(Mode.CSV));
        btnXlsxTab.setOnClickListener(v -> switchMode(Mode.XLSX));

        loadFiles();
        return root;
    }

    private void switchMode(Mode mode) {
        if (mode == currentMode) return;
        currentMode = mode;

        if (mode == Mode.CSV) {
            btnCsvTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3));
            btnCsvTab.setTextColor(0xFFFFFFFF);
            btnXlsxTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFBDBDBD));
            btnXlsxTab.setTextColor(0xFF222222);
        } else {
            btnCsvTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFBDBDBD));
            btnCsvTab.setTextColor(0xFF222222);
            btnXlsxTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3));
            btnXlsxTab.setTextColor(0xFFFFFFFF);
        }

        loadFiles();
    }

    /**
     * CSV/XLSX 파일을 두 경로에서 모두 불러오기
     */
    private void loadFiles() {
        boolean isCsv = (currentMode == Mode.CSV);

        // 1) 앱 내부 저장소에서 찾기
        List<UriFile> internalFiles = loadInternalFiles(isCsv);

        // 2) 문서 폴더(MediaStore Documents)에서 찾기 (CSV MIME 지원 확장)
        List<UriFile> documentFiles = loadDocumentFiles(isCsv);

        // 3) 합치기
        List<UriFile> all = new ArrayList<>();
        all.addAll(internalFiles);
        all.addAll(documentFiles);

        if (all.isEmpty()) {
            tvEmpty.setText(isCsv ? "저장된 CSV 파일이 없습니다."
                    : "저장된 엑셀 파일이 없습니다.");
            showEmpty(true);
            return;
        }

        // 최신순 정렬
        all.sort((a, b) -> Long.compare(b.lastModified, a.lastModified));

        showEmpty(false);
        adapter.setItems(all);
    }

    private void showEmpty(boolean empty) {
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerFiles.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    /**
     * 1) 앱 내부 저장소 파일 확인
     */
    private List<UriFile> loadInternalFiles(boolean isCsvMode) {
        List<UriFile> list = new ArrayList<>();
        File dir = requireContext().getExternalFilesDir(null);

        if (dir == null) return list;

        File[] files = dir.listFiles((f, name) ->
                isCsvMode ? name.endsWith(".csv") : name.endsWith(".xlsx"));

        if (files == null) return list;

        for (File f : files) {
            list.add(new UriFile(
                    f.getName(),
                    Uri.fromFile(f),
                    f.lastModified(),
                    f.length()
            ));
        }
        return list;
    }

    /**
     * 2) 문서(Documents) 폴더(MediaStore) CSV/XLSX 조회
     * CSV MIME 타입을 여러 개 지원한다.
     */
    private List<UriFile> loadDocumentFiles(boolean isCsvMode) {

        List<UriFile> result = new ArrayList<>();
        ContentResolver resolver = requireContext().getContentResolver();

        Uri collection = MediaStore.Files.getContentUri("external");

        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.SIZE
        };

        String selection;
        String[] selectionArgs;

        if (isCsvMode) {
            // CSV MIME 타입은 기기별로 다르게 저장되므로 OR 조건으로 검색
            selection =
                    MediaStore.Files.FileColumns.RELATIVE_PATH + " LIKE ? AND (" +
                            MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
                            MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
                            MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
                            MediaStore.Files.FileColumns.MIME_TYPE + "=?)";

            selectionArgs = new String[]{
                    "Documents/%",
                    "text/csv",
                    "text/comma-separated-values",
                    "application/csv",
                    "application/vnd.ms-excel"
            };

        } else {
            // XLSX 는 MIME 타입 고정
            selection =
                    MediaStore.Files.FileColumns.RELATIVE_PATH + " LIKE ? AND " +
                            MediaStore.Files.FileColumns.MIME_TYPE + "=?";

            selectionArgs = new String[]{
                    "Documents/%",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            };
        }

        try (Cursor cursor = resolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
        )) {

            if (cursor == null) return result;

            int colId = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            int colName = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
            int colDate = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
            int colSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);

            while (cursor.moveToNext()) {

                long id = cursor.getLong(colId);
                String name = cursor.getString(colName);
                long lastMod = cursor.getLong(colDate) * 1000L;
                long size = cursor.getLong(colSize);

                Uri uri = Uri.withAppendedPath(collection, String.valueOf(id));

                result.add(new UriFile(name, uri, lastMod, size));
            }
        }
        return result;
    }

    /** 파일 모델 */
    private static class UriFile {
        String name;
        Uri uri;
        long lastModified;
        long size;

        UriFile(String name, Uri uri, long lastModified, long size) {
            this.name = name;
            this.uri = uri;
            this.lastModified = lastModified;
            this.size = size;
        }
    }

    /** RecyclerView Adapter */
    private class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

        private final List<UriFile> items = new ArrayList<>();

        void setItems(List<UriFile> list) {
            items.clear();
            items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new FileViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    /** ViewHolder */
    private class FileViewHolder extends RecyclerView.ViewHolder {

        private final TextView text1;
        private final TextView text2;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }

        void bind(UriFile file) {
            text1.setText(file.name);
            text2.setText(DateFormat.format("yyyy-MM-dd HH:mm", file.lastModified));

            itemView.setOnClickListener(v -> openFile(file));
        }
    }

    /** 파일 열기 */
    private void openFile(UriFile file) {
        try {
            String mime = file.name.endsWith(".csv")
                    ? "text/csv"
                    : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(file.uri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "파일 열기"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
