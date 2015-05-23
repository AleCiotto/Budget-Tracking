package mercurio.alessandro.budgettracking;

import android.provider.BaseColumns;

/**
 * Created by Alessandro on 07/08/2014.
 */
public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public FeedReaderContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "cost";
        public static final String COLUMN_NAME_ENTRY_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_IMG_URL = "img_url";
        public static final String COLUMN_NAME_IMG_URL_CUSTOM = "img_url_custom";
        public static final String COLUMN_NAME_ALERT = "alert";
    }
}