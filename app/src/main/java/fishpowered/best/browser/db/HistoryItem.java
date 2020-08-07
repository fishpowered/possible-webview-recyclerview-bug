package fishpowered.best.browser.db;

import fishpowered.best.browser.utilities.DateTimeHelper;

public class HistoryItem {
    public int id;
    public String title;
    public String address;
    public int tabId;
    public int beginTimeStamp;
    public int endTimeStamp;

    public int getDurationInSeconds(){
        int localEndTime;
        if(endTimeStamp > beginTimeStamp){
            return endTimeStamp - beginTimeStamp;
        } else {
            localEndTime = DateTimeHelper.getCurrentTimeStamp();
            return Math.abs(localEndTime - beginTimeStamp);
        }
    }
}
