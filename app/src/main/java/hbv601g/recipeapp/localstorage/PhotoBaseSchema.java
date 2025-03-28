package hbv601g.recipeapp.localstorage;

/**
 * A class defining the schema of the photo database. This defines any tables and columns that
 * should be used in the database, and their names.
 */
public class PhotoBaseSchema {
    public static final class ImageTable {
        public static final String NAME = "photos";

        public static final class Cols {
            public static final String PHOTO_ID = "id";
            public static final String PHOTO_BLOB = "photo";
        }
    }
}
