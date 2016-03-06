package io.robrose.hack.indoorgps.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Robert on 3/5/2016.
 */
public class TrainingProvider extends ContentProvider{
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TrainingDbHelper mOpenHelper;
    private static final SQLiteQueryBuilder sQueryBuilder = new SQLiteQueryBuilder();

    // Get ALL the things!
    static final int TRAINING = 100;
    // Get all data points at a location
    static final int TRAINING_LOCATION = 101;
    // Get all data points with the same MAC address.
    static final int TRAINING_MAC = 102;
    // Get the individual data point at a provided location and MAC address
    static final int TRAINING_INDIV = 103;
    
    //signal.location = ?
    private static final String sLocationSelection =
            TrainingContract.TrainingEntry.TABLE_NAME+
                    "." + TrainingContract.TrainingEntry.COLUMN_LOCATION + " = ? ";

    private static final String sMacSelection =
            TrainingContract.TrainingEntry.TABLE_NAME +
                    "." + TrainingContract.TrainingEntry.COLUMN_MAC + " = ? ";

    private static final String sIndividualSelection =
            TrainingContract.TrainingEntry.TABLE_NAME +
                    "." + TrainingContract.TrainingEntry.COLUMN_LOCATION + " = ? AND " +
                    TrainingContract.TrainingEntry.COLUMN_MAC + " = ? ";

    private Cursor getTrainingByLocation(Uri uri, String[] projection, String sortOrder) {
        String location = TrainingContract.TrainingEntry.getLocationFromUri(uri);

        String selection = sLocationSelection;
        String[] selectionArgs = new String[]{location};

        return sQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrainingByMac(Uri uri, String[] projection, String sortOrder) {
        String mac = TrainingContract.TrainingEntry.getMacFromUri(uri);

        String selection = sMacSelection;
        String[] selectionArgs = new String[]{mac};

        return sQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrainingIndiv(Uri uri, String[] projection, String sortOrder) {
        String location = TrainingContract.TrainingEntry.getLocationInIndivQueryFromUri(uri);
        String mac = TrainingContract.TrainingEntry.getMacInIndivQueryFromUri(uri);

        String selection = sIndividualSelection;
        String[] selectionArgs = new String[]{location, mac};

        return sQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TrainingContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, TrainingContract.PATH_TRAINING, TRAINING);
        matcher.addURI(authority, TrainingContract.PATH_TRAINING + "/" +
                TrainingContract.TrainingEntry.COLUMN_LOCATION + "/*", TRAINING_LOCATION);
        matcher.addURI(authority, TrainingContract.PATH_TRAINING + "/" +
                TrainingContract.TrainingEntry.COLUMN_MAC + "/*", TRAINING_MAC);
        matcher.addURI(authority, TrainingContract.PATH_TRAINING + "/*/*", TRAINING_INDIV);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new TrainingDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.
     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case TRAINING:
                return TrainingContract.TrainingEntry.CONTENT_TYPE;
            case TRAINING_LOCATION:
                return TrainingContract.TrainingEntry.CONTENT_TYPE;
            case TRAINING_MAC:
                return TrainingContract.TrainingEntry.CONTENT_TYPE;
            case TRAINING_INDIV:
                return TrainingContract.TrainingEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // training
            case TRAINING: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TrainingContract.TrainingEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // training/location/*
            case TRAINING_LOCATION: {
                retCursor = getTrainingByLocation(uri, projection, sortOrder);
                break;
            }

            // training/mac/*
            case TRAINING_MAC: {
                retCursor = getTrainingByMac(uri, projection, sortOrder);
                break;
            }

            // training/*/*
            case TRAINING_INDIV: {
                retCursor = getTrainingIndiv(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TRAINING: {
                long _id = db.insert(TrainingContract.TrainingEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TrainingContract.TrainingEntry.buildSignalUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case TRAINING:
                rowsDeleted = db.delete(
                        TrainingContract.TrainingEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case TRAINING:
                rowsUpdated = db.update(TrainingContract.TrainingEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRAINING:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TrainingContract.TrainingEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
