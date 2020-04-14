package com.example.demochat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class PickCharacter extends AppCompatActivity {

    boolean isFinish = false;
    ImageView[] imageViews_Characters;
    Bitmap[] bitmap_Characters;

    private void Mapping(){
        imageViews_Characters = new ImageView[35];
        imageViews_Characters[0] = findViewById(R.id.iv_Character_1);
        imageViews_Characters[1] = findViewById(R.id.iv_Character_2);
        imageViews_Characters[2] = findViewById(R.id.iv_Character_3);
        imageViews_Characters[3] = findViewById(R.id.iv_Character_4);
        imageViews_Characters[4] = findViewById(R.id.iv_Character_5);
        imageViews_Characters[5] = findViewById(R.id.iv_Character_6);
        imageViews_Characters[6] = findViewById(R.id.iv_Character_7);
        imageViews_Characters[7] = findViewById(R.id.iv_Character_8);
        imageViews_Characters[8] = findViewById(R.id.iv_Character_9);
        imageViews_Characters[9] = findViewById(R.id.iv_Character_10);
        imageViews_Characters[10] = findViewById(R.id.iv_Character_11);
        imageViews_Characters[11] = findViewById(R.id.iv_Character_12);
        imageViews_Characters[12] = findViewById(R.id.iv_Character_13);
        imageViews_Characters[13] = findViewById(R.id.iv_Character_14);
        imageViews_Characters[14] = findViewById(R.id.iv_Character_15);
        imageViews_Characters[15] = findViewById(R.id.iv_Character_16);
        imageViews_Characters[16] = findViewById(R.id.iv_Character_17);
        imageViews_Characters[17] = findViewById(R.id.iv_Character_18);
        imageViews_Characters[18] = findViewById(R.id.iv_Character_19);
        imageViews_Characters[19] = findViewById(R.id.iv_Character_20);
        imageViews_Characters[20] = findViewById(R.id.iv_Character_21);
        imageViews_Characters[21] = findViewById(R.id.iv_Character_22);
        imageViews_Characters[22] = findViewById(R.id.iv_Character_23);
        imageViews_Characters[23] = findViewById(R.id.iv_Character_24);
        imageViews_Characters[24] = findViewById(R.id.iv_Character_25);
        imageViews_Characters[25] = findViewById(R.id.iv_Character_26);
        imageViews_Characters[26] = findViewById(R.id.iv_Character_27);
        imageViews_Characters[27] = findViewById(R.id.iv_Character_28);
        imageViews_Characters[28] = findViewById(R.id.iv_Character_29);
        imageViews_Characters[29] = findViewById(R.id.iv_Character_30);
        imageViews_Characters[30] = findViewById(R.id.iv_Character_31);
        imageViews_Characters[31] = findViewById(R.id.iv_Character_32);
        imageViews_Characters[32] = findViewById(R.id.iv_Character_33);
        imageViews_Characters[33] = findViewById(R.id.iv_Character_34);
        imageViews_Characters[34] = findViewById(R.id.iv_Character_35);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pick_character);
        Mapping();
        bitmap_Characters = Create_ArrayBitmapCharacters();

        for(int i = 0; i < imageViews_Characters.length; i++){
            imageViews_Characters[i].setImageBitmap(GetBitmapCharacterAnimation(1, 0, bitmap_Characters[i]));
        }

        for(int i = 0; i < imageViews_Characters.length; i++){
            final int var_i = i;
            imageViews_Characters[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PickCharacter.this, Game.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("CharacterIndex", var_i);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private Bitmap GetBitmapCharacterAnimation(int MoveDirection, int index, Bitmap bitmap_Character){
        int height = bitmap_Character.getHeight()/4;
        int width = bitmap_Character.getWidth()/4;
        int w = bitmap_Character.getWidth();
        int h = bitmap_Character.getHeight();
        MoveDirection--;

        Bitmap bitmap;
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        bitmap = Bitmap.createBitmap(bitmap_Character, w/4*index, h/4*MoveDirection, width, height);
        canvas.drawBitmap(bitmap, 0, 0, null);

        return result;
    }

    private Bitmap[] Create_ArrayBitmapCharacters(){
        Bitmap[] bitmap_Characters = new Bitmap[35];
        bitmap_Characters[0] = BitmapFactory.decodeResource(getResources(), R.drawable.character_1);
        bitmap_Characters[1] = BitmapFactory.decodeResource(getResources(), R.drawable.character_2);
        bitmap_Characters[2] = BitmapFactory.decodeResource(getResources(), R.drawable.character_3);
        bitmap_Characters[3] = BitmapFactory.decodeResource(getResources(), R.drawable.character_4);
        bitmap_Characters[4] = BitmapFactory.decodeResource(getResources(), R.drawable.character_5);
        bitmap_Characters[5] = BitmapFactory.decodeResource(getResources(), R.drawable.character_6);
        bitmap_Characters[6] = BitmapFactory.decodeResource(getResources(), R.drawable.character_7);
        bitmap_Characters[7] = BitmapFactory.decodeResource(getResources(), R.drawable.character_8);
        bitmap_Characters[8] = BitmapFactory.decodeResource(getResources(), R.drawable.character_9);
        bitmap_Characters[9] = BitmapFactory.decodeResource(getResources(), R.drawable.character_10);
        bitmap_Characters[10] = BitmapFactory.decodeResource(getResources(), R.drawable.character_11);
        bitmap_Characters[11] = BitmapFactory.decodeResource(getResources(), R.drawable.character_12);
        bitmap_Characters[12] = BitmapFactory.decodeResource(getResources(), R.drawable.character_13);
        bitmap_Characters[13] = BitmapFactory.decodeResource(getResources(), R.drawable.character_14);
        bitmap_Characters[14] = BitmapFactory.decodeResource(getResources(), R.drawable.character_15);
        bitmap_Characters[15] = BitmapFactory.decodeResource(getResources(), R.drawable.character_16);
        bitmap_Characters[16] = BitmapFactory.decodeResource(getResources(), R.drawable.character_17);
        bitmap_Characters[17] = BitmapFactory.decodeResource(getResources(), R.drawable.character_18);
        bitmap_Characters[18] = BitmapFactory.decodeResource(getResources(), R.drawable.character_19);
        bitmap_Characters[19] = BitmapFactory.decodeResource(getResources(), R.drawable.character_20);
        bitmap_Characters[20] = BitmapFactory.decodeResource(getResources(), R.drawable.character_21);
        bitmap_Characters[21] = BitmapFactory.decodeResource(getResources(), R.drawable.character_22);
        bitmap_Characters[22] = BitmapFactory.decodeResource(getResources(), R.drawable.character_23);
        bitmap_Characters[23] = BitmapFactory.decodeResource(getResources(), R.drawable.character_24);
        bitmap_Characters[24] = BitmapFactory.decodeResource(getResources(), R.drawable.character_25);
        bitmap_Characters[25] = BitmapFactory.decodeResource(getResources(), R.drawable.character_26);
        bitmap_Characters[26] = BitmapFactory.decodeResource(getResources(), R.drawable.character_27);
        bitmap_Characters[27] = BitmapFactory.decodeResource(getResources(), R.drawable.character_28);
        bitmap_Characters[28] = BitmapFactory.decodeResource(getResources(), R.drawable.character_29);
        bitmap_Characters[29] = BitmapFactory.decodeResource(getResources(), R.drawable.character_30);
        bitmap_Characters[30] = BitmapFactory.decodeResource(getResources(), R.drawable.character_31);
        bitmap_Characters[31] = BitmapFactory.decodeResource(getResources(), R.drawable.character_32);
        bitmap_Characters[32] = BitmapFactory.decodeResource(getResources(), R.drawable.character_33);
        bitmap_Characters[33] = BitmapFactory.decodeResource(getResources(), R.drawable.character_34);
        bitmap_Characters[34] = BitmapFactory.decodeResource(getResources(), R.drawable.character_35);
        return bitmap_Characters;
    }

    @Override
    public void onBackPressed() {
        if(!isFinish){
            isFinish = true;
            Toast.makeText(this, "Nhấn quay lại lần nữa để thoát game!", Toast.LENGTH_SHORT).show();
        }
        else {
            finish();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isFinish = false;
            }
        }, 2500);
    }
}
