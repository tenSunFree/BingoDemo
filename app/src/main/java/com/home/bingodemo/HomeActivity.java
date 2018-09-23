package com.home.bingodemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.home.bingodemo.widget.SetGameNameDialog;

import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private static final int RC_SIGN_IN = 100;
    private CardView emailCardView, googleCardView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private GoogleSignInClient googleSignInClient;
    private GoogleSignInOptions googleSignInOptions;
    private List<com.firebase.ui.auth.AuthUI.IdpConfig> idpConfigList;
    private User user;
    private SetGameNameDialog setGameNameDialog;
    private Context context;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,                          // 去除状态栏
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        context = this;

        googleCardView = findViewById(R.id.googleCardView);
        emailCardView = findViewById(R.id.emailCardView);

        setOnTouchListener(googleCardView, "google");
        setOnTouchListener(emailCardView, "email");

        firebaseAuth = FirebaseAuth.getInstance();
        signOut();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        signOut();
        firebaseAuth.removeAuthStateListener(this);
    }

    private void signOut() {
        /** 將Google自動選擇上一次登入的帳號設定 清空 */
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        googleSignInClient.signOut();

        firebaseAuth.signOut();
    }

    /**
     * 針對按下放開的行為, 設定對應的動畫效果
     */
    public void setOnTouchListener(View view, final String type) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        touchDownAnimation(view);
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        touchUpAnimation(view);
                        if (type.equals("google")) {
                            idpConfigList = Arrays.asList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build()
                            );
                        } else {
                            idpConfigList = Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build()
                            );
                        }
                        startActivityForResult(
                                AuthUI.getInstance().createSignInIntentBuilder()
                                        .setAvailableProviders(idpConfigList)
                                        .setIsSmartLockEnabled(false, false)
                                        .build(),
                                RC_SIGN_IN);
                        return true;
                    }
                    default: {
                        return true;
                    }
                }
            }
        });
    }

    /**
     * 放大的動畫
     */
    void touchDownAnimation(View view) {
        Log.d("more", "touchDownAnimation");
        view.animate()
                .scaleX(1.24f)
                .scaleY(1.24f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    /**
     * 還原的動畫
     */
    void touchUpAnimation(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    @Override
    public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "請選擇登入方式", Toast.LENGTH_SHORT).show();
        } else {

            /** 先建立一個位置 */
            uid = firebaseUser.getUid();
            FirebaseDatabase.getInstance()
                    .getReference("BingoDemo")
                    .child(uid)
                    .child("uid")
                    .setValue(uid);

            /** 針對位置去判斷 是否有設置gameName值 */
            FirebaseDatabase.getInstance()
                    .getReference("BingoDemo")
                    .child(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            user = dataSnapshot.getValue(User.class);

                            /** 判斷該firebaseUser是否有建立遊戲暱稱, 如果沒有 就顯示Dialog提供建立, 如果有 就直接跳轉下個畫面 */
                            if (user.getGameName() == null) {
                                showSetGameNameDialog(firebaseAuth);
                            } else {
                                Intent intent = new Intent(HomeActivity.this, LoadActivity.class);
                                intent.putExtra("gameName", user.getGameName());
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        }
    }

    /**
     * 顯示提供設定遊戲暱稱的Dialog
     */
    private void showSetGameNameDialog(@NonNull final FirebaseAuth firebaseAuth) {
        setGameNameDialog =
                new SetGameNameDialog(
                        context, R.style.SetGameNameDialog);
        setGameNameDialog.setTitleText("設定遊戲暱稱");
        setGameNameDialog.setOnYesListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!setGameNameDialog.getGameNameText().equals("")) {
                    FirebaseDatabase.getInstance()
                            .getReference("BingoDemo")
                            .child(firebaseAuth.getUid())
                            .child("gameName")
                            .setValue(setGameNameDialog.getGameNameText());
                    setGameNameDialog.dismiss();
                } else {
                    Toast.makeText(HomeActivity.this, "請輸入遊戲暱稱", Toast.LENGTH_SHORT).show();
                }
            }
        });
        setGameNameDialog.setOnNoListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setGameNameDialog.dismiss();
            }
        });
        setGameNameDialog.show();
    }
}
