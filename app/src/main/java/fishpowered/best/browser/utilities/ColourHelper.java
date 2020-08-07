package fishpowered.best.browser.utilities;

public class ColourHelper {

    public static int hexStringToHex(String hex){
        if(hex==null){
            return 0;
        }
        if(hex.startsWith("#")){
            hex = hex.substring(1);
        }
        return Integer.parseInt(hex, 16);
    }
}
