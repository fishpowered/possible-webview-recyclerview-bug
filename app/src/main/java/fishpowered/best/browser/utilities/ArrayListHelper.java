package fishpowered.best.browser.utilities;

import java.util.ArrayList;

public class ArrayListHelper {

    public static String[] convertToStringArray(ArrayList<String> list){
        String[] stringParamArray = new String[list.size()];
        int stringIndex = 0;
        for(String qParam : list){
            stringParamArray[stringIndex] = qParam;
            stringIndex++;
        }
        return stringParamArray;
    }

    public static void removeIntValue(ArrayList<Integer> list, int tagId) {
        list.remove(Integer.valueOf(tagId)); // important to cast to Integer as int will remove index
    }
}
