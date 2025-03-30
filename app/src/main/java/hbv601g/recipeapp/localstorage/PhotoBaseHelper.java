package hbv601g.recipeapp.localstorage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A helper class to manage database creation and version management for the photo database
 */
public class PhotoBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "imageBase.db";

    public PhotoBaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + PhotoBaseSchema.ImageTable.NAME + "(" +
                PhotoBaseSchema.ImageTable.Cols.PHOTO_ID + " integer primary key autoincrement, " +
                PhotoBaseSchema.ImageTable.Cols.PHOTO_BLOB + " blob" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
