package fishpowered.best.browser.db;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * !!IMPORTANT!!
     * !!IMPORTANT!!
     * !!IMPORTANT!!
     * !!IMPORTANT!!
     * !!IMPORTANT!!
     * !!IMPORTANT!!
     *
     * REMEMBER TO UPDATE THIS WHEN UPDATING THE DATABASE, PLUS onCreate AND onUpdate SCRIPTS
     * DB VERSION FOLLOWS SOFTWARE VERSION e.g. 0.9.0 => 90, 1.1.2 => 112
     * Versions can jump when updating, so must do checks like if(oldVersion < version being set now)
     *
     * !!IMPORTANT!!
     * !!IMPORTANT!!
     * !!IMPORTANT!!
     * !!IMPORTANT!!
     * !!IMPORTANT!!
     * !!IMPORTANT!!
     */
    private static final int DATABASE_VERSION = 120;
    public static final String DATABASE_NAME = "fishpoweredbrowser";
    private final Context context;
    public static boolean showBrowserUpdatedWhatsNewPage = false;

    /**
     * @deprecated not sure i can make this private due to db install stuff. Use getInstance
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    private static DatabaseHelper mInstance = null;
    public static DatabaseHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON");
        super.onConfigure(db);
    }

    /**
     * onCreate() is only run when the database file did not exist and was just created.
     * If onCreate() returns successfully (doesn't throw an exception), the database is assumed to
     * be created with the requested version number. As an implication, you should not catch
     * SQLExceptions in onCreate() yourself.
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                ItemRepository.tableName + " (" +
                ItemRepository.fieldId +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ItemRepository.fieldTypeId + " INTEGER NOT NULL," +
                ItemRepository.fieldAddress + " TEXT COLLATE NOCASE," +
                ItemRepository.fieldTrustedAddress + " TEXT COLLATE NOCASE," +
                ItemRepository.fieldThumbAddress + " TEXT COLLATE NOCASE," +
                ItemRepository.fieldTitle + " TEXT COLLATE NOCASE," +
                ItemRepository.fieldIsFavourite + " INTEGER," +
                ItemRepository.fieldIsTrusted + " INTEGER," +
                ItemRepository.fieldReadLaterStatus + " INTEGER," +
                ItemRepository.fieldBlockGdprWarnings + " INTEGER," +
                ItemRepository.fieldBlockAds + " INTEGER," +
                ItemRepository.fieldDesktopMode + " INTEGER," +
                ItemRepository.fieldCustomCss+ " TEXT," +
                ItemRepository.fieldCustomJs+ " TEXT," +
                ItemRepository.fieldAllowGps + " INTEGER," +
                ItemRepository.fieldDailyUsageLimit + " INTEGER," +
                ItemRepository.fieldClickCount+ " INTEGER DEFAULT 0" +
                ")");
        /*String address = "https://fishpowered.github.io/fishpowered-browser-welcome/faving-images-animation.gif";
        ItemRepository.save(context, 0, address, "How to fave images",
                0, true, true, ItemType.FAVE_IMAGE, sqLiteDatabase);*/
        createTagTable(sqLiteDatabase);
        createItem2TagTable(sqLiteDatabase);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                BrowsingHistoryRepository.tableName + " (" +
                BrowsingHistoryRepository.fieldId+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BrowsingHistoryRepository.fieldTitle + " TEXT COLLATE NOCASE," +
                BrowsingHistoryRepository.fieldAddress + " TEXT COLLATE NOCASE," +
                BrowsingHistoryRepository.fieldDomain + " TEXT COLLATE NOCASE," +
                BrowsingHistoryRepository.fieldTabId + " INTEGER," + // info collected but not used currently
                BrowsingHistoryRepository.fieldBeginTimeStamp + " INTEGER," +
                BrowsingHistoryRepository.fieldEndTimeStamp + " INTEGER" + // no longer used but leave just in case
        ")");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                SearchHistoryRepository.tableName + " (" +
                SearchHistoryRepository.fieldId+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SearchHistoryRepository.fieldSearch + " TEXT COLLATE NOCASE," +
                SearchHistoryRepository.fieldTimeStamp + " INTEGER" +
                ")");
    }

    /**
     * onUpgrade() is only called when the database file exists but the stored version number is lower than
     * requested in constructor. The onUpgrade() should update the table schema to the requested version.
     *
     * DB VERSIONS CAN JUMP (AT LEAST OUTSIDE OF PLAY STORE) SO PERHAPS BEST APPROACH IS TO CHECK THE
     * OLDVERSION TO DECIDE WHETHER A DB UPDATE NEEDS TO BE PERFORMED
     *
     * @param sqLiteDatabase
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        /*sqLiteDatabase.execSQL("ALTER TABLE "+ItemRepository.tableName+ " ADD COLUMN "+ItemRepository.fieldClickCount+ " INTEGER DEFAULT 0");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ItemRepository.tableName);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ItemTagRepository.tableName);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HistoryRepository.tableName); */
        // TODO STOP USING CONSTANTS HERE, BECAUSE THEN UPDATING CODE WILL BREAK PATCHES...
        if(oldVersion==8){
            // PRE-RELEASE UPGRADE
            sqLiteDatabase.execSQL("ALTER TABLE "+ItemRepository.tableName+ " " +
                    "ADD COLUMN "+ItemRepository.fieldBlockGdprWarnings+ " INTEGER");
            sqLiteDatabase.execSQL("ALTER TABLE "+ItemRepository.tableName+ " " +
                    "ADD COLUMN "+ItemRepository.fieldBlockAds+ " INTEGER");
            sqLiteDatabase.execSQL("ALTER TABLE "+ItemRepository.tableName+ " " +
                    "ADD COLUMN "+ItemRepository.fieldAllowGps+ " INTEGER");
        }
        if(oldVersion<=10){
            // PRE-RELEASE UPGRADE
            sqLiteDatabase.execSQL("ALTER TABLE "+ItemRepository.tableName+ " " +
                    "ADD COLUMN "+ItemRepository.fieldThumbAddress+ " TEXT COLLATE NOCASE");
        }
        if(oldVersion < 90) {
            // SQL upgrades for 0.9.0 release (PRE-RELEASE)
            sqLiteDatabase.execSQL("ALTER TABLE "+ItemRepository.tableName+ " " +
                    "ADD COLUMN "+ItemRepository.fieldReadLaterStatus+ " INTEGER");
        }
        if(oldVersion < 104){
            // Adding fave tag support
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS ItemTag");
            createTagTable(sqLiteDatabase);
            createItem2TagTable(sqLiteDatabase);
        }
        if(oldVersion < 118){
            // Adding desktop mode support
            sqLiteDatabase.execSQL("ALTER TABLE "+ItemRepository.tableName+ " " +
                    "ADD COLUMN "+ItemRepository.fieldDesktopMode+ " INTEGER");
        }
        if(oldVersion < 119){
            // Adding custom css+js
            sqLiteDatabase.execSQL("ALTER TABLE "+ItemRepository.tableName+ " " +
                    "ADD COLUMN "+ItemRepository.fieldCustomCss+ " TEXT");
            sqLiteDatabase.execSQL("ALTER TABLE "+ItemRepository.tableName+ " " +
                    "ADD COLUMN "+ItemRepository.fieldCustomJs+ " TEXT");
        }

        // REMEMBER TO ADD TO THE onCREATE SCRIPT AS WELL WHEN ADDING NEW COLUMNS!

        if(oldVersion==104 || oldVersion==107 || oldVersion==110 || oldVersion==118 || oldVersion==119){
            showBrowserUpdatedWhatsNewPage = true;
        }
        onCreate(sqLiteDatabase);
    }

    private void createItem2TagTable(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                "Item2Tag (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "itemId INTEGER NOT NULL, " +
                "tagId INTEGER NOT NULL, " +
                "CONSTRAINT tagIdFK" +
                "    FOREIGN KEY (tagId)" +
                "    REFERENCES Tag (id)" +
                "    ON DELETE CASCADE," +
                "CONSTRAINT itemIdFK " +
                "    FOREIGN KEY (itemId)" +
                "    REFERENCES Item (id)" +
                "    ON DELETE CASCADE" +
        ")");
    }

    private void createTagTable(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                "Tag (" +
                "name TEXT COLLATE NOCASE, " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT " +
        ")");
    }

    /**
     * Called when the database needs to be downgraded. This is strictly similar to
     * {@link #onUpgrade} method, but is called whenever current version is newer than requested one.
     * However, this method is not abstract, so it is not mandatory for a customer to
     * implement it. If not overridden, default implementation will reject downgrade and
     * throws SQLiteException
     *
     * <p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // just so we don't get errors testing old versions. This doesn't revert tables or anything
        //super.onDowngrade(db, oldVersion, newVersion);
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Toast.makeText(context, "DB set up error: "+sqlEx.getMessage(), Toast.LENGTH_LONG).show();
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Toast.makeText(context, "DB set up exception: "+ex.getMessage(), Toast.LENGTH_LONG).show();
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
}