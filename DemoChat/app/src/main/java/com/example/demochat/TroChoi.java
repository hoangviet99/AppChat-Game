package com.example.demochat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TroChoi extends AppCompatActivity {
    Button choi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tro_choi);
        choi = findViewById(R.id.btn_Choi);

        choi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TroChoi.this, Caro.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
