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
        // Retrieving action
        action = intent.getStringExtra("action");
        // Retrieving contact id
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

    /**
     * Used when returning from image picking.
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

    /**
     * Creating a new contact with the data provided. Return true if it worked.
     * @param name name
     * @param surname surname
     * @param phone phone
     * @param mail mail address
     * @param address postal address
     * @param bitmap avatar's picture
     * @return true if it worked. False otherwise.
     */
    private boolean createUser(String name, String surname, String phone, String mail, String address, Bitmap bitmap) {
        // Converting Bitmap to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] avatar = stream.toByteArray();

        long rowId = mDbHelper.createContact(name, surname, phone, mail, address, avatar);
        return (rowId != -1);
    }

    /**
     * Updating a contact with the data provided. Return true if it worked.
     * @param name new name
     * @param surname new surname
     * @param phone new phone number
     * @param mail new mail address
     * @param address new postal address
     * @param bitmap new avatar's picture
     * @return true if the update worked. False otherwise.
     */
    private boolean updateUser(String name, String surname, String phone, String mail, String address, Bitmap bitmap) {
        if(id == -1) return false;
        // Converting Bitmap to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] avatar = stream.toByteArray();

        return mDbHelper.updateContact(id, name, surname, phone, mail, address, avatar);
    }

    /**
     * Setting view as a creation.
     */
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
                else {
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

    /**
     * Setting view as an update.
     */
    private void setAsUpdate() {
        title_crt.setText("Modifier un contact");
        createBtn.setText("Modifier");
        fillContactInfo();
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

    /**
     * Convert a VectorDrawable to a Bitmap.
     * @param context Application context
     * @param drawable drawable to convert
     * @return a bitmap corresponding to the drawable sent
     */
    public static Bitmap getBitmapFromVectorDrawable(Context context, Drawable drawable) {
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
     * Find on stackoverflow.
     *
     * Checks if the app has permission to write to device storage.
     *
     * If the app does not has permission then the user will be prompted to grant permissions.
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

    private void fillContactInfo() {

        Cursor c = mDbHelper.fetchContact(id); // Show contact

        if(c.moveToFirst()) {
            // name
            int index = c.getColumnIndex(ContactDbAdapter.KEY_NAME);
            String name = c.getString(index);
            name_crt.setText(name);

            // surname
            index = c.getColumnIndex(ContactDbAdapter.KEY_SURNAME);
            String surname = c.getString(index);
            surname_crt.setText(surname);

            // phone
            index = c.getColumnIndex(ContactDbAdapter.KEY_PHONE);
            String phone = c.getString(index);
            phone_crt.setText(phone);

            // address
            index = c.getColumnIndex(ContactDbAdapter.KEY_ADDRESS);
            String address = c.getString(index);
            address_crt.setText(address);

            // mail
            index = c.getColumnIndex(ContactDbAdapter.KEY_MAIL);
            String mail = c.getString(index);
            mail_crt.setText(mail);

            // avatar
            index = c.getColumnIndex(ContactDbAdapter.KEY_IMAGE);
            byte[] bitmap = c.getBlob(index);
            avatar_crt.setImageBitmap(BitmapFactory.decodeByteArray(bitmap,0, bitmap.length));
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateContact.this);
            builder.setMessage("Une erreur est survenue lors de l'affichage des informations contact : " + id);
            builder.setNeutralButton("Retourner au profil", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(CreateContact.this, Profil.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                    return;
                }
            });
            AlertDialog dialog = builder.show();
            return;
        }

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
}
