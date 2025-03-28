package hbv601g.recipeapp.localstorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;


/**
 * A singleton class for database interactions with the photo database
 */
public class PhotoBaseLab {
    private static PhotoBaseLab sPhotoBaseLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    /**
     * Creates a new PhotoBaseLab with the given Context. Uses the context to get a writable
     * database
     * @param context the context to use for the ImageLab
     */
    private PhotoBaseLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new PhotoBaseHelper(mContext).getWritableDatabase();
    }

    /**
     * Returns the static PhotoBaseLab instance, or creates a new one with the given context
     * if no PhotoBaseLab exists. Only one PhotoBaseLab instance can exist at any given time
     *
     * @param context the context to be used for a new PhotoBaseLab instance
     * @return the ImageLab instance
     */
    public static PhotoBaseLab get(Context context) {
        if (sPhotoBaseLab == null) {
            sPhotoBaseLab = new PhotoBaseLab(context);
        }
        return sPhotoBaseLab;
    }

    /**
     * Turns the given bitmap into a blob that can be used in the database
     * @param bitmap a Bitmap that should be turned into a blob
     * @return ContentValues containing a blob compatible with the database
     */
    private static ContentValues getContentValues(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageBytes = stream.toByteArray();
        values.put(PhotoBaseSchema.ImageTable.Cols.PHOTO_BLOB, imageBytes);
        return values;
    }


    /**
     * Deletes the image currently in the database and replaces it with the image represented
     * by the given bitmap
     * @param bitmap a bitmap for the new image
     */
    public void addPhoto(Bitmap bitmap){
        ContentValues values = getContentValues(bitmap);
        mDatabase.delete(PhotoBaseSchema.ImageTable.NAME,null,null);
        mDatabase.insert(PhotoBaseSchema.ImageTable.NAME, null, values);
    }

    /**
     * Queries the database to try to get the saved image from it.
     * @return a bitmap for the image in the database, or null if none is found
     */
    public Bitmap getPhoto(){
        Cursor cursor = mDatabase.query(PhotoBaseSchema.ImageTable.NAME,
                new String[]{PhotoBaseSchema.ImageTable.Cols.PHOTO_BLOB}, null, null, null, null, null);

        if (cursor.moveToFirst()){
            byte[] imageBytes = cursor.getBlob(0);
            cursor.close();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }

        return  null;
    }
}
