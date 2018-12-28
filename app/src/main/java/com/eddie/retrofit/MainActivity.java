package com.eddie.retrofit;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Api api;
    private OkHttpClient client;

    private EditText inputEmail, inputPassword;
    private Button regBtn, loginBrn, getAllContacts, addBtn, deleteBtn;

    private String token;

    private Auth currentAuth;

    private String superToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);

        regBtn = findViewById(R.id.reg_btn);
        regBtn.setOnClickListener(this);

        loginBrn = findViewById(R.id.login_btn);
        loginBrn.setOnClickListener(this);

        getAllContacts = findViewById(R.id.getContactList);
        getAllContacts.setOnClickListener(this);

        addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(this);

        deleteBtn = findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://contacts-telran.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(Api.class);

        client = new OkHttpClient();
    }

    // Retrofit registration
    private void registration(String email, String password) throws IOException {

        Auth auth = new Auth(email, password);
        Call<AuthResponse> call = api.registration(auth);

        Response<AuthResponse> response = call.execute();

        if (response.isSuccessful()) {

            AuthResponse token = response.body();

            this.token = token.getToken();

            superToken = token.getToken();

            Log.d("MY_TAG", "Registration" + token.getToken());

        } else if (response.code() == 409) {

            String json = response.errorBody().string();
            Log.d("MY_TAG", "Error: " + response.code());
        }
    }

    private void login(String email, String password) throws IOException {

        Auth auth = new Auth(email, password);
        Call<AuthResponse> call = api.login(auth);
        //Call<AuthResponse> callLog = api.login(auth);
        Response<AuthResponse> response = call.execute();

        currentAuth = auth;

        if (response.isSuccessful()) {

            AuthResponse token = response.body();
            this.token = token.getToken();
            Log.d("MY_TAG", "login: " + token.getToken());

        } else if (response.code() == 401) {

            String json = response.errorBody().string();
            Log.d("MY_TAG", "login: " + json);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.login_btn) {

            final String email = inputEmail.getText().toString();
            final String password = inputPassword.getText().toString();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        login(email,password);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } else if (v.getId() == R.id.reg_btn) {

            final String email = inputEmail.getText().toString();
            final String password = inputPassword.getText().toString();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        registration(email, password);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
//            asyncReg(email,password);

        } else if (v.getId() == R.id.getContactList) {


            new Thread(new Runnable() {

                @Override
                public void run() {

                    try {

                        Response<ContactsResponse> response = api.getAllContacts(token).execute();

                        if (response.isSuccessful()) {

                            ContactsResponse contacts = response.body();

                            for (Contact c : contacts.contacts) {

                                Log.d("MY_TAG", "getContactList: " + c);
                            }
                        } else {

                            Log.d("MY_TAG", "getContactList: " + response.errorBody().string());
                        }

                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }
            }).start();

        } else if (v.getId() == R.id.addBtn) {

            //Contact newContact = new Contact("New", "Man", 3333, "88888888","newman@ocm.ua", "Haifa", "Friend");
            //progressBar.setVisibility(View.VISIBLE);

            //api.addContact(currentAuth, newContact);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {

                        Response<Contact> response = api.addContact(token, new Contact("New", "Man", 0, "88888888","newman@ocm.ua", "Haifa", "Friend")).execute();

                        if (response.isSuccessful()) {


                            Contact contact = response.body();

                            Log.d("MY_TAG", "Contact added: " + contact);

                        } else if (response.code() == 400) {

                            Log.d("MY_TAG", "addContact: " + "Wrong contact format");

                        } else if (response.code() == 401) {

                            Log.d("MY_TAG", "addContact: " + "Wrong authorization");
                        } else if (response.code() == 409) {

                            Log.d("MY_TAG", "addContact: " + "Duplicate contact fields! Email and phone need be unique to each contact");
                        } else {

                            Log.d("MY_TAG", "addContact: " + response.errorBody().string());
                        }

                    } catch (IOException e) {

                        e.printStackTrace();
                    }

                }
            }).start();

        } else if (v.getId() == R.id.deleteBtn) {

            try {
                Response<Contact> response = api.deleteContact(token, 1317).execute();

                if (response.isSuccessful()) {

                    Log.d("MY_TAG", "Contact deleted: ");
                } else if (response.code() == 400) {

                    Log.d("MY_TAG", "addContact: " + "Wrong format ID of contact");

                } else if (response.code() == 401) {

                    Log.d("MY_TAG", "addContact: " + "Wrong authorization");
                } else if (response.code() == 404) {

                    Log.d("MY_TAG", "addContact: " + "Contact ID not found");
                } else {

                    Log.d("MY_TAG", "addContact: " + response.errorBody().string());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void asyncReg(String email, String password) {

        Auth auth = new Auth(email, password);
        api.registration(auth).enqueue(new Callback<AuthResponse>() {

            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {

                if (response.isSuccessful()) {

                    token = response.body().getToken();

                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Registration Ok!")
                            .create()
                            .show();;
                } else {

                   String error = null;

                    try {
                        error = response.errorBody().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(error)
                            .create()
                            .show();;
                }

            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {

                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Connection Error!")
                        .create()
                        .show();;
            }
        });
    }

    // OkHttp
    private void registrationOkHttp(String email, String password) throws IOException {

        Gson gson = new Gson();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        Auth auth = new Auth(email, password);

        String jsonRequest = gson.toJson(auth);

        RequestBody body = RequestBody.create(JSON, jsonRequest);

        Request request = new Request.Builder()
                .url("https://contacts-telran.herokuapp.com/api/registration/")
                .post(body)
                .build();

        okhttp3.Call call = client.newCall(request);

        okhttp3.Response response = call.execute();

        if (response.isSuccessful()) {

            String jsonResponse = response.body().string();

            AuthResponse authResponse = gson.fromJson(jsonResponse, AuthResponse.class);

            Log.d("MY_TAG", "RegistrationOkHttp" + authResponse.getToken());
        }
    }
}
