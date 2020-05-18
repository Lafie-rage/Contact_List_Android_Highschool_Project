package com.example.to_dolist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class Profil extends AppCompatActivity {

    // Data base
    private ContactDbAdapter mDbHelper;

    // Layout contents
    private ImageView backBtn_pfl;
    private Button modifyBtn;
    private long selectedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        Intent intent = getIntent();
        selectedContact = intent.getLongExtra("id", 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(Profil.this);
        builder.setMessage("id : "+ selectedContact);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {return;    }
        });
        AlertDialog dialog = builder.show();

        // Profil page
        backBtn_pfl = findViewById(R.id.backBtn_pfl);
        modifyBtn = findViewById(R.id.modifyBtn);

        Cursor c = mDbHelper.fetchContact(selectedContact); // Show contact
        startManagingCursor(c);
        String[] from = new String[] {  ContactDbAdapter.KEY_NAME,
                ContactDbAdapter.KEY_SURNAME,
                ContactDbAdapter.KEY_IMAGE,
                ContactDbAdapter.KEY_PHONE,
                ContactDbAdapter.KEY_MAIL,
                ContactDbAdapter.KEY_ADDRESS};
        int[] to = new int[] {  R.id.name_pfl,
                R.id.surname_pfl,
                R.id.avatar_pfl,
                R.id.phone_pfl,
                R.id.mail_pfl,
                R.id.address_pfl};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter contacts =
                new SimpleCursorAdapter(this, R.layout.activity_profil, c, from, to);
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
}
