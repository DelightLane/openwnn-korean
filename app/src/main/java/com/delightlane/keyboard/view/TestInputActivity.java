package com.delightlane.keyboard.view;

import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.delightlane.keyboard.R;

public class TestInputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_input);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.preference_test_input_menu);
        }

        EditText singleLineInput = findViewById(R.id.test_input_single);
        EditText multiLineInput = findViewById(R.id.test_input_multi);

        findViewById(R.id.select_ime_button).setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if(imm != null) {
                imm.showInputMethodPicker();
            }
        });

        findViewById(R.id.clear_button).setOnClickListener(v -> {
            singleLineInput.setText("");
            multiLineInput.setText("");
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scroll_view), (OnApplyWindowInsetsListener) (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
