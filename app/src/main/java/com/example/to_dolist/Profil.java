package com.example.to_dolist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Profil extends AppCompatActivity {

    // Data base
    private ContactDbAdapter mDbHelper;

    // Layout contents
    private ImageView backBtn_pfl;
    private Button modifyBtn;
    private ImageView callBtn_pfl;
    private ImageView delBtn_pfl;
    private ImageView smsBtn_pfl;
    private ImageView mailBtn_pfl;
    private ImageView locaBtn_pfl;
    private ImageView avatar_pfl;
    private TextView name_pfl;
    private TextView surname_pfl;
    private TextView phone_pfl;
    private TextView address_pfl;
    private TextView mail_pfl;

    // Permissions storage
    private int MY_PERMISSIONS_REQUEST_CALL_PHONE;
    private int MY_PERMISSIONS_REQUEST_SEND_SMS;

    private long selectedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        mDbHelper = MainActivity.getDB();

        // Retrieve contact id
        Intent intent = getIntent();
        selectedContact = intent.getLongExtra("id", 0);



        // Profil page
        backBtn_pfl = findViewById(R.id.backBtn_pfl);
        modifyBtn = findViewById(R.id.modifyBtn);
        callBtn_pfl = findViewById(R.id.callBtn_pfl);
        delBtn_pfl = findViewById(R.id.delBtn_pfl);
        smsBtn_pfl = findViewById(R.id.smsBtn_pfl);
        mailBtn_pfl = findViewById(R.id.mailBtn_pfl);
        locaBtn_pfl = findViewById(R.id.locaBtn_pfl);
        avatar_pfl = findViewById(R.id.avatar_pfl);
        name_pfl = findViewById(R.id.name_pfl);
        surname_pfl = findViewById(R.id.surname_pfl);
        phone_pfl = findViewById(R.id.phone_pfl);
        address_pfl = findViewById(R.id.address_pfl);
        mail_pfl = findViewById(R.id.mail_pfl);

        fillContactProfil();


        // On click actions
        backBtn_pfl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profil.this, MainActivity.class);
                startActivity(intent);
            }
        });

        callBtn_pfl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(Profil.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Profil.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                 }else {
                    //Creating intents for making a call
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+phone_pfl.getText().toString()));
                    startActivity(callIntent);
                }
            }
        });

        mailBtn_pfl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mailAddress = mail_pfl.getText().toString();
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",mailAddress, null));
                startActivity(emailIntent);
            }
        });

        smsBtn_pfl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(Profil.this,
                        Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Profil.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                }else {
                    Uri uri = Uri.parse("smsto:"+phone_pfl.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    startActivity(intent);
                }
            }
        });

        locaBtn_pfl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = address_pfl.getText().toString();
                Uri uri = Uri.parse("geo:0,0?q="+location);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        delBtn_pfl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbHelper.deleteContact(selectedContact);
                Intent intent = new Intent(Profil.this, MainActivity.class);
                startActivity(intent);
            }
        });

        modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profil.this, CreateContact.class);
                String action = "update";
                intent.putExtra("action", action);
                intent.putExtra("id", selectedContact);
                startActivity(intent);
            }
        });
    }

    private void fillContactProfil() {

        Cursor c = mDbHelper.fetchContact(selectedContact); // Show contact

        if(c.moveToFirst()) {
            // name
            int index = c.getColumnIndex(ContactDbAdapter.KEY_NAME);
            String name = c.getString(index);
            name_pfl.setText(name);

            // surname
            index = c.getColumnIndex(ContactDbAdapter.KEY_SURNAME);
            String surname = c.getString(index);
            surname_pfl.setText(surname);

            // phone
            index = c.getColumnIndex(ContactDbAdapter.KEY_PHONE);
            String phone = c.getString(index);
            phone_pfl.setText(phone);

            // address
            index = c.getColumnIndex(ContactDbAdapter.KEY_ADDRESS);
            String address = c.getString(index);
            address_pfl.setText(address);

            // mail
            index = c.getColumnIndex(ContactDbAdapter.KEY_MAIL);
            String mail = c.getString(index);
            mail_pfl.setText(mail);

            // avatar
            index = c.getColumnIndex(ContactDbAdapter.KEY_IMAGE);
            byte[] bitmap = c.getBlob(index);
            avatar_pfl.setImageBitmap(BitmapFactory.decodeByteArray(bitmap,0, bitmap.length));
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(Profil.this);
            builder.setMessage("Une erreur est survenue lors de l'affichage du contact : " + selectedContact);
            builder.setNeutralButton("Retourner à l'accueil", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(Profil.this, MainActivity.class);
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
