package hbv601g.recipeapp.localstorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;


public class ImageLab {
    private static ImageLab sImageLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private ImageLab (Context context) {
        mContext=context.getApplicationContext();
        mDatabase = new DbHelper(mContext).getWritableDatabase();
    }

    public static ImageLab get(Context context) {
        if (sImageLab == null) {
            sImageLab = new ImageLab(context);
        }
        return sImageLab;
    }

    private static ContentValues getContentValues(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageBytes = stream.toByteArray();
        values.put(DbSchema.ImageTable.Cols.PHOTO_BLOB, imageBytes);
        return values;
    }


    public void addPhoto(Bitmap bitmap){
        ContentValues values = getContentValues(bitmap);
        mDatabase.delete(DbSchema.ImageTable.NAME,null,null);
        mDatabase.insert(DbSchema.ImageTable.NAME, null, values);
    }

    public Bitmap getPhoto(){
        Cursor cursor = mDatabase.query(DbSchema.ImageTable.NAME, /*null*/new String[]{DbSchema.ImageTable.Cols.PHOTO_BLOB}, null, null, null, null, null);

        if (cursor.moveToFirst()){
            int colIndex = cursor.getColumnIndex(DbSchema.ImageTable.Cols.PHOTO_BLOB);

            byte[] imageBytes = cursor.getBlob(colIndex);
            cursor.close();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }

        return  null;
    }
}
