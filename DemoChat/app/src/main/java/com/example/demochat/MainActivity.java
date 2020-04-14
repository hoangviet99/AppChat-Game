package com.example.demochat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.IpSecManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {


    Socket mSocket;
    Button btn_Send;
    ImageButton btn_More;
    EditText editText_Message;
    ListView listView_Chat;
    ListView listView_Online;
    ArrayList<Chating> arrChatting = new ArrayList<>();
    ArrayList<Chating> arrUserOnline = new ArrayList<>();
    CustomAdapterChating customAdapterChating;
    CustomAdapterChating customAdapterUserOnline;
    String pref_Username = "";
    String interfaceName;

    boolean isLoginSuccessful = false;
    boolean isConnect = false;

    private void Mapping() {
        btn_Send = findViewById(R.id.btn_Send);
        btn_More = findViewById(R.id.btn_More);
        editText_Message = findViewById(R.id.editText_Message);
        listView_Chat = findViewById(R.id.listView_Chat);
        listView_Online = findViewById(R.id.listView_Online);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null)
        {
            interfaceName = bundle.getString("InterfaceName", "");
            if(interfaceName.equals("Black")){
                setContentView(R.layout.activity_main_black);
            }
            if(interfaceName.equals("White")){
                setContentView(R.layout.activity_main);
            }
        } else{
            setContentView(R.layout.activity_main);
            interfaceName = "";
        }

        pref_Username = Get_SharedPreference_Username();

        try {
            mSocket = IO.socket("http://14.184.254.84:8888");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Mapping();

        ShowDialog_Login();

        mSocket.connect();

        mSocket.emit("Client-Testing-Server", "");
        mSocket.on("Server-reply-Testing", onTestServer);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSocket.on("Server-reply-Testing", onTestServer);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isConnect) {
                            Toast.makeText(MainActivity.this, "Không kết nối được đến Server!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            if (!pref_Username.equals("")) {
                                mSocket.emit("Client-ask-to-Login", pref_Username);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isLoginSuccessful) {
                                            mSocket.emit("Client-obtain-Chatting-Data", "");
                                            mSocket.on("Server-send-Chatting-Data", onObtainChatData);
                                            mSocket.emit("Client-request-Login", pref_Username);
                                        } else {
                                            Toast.makeText(MainActivity.this, "Tên bạn nhập đã tồn tại! Nhập tên khác.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, 500);
                            }
                        }
                    }
                }, 800);
            }
        }, 500);

        mSocket.on("Server-Welcome-Client", onServerWelcome);
        mSocket.on("Server-agree-Login", onAgreeLogin);
        mSocket.on("Server-reply-Login", onClientLogin);
        mSocket.on("Server-reply-Chatting", onChattingListen);
        mSocket.on("Client-Logout", onClientLogout);

        if(!interfaceName.equals("")) {
            if (interfaceName.equals("Black")) {
                customAdapterChating = new CustomAdapterChating(this, R.layout.custom_listview_chat_black, arrChatting);
                listView_Chat.setAdapter(customAdapterChating);
                customAdapterUserOnline = new CustomAdapterChating(this, R.layout.custom_listview_chat_black, arrUserOnline);
                listView_Online.setAdapter(customAdapterUserOnline);
            }
            if (interfaceName.equals("White")) {
                customAdapterChating = new CustomAdapterChating(this, R.layout.custom_listview_chat, arrChatting);
                listView_Chat.setAdapter(customAdapterChating);
                customAdapterUserOnline = new CustomAdapterChating(this, R.layout.custom_listview_chat, arrUserOnline);
                listView_Online.setAdapter(customAdapterUserOnline);
            }
        }
        else {
            customAdapterChating = new CustomAdapterChating(this, R.layout.custom_listview_chat, arrChatting);
            listView_Chat.setAdapter(customAdapterChating);
            customAdapterUserOnline = new CustomAdapterChating(this, R.layout.custom_listview_chat, arrUserOnline);
            listView_Online.setAdapter(customAdapterUserOnline);
        }

        listView_Chat.setStackFromBottom(true);

        btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editText_Message.getText().toString();
                if (!message.equals("")) {
                    mSocket.emit("Client-request-Chatting", message);
                    editText_Message.setText("");
                    listView_Chat.setStackFromBottom(true);
                }
            }
        });


        btn_More.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDialog_More();
            }
        });
    }



    private void ShowDialog_More(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_more);

        Button btn_Logout =                             dialog.findViewById(R.id.btn_Logout);
        Button btn_RoomChat2D =                         dialog.findViewById(R.id.btn_RoomChat2D);
        Button btn_Game =                               dialog.findViewById(R.id.btn_Game);
        final Button btn_InterfaceChange =              dialog.findViewById(R.id.btn_InterfaceChange);
        Button btn_Black =                              dialog.findViewById(R.id.btn_Black);
        Button btn_White =                              dialog.findViewById(R.id.btn_White);
        final LinearLayout linearLayout_Interface =     dialog.findViewById(R.id.linearLayout_Interface);

        btn_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set_SharedPreference_Username("");
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                mSocket.disconnect();
                startActivity(intent);
                finish();
            }
        });

        btn_Game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TroChoi.class);
                startActivity(intent);
                dialog.cancel();
            }
        });

        btn_RoomChat2D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PickCharacter.class);
                startActivity(intent);
                dialog.cancel();
            }
        });

        btn_InterfaceChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout_Interface.setVisibility(View.VISIBLE);
                btn_InterfaceChange.setVisibility(View.GONE);
            }
        });

        btn_Black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("InterfaceName", "Black");
                intent.putExtras(bundle);
                startActivity(intent);
                mSocket.disconnect();
                finish();
                dialog.cancel();
                Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
            }
        });

        btn_White.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("InterfaceName", "White");
                intent.putExtras(bundle);
                startActivity(intent);
                mSocket.disconnect();
                finish();
                dialog.cancel();
                Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void ShowDialog_Login(){
        if(!pref_Username.equals("")){
            return;
        }
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_login);
        TextView tv = dialog.findViewById(R.id.txtTV);
        TextView ta = dialog.findViewById(R.id.txtTA);
        TextView tf = dialog.findViewById(R.id.txtTP);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Translate("vi");
            }
        });
        ta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Translate("en");
            }
        });
        tf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Translate("fr");
            }
        });

        final EditText editText_Name = dialog.findViewById(R.id.editText_Name);
        Button btn_Login =       dialog.findViewById(R.id.btn_Login);

        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText_Name.getText().toString().trim().length() == 0){
                    Toast.makeText(MainActivity.this, "Vui lòng nhập tên để tiếp tục!", Toast.LENGTH_SHORT).show();
                }
                else {
                    pref_Username = editText_Name.getText().toString().trim();
                    Set_SharedPreference_Username(pref_Username);
                    mSocket.emit("Client-ask-to-Login", pref_Username);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(isLoginSuccessful) {
                                mSocket.emit("Client-obtain-Chatting-Data", "");
                                mSocket.on("Server-send-Chatting-Data", onObtainChatData);
                                mSocket.emit("Client-request-Login", editText_Name.getText().toString().trim());
                                dialog.cancel();
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Tên bạn nhập đã tồn tại! Nhập tên khác.", Toast.LENGTH_SHORT).show();
                                editText_Name.setText("");
                            }
                        }
                    }, 500);
                }
            }
        });

        dialog.show();
    }

    private void Set_SharedPreference_Username(String pref_Username){
        SharedPreferences sharedPreferences_Username = getSharedPreferences("share_preference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences_Username.edit();
        editor.clear();
        editor.putString("Username", pref_Username);
        editor.apply();
    }

    private String Get_SharedPreference_Username(){
        SharedPreferences sharedPreferences_Username = getSharedPreferences("share_preference", Context.MODE_PRIVATE);
        return sharedPreferences_Username.getString("Username", "");
    }

    private Emitter.Listener onTestServer = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        isConnect = jsonObject.getBoolean("bool");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onServerWelcome = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        String username = jsonObject.getString("username");
                        String chatting = jsonObject.getString("welcome");
                        arrChatting.add(new Chating(username, chatting));
                        customAdapterChating.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onClientLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isLoginSuccessful){
                        return;
                    }
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        JSONArray array = jsonObject.getJSONArray("arrOnline");
                        arrUserOnline.clear();
                        for(int i = 0; i < array.length(); i++){
                            String username = array.getString(i);
                            arrUserOnline.add(new Chating("", username));
                        }
                        customAdapterUserOnline.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onChattingListen = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        String username = jsonObject.getString("username");
                        String chatContent = jsonObject.getString("chat");
                        arrChatting.add(new Chating(username, chatContent));
                        customAdapterChating.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onClientLogout = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        String username = jsonObject.getString("username");
                        String content = jsonObject.getString("content");
                        arrChatting.add(new Chating(username, content));
                        customAdapterChating.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onAgreeLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        isLoginSuccessful = jsonObject.getBoolean("bool");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onObtainChatData = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("chatData");
                        arrChatting.clear();
                        for(int i = 0; i < jsonArray.length(); i++){
                            String string = jsonArray.getString(i);
                            String[] strings = new String[2];
                            int sub = string.indexOf("|");
                            strings[0] = string.substring(0, sub);
                            strings[1] = string.substring(sub + 1);
                            arrChatting.add(new Chating(strings[0], strings[1]));
                        }
                        customAdapterChating.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private void Translate(String language){
        Locale locale = new Locale(language);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(locale);
        } else{
            configuration.locale = locale;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            getApplicationContext().createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration,displayMetrics);
        }
        startActivity(new Intent(this,MainActivity.class));
        mSocket.disconnect();
        finish();
    }
}
