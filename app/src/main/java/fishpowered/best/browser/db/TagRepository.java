package fishpowered.best.browser.db;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import fishpowered.best.browser.R;
import fishpowered.best.browser.utilities.ItemList;

/**
 * Tags for arranging faves
 */
public class TagRepository {

    public static final String tableName = "Tag";
    public static final String fieldName = "name";
    public static final String fieldId = "id";

    public static int insertTag(String tag, Context context){
        if(tag==null || tag.trim().equals("")){
            return 0;
        }
        ContentValues values = new ContentValues();
        values.put(TagRepository.fieldName, tag.trim());
        return (int) DatabaseHelper.getInstance(context).getWritableDatabase().insertOrThrow(TagRepository.tableName, null, values);
    }

    public static ArrayList<Tag> fetchTags(Context context){
        ArrayList<Tag> tags = new ArrayList<>();
        String sql = "SELECT "
                + TagRepository.fieldId + ", "
                + TagRepository.fieldName;
        sql += " FROM " + TagRepository.tableName;
        sql += " ORDER BY " + TagRepository.fieldName + " ASC ";

        /*if(pageItemLimit >=0 ) {
            sql += " ORDER BY numberOfSearches DESC, " + SearchHistoryRepository.fieldTimeStamp + " DESC ";
            sql += " LIMIT "+(int) (pageItemLimit+1); // +1 so we can see if the limit has been reached for paging
        }else{
            sql += " ORDER BY " + SearchHistoryRepository.fieldSearch +" ASC";
        }*/
        ArrayList<String> queryParams = new ArrayList<String>();
        String[] paramArr = new String[0];
        paramArr = queryParams.toArray(paramArr);

        Cursor c = null;
        try {
            c = DatabaseHelper.getInstance(context).getReadableDatabase().rawQuery(sql, paramArr);

            // Add the spacer before every list heading that has results except the first list
            if (c.moveToFirst()) {
                do {
                    Tag tag = new Tag();
                    tag.bindFromDbRecord(c);
                    tags.add(tag);
                } while (c.moveToNext());
            }
        }catch(CursorIndexOutOfBoundsException e){
        }finally {
            if(c!=null && !c.isClosed()){
                c.close();
            }
        }
        return tags;
    }

    public static boolean addTagsToItemList(ItemList itemList, Context context, ArrayList<Integer> filterByTagIds, String title, boolean addHeadingSpacer) {
        ArrayList<Tag> tags = fetchTags(context);
        boolean hasResults = false;
        if(tags.size()>0){
            if(addHeadingSpacer) {
                itemList.add(ItemRepository.getListSpacingHack(false));
            }
            if(title!=null) {
                Item listHeading = ItemRepository.createListItemHeading(title);
                itemList.add(listHeading);
            }
            for(Tag tag  : tags){
                Item listItem = new Item();
                listItem.typeId = ItemType.TAG_FILTER;
                listItem.title = tag.getName();
                listItem.address = String.valueOf(tag.getId());
                if(filterByTagIds!=null && filterByTagIds.contains(tag.getId())){
                    listItem.isSelected = true;
                }
                itemList.add(listItem);
                hasResults = true;
            }
        }
        return hasResults;
    }

    public static Tag selectTagById(Context context, Integer tagId) {
        ArrayList<Tag> tags = new ArrayList<>();
        String sql = "SELECT "
                + TagRepository.fieldId + ", "
                + TagRepository.fieldName;
        sql += " FROM " + TagRepository.tableName;
        sql += " WHERE id=?";
        sql += " ORDER BY " + TagRepository.fieldName + " ASC ";

        String[] paramArr = new String[1];
        paramArr[0] = tagId+"";

        Cursor c = null;
        try {
            c = DatabaseHelper.getInstance(context).getReadableDatabase().rawQuery(sql, paramArr);

            // Add the spacer before every list heading that has results except the first list
            if (c.moveToFirst()) {
                Tag tag = new Tag();
                tag.bindFromDbRecord(c);
                return tag;
            }
        }catch(CursorIndexOutOfBoundsException e){
        }finally {
            if(c!=null && !c.isClosed()){
                c.close();
            }
        }
        return null;
    }

    public static void deleteTag(int tagId, Context context) {
        String sql = "DELETE ";
        sql += " FROM "+tableName+" WHERE id=?";

        ArrayList<String> queryParams = new ArrayList<String>();
        queryParams.add(String.valueOf(tagId));
        String[] paramArr = new String[queryParams.size()];
        paramArr = queryParams.toArray(paramArr);

        Cursor c = null;
        try {
            c = DatabaseHelper.getInstance(context).getWritableDatabase().rawQuery(sql, paramArr);
            c.moveToFirst();
        }catch(CursorIndexOutOfBoundsException e){
        }finally {
            if(c!=null && !c.isClosed()){
                c.close();
            }
        }
    }

    public static void askToEditOrRemoveTag(@NonNull final Context context, @NonNull final int tagId, String promptMessage,
                                            final Runnable runPostEditOrDelete, @NonNull final TextView tagTextView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setMessage(promptMessage)
                .setNegativeButton(R.string.remove,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        deleteTag(tagId, context);
                        if(runPostEditOrDelete !=null){
                            runPostEditOrDelete.run();
                        }
                    }
                })
                .setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        showEditTagDialog(context, tagTextView, tagId, runPostEditOrDelete);
                    }
                })
                .setPositiveButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private static void showEditTagDialog(final Context context, final TextView existingTagTextView, final int existingTagId, final Runnable runPostEditOrDelete) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.edit_tag));

        // Set up the input
        LayoutInflater li = LayoutInflater.from(context);
        View layout = li.inflate(R.layout.edit_tag_name_layout, null);
        final EditText input = layout.findViewById(R.id.edit_tag_name_text_view);
        input.setText(existingTagTextView.getText());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(layout);
        // Set up the buttons
        builder.setPositiveButton(context.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String tagString = input.getText().toString();
                if(!tagString.trim().equals("")){
                    TagRepository.updateTag(existingTagId, tagString.trim(), context);
                    //existingTagTextView.setText(tagString.trim()); doesn't work cos the recyclerview doesn't know about it
                    runPostEditOrDelete.run();
                }
            }
        });
        builder.setNegativeButton(context.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private static void updateTag(int existingTagId, String tagName, Context context) {
        ContentValues values = new ContentValues();
        values.put(TagRepository.fieldName, tagName.trim());
        String[] whereArgs = new String[1];
        whereArgs[0] = existingTagId+"";
        DatabaseHelper.getInstance(context).getWritableDatabase().update(
                TagRepository.tableName, values, "id=?", whereArgs
        );
    }
}
