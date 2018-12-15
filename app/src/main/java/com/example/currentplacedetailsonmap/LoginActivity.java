package com.example.currentplacedetailsonmap;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.reflect.TypeToken;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int SIGN_IN = 50001;
    public static final int SIGN_UP = 50002;

    GoogleSignInClient mGoogleSignInClient = null;
    GoogleSignInAccount mGoogleAccount;
    SignInButton signInButton = null;
    Button mSignUPButton = null;
    GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 사용자의 ID, 전자 메일 주소 및 기본
        // 프로필 을 요청하도록 로그인을 구성 합니다. ID 및 기본 프로필은 DEFAULT_SIGN_IN에 포함됩니다.
        // GoogleSignInOptions gso = 새 GoogleSignInOptions . 작성자 ( GoogleSignInOptions . DEFAULT_SIGN_IN ) . requestEmail () . 빌드 ();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // gso로 지정된 옵션을 사용하여 GoogleSignInClient를 빌드합니다.
        mGoogleSignInClient = GoogleSignIn. getClient ( this , gso );

        // 기존 Google 로그인 계정을 확인합니다 (사용자가 이미 로그인 한 경우).
        // GoogleSignInAccount는 null이 아닙니다.
        // GoogleSignInAccount 계정 = GoogleSignIn . getLastSignedInAccount ( this ); updateUI ( 계정 );


        signInButton  = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        mSignUPButton = findViewById(R.id.sign_up_button);
        mSignUPButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        /*

        if(GoogleSignIn.getLastSignedInAccount(this) != null){
            signOut();
        }

        */

        switch (v.getId()){
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_up_button:
                signUp();
                break;
        }
    }

    public void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, SIGN_IN);
    }

    public void signUp(){
        if(GoogleSignIn.getLastSignedInAccount(this) != null){
            signOut();
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, SIGN_UP);
    }

    public void signOut(){
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>(){
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Toast.makeText(LoginActivity.this, "signed out", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                 account = task.getResult(ApiException.class);
                Log.d("account_id", account.getId());


                new HttpAsyncTask.Builder("GET", "users/" + account.getId(), new TypeToken<ResultBody<User>>() {
                }.getType(),
                        new MyCallBack() {
                            @Override
                            public void doTask(Object resultBody) {
                                ResultBody<User> result = (ResultBody<User>) resultBody;

                                /*  회원 가입이 되어 있을 때  */
                                if (Integer.parseInt(result.getSize()) == 1) {
                                    Intent intent = new Intent(LoginActivity.this, MapsActivityCurrentPlace.class);
                                    intent.putExtra("USER_NAME", result.getDatas().get(0).name);
                                    startActivity(intent);
                                }
                                /*  회원 가입이 되어 있지 않을 때  */
                                else {
                                    Toast.makeText(getApplicationContext(), "회원가입 안되있어용!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).build()
                        .execute();

            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == SIGN_UP){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                account = task.getResult(ApiException.class);
                Log.d("account_id", account.getId());


                new HttpAsyncTask.Builder("GET", "users/" + account.getId(), new TypeToken<ResultBody<User>>() {
                }.getType(),
                        new MyCallBack() {
                            @Override
                            public void doTask(Object resultBody) {
                                ResultBody<User> result = (ResultBody<User>) resultBody;

                                /*  회원 가입이 되어 있을 때  */
                                if (Integer.parseInt(result.getSize()) == 1) {
                                    Toast.makeText(getApplicationContext(), "회원가입 되어있어용!", Toast.LENGTH_SHORT).show();
                                }
                                /*  회원 가입이 되어 있지 않을 때  */
                                else {
                                    new HttpAsyncTask.Builder("POST", "users/", new TypeToken<ResultBody<User>>() {
                                    }.getType(),
                                            new MyCallBack() {
                                                @Override
                                                public void doTask(Object resultBody) {

                                                }
                                            })
                                            .requestBodyJson(new User(account.getId(), account.getFamilyName()+account.getGivenName()).getJSONObject())
                                            .build()
                                            .execute();
                                }
                            }
                        }).build()
                        .execute();

            }catch (ApiException e){
                e.printStackTrace();
            }
        }
    }

    public void updateUI(GoogleSignInAccount account){
        Toast.makeText(getApplicationContext(), account.getFamilyName() + account.getGivenName(), Toast.LENGTH_LONG).show();
    }
}
