package fishpowered.best.browser.db;

import android.database.Cursor;

public class Tag {
    private int id;
    private String name;
    public void bindFromDbRecord(Cursor c) {
        id          = c.getInt(c.getColumnIndex(TagRepository.fieldId));
        name        = c.getString(c.getColumnIndex(TagRepository.fieldName));
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
