
package com.franktom.horizon;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import java.util.HashMap;

public class LocationsProvider extends ContentProvider {

    public static final String AUTHORITY = "com.franktom.Horizon";

    // The incoming URI matches specific URI pattern
    private static final int LOCATIONS = 1;
    private static final int LOCATION_ID = 2;
    private static final int TYPES = 3;
    private static final int TYPE_ID = 4;
    private static final int VISIBLE_LOCATIONS = 5;


    public static final int _ID_PATH_POSITION = 1;
    /**
     * The table name offered by this provider
     */
    public static final String TABLE_NAME = "locations";
    public static final String TABLE_NAME_TYPES = "types";

    public static final Uri CONTENT_ID_URI_LOCATION
    = Uri.parse("content://" + AUTHORITY + "/locations/");

    public static final Uri CONTENT_ID_URI_VISIBLE_LOCATION
    = Uri.parse("content://" + AUTHORITY + "/visiblelocations/");

    public static final Uri CONTENT_ID_URI_TYPE
    = Uri.parse("content://" + AUTHORITY + "/types/");

    /*
     * MIME type definitions
     */

    /**
     * The MIME types of {@link #CONTENT_URI}
     */
    public static final String LOCATION_TYPE = "vnd.android.cursor.dir/vnd.google.location";
    public static final String LOCATION_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.location";
    public static final String LOCTYPE_TYPE = "vnd.android.cursor.item/vnd.google.loctype";
    public static final String LOCTYPE_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.loctype";


    /**
     * A UriMatcher instance
     */
    private static final UriMatcher sUriMatcher;

    private static HashMap<String, String> sNotesProjectionMap;
    private static HashMap<String, String> sTypesProjectionMap;

    /*
     * Defines a handle to the database helper object. The MainDatabaseHelper class is defined
     * in a following snippet.
     */
    private MainDatabaseHelper mOpenHelper;

    // Defines the database name
    private static final String DBNAME = "locationsdb";


 // A string that defines the SQL statement for creating a table
    private static final String SQL_CREATE_MAIN = "CREATE TABLE " +
        TABLE_NAME +                       // Table's name
        "( " +                           // The columns in the table
        "'_id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "NAME TEXT, " +
        "DESCRIPTION TEXT, " +
        "LATITUDE REAL, " +
        "LONGITUDE REAL, " +
        "ELEVATION REAL, " +
        "TYPE INTEGER, " +
        "FOREIGN KEY(TYPE) REFERENCES " + TABLE_NAME_TYPES + "(_id)" +
        ")";

    private static final String SQL_CREATE_TYPES = "CREATE TABLE " +
            TABLE_NAME_TYPES +                       // Table's name
            "(" +                           // The columns in the table
            " '_id' INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, VISIBLE INTEGER )";

    /**
     * Helper class that actually creates and manages the provider's underlying data repository.
     */
    protected static final class MainDatabaseHelper extends SQLiteOpenHelper {

        /*
         * Instantiates an open helper for the provider's SQLite data repository
         * Do not do database creation and upgrade here.
         */
        MainDatabaseHelper(Context context) {
            super(context, DBNAME, null, 1);
        }

        /*
         * Creates the data repository. This is called when the provider attempts to open the
         * repository and SQLite reports that it doesn't exist.
         */
        public void onCreate(SQLiteDatabase db) {

            // Creates the main table
            db.execSQL(SQL_CREATE_MAIN);
            db.execSQL(SQL_CREATE_TYPES);
            db.execSQL("INSERT INTO " + TABLE_NAME_TYPES + "(_id, NAME, VISIBLE) VALUES (0, 'Mountains', 1)");
            db.execSQL("INSERT INTO " + TABLE_NAME_TYPES + "(_id, NAME, VISIBLE) VALUES (1, 'Palaces', 1)");
            db.execSQL("INSERT INTO " + TABLE_NAME_TYPES + "(_id, NAME, VISIBLE) VALUES (2, 'Castles', 1)");
            db.execSQL("INSERT INTO " + TABLE_NAME_TYPES + "(_id, NAME, VISIBLE) VALUES (3, 'View towers', 1)");
            db.execSQL("INSERT INTO " + TABLE_NAME_TYPES + "(_id, NAME, VISIBLE) VALUES (4, 'Transmitters', 1)");
            db.execSQL("INSERT INTO " + TABLE_NAME_TYPES + "(_id, NAME, VISIBLE) VALUES (5, 'Ruins', 1)");
            db.execSQL("INSERT INTO " + TABLE_NAME_TYPES + "(_id, NAME, VISIBLE) VALUES (6, 'Lakes', 1)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {// Kills the table and existing data
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TYPES);
            onCreate(db);
        }
    }



