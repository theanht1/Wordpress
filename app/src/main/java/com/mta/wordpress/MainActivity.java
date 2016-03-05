package com.mta.wordpress;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PICTURE = 2;

    private  WordPressRpcClient wp = new WordPressRpcClient();
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button mBtnLogin = (Button) findViewById(R.id.main_login);
        Button mBtnTakePhoto = (Button) findViewById(R.id.main_take_photo);
        Button mBtnUpload = (Button) findViewById(R.id.main_upload_picture);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        mBtnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        mBtnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });
    }

    private void login() {
        View rootView = this.getLayoutInflater().inflate(R.layout.login, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Login")
                .setView(rootView)
                .create();

        final EditText mUsername = (EditText) rootView.findViewById(R.id.login_username);
        final EditText mPassword = (EditText) rootView.findViewById(R.id.login_password);
        final EditText mAddress = (EditText) rootView.findViewById(R.id.login_web_address);

        Button btnSubmit = (Button) rootView.findViewById(R.id.login_submit);
        Button btnCancel = (Button) rootView.findViewById(R.id.login_cancel);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mUsername.getText().toString().equals("") & !mPassword.getText().toString().equals("")
                        & !mAddress.getText().toString().equals("")) {

                    String username = mUsername.getText().toString();
                    String password = mPassword.getText().toString();
                    String xmlrpcurl = "https://" + mAddress.getText().toString() + "/xmlrpc.php";
                    System.out.println(username + password + xmlrpcurl);

                    LoginTask loginTask = new LoginTask();
                    loginTask.execute(username, password, xmlrpcurl);

                    dialog.dismiss();
                } else {
                    Toast.makeText(getApplication(), "Need to fill all fields", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void takePhoto() {
        if (wp.isLoggedIn()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photo));
            imageUri = Uri.fromFile(photo);

            startActivityForResult(intent, TAKE_PICTURE);
        } else {
            Toast.makeText(this, "Please login first",Toast.LENGTH_LONG).show();
        }
    }

    private void choosePicture() {
        if (wp.isLoggedIn()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        } else {
            Toast.makeText(this, "Please login first",Toast.LENGTH_LONG).show();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    String path = getRealPathFromURI(selectedImageUri);

                    Log.i(TAG, path);
                    WordpressUploader wpu = new WordpressUploader();
                    wpu.execute(path);
                };
                break;

            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImageUri = imageUri;
                    String path = getRealPathFromURI(selectedImageUri);

                    Log.i(TAG, path);
                    WordpressUploader wpu = new WordpressUploader();
                    wpu.execute(path);
                }
                break;
        }
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
     //       System.out.println(params[0]+params[1]+params[2]);
            return loginTask(params[0], params[1], params[2]);
        }

        private String loginTask(String username, String password, String xmlrpcurl) {
            wp.setUsername(username);
            wp.setPassword(password);
            wp.setXmlrpcUrl(xmlrpcurl);

            wp.checkLoggedIn();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (wp.isLoggedIn()) {
                Toast.makeText(getApplication(), "Login Successful", Toast.LENGTH_LONG).show();
            } else Toast.makeText(getApplication(), "Login Unsuccessful", Toast.LENGTH_LONG).show();
        }
    }

    private class WordpressUploader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "Enter WordPressConnection");

            return request(params[0]);
        }

        private String request(String path) {
            Log.i(TAG, path);
            Bitmap bitmap = BitmapFactory.decodeFile(path);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapData = stream.toByteArray();

            String imageName = path.substring(path.lastIndexOf(File.separator) + 1);
            Log.i(TAG, imageName);
            wp.uploadImage(imageName, bitmapData);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getApplicationContext(), "Upload Successful", Toast.LENGTH_LONG).show();
        }
    }
}
