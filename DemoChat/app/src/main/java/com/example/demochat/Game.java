package com.example.demochat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RenderNode;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.BoringLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.zip.CheckedOutputStream;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Game extends AppCompatActivity {

    LinearLayout[] linearLayouts_Players;
    FrameLayout frameLayout_Game;
    FrameLayout frameLayout_Bomb;
    FrameLayout frameLayout_Explosion;
    FrameLayout frameLayout_Death;
    FrameLayout frameLayout_Chat;
    LinearLayout linearLayout_MyPlayer;
    ImageView iv_Time;
    Socket mSocket;
    String pref_Username;
    int myID, myID1;
    ImageButton btn_arrow_down;
    ImageButton btn_arrow_up;
    ImageButton btn_arrow_left;
    ImageButton btn_arrow_right;
    ImageButton btn_setBomb;
    ImageButton btn_Chat;
    double coordinates_ratio_x = 0.0, coordinates_ratio_y = 0.0;
    CountDownTimer[] countDownTimer_Move = new CountDownTimer[1];
    CountDownTimer[] countDownTimer_SetBomb = new CountDownTimer[1];
    CountDownTimer[] countDownTimer_CharacterAnimation = new CountDownTimer[16];
    int MOVE_DOWN = 1, MOVE_LEFT = 2, MOVE_RIGHT = 3, MOVE_UP = 4;
    int currMoveDirectory;
    double x, y;
    double speed;
    boolean isFinish = false;
    DisplayMetrics metrics;
    Bitmap[] bitmap_Characters;
    Bitmap bitmap_Explosion;
    int myCharacterIndex = -1;
    int[] arrCharacterIndex = new int[16];
    ImageView iv_MyPlayer;
    int numberPlayer;
    ImageView[] imageViews_Players = new ImageView[16];
    TextView[] textViews_PlayerUsernames = new TextView[16];
    TextView[] textViews_PlayerChat = new TextView[16];
    String[] flags_PlayerAnimation = new String[16];
    boolean isTouch_BtnMove = false;
    boolean isDead = false;
    int numBombSet = 0;
    int maxBombSet = 3;
    MediaPlayer mediaPlayer_BackgroundMusic;

    private void Mapping(){
        linearLayouts_Players =     new LinearLayout[16];
        frameLayout_Game =          findViewById(R.id.frameLayout_Game);
        frameLayout_Bomb =          findViewById(R.id.frameLayout_Bomb);
        frameLayout_Death =         findViewById(R.id.frameLayout_Death);
        frameLayout_Chat =         findViewById(R.id.frameLayout_Chat);
        iv_Time =                   findViewById(R.id.iv_Time);
        frameLayout_Explosion =     findViewById(R.id.frameLayout_Explosion);
        btn_arrow_down =            findViewById(R.id.btn_arrow_down);
        btn_arrow_up =              findViewById(R.id.btn_arrow_up);
        btn_arrow_left =            findViewById(R.id.btn_arrow_left);
        btn_arrow_right =           findViewById(R.id.btn_arrow_right);
        btn_setBomb =               findViewById(R.id.btn_setBomb);
        btn_Chat =                  findViewById(R.id.btn_Chat);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        Mapping();

        bitmap_Characters = Create_ArrayBitmapCharacters();
        bitmap_Explosion = BitmapFactory.decodeResource(getResources(), R.drawable.explosion);

        pref_Username = Get_SharedPreference_Username();
        if(pref_Username.equals("")){
            Toast.makeText(this, "Crash nhé :v", Toast.LENGTH_SHORT).show();
            finish();
        }
        if(!pref_Username.equals("Sokou")) {
            Start_MusicBackground();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            myCharacterIndex = bundle.getInt("CharacterIndex", -1);
        }

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        x = (double) metrics.widthPixels;
        y = (double) metrics.heightPixels;
        x -= x/13;
        y -= y/13;
        speed = x/85;


        if(pref_Username.equals("Zipra")){
            maxBombSet = 18;
            speed = x/75;
        }

        try {
            mSocket = IO.socket("http://14.184.254.84:3508/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket.connect();

        mSocket.on("Server-reply-Login", onServerReplyLogin);
        mSocket.on("Server-reply-Moving", onServerReplyMoving);
        mSocket.on("Server-reply-Animation", onServerReplyAnimation);
        mSocket.on("Server-reply-Set-Bomb", onServerReplySetBomb);
        mSocket.on("Server-reply-Dead", onServerReplyDead);
        mSocket.on("Server-reply-Immortal", onServerReplyImmortal);
        mSocket.on("Server-reply-Chat", onServerReplyChatting);

        Emit_PlayerRequestLogin();
        SetOnTouchButtonMoving(countDownTimer_Move);

        countDownTimer_SetBomb[0] = new CountDownTimer(99999999,200) {
            @Override
            public void onTick(long l) {
                if(numBombSet < maxBombSet) {
                    mSocket.emit("Client-request-Set-Bomb", pref_Username, coordinates_ratio_x, coordinates_ratio_y);
                    numBombSet++;
                }
            }

            @Override
            public void onFinish() {

            }
        };

        btn_setBomb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    countDownTimer_SetBomb[0].start();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    countDownTimer_SetBomb[0].cancel();
                }
                return false;
            }
        });

        btn_Chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Emit_PlayerChatting("Đôi chân dạo quanh nơi khắp phố xã bụi bay vào mắt yah!\nĐập vào đôi mắt anh muốn chới với...");
                ShowDialog_Chat();
            }
        });
    }

    private void Start_MusicBackground(){
        mediaPlayer_BackgroundMusic = MediaPlayer.create(Game.this, R.raw.background);
        mediaPlayer_BackgroundMusic.setLooping(true);
        mediaPlayer_BackgroundMusic.start();
    }

    private void SetBomb(double coor_X, double coor_Y){
        double X, Y;
        X = coor_X*x;
        Y = coor_Y*y;
        X += ((x/13)-(x/20))/3*2;
        Y += (x/13)-(x/20);

        final ImageView imageView_Bomb = new ImageView(this);

        imageView_Bomb.setImageResource(R.drawable.bomb);
        imageView_Bomb.setLayoutParams(new ViewGroup.LayoutParams((int)x/20,(int)x/20));
        imageView_Bomb.setTranslationX((float)X);
        imageView_Bomb.setTranslationY((float)Y);

        frameLayout_Bomb.addView(imageView_Bomb);

        final float x_Explosion, y_Explosion, x_Bomb, y_Bomb;
        x_Bomb = (float)(X + x/40);
        y_Bomb = (float)(Y + x/40);
        x_Explosion = (float)(X - x/8 + x/40);
        y_Explosion = (float)(Y - x/8 + x/40);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!pref_Username.equals("Sokou")) {
                    MediaPlayer.create(Game.this, R.raw.explosion).start();
                }
                frameLayout_Bomb.removeView(imageView_Bomb);
                numBombSet--;
                Start_ExplosionAnimation(bitmap_Explosion, x_Explosion, y_Explosion);
                if(!isDead) {
                    double x_Character = linearLayout_MyPlayer.getTranslationX() + (x / 26);
                    double y_Character = linearLayout_MyPlayer.getTranslationY() + (x / 26);
                    isDead = Check_Is_Death_Explosion(x_Character, y_Character, (double) x_Bomb, (double) y_Bomb);
                    if(isDead){
                        if(pref_Username.equals("Zipra")) {
                            Emit_PlayerImmortal();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isDead = false;
                                }
                            }, 5000);
                        }
                        else {
                            CharacterDeath();
                        }
                    }
                }
            }
        }, 1500);
    }

    private void ShowDialog_Chat(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chat);

        final EditText editText_Chat =    dialog.findViewById(R.id.editText_Chat);
        Button btn_Send =                 dialog.findViewById(R.id.btn_Send);

        btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText_Chat.getText().toString().trim().length() > 0){
                    Emit_PlayerChatting(editText_Chat.getText().toString().trim());
                    dialog.cancel();
                }
            }
        });

        dialog.show();
    }

    private void CharacterDeath(){
        if(countDownTimer_Move[0] != null) {
            countDownTimer_Move[0].cancel();
        }
        if(countDownTimer_SetBomb[0] != null) {
            countDownTimer_SetBomb[0].cancel();
        }
        if(isTouch_BtnMove) {
            Emit_PlayerRequestAnimation(currMoveDirectory + 4);
            isTouch_BtnMove = false;
        }
        frameLayout_Death.setVisibility(View.VISIBLE);
        btn_arrow_down.setVisibility(View.GONE);
        btn_arrow_up.setVisibility(View.GONE);
        btn_arrow_right.setVisibility(View.GONE);
        btn_arrow_left.setVisibility(View.GONE);
        btn_setBomb.setVisibility(View.GONE);
        Emit_PlayerDead();
        new CountDownTimer(99999, 1000){
            int time = 4;
            @Override
            public void onTick(long l) {
                time--;
                if(time == 3){
                    iv_Time.setImageResource(R.drawable.time_3);
                }
                if(time == 2){
                    iv_Time.setImageResource(R.drawable.time_2);
                }
                if(time == 1){
                    iv_Time.setImageResource(R.drawable.time_1);
                }
                if(time < 1){
                    frameLayout_Death.setVisibility(View.GONE);
                    btn_arrow_down.setVisibility(View.VISIBLE);
                    btn_arrow_up.setVisibility(View.VISIBLE);
                    btn_arrow_right.setVisibility(View.VISIBLE);
                    btn_arrow_left.setVisibility(View.VISIBLE);
                    btn_setBomb.setVisibility(View.VISIBLE);
                    new CountDownTimer(1500, 500){
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            isDead = false;
                        }
                    }.start();
                    this.cancel();
                    Emit_PlayerImmortal();
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    private boolean Check_Is_Death_Explosion(double x_Character, double y_Character, double x_Bomb, double y_Bomb){
        double distance;
        double width, height;
        width = Math.abs(x_Character - x_Bomb);
        height = Math.abs(y_Character - y_Bomb);
        distance = Math.sqrt(Math.pow(width,2) + Math.pow(height,2));
        if(distance <= x/8){
            return true;
        }
        return false;
    }

    private void Start_ExplosionAnimation(final Bitmap bitmap_Explosion, float X, float Y){
        final ImageView imageView_Explosion = new ImageView(this);

        imageView_Explosion.setLayoutParams(new ViewGroup.LayoutParams((int)x/4,(int)x/4));
        imageView_Explosion.setTranslationX(X);
        imageView_Explosion.setTranslationY(Y);

        frameLayout_Explosion.addView(imageView_Explosion);

        new CountDownTimer(9999999, 50){
            int i = 0, j = 0;
            @Override
            public void onTick(long l) {
                imageView_Explosion.setImageBitmap(GetBitmapExplore(bitmap_Explosion, i, j));
                i++;
                if(i > 4){
                    i = 0;
                    j++;
                    if(j > 4){
                        frameLayout_Explosion.removeView(imageView_Explosion);
                        this.cancel();
                    }
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    private Bitmap GetBitmapExplore(Bitmap bitmap_Explosion, int x, int y){
        int width = bitmap_Explosion.getWidth()/5;
        int height = bitmap_Explosion.getHeight()/5;

        Bitmap bitmap;
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        bitmap = Bitmap.createBitmap(bitmap_Explosion, width*x, height*y, width, height);
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

    private void SetOnTouchButtonMoving(final CountDownTimer[] countDownTimer_Move){
        btn_arrow_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    countDownTimer_Move[0] = GetCountDownTimer_PlayerMoving(MOVE_RIGHT);
                    if (!isTouch_BtnMove) {
                        currMoveDirectory = MOVE_RIGHT;
                        isTouch_BtnMove = true;
                        Emit_PlayerRequestAnimation(MOVE_RIGHT);
                        countDownTimer_Move[0].start();
                    }
                    else {
                        return false;
                    }
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (currMoveDirectory == MOVE_RIGHT && isTouch_BtnMove) {
                        Emit_PlayerRequestAnimation(MOVE_RIGHT + 4);
                        countDownTimer_Move[0].cancel();
                        isTouch_BtnMove = false;
                    }
                }
                return false;
            }
        });

        btn_arrow_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isTouch_BtnMove) {
                        currMoveDirectory = MOVE_LEFT;
                        countDownTimer_Move[0] = GetCountDownTimer_PlayerMoving(MOVE_LEFT);
                        isTouch_BtnMove = true;
                        Emit_PlayerRequestAnimation(MOVE_LEFT);
                        countDownTimer_Move[0].start();
                    }
                    else {
                        return false;
                    }
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if (currMoveDirectory == MOVE_LEFT && isTouch_BtnMove == true) {
                        Emit_PlayerRequestAnimation(MOVE_LEFT + 4);
                        countDownTimer_Move[0].cancel();
                        isTouch_BtnMove = false;
                    }
                }
                return false;
            }
        });

        btn_arrow_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isTouch_BtnMove) {
                        currMoveDirectory = MOVE_UP;
                        countDownTimer_Move[0] = GetCountDownTimer_PlayerMoving(MOVE_UP);
                        isTouch_BtnMove = true;
                        Emit_PlayerRequestAnimation(MOVE_UP);
                        countDownTimer_Move[0].start();
                    }
                    else {
                        return false;
                    }
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if (currMoveDirectory == MOVE_UP && isTouch_BtnMove == true) {
                        Emit_PlayerRequestAnimation(MOVE_UP + 4);
                        countDownTimer_Move[0].cancel();
                        isTouch_BtnMove = false;
                    }
                }
                return false;
            }
        });

        btn_arrow_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(!isTouch_BtnMove) {
                        currMoveDirectory = MOVE_DOWN;
                        countDownTimer_Move[0] = GetCountDownTimer_PlayerMoving(MOVE_DOWN);
                        isTouch_BtnMove = true;
                        Emit_PlayerRequestAnimation(MOVE_DOWN);
                        countDownTimer_Move[0].start();
                    }
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (currMoveDirectory == MOVE_DOWN && isTouch_BtnMove == true) {
                        Emit_PlayerRequestAnimation(MOVE_DOWN + 4);
                        countDownTimer_Move[0].cancel();
                        isTouch_BtnMove = false;
                    }
                }
                return false;
            }
        });
    }

    private void PlayersMoving(int moveDirection){
        double x_object = (double) linearLayout_MyPlayer.getTranslationX();
        double y_object = (double) linearLayout_MyPlayer.getTranslationY();

        switch (moveDirection){
            case 1:
                if (y_object == 0) {
                    y_object = speed;
                }
                if(y_object + speed <= metrics.heightPixels - linearLayout_MyPlayer.getHeight()){
                    coordinates_ratio_x = x_object/x;
                    coordinates_ratio_y = y_object/y;
                    coordinates_ratio_y += speed/y;
                    mSocket.emit("Client-request-Moving", myID, pref_Username, coordinates_ratio_x, coordinates_ratio_y, moveDirection, myCharacterIndex);
                }
                else {
                    coordinates_ratio_y = (metrics.heightPixels - linearLayout_MyPlayer.getHeight())/y;
                    mSocket.emit("Client-request-Moving", myID, pref_Username, coordinates_ratio_x, coordinates_ratio_y, moveDirection, myCharacterIndex);
                }
                break;
            case 2:
                if(x_object - speed >= 0){
                    coordinates_ratio_x = x_object/x;
                    coordinates_ratio_y = y_object/y;
                    coordinates_ratio_x -= speed/x;
                    mSocket.emit("Client-request-Moving", myID, pref_Username, coordinates_ratio_x, coordinates_ratio_y, moveDirection, myCharacterIndex);
                }
                else {
                    coordinates_ratio_x = 0.0;
                    mSocket.emit("Client-request-Moving", myID, pref_Username, coordinates_ratio_x, coordinates_ratio_y, moveDirection, myCharacterIndex);
                }
                break;
            case 3:
                if (x_object == 0) {
                    x_object = speed;
                }
                if(x_object + speed <= x){
                    coordinates_ratio_x = x_object/x;
                    coordinates_ratio_y = y_object/y;
                    coordinates_ratio_x += speed/x;
                    mSocket.emit("Client-request-Moving", myID, pref_Username, coordinates_ratio_x, coordinates_ratio_y, moveDirection, myCharacterIndex);
                }
                else {
                    coordinates_ratio_x = 1.0;
                    mSocket.emit("Client-request-Moving", myID, pref_Username, coordinates_ratio_x, coordinates_ratio_y, moveDirection, myCharacterIndex);
                }
                break;
            case 4:
                if(y_object - speed >= 0){
                    coordinates_ratio_x = x_object/x;
                    coordinates_ratio_y = y_object/y;
                    coordinates_ratio_y -= speed/y;
                    mSocket.emit("Client-request-Moving", myID, pref_Username, coordinates_ratio_x, coordinates_ratio_y, moveDirection, myCharacterIndex);
                }
                else {
                    coordinates_ratio_y = 0.0;
                    mSocket.emit("Client-request-Moving", myID, pref_Username, coordinates_ratio_x, coordinates_ratio_y, moveDirection, myCharacterIndex);
                }
                break;
        }
    }

    private CountDownTimer GetCountDownTimer_PlayerMoving(final int MoveDirection){
        switch (MoveDirection){
            case 1:
                return new CountDownTimer(9999999, 50) {
                    @Override
                    public void onTick(long l) {
                        PlayersMoving(MOVE_DOWN);
                    }

                    @Override
                    public void onFinish() {

                    }
                };
            case 2:
                return new CountDownTimer(9999999, 50) {
                    @Override
                    public void onTick(long l) {
                        PlayersMoving(MOVE_LEFT);
                    }

                    @Override
                    public void onFinish() {

                    }
                };
            case 3:
                return new CountDownTimer(9999999, 50) {
                    @Override
                    public void onTick(long l) {
                        PlayersMoving(MOVE_RIGHT);
                    }

                    @Override
                    public void onFinish() {

                    }
                };
            case 4:
                return new CountDownTimer(9999999, 50) {
                    @Override
                    public void onTick(long l) {
                        PlayersMoving(MOVE_UP);
                    }

                    @Override
                    public void onFinish() {

                    }
                };
        }
        return null;
    }

    private String Get_SharedPreference_Username(){
        SharedPreferences sharedPreferences = getSharedPreferences("share_preference", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Username", "");
    }

    private void Emit_PlayerRequestLogin(){
        Random random = new Random();
        coordinates_ratio_x = random.nextInt((int)x)+1;
        coordinates_ratio_y = random.nextInt((int)y)+1;
        coordinates_ratio_x /= x;
        coordinates_ratio_y /= y;
        mSocket.emit("Client-request-Login", pref_Username.trim(), coordinates_ratio_x, coordinates_ratio_y, myCharacterIndex);
    }

    private void Emit_PlayerRequestAnimation(int moveDirection){
        mSocket.emit("Client-request-Animation", myID*2, moveDirection, myCharacterIndex);
    }

    private void Emit_PlayerDead(){
        mSocket.emit("Client-request-Dead", myID*2, currMoveDirectory, myCharacterIndex);
    }

    private void Emit_PlayerImmortal(){
        mSocket.emit("Client-request-Immortal", pref_Username);
    }

    private void Emit_PlayerChatting(String chatContent){
        mSocket.emit("Client-request-Chat", chatContent, coordinates_ratio_x, coordinates_ratio_y);
    }

    private CountDownTimer GetCountDownTimer_CharacterAnimation(final int MoveDirection, final ImageView iv_Player, final int characterIndex){
        CountDownTimer countDownTimer_Character = new CountDownTimer(9999999, 180) {
            int index = 1;
            @Override
            public void onTick(long l) {
                if(index >= 4){
                    index = 0;
                }
                Bitmap bitmap = GetBitmapCharacterAnimation(MoveDirection, index, bitmap_Characters[characterIndex]);
                iv_Player.setImageBitmap(bitmap);
                index++;
            }

            @Override
            public void onFinish() {

            }
        };
        return countDownTimer_Character;
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

    private void Add_NewPlayer(int id, String username, double coor_X, double coor_Y, int moveDirection, int index){
        double X, Y;
        X = coor_X*x;
        Y = coor_Y*y;
        LinearLayout linearLayout_Player = new LinearLayout(this);
        TextView textView_Username = new TextView(this);
        TextView textView_Chat = new TextView(this);
        ImageView imageView_Player = new ImageView(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)(x/5), LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        textView_Username.setText(username);
        textView_Username.setTextColor(Color.BLACK);
        textView_Username.setLayoutParams(layoutParams);
        textView_Username.setGravity(Gravity.CENTER);
        textView_Username.setTextSize(11);
        textView_Username.setId(id*3);
        textViews_PlayerUsernames[index] = textView_Username;

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        textView_Chat.setText(username+"~");
        textView_Chat.setTextColor(Color.BLACK);
        textView_Chat.setLayoutParams(layoutParams1);
        textView_Chat.setGravity(Gravity.CENTER);
        textView_Chat.setTextSize(11);
        textView_Chat.setId(id*3+1000);
        textView_Chat.setBackgroundResource(R.drawable.drawable_talkingbackground);
        textView_Chat.setPadding(15,5,15,5);
        textView_Chat.setVisibility(View.GONE);
        textView_Chat.measure(0,0);
        textViews_PlayerChat[index] = textView_Chat;

        imageView_Player.setImageBitmap(GetBitmapCharacterAnimation(moveDirection, 0, bitmap_Characters[arrCharacterIndex[index]]));
        imageView_Player.setLayoutParams(new LinearLayout.LayoutParams((int)(x/13), ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView_Player.setAdjustViewBounds(true);
        imageView_Player.setId(id*2);
        imageViews_Players[index] = imageView_Player;

        linearLayout_Player.setLayoutParams(new LinearLayout.LayoutParams((int)(x/13), (int)(x/13)));
        linearLayout_Player.setOrientation(LinearLayout.VERTICAL);
        linearLayout_Player.setId(id);
        linearLayouts_Players[index] = linearLayout_Player;

        linearLayout_Player.setTranslationX((float)X);
        linearLayout_Player.setTranslationY((float)Y);
        double temp1 = (textView_Username.getLayoutParams().width - linearLayout_Player.getLayoutParams().width)/2;
        double temp2 = linearLayout_Player.getLayoutParams().width/3;
        double temp3 = (textView_Chat.getMeasuredWidth() - linearLayout_Player.getLayoutParams().width)/2;
        double temp4 = textView_Chat.getMeasuredHeight();
        Log.d("ZZZ", textView_Chat.getMeasuredWidth()+"");
        Log.d("ZZZ", textView_Chat.getMeasuredHeight()+"");
        textView_Username.setTranslationX((float)(X - temp1));
        textView_Username.setTranslationY((float)(Y - temp2));
        textView_Chat.setTranslationX((float)(X - temp3));
        textView_Chat.setTranslationY((float)(Y - temp4 - temp2));

        linearLayout_Player.addView(imageView_Player);
        frameLayout_Game.addView(textView_Username);
        frameLayout_Game.addView(linearLayout_Player);
        frameLayout_Chat.addView(textView_Chat);

        if(username.equals(pref_Username)){
            myID = id;
            myID1 = id*2;
            linearLayout_MyPlayer = linearLayout_Player;
            iv_MyPlayer = imageView_Player;
        }
    }

    private void UpdateMoving(int id, double coor_x, double coor_y){
        for(int i = 0; i < linearLayouts_Players.length; i++){
            if(linearLayouts_Players[i] != null) {
                if (linearLayouts_Players[i].getId() == id) {
                    double newX = coor_x * x;
                    double newY = coor_y * y;
                    double temp = (textViews_PlayerUsernames[i].getLayoutParams().width - linearLayouts_Players[i].getLayoutParams().width)/2;
                    double temp2 = linearLayouts_Players[i].getLayoutParams().width/3;
                    double temp3 = (textViews_PlayerChat[i].getMeasuredWidth() - linearLayouts_Players[i].getLayoutParams().width)/2;
                    double temp4 = textViews_PlayerChat[i].getMeasuredHeight() + x/39;
                    linearLayouts_Players[i].setTranslationX((float) newX);
                    linearLayouts_Players[i].setTranslationY((float) newY);
                    textViews_PlayerUsernames[i].setTranslationX((float)(newX - temp));
                    textViews_PlayerUsernames[i].setTranslationY((float)(newY - temp2));
                    textViews_PlayerChat[i].setTranslationX((float)(newX - temp3));
                    textViews_PlayerChat[i].setTranslationY((float)(newY - temp4));
                }
            }
        }
        for(int i = 0; i < linearLayouts_Players.length; i++){
            if(linearLayouts_Players[i] != null) {
                float minY = linearLayouts_Players[i].getTranslationY();
                for (int j = i + 1; j < linearLayouts_Players.length; j++) {
                    if(linearLayouts_Players[j] != null) {
                        if (linearLayouts_Players[j].getTranslationY() < minY) {
                            LinearLayout temp = linearLayouts_Players[i];
                            linearLayouts_Players[i] = linearLayouts_Players[j];
                            linearLayouts_Players[j] = temp;
                            TextView temp2 = textViews_PlayerUsernames[i];
                            textViews_PlayerUsernames[i] = textViews_PlayerUsernames[j];
                            textViews_PlayerUsernames[j] = temp2;
                            TextView temp3 = textViews_PlayerChat[i];
                            textViews_PlayerChat[i] = textViews_PlayerChat[j];
                            textViews_PlayerChat[j] = temp3;
                        }
                    }
                }
                textViews_PlayerUsernames[i].bringToFront();
                linearLayouts_Players[i].bringToFront();
                textViews_PlayerChat[i].bringToFront();
            }
        }
    }

    private void UpdateAnimation(int id, int moveDirection, int characterIndex){
        for(int i = 0; i < imageViews_Players.length; i++){
            if(imageViews_Players[i] != null){
                if(imageViews_Players[i].getId() == id){
                    if(moveDirection > 4) {
                        countDownTimer_CharacterAnimation[i].cancel();
                        imageViews_Players[i].setImageBitmap(GetBitmapCharacterAnimation(moveDirection-4,0,bitmap_Characters[characterIndex]));
                    }
                    else {
                        countDownTimer_CharacterAnimation[i] = GetCountDownTimer_CharacterAnimation(moveDirection, imageViews_Players[i], characterIndex);
                        countDownTimer_CharacterAnimation[i].start();
                    }
                }
            }
        }
    }

    private void Display_PlayerDead(int id, final int moveDirectory, final int characterIndex){
        for(int i = 0; i < imageViews_Players.length; i++) {
            if(imageViews_Players[i] != null){
                if(imageViews_Players[i].getId() == id){
                    imageViews_Players[i].setImageResource(R.drawable.rip);
                    final int var_i = i;
                    new CountDownTimer(3000,100){
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            imageViews_Players[var_i].setImageBitmap(GetBitmapCharacterAnimation(moveDirectory,0,bitmap_Characters[characterIndex]));
                        }
                    }.start();
                }
            }
        }
    }

    private void Display_PlayerImmortal(final String name){
        new CountDownTimer(1500, 200){
            @Override
            public void onTick(long l) {
                Random random = new Random();
                for(int i = 0; i < textViews_PlayerUsernames.length; i++) {
                    if(textViews_PlayerUsernames[i] == null){
                        break;
                    }
                    if (textViews_PlayerUsernames[i] != null) {
                        if (textViews_PlayerUsernames[i].getText().equals(name)) {
                            textViews_PlayerUsernames[i].setTextColor(Color.rgb(random.nextInt(256),random.nextInt(256),random.nextInt(256)));
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                for(int i = 0; i < textViews_PlayerUsernames.length; i++) {
                    if (textViews_PlayerUsernames[i] != null) {
                        if (textViews_PlayerUsernames[i].getText().equals(name)) {
                            textViews_PlayerUsernames[i].setTextColor(Color.BLACK);
                        }
                    }
                }
            }
        }.start();
    }

    private void Display_PlayerChat(String username, String data, Double coor_x, Double coor_y){
        for(int i = 0; i < linearLayouts_Players.length; i++) {
            if (textViews_PlayerChat[i] != null) {
                if(textViews_PlayerUsernames[i].getText().equals(username)) {
                    textViews_PlayerChat[i].setText(data);
                    textViews_PlayerChat[i].setVisibility(View.VISIBLE);
                    textViews_PlayerChat[i].measure(0, 0);
                    double newX = coor_x * x;
                    double newY = coor_y * y;
                    double temp = (textViews_PlayerChat[i].getMeasuredWidth() - linearLayouts_Players[i].getLayoutParams().width) / 2;
                    double temp1 = textViews_PlayerChat[i].getMeasuredHeight() + x / 39;
                    textViews_PlayerChat[i].setTranslationX((float) (newX - temp));
                    textViews_PlayerChat[i].setTranslationY((float) (newY - temp1));
                    final int var_i = i;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            textViews_PlayerChat[var_i].setVisibility(View.GONE);
                        }
                    }, 3000);
                }
            } else {
                break;
            }
        }
    }

    private Emitter.Listener onServerReplyChatting = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        String data = jsonObject.getString("data");
                        String username = jsonObject.getString("username");
                        Double coor_x = jsonObject.getDouble("coor_x");
                        Double coor_y = jsonObject.getDouble("coor_y");
                        Display_PlayerChat(username, data, coor_x, coor_y);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onServerReplyImmortal = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        String name = jsonObject.getString("data");
                        Display_PlayerImmortal(name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onServerReplyDead = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        String data[] = jsonObject.getString("data").split("\\|");
                        int id = Integer.parseInt(data[0]);
                        int moveDirection = Integer.parseInt(data[1]);
                        int characterIndex = Integer.parseInt(data[2]);
                        Display_PlayerDead(id, moveDirection, characterIndex);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onServerReplySetBomb = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    Double coorX, coorY;
                    try {
                        String[] data = jsonObject.getString("data").split("\\|");
                        coorX = Double.parseDouble(data[1]);
                        coorY = Double.parseDouble(data[2]);
                        SetBomb(coorX, coorY);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onServerReplyAnimation = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        String data[] = jsonObject.getString("data").split("\\|");
                        int id = Integer.parseInt(data[0]);
                        int moveDirection = Integer.parseInt(data[1]);
                        int characterIndex = Integer.parseInt(data[2]);
                        UpdateAnimation(id, moveDirection, characterIndex);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onServerReplyMoving = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        String data[] = jsonObject.getString("data").split("\\|");
                        int id = Integer.parseInt(data[0]);
                        double coor_X = Double.parseDouble(data[2]);
                        double coor_Y = Double.parseDouble(data[3]);
                        UpdateMoving(id, coor_X, coor_Y);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onServerReplyLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        linearLayouts_Players = new LinearLayout[16];
                        imageViews_Players = new ImageView[16];
                        countDownTimer_CharacterAnimation = new CountDownTimer[16];
                        textViews_PlayerUsernames = new TextView[16];
                        textViews_PlayerChat = new TextView[16];
                        arrCharacterIndex = new int[16];
                        frameLayout_Game.removeAllViews();
                        frameLayout_Chat.removeAllViews();
                        numberPlayer = jsonArray.length();
                        for(int i = 0; i < jsonArray.length(); i++){
                            String data[] = jsonArray.getString(i).split("\\|");
                            int id = Integer.parseInt(data[0]);
                            double coor_X = Double.parseDouble(data[2]);
                            double coor_Y = Double.parseDouble(data[3]);
                            int moveDirection = Integer.parseInt(data[4]);
                            arrCharacterIndex[i] = Integer.parseInt(data[5]);
                            flags_PlayerAnimation[i] = id*2 + "|" + 0;
                            Add_NewPlayer(id, data[1], coor_X, coor_Y, moveDirection, i);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Override
    public void onBackPressed() {
        if(!isFinish){
            isFinish = true;
            Toast.makeText(this, "Nhấn quay lại lần nữa để thoát game!", Toast.LENGTH_SHORT).show();
        }
        else {
            mSocket.disconnect();
            if(!pref_Username.equals("Sokou")) {
                if (mediaPlayer_BackgroundMusic.isPlaying()) {
                    mediaPlayer_BackgroundMusic.stop();
                    mediaPlayer_BackgroundMusic.release();
                }
            }
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

