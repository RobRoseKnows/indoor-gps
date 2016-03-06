package io.robrose.hack.indoorgps.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This is the contract for the signal database. It holds all the constants for the database.
 * @author Robert Rose
 */
public class TrainingContract {
    public static final String CONTENT_AUTHORITY = "io.robrose.hack.indoorgps";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TRAINING = "training";

    public static final class TrainingEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAINING).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAINING;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAINING;

        public static final String TABLE_NAME = "training";

        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_MAC = "mac";
        public static final String COLUMN_AVG_STRENGTH = "avg_strength";
        public static final String COLUMN_STD_DEV = "std_dev";
        public static final String COLUMN_SAMPLE = "sample_size";

        public static Uri buildSignalUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrainingLocation(String loc) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_LOCATION).appendPath(loc).build();
        }

        public static Uri buildTrainingMac(String mac) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_MAC).appendPath(mac).build();
        }
        
        public static Uri buildTrainingIndiv(String loc, String mac) {
            return CONTENT_URI.buildUpon().appendPath(loc).appendPath(mac).build();
        }

        public static String getLocationFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getMacFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getLocationInIndivQueryFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getMacInIndivQueryFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }
}
