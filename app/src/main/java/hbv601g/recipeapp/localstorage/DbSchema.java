package hbv601g.recipeapp.localstorage;

public class DbSchema {
    public static final class ImageTable {
        public static final String NAME = "photos";
        public static final class Cols {
            public static final String PHOTO_ID = "id";
            public static final String PHOTO_BLOB = "photo";
        }
    }
}
