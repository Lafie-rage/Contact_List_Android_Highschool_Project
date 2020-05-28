package com.example.to_dolist;


import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Find on stackoverflow and adapted to the project structure.
 *
 * Class managing data with an image.
 * Retrieving database information to set in on a view.
 */
public class ImageCursorAdapter extends SimpleCursorAdapter {

    private Cursor c;
    private Context context;

    public ImageCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.c = c;
        this.context = context;
    }

    public View getView(int pos, View inView, ViewGroup parent) {
        View v = inView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item, null);
        }
        this.c.moveToPosition(pos);
        String name = this.c.getString(this.c.getColumnIndex(ContactDbAdapter.KEY_NAME));
        String surname = this.c.getString(this.c.getColumnIndex(ContactDbAdapter.KEY_SURNAME));
        String phone = this.c.getString(this.c.getColumnIndex(ContactDbAdapter.KEY_PHONE));
        byte[] image = this.c.getBlob(this.c.getColumnIndex(ContactDbAdapter.KEY_IMAGE));
        ImageView iv = (ImageView) v.findViewById(R.id.avatar_list);
        if (image != null) {
            // If there is no image in the database "NA" is stored instead of a blob
            // test if there more than 3 chars "NA" + a terminating char if more than
            // there is an image otherwise load the default
            if (image.length > 3) {
                iv.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
            }
        }
        TextView ref_name = (TextView) v.findViewById(R.id.name_list);
        ref_name.setText(name);

        TextView ref_surname = (TextView) v.findViewById(R.id.surname_list);
        ref_surname.setText(surname);

        TextView ref_phone = (TextView) v.findViewById(R.id.phone_list);
        ref_phone.setText(phone);
        return (v);
    }
}
