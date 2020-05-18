package com.example.to_dolist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class CreateContact extends AppCompatActivity {

    // Data base
    private ContactDbAdapter mDbHelper;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // Layout components
    private TextView title_crt;
    private ImageView backBtn_crt;
    private Button createBtn;
    private ImageView avatar_crt;
    private EditText name_crt;
    private EditText surname_crt;
    private EditText phone_crt;
    private EditText mail_crt;
    private EditText address_crt;

    // Intent data transmission
    private String action;
    private long id;
    public static final int PICK_PHOTO_FOR_AVATAR = 1;
    private boolean workingOnInsertion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Data base
        mDbHelper = MainActivity.getDB();

        Intent intent = getIntent();
        action = intent.getStringExtra("action");
        id = intent.getLongExtra("id", -1);

        title_crt = findViewById(R.id.title_crt);
        backBtn_crt = findViewById(R.id.backBtn_crt);
        createBtn = findViewById(R.id.createBtn);
        avatar_crt = findViewById(R.id.avatar_crt);
        name_crt = findViewById(R.id.name_crt);
        surname_crt = findViewById(R.id.surname_crt);
        phone_crt = findViewById(R.id.phone_crt);
        mail_crt = findViewById(R.id.mail_crt);
        address_crt = findViewById(R.id.address_crt);

        if(action.equals("create")) {
            setAsCreation();
        }
        else {
            setAsUpdate();
        }

        avatar_crt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyStoragePermissions(CreateContact.this);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO_FOR_AVATAR);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == RESULT_OK) {
            if (data == null) {
                //Display an error
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateContact.this);
                builder.setMessage("Erreur lors de l'importation de l'image.");
                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
                AlertDialog dialog = builder.show();
                return;
            }

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                ImageView imageView = (ImageView) findViewById(R.id.avatar_crt);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillContactProfil(long id) {

        Cursor c = mDbHelper.fetchContact(id); // Show contact
        startManagingCursor(c);
        String[] from = new String[] {  ContactDbAdapter.KEY_NAME,
                ContactDbAdapter.KEY_SURNAME,
                ContactDbAdapter.KEY_IMAGE,
                ContactDbAdapter.KEY_PHONE,
                ContactDbAdapter.KEY_MAIL,
                ContactDbAdapter.KEY_ADDRESS};
        int[] to = new int[] {  R.id.name_crt,
                R.id.surname_crt,
                R.id.avatar_crt,
                R.id.phone_crt,
                R.id.mail_crt,
                R.id.address_crt};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter contacts =
                new SimpleCursorAdapter(this, R.layout.activity_create, c, from, to);
    }

    private boolean createUser(String name, String surname, String phone, String mail, String address, Bitmap bitmap) {
        // Converting Bitmap to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] avatar = stream.toByteArray();

        long rowId = mDbHelper.createContact(name, surname, phone, mail, address, avatar);
        return (rowId != -1);
    }

    private boolean updateUser(String name, String surname, String phone, String mail, String address, Bitmap bitmap) {
        if(id == -1) return false;
        // Converting Bitmap to byte array
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getWidth() * bitmap.getHeight());
        bitmap.copyPixelsToBuffer(buffer);
        byte[] avatar = buffer.array();

        return mDbHelper.updateContact(id, name, surname, phone, mail, address, avatar);
    }

    private void setAsCreation() {
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(workingOnInsertion) return;
                workingOnInsertion = true;
                if( name_crt.getText().equals("") ||
                        phone_crt.getText().equals("")) { // The minimum required fields
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateContact.this);
                    builder.setMessage("Vous devez au moins saisir un numéro de téléphone et un nom pour votre contact.");
                    builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {return;    }
                    });
                    AlertDialog dialog = builder.show();
                    return;
                }
                // Retrieving data from fields
                String name = name_crt.getText().toString();
                String surname = surname_crt.getText().toString();
                String phone = phone_crt.getText().toString();
                String mail = mail_crt.getText().toString();
                String address = address_crt.getText().toString();
                Bitmap avatar = getBitmapFromVectorDrawable(CreateContact.this, avatar_crt.getDrawable());

                if(createUser(name, surname, phone, mail, address, avatar)) {
                    Intent intent = new Intent(CreateContact.this, MainActivity.class);
                    startActivity(intent);
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateContact.this);
                builder.setMessage("Une erreur est survenue lors de l'ajout du contact. Veuillez réessayer.");
                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
                AlertDialog dialog = builder.show();
                return;
            }
        });
        backBtn_crt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateContact.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setAsUpdate() {
        title_crt.setText("Modifier un contact");
        createBtn.setText("Modifier");
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (workingOnInsertion) return;
                workingOnInsertion = true;
                if( name_crt.getText().equals("") ||
                        phone_crt.getText().equals("")) { // The minimum required fields
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateContact.this);
                    builder.setMessage("Vous devez au moins saisir un numéro de téléphone et un nom pour votre contact.");
                    builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {return;    }
                    });
                    AlertDialog dialog = builder.show();
                    return;
                }
                // Retrieving data from fields
                String name = name_crt.getText().toString();
                String surname = surname_crt.getText().toString();
                String phone = phone_crt.getText().toString();
                String mail = mail_crt.getText().toString();
                String address = address_crt.getText().toString();
                Bitmap avatar = getBitmapFromVectorDrawable(CreateContact.this, avatar_crt.getDrawable());

                if(updateUser(name, surname, phone, mail, address, avatar)) {
                    // Get back to main page
                    Intent intent = new Intent(CreateContact.this, MainActivity.class);
                    startActivity(intent);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateContact.this);
                builder.setMessage("Une erreur est survenue lors de la modification du contact. Veuillez réessayer.");
                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
                AlertDialog dialog = builder.show();
                return;
            }
        });
        backBtn_crt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateContact.this, Profil.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
    }

    private static Bitmap getBitmapFromVectorDrawable(Context context, Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    private static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
