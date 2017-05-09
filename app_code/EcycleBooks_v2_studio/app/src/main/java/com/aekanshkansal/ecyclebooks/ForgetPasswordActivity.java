package com.aekanshkansal.ecyclebooks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

public class ForgetPasswordActivity extends Activity {
    private Button sendCodeButton;
    private EditText emailEditText;
    private TextView resendCodeTextView;

    private String email;
    private String randomStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        emailEditText = (EditText) findViewById(R.id.input_email);
        sendCodeButton = (Button) findViewById(R.id.btn_code);
        resendCodeTextView = (TextView) findViewById(R.id.textView_resendCode);

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailEditText.getText().toString();
                if (email.isEmpty()) {
                    MyGlobal.showAlert(ForgetPasswordActivity.this, "Notice", "Please Enter E-mail Id");
                } else if (!email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
                    MyGlobal.showAlert(ForgetPasswordActivity.this, "Notice", "Please Enter Valid E-mail Id");
                } else {
                    Random random = new Random();
                    int num = random.nextInt(90000000) + 10000000;
                    randomStr = Integer.toString(num);
                    new ForgetPasswordActivity.NetCheck().execute();
                }
            }
        });

        resendCodeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailEditText.getText().toString();
                if (email.isEmpty()) {
                    MyGlobal.showAlert(ForgetPasswordActivity.this, "Notice", "Please Enter E-mail Id");
                } else if (!email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
                    MyGlobal.showAlert(ForgetPasswordActivity.this, "Notice", "Please Enter Valid E-mail Id");
                } else {
                    new ForgetPasswordActivity.NetCheck().execute();
                }
            }
        });

    }

    private class NetCheck extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            nDialog = new ProgressDialog(ForgetPasswordActivity.this);
            nDialog.setTitle("Checking Network");
            nDialog.setMessage("Loading...");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                URL url = new URL("http://www.google.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(3000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                }
            } catch (Exception ex) {
                Log.e("Runtime problem", "exception in netcheck code" + ex);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            nDialog.dismiss();
            if (result == true) {
                sendCodeAtServer();
            } else {
                MyGlobal.showAlert(ForgetPasswordActivity.this, "Notice", "No Internet Connection");
            }
        }

    }

    private void sendCodeAtServer() {
        new sendVerificationLinkAtServer().execute(email, randomStr);
    }

    private class sendVerificationLinkAtServer extends AsyncTask<String, Void, JSONObject> {

        private ProgressDialog pDialog;
        private static final String FORGET_URL = "http://ecyclebooks.com/forgetpassword.php";
        JSONParser jParser = new JSONParser();

        @Override
        protected JSONObject doInBackground(String... params) {
            String emailLocal = params[0];
            String randomStrLocal = params[1];
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("email", emailLocal);
            parameters.put("code", randomStrLocal);
            JSONObject json = jParser.getJSONFromUrl(FORGET_URL, "POST", parameters);
            return json;
        }

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(ForgetPasswordActivity.this);
            pDialog.setTitle("Sending Password");
            pDialog.setMessage("Sending...");
            pDialog.setIndeterminate(false); //true means indeterminate i.e.a spinner rotate don't know how much time is taken and false stands for determinate.
            pDialog.setCancelable(true);     //progessdialog is cancelable on clicking outside box
            pDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null) {
                //return the string if exist else returns empty string. No Exception is thrown like getString if no string exist.
                boolean error = Boolean.parseBoolean(json.optString("error"));
                String status = json.optString("status");
                if (error) {
                    String errmsg = json.optString("error_msg");
                    MyGlobal.showAlert(ForgetPasswordActivity.this, "Error", errmsg);
                } else {
                    if (status.equals("success")) {
                        //Future Scope Way to go to gmail directly
                        Toast.makeText(ForgetPasswordActivity.this, "Password Sent Successfully", Toast.LENGTH_SHORT);
                        AlertDialog.Builder ab = new AlertDialog.Builder(ForgetPasswordActivity.this);
                        ab.setTitle("Directions");
                        ab.setMessage("Your Password is sent to your E-mail Id.\nPlease login using it and change your password.");
                        ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                Intent i = new Intent(ForgetPasswordActivity.this, SignInActivity.class);
                                startActivity(i);
                            }
                        });
                        ab.show();
                    }
                }

            }
            //stopping progress dialog
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

        }
    }
}