    public LocationsProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        /**
         * Chooses the MIME type based on the incoming URI pattern
         */
        switch (sUriMatcher.match(uri)) {

            // If the pattern is for notes or live folders, returns the general content type.
            case LOCATIONS:
                return LOCATION_TYPE;

            // If the pattern is for note IDs, returns the note ID content type.
            case LOCATION_ID:
                return LOCATION_ITEM_TYPE;

            case TYPES:
                return LOCTYPE_TYPE;

            // If the pattern is for note IDs, returns the note ID content type.
            case TYPE_ID:
                return LOCTYPE_ITEM_TYPE;

            // If the URI pattern doesn't match any permitted patterns, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (sUriMatcher.match(uri) == LOCATIONS) {

            // A map to hold the new record's values.
            ContentValues values;

            // If the incoming values map is not null, uses it for the new values.
            if (initialValues == null) {
                values = new ContentValues();
                values.put("NAME", "");
                values.put("DESCRIPTION", "");
                values.put("LATITUDE", 0.0);
                values.put("LONGITUDE", 0.0);
                values.put("ELEVATION", 0.0);
                values.put("TYPE", 0);
                //throw new IllegalArgumentException("No initial values for a new record in " + uri);
            } else {
                values = new ContentValues(initialValues);
            }

            // Opens the database object in "write" mode.
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();

            // Performs the insert and returns the ID of the new note.
            long rowId = db.insert(TABLE_NAME, null, values);


            // If the insert succeeded, the row ID exists.
            if (rowId > 0) {
                // Creates a URI with the note ID pattern and the new row ID appended to it.
                Uri noteUri = ContentUris.withAppendedId(CONTENT_ID_URI_LOCATION, rowId);

                // Notifies observers registered against this provider that the data changed.
                getContext().getContentResolver().notifyChange(noteUri, null);
                return noteUri;
            }


            // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
            throw new SQLException("Failed to insert row into " + uri);
        } else if (sUriMatcher.match(uri) == TYPES) {
            // A map to hold the new record's values.
            ContentValues values;

            // If the incoming values map is not null, uses it for the new values.
            if (initialValues == null) {
                values = new ContentValues();
                values.put("NAME", "");
            } else {
                values = new ContentValues(initialValues);
            }

            // Opens the database object in "write" mode.
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();

            // Performs the insert and returns the ID of the new note.
            long rowId = db.insert(TABLE_NAME_TYPES, null, values);


            // If the insert succeeded, the row ID exists.
            if (rowId > 0) {
                // Creates a URI with the note ID pattern and the new row ID appended to it.
                Uri noteUri = ContentUris.withAppendedId(CONTENT_ID_URI_TYPE, rowId);

                // Notifies observers registered against this provider that the data changed.
                getContext().getContentResolver().notifyChange(noteUri, null);
                return noteUri;
            }


            // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
            throw new SQLException("Failed to insert row into " + uri);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        /*
         * Creates a new helper object. This method always returns quickly.
         * Notice that the database itself isn't created or opened
         * until SQLiteOpenHelper.getWritableDatabase is called
         */
        mOpenHelper = new MainDatabaseHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            // If the incoming URI is for notes, chooses the Notes projection
            case LOCATIONS:
                qb.setTables(TABLE_NAME);
                qb.setProjectionMap(sNotesProjectionMap);
                break;

            /* If the incoming URI is for a single note identified by its ID, chooses the
             * note ID projection, and appends "_ID = <noteID>" to the where clause, so that
             * it selects that single note
             */
            case LOCATION_ID:
                {
                    qb.setTables(TABLE_NAME);
                    qb.setProjectionMap(sNotesProjectionMap);
                    String s = "_id = " + uri.getPathSegments().get(_ID_PATH_POSITION);
                    qb.appendWhere(s);
                }
                break;

            case TYPES:
                qb.setTables(TABLE_NAME_TYPES);
                qb.setProjectionMap(sTypesProjectionMap);
                break;

            /* If the incoming URI is for a single note identified by its ID, chooses the
             * note ID projection, and appends "_ID = <noteID>" to the where clause, so that
             * it selects that single note
             */
            case TYPE_ID:
                {
                    qb.setTables(TABLE_NAME_TYPES);
                    qb.setProjectionMap(sTypesProjectionMap);
                    String s = "_id = " + uri.getPathSegments().get(_ID_PATH_POSITION);
                    qb.appendWhere(s);
                }
                break;

            case VISIBLE_LOCATIONS:
                {
                    qb.setTables(TABLE_NAME + ", " + TABLE_NAME_TYPES);
                    qb.setProjectionMap(sNotesProjectionMap);
                    qb.appendWhere("locations.TYPE=types._id AND types.VISIBLE<>0");
                }
                break;
            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
            }


        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        //Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null );
        /*Cursor c = db.query(TABLE_NAME, null, selection, selectionArgs, groupBy, having, orderBy)
                qb.query(
                db,            // The database to query
                projection,    // The columns to return from the query
                selection,     // The columns for the where clause
                selectionArgs, // The values for the where clause
                null,          // don't group the rows
                null,          // don't filter by row groups
                sortOrder        // The sort order
            );*/

