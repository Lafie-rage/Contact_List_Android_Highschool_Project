/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.to_dolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple contacts database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all contacts as well as
 * retrieve or modify a specific note.
 *
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class ContactDbAdapter {

    public static final String KEY_NAME = "name";
    public static final String KEY_SURNAME = "surname";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_MAIL = "mail";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_ISFAV = "isFav";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "contactsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table contacts (_id integer primary key autoincrement, "
                    + "name text not null,"
                    + "surname text,"
                    + "phone text not null,"
                    + "mail text,"
                    + "address text ,"
                    + "image blob ,"
                    + "isFav integer);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "contacts";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public ContactDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the contacts database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ContactDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new contact using the data provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     *
     * @param name the contact's name
     * @param surname the contact's surname
     * @param phone the phone number
     * @param mail the contact's mail
     * @param address the contact's address
     * @param image the contact's image
     * @return rowId or -1 if failed
     */
    public long createContact(String name, String surname, String phone, String mail, String address, byte[] image) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_SURNAME, surname);
        initialValues.put(KEY_PHONE, phone);
        initialValues.put(KEY_MAIL, mail);
        initialValues.put(KEY_ADDRESS, address);
        initialValues.put(KEY_IMAGE, image);
        initialValues.put(KEY_ISFAV, 0);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the contact with the given rowId
     *
     * @param rowId id of contact to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteContact(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }


    /**
     * Return a Cursor over the list of all contacts that are not favorites in the database
     *
     * @return Cursor over all non favorite contacts
     */
    public Cursor fetchAllContacts() {

        return mDb.query(DATABASE_TABLE, new String[] {
            KEY_ROWID, KEY_NAME, KEY_SURNAME, KEY_PHONE, KEY_IMAGE
        }, KEY_ISFAV + "=0", null, null, null, KEY_NAME + " ASC, " + KEY_SURNAME + " ASC, " + KEY_PHONE + " ASC");
    }

    /**
     * Return a Cursor over the list of all contacts that are favorites in the database
     *
     * @return Cursor over all favorite contacts
     */
    public Cursor fetchAllFav() {
        return mDb.query(DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_NAME, KEY_SURNAME, KEY_PHONE, KEY_IMAGE
        }, KEY_ISFAV + "<>0", null, null, null, KEY_NAME + " ASC, " + KEY_SURNAME + " ASC, " + KEY_PHONE + " ASC");
    }

    /**
     * Return a Cursor positioned at the contact that matches the given rowId
     *
     * @param rowId id of contact to retrieve
     * @return Cursor positioned to matching contact, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchContact(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {
                            KEY_ROWID, KEY_NAME, KEY_SURNAME, KEY_PHONE,
                            KEY_MAIL, KEY_ADDRESS, KEY_IMAGE
                        }, KEY_ROWID + "=" + rowId, null,
                        null, null, KEY_NAME, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the contact using the details provided. The contact to be updated is
     * specified using the rowId, and it is altered to use the datas
     * passed in
     *
     * @param rowId id of contact to update
     * @param name the contact's name
     * @param surname the contact's surname
     * @param phone the phone number
     * @param mail the contact's mail
     * @param address the contact's address
     * @param image the contact's image
     * @return true if the contact was successfully updated, false otherwise
     */
    public boolean updateContact (long rowId, String name, String surname, String phone, String mail, String address, byte[] image) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_NAME, name);
        args.put(KEY_SURNAME, surname);
        args.put(KEY_PHONE, phone);
        args.put(KEY_MAIL, mail);
        args.put(KEY_ADDRESS, address);
        args.put(KEY_IMAGE, image);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Set the contact as favorite. The contact to be updated is
     * specified using the rowId.
     *
     * @param rowId id of contact to update
     * @return true if the contact was successfully updated, false otherwise
     */
    public boolean setFavorite(long rowId) {
        ContentValues args = new ContentValues();
        args.put(KEY_ISFAV, 1);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Set the contact as non favorite. The contact to be updated is
     * specified using the rowId.
     *
     * @param rowId id of contact to update
     * @return true if the contact was successfully updated, false otherwise
     */
    public boolean setNonFavorite(long rowId) {
        ContentValues args = new ContentValues();
        args.put(KEY_ISFAV, 0);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
