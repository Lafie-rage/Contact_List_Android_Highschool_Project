package com.example.to_dolist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ContactDbAdapter mDbHelper;

    // Main page
    private EditText myEditText;
    private ListView contactListView;

    // Create page
    private ImageView backBtn_crt;
    private Button createBtn;
    private ImageView avatar_crt;
    private EditText name_PT_crt;
    private EditText surname_PT_crt;
    private EditText phone_PT_crt;
    private EditText email_PT_crt;
    private EditText address_PT_crt;


    // Profil page
    private ImageView backBtn_pfl;
    private Button modifyBtn;
    private long selectedContact;

    // Modify page
    private ImageView backBtn_mdf;
    private Button validateModifBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Intanciate attributs
        mDbHelper = new ContactDbAdapter(MainActivity.this);

        // Main page
        contactListView = findViewById(R.id.contactList);

        // Create page
        backBtn_crt = findViewById(R.id.backBtn_crt);
        createBtn = findViewById(R.id.createBtn);
        avatar_crt = findViewById(R.id.avatar_crt);
        name_PT_crt = findViewById(R.id.name_crt);
        surname_PT_crt = findViewById(R.id.surname_crt);
        phone_PT_crt = findViewById(R.id.phone_crt);
        email_PT_crt = findViewById(R.id.mail_crt);
        address_PT_crt = findViewById(R.id.address_crt);
        
        // Profil page
        backBtn_pfl = findViewById(R.id.backBtn_pfl);
        modifyBtn = findViewById(R.id.modifyBtn);

        // Modify page
        backBtn_mdf = findViewById(R.id.backBtn_mdf);
        validateModifBtn = findViewById(R.id.modifyBtn);

        
        // Open DB connection
        mDbHelper.open();
        fillData();



        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showProfil(id);
                selectedContact = id;
            }
        });

        modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillData_Mdf(selectedContact);
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( name_PT_crt.getText().equals("") ||
                    phone_PT_crt.getText().equals("")) { // The minimum required fields
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Vous devez au moins saisir un numéro de téléphone et un nom pour votre contact.")
                    builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    });
                    return;
                }
                createUser();
            }
        });

        registerForContextMenu(contactListView);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu,View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor SelectedTaskCursor = (Cursor)contactListView.getItemAtPosition(info.position);
        final String selectedTask = SelectedTaskCursor.getString(SelectedTaskCursor.getColumnIndex("title"));
        Intent intent;
        switch (item.getItemId()) {
            case R.id.google:
                Uri webpage = Uri.parse("http://www.google.com/search?q="+selectedTask);
                intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
                break;
            case R.id.google_map:
                Uri location = Uri.parse("geo:0,0?q="+selectedTask);
                intent = new Intent(Intent.ACTION_VIEW, location);
                break;
            default:
                return super.onContextItemSelected(item);
        }
        // Verifying if the app is ready to receive the intent
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activites = packageManager.queryIntentActivities(intent, 0);

        if (activites.size() > 0)
            startActivity(intent);
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.addContact:
                setContentView(R.layout.create);
                return true;
            default:
                return false;
        }
    }

    private void createUser(String name, String surname, String phone, String mail, String address, Bitmap avatar) {

    }

    private void fillData_Mdf(long id) {
        setContentView(R.layout.modify);
        Cursor c = mDbHelper.fetchContact(id); // Show contact
        startManagingCursor(c);
        String[] from = new String[] {  ContactDbAdapter.KEY_NAME,
                ContactDbAdapter.KEY_SURNAME,
                ContactDbAdapter.KEY_IMAGE,
                ContactDbAdapter.KEY_PHONE,
                ContactDbAdapter.KEY_MAIL,
                ContactDbAdapter.KEY_ADDRESS};
        int[] to = new int[] {  R.id.name_mdf,
                R.id.surname_mdf,
                R.id.avatar_mdf,
                R.id.phone_mdf,
                R.id.mail_mdf,
                R.id.address_mdf};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter contacts =
                new SimpleCursorAdapter(this, R.layout.modify, c, from, to);
        contactListView.setAdapter(contacts);
    }

    private void showProfil(long id) {
        setContentView(R.layout.profil);
        Cursor c = mDbHelper.fetchContact(id); // Show contact
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
                new SimpleCursorAdapter(this, R.layout.profil, c, from, to);
        contactListView.setAdapter(contacts);
    }

    private void fillData() {
        // Get all of the contacts from the database and create the item list
        Cursor c = mDbHelper.fetchAllContacts();
        startManagingCursor(c);

        String[] from = new String[] {  ContactDbAdapter.KEY_NAME,
                                        ContactDbAdapter.KEY_SURNAME,
                                        ContactDbAdapter.KEY_IMAGE,
                                        ContactDbAdapter.KEY_PHONE };
        int[] to = new int[] {  R.id.name_list,
                                R.id.surname_list,
                                R.id.avatar_list,
                                R.id.phone_list };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter contacts =
                new SimpleCursorAdapter(this, R.layout.item, c, from, to);
        contactListView.setAdapter(contacts);
    }
}
