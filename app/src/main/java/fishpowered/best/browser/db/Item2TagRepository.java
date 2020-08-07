package fishpowered.best.browser.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;

import java.util.ArrayList;

/**
 * Tags for arranging faves
 */
public class Item2TagRepository {

    public static final String tableName = "Item2Tag";
    public static final String field_id = "id";
    public static final String field_itemId = "itemId";
    public static final String field_tagId = "tagId";

    public static void insertItem2Tag(int itemId, int tagId, Context context){
        ContentValues values = new ContentValues();
        values.put(field_itemId, itemId);
        values.put(field_tagId, tagId);
        DatabaseHelper.getInstance(context).getWritableDatabase().insertOrThrow(tableName, null, values);
    }

    public static ArrayList<Integer> getTagsForItemId(int itemId, Context context){
        ArrayList<Integer> returnValues = new ArrayList<Integer>();
        String sql = "SELECT "
                + field_tagId;
        sql += " FROM " + tableName+" WHERE itemId=?";

        ArrayList<String> queryParams = new ArrayList<String>();
        queryParams.add(String.valueOf(itemId));
        String[] paramArr = new String[queryParams.size()];
        paramArr = queryParams.toArray(paramArr);

        Cursor c = null;
        try {
            c = DatabaseHelper.getInstance(context).getReadableDatabase().rawQuery(sql, paramArr);

            // Add the spacer before every list heading that has results except the first list
            if (c.moveToFirst()) {
                do {
                    returnValues.add(c.getInt(0));
                } while (c.moveToNext());
            }
        }catch(CursorIndexOutOfBoundsException e){
            if(c!=null && !c.isClosed()){
                c.close();
            }
        }finally {
            if(c!=null && !c.isClosed()){
                c.close();
            }
        }
        return returnValues;
    }

    public static void removeItem2Tag(int itemId, int tagId, Context context) {
        String sql = "DELETE ";
        sql += " FROM " + tableName+" WHERE itemId=? AND tagId=?";

        ArrayList<String> queryParams = new ArrayList<String>();
        queryParams.add(String.valueOf(itemId));
        queryParams.add(String.valueOf(tagId));
        String[] paramArr = new String[queryParams.size()];
        paramArr = queryParams.toArray(paramArr);

        Cursor c = null;
        try {
            c = DatabaseHelper.getInstance(context).getWritableDatabase().rawQuery(sql, paramArr);
            c.moveToFirst();
        }catch(CursorIndexOutOfBoundsException e){
            if(c!=null && !c.isClosed()){
                c.close();
            }
        }finally {
            if(c!=null && !c.isClosed()){
                c.close();
            }
        }
    }
}
