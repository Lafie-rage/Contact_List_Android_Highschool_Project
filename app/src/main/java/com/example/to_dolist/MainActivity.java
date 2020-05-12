package com.example.to_dolist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ContactDbAdapter mDbHelper;

    private Button myBtn;
    private EditText myEditText;
    private ListView myListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Intanciate attributs
        mDbHelper = new ContactDbAdapter(MainActivity.this);



       /* myBtn = findViewById(R.id.monBouton);
        myEditText = findViewById(R.id.monEditText);
        myListView = findViewById(R.id.maListe);*/

        // Open DB connection
        mDbHelper.open();
        fillData();

        /*myBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbHelper.createAction(myEditText.getText().toString());
                fillData();
                myEditText.setText("");
            }
        });*/

        /*myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mDbHelper.deleteAction(id);
                fillData();
            }
        });*/

        registerForContextMenu(myListView);

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
        Cursor SelectedTaskCursor = (Cursor)myListView.getItemAtPosition(info.position);
        final String selectedTask = SelectedTaskCursor.getString(SelectedTaskCursor.getColumnIndex("title"));
        Intent intent;
        /*switch (item.getItemId()) {
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
        }*/
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

        /*switch (item.getItemId()) {
            case R.id.deleteAll:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure to delete the whole list");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mDbHelper.deleteAllActions();
                        fillData();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
                AlertDialog dialog = builder.show();
                return true;
            default:
                return false;
        }*/
    }

    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor c = mDbHelper.fetchAllContacts();
        startManagingCursor(c);

        String[] from = new String[] { ContactDbAdapter.KEY_NAME };
        int[] to = new int[] { R.id.title };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter actions =
                new SimpleCursorAdapter(this, R.layout.item, c, from, to);
        myListView.setAdapter(actions);
    }
}
