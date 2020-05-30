package com.example.to_dolist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

/**
 * @author DESTREZ Corentin
 * @author LASUS Léo
 * @author VAISSEAU Paul
 * Allez voir notre repo github : https://github.com/Lafie-rage/Contact_List_Android_Highschool_Project
 */

public class MainActivity extends AppCompatActivity {


    // Data base
    private static ContactDbAdapter mDbHelper;

    // Main page
    private ListView contactListView;
    private ListView favListView;
    private TextView titleContact;

    // Menu
    private MenuItem favOnlyBtn;

    // Used to know if the favOnly mode is activated
    private boolean favOnly = false;

    // Permissions variables
    private int MY_PERMISSIONS_REQUEST_CALL_PHONE;
    private int MY_PERMISSIONS_REQUEST_SEND_SMS;
    private int ZXING_CAMERA_PERMISSION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate attributes
        mDbHelper = new ContactDbAdapter(MainActivity.this);

        // Main page
        contactListView = findViewById(R.id.contactList);
        favListView = findViewById(R.id.favList);
        titleContact = findViewById(R.id.titleContactList);

        // Open DB connection
        mDbHelper.open();
        fillContactList();

        // Event on click linked to the list views
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MainActivity.this, Profil.class);
            intent.putExtra("action", "default");
            intent.putExtra("id", id);
            startActivity(intent);
            }
        });
        favListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, Profil.class);
                intent.putExtra("action", "default");
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });


        registerForContextMenu(contactListView);
        registerForContextMenu(favListView);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu,View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        // Changing item depending on list view
        ListView list = (ListView) v;
        MenuItem favItem = menu.getItem(4);
        if (list.equals(favListView))
            favItem.setTitle("Retirer des favoris");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor selectedTask = (Cursor)contactListView.getItemAtPosition(info.position);
        switch (item.getItemId()) {
            // Call
            case R.id.call_context:
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                }else {
                    //Creating intents for making a call
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+selectedTask.getString(selectedTask.getColumnIndex("phone"))));
                    startActivity(callIntent);
                }
                break;
            // SMS
            case R.id.sms_context:
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                }else {
                    Uri uri = Uri.parse("smsto:"+selectedTask.getString(selectedTask.getColumnIndex("phone")));
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    startActivity(intent);
                }
                break;
            // Mail
            case R.id.mail_context:
                String mailAddress = getMailAddress(info.id);
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",mailAddress, null));
                startActivity(emailIntent);
                break;
            // Gmaps
            case R.id.loc_context:
                String location = getLocation(info.id);
                Uri uri = Uri.parse("geo:0,0?q="+location);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            // Management of favorites
            case R.id.fav_context:
                if (item.getTitle()=="Retirer des favoris")
                    mDbHelper.setNonFavorite(info.id);
                else
                    mDbHelper.setFavorite(info.id);
                fillContactList();
                break;
            // Deleting contact
            case R.id.del_context :
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Voulez-vous réellement effacer ce contact ?");
                builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mDbHelper.deleteContact(info.id);
                        fillContactList();
                        return;
                    }
                });
                AlertDialog dialog = builder.show();
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        // Define fav button to change it dynamically
        favOnlyBtn = menu.getItem(1);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // New conctact
            case R.id.addContact:
                Intent intent = new Intent(this, CreateContact.class);
                String action = "create";
                intent.putExtra("action", action);
                startActivity(intent);
                return true;
            case R.id.addQrCode:
                launchActivity(SimpleScannerActivity.class);
                return true;
            // Show only favorites
            case R.id.favOnly:
                // Changing view dynamically
                if(favOnly) {
                    titleContact.setVisibility(View.VISIBLE);
                    contactListView.setVisibility(View.VISIBLE);
                    favOnlyBtn.setTitle("Uniquement les favoris");
                }
                else {
                    titleContact.setVisibility(View.INVISIBLE);
                    contactListView.setVisibility(View.INVISIBLE);
                    favOnlyBtn.setTitle("Tous les contacts");
                }
                favOnly = !favOnly;
                return true;
            default:
                return false;
        }
    }

    // Data base interaction

    /**
     * Fill list view with all non favorites contacts.
     * Then call fillFavList.
     */
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
        fillFavList();
    }
    /**
     * Fill list view with all favorites contacts.
     */
    private void fillFavList() {
        // Get all of the contacts from the database and create the item list
        Cursor c = mDbHelper.fetchAllFav();
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
        favListView.setAdapter(contacts);
    }

    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Class<?> mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivity(intent);
        }
    }

    /**
     * Retrieve mail address of the specified contact.
     * @param id idRow of the contact
     * @return  contact's mail address
     */
    private String getMailAddress(long id) {
        Cursor c = mDbHelper.fetchContact(id); // Show contact
        if(c.moveToFirst()) {
            int index = c.getColumnIndex(ContactDbAdapter.KEY_MAIL);
            return c.getString(index);
        }
        return "";
    }

    /**
     * Retrieve postal address of the specified contact.
     * @param id idRow of the contact
     * @return  contact's postal address
     */
    private String getLocation(long id) {
        Cursor c = mDbHelper.fetchContact(id); // Show contact

        if(c.moveToFirst()) {
            int index = c.getColumnIndex(ContactDbAdapter.KEY_ADDRESS);
            return c.getString(index);
        }
        return "";
    }

    /**
     * Return database reference.
     * @return Database reference
     */
    public static ContactDbAdapter getDB() {
        return mDbHelper;
    }
}