         // Tells the Cursor what URI to watch, so it knows when its source data changes
         int items = c.getCount();
         Log.e("DB", "Items: " + items);

         c.setNotificationUri(getContext().getContentResolver(), uri);
         return c;

    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String finalWhere;

        // Does the update based on the incoming URI pattern
        switch (sUriMatcher.match(uri)) {

            // If the incoming URI matches the general notes pattern, does the update based on
            // the incoming data.
            case LOCATIONS:

                // Does the update and returns the number of rows updated.
                count = db.update(
                    TABLE_NAME, // The database table name.
                    values,                   // A map of column names and new values to use.
                    where,                    // The where clause column names.
                    whereArgs                 // The where clause column values to select on.
                );
                break;

            // If the incoming URI matches a single note ID, does the update based on the incoming
            // data, but modifies the where clause to restrict it to the particular note ID.
            case LOCATION_ID:
                // From the incoming URI, get the note ID
                String noteId = uri.getPathSegments().get(_ID_PATH_POSITION);

                /*
                 * Starts creating the final WHERE clause by restricting it to the incoming
                 * note ID.
                 */
                finalWhere = "_id = " + noteId;

                // If there were additional selection criteria, append them to the final WHERE
                // clause
                if (where !=null) {
                    finalWhere = finalWhere + " AND " + where;
                }


                // Does the update and returns the number of rows updated.
                count = db.update(
                    TABLE_NAME, // The database table name.
                    values,                   // A map of column names and new values to use.
                    finalWhere,               // The final WHERE clause to use
                                              // placeholders for whereArgs
                    whereArgs                 // The where clause column values to select on, or
                                              // null if the values are in the where argument.
                );
                break;

            // If the incoming URI matches the general notes pattern, does the update based on
            // the incoming data.
            case TYPES:

                // Does the update and returns the number of rows updated.
                count = db.update(
                    TABLE_NAME_TYPES, // The database table name.
                    values,                   // A map of column names and new values to use.
                    where,                    // The where clause column names.
                    whereArgs                 // The where clause column values to select on.
                );
                break;

            // If the incoming URI matches a single note ID, does the update based on the incoming
            // data, but modifies the where clause to restrict it to the particular note ID.
            case TYPE_ID:
                // From the incoming URI, get the note ID
                String typeId = uri.getPathSegments().get(_ID_PATH_POSITION);

                /*
                 * Starts creating the final WHERE clause by restricting it to the incoming
                 * note ID.
                 */
                finalWhere = "_id = " + typeId;

                // If there were additional selection criteria, append them to the final WHERE
                // clause
                if (where !=null) {
                    finalWhere = finalWhere + " AND " + where;
                }


                // Does the update and returns the number of rows updated.
                count = db.update(
                    TABLE_NAME_TYPES, // The database table name.
                    values,                   // A map of column names and new values to use.
                    finalWhere,               // The final WHERE clause to use
                                              // placeholders for whereArgs
                    whereArgs                 // The where clause column values to select on, or
                                              // null if the values are in the where argument.
                );
                break;

            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows updated.
        return count;
    }

    /**
     * A block that instantiates and sets static objects
     */
    static {

        /*
         * Creates and initializes the URI matcher
         */
        // Create a new instance
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add a pattern that routes URIs
        sUriMatcher.addURI(AUTHORITY, "locations", LOCATIONS);
        sUriMatcher.addURI(AUTHORITY, "locations/#", LOCATION_ID);
        sUriMatcher.addURI(AUTHORITY, "types", TYPES);
        sUriMatcher.addURI(AUTHORITY, "types/#", TYPE_ID);
        sUriMatcher.addURI(AUTHORITY, "visiblelocations", VISIBLE_LOCATIONS);


        sNotesProjectionMap = new HashMap<String, String>();
        sNotesProjectionMap.put("locations._id", "locations._id");
        sNotesProjectionMap.put("locations.NAME", "locations.NAME");
        sNotesProjectionMap.put("locations.DESCRIPTION", "locations.DESCRIPTION");
        sNotesProjectionMap.put("locations.LATITUDE", "locations.LATITUDE");
        sNotesProjectionMap.put("locations.LONGITUDE", "locations.LONGITUDE");
        sNotesProjectionMap.put("locations.ELEVATION", "locations.ELEVATION");
        sNotesProjectionMap.put("locations.TYPE", "locations.TYPE");

        sTypesProjectionMap = new HashMap<String, String>();
        sTypesProjectionMap.put("_id", "_id");
        sTypesProjectionMap.put("NAME", "NAME");
        sTypesProjectionMap.put("VISIBLE", "VISIBLE");
    }
}
