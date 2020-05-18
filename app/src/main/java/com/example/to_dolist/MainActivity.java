package com.example.to_dolist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Data base
    private static ContactDbAdapter mDbHelper;

    // Main page
    private EditText myEditText;
    private ListView contactListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate attributes
        mDbHelper = new ContactDbAdapter(MainActivity.this);

        // Main page
        contactListView = findViewById(R.id.contactList);

        // Open DB connection
        mDbHelper.open();
        fillContactList();



        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MainActivity.this, Profil.class);
            intent.putExtra("id", id);
            startActivity(intent);
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
                Intent intent = new Intent(this, CreateContact.class);
                String message = "create";
                intent.putExtra("action", message);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    // Data base interaction

    private void fillContactList() {
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

        BaseAdapter contacts = new ImageCursorAdapter(this,
                    R.layout.item,
                    c,
                    from,
                    to);
        contactListView.setAdapter(contacts);
    }

    public static ContactDbAdapter getDB() {
        return mDbHelper;
    }
}
