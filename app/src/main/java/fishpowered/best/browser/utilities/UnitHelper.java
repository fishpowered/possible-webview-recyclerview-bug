package fishpowered.best.browser.utilities;

import android.content.Context;
import android.util.TypedValue;

public class UnitHelper {

    /**
     * @param val
     * @return
     */
    public static int convertDpToPx(float val, Context context){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, context.getResources().getDisplayMetrics());
    }

    public static float convertPxToDp(int px, Context context){
        return (px / context.getResources().getDisplayMetrics().density);
    }

    public static int getNavBarHeightInPx(boolean isCompactMode, Context context) {
        return UnitHelper.convertDpToPx(isCompactMode ? 48 : 62, context);
    }

    public static int getSpeedDialToolBarHeightInPx(boolean isCompactMode, Context context) {
        return UnitHelper.convertDpToPx(isCompactMode ? 36 : 42, context);
    }

    public static float getScreenDensity(Context context){
        float scale = context.getResources().getDisplayMetrics().density;
        return scale;
    }

    public static float calculatePercentagePageIsSelected(int pageNumber, int pageWidth, int currentOffset){
        float pageOffset = pageNumber * pageWidth;
        pageOffset = Math.abs(currentOffset - pageOffset);
        if(pageOffset > pageWidth){
            pageOffset = pageWidth;
        }
        return 1f - (pageOffset / pageWidth);
    }

    /**
     * Given some text, see if we can parse a number unit from it e.g. 12kg
     * @param possibleNumberString
     * @return
     */
    public static String getRealWorldUnitConversion(String possibleNumberString){
        if(possibleNumberString ==null){
            return null;
        }

        // TODO support fractions done with the superscript looking characters
        possibleNumberString = possibleNumberString.trim().toLowerCase();
        String converted = attemptRealWorldUnitConversion(possibleNumberString, " kg", " lbs", 2.20462262185f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " kgs", " lbs", 2.20462262185f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " kilogram", " pounds", 2.20462262185f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " kilograms", " pounds", 2.20462262185f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " lbs", " kg", 0.45359237f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " lb", " kg", 0.45359237f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " pound", " kg", 0.45359237f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " pounds", " kg", 0.45359237f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " miles", " km", 1.60934f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " km", " miles", 0.621371f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " km(s)", " miles", 0.621371f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " kilometers", " miles", 0.621371f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " kilometres", " miles", 0.621371f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, "\"", " cm", 2.54f); // inches
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " m", " feet", 3.28084f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " metre", " feet", 3.28084f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " metres", " feet", 3.28084f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " meter", " feet", 3.28084f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " meters", " feet", 3.28084f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " cm", " inches", 0.393701f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " inches", " cm", 2.54f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " inch", " cm", 2.54f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " in", " cm", 2.54f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " feet", " meters", 0.3048f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " ft", " meters", 0.3048f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " foot", " meters", 0.3048f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, "'", " meters", 0.3048f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " litre", " gallons", 0.264172f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " litres", " gallons", 0.264172f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " dl", " oz", 3.3814f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " cl", " oz", 0.33814f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " l", " gallons", 0.264172f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " oz", " decilitres (dl)", 0.295735f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " ounce", " decilitres (dl)", 0.295735f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " ounces", " decilitres (dl)", 0.295735f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " yards", " meters", 0.9144f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " yard", " meters", 0.9144f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " yd", " meters", 0.9144f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " yds", " meters", 0.9144f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " grams", " oz", 0.035274f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " g", " oz", 0.035274f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " teaspoon", " grams", 4.2605668f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " teaspoons", " grams", 4.2605668f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " tsp", " grams", 4.2605668f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " millilitre", " oz", 0.033814f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " millilitres", " oz", 0.033814f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " ml", " oz", 0.033814f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " tablespoon", " grams", 15f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " tablespoons", " grams", 15f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " tbsp", " g", 15f);
        if(converted!=null){
            return removePointZero(converted);
        }
        if(possibleNumberString.endsWith("cups") || possibleNumberString.endsWith("cup")){
            return "__show_cup_conversion__";
        }
        if(possibleNumberString.endsWith("ºc") ){
            Float celsius = getNumberFromNumberAndUnit(possibleNumberString, "ºC");
            if(celsius!=null){
                return convertCelsiusToFahrenheitAndGasMark(celsius);
            }
        }
        if(possibleNumberString.endsWith("°c") ){ // note: this is not a copy and paste mistake, there are multiple degree chars that look the same
            Float celsius = getNumberFromNumberAndUnit(possibleNumberString, "°C");
            if(celsius!=null){
                return convertCelsiusToFahrenheitAndGasMark(celsius);
            }
        }
        if(possibleNumberString.endsWith("c") ){
            Float celsius = getNumberFromNumberAndUnit(possibleNumberString, "C");
            if(celsius!=null){
                return convertCelsiusToFahrenheitAndGasMark(celsius);
            }
        }
        if(possibleNumberString.endsWith("° celsius") ){
            Float celsius = getNumberFromNumberAndUnit(possibleNumberString, "° celsius");
            if(celsius!=null){
                return convertCelsiusToFahrenheitAndGasMark(celsius);
            }
        }
        if(possibleNumberString.endsWith("º celsius") ){
            Float celsius = getNumberFromNumberAndUnit(possibleNumberString, "º celsius");
            if(celsius!=null){
                return convertCelsiusToFahrenheitAndGasMark(celsius);
            }
        }

        if(possibleNumberString.endsWith("ºf")){ // note: this is not a copy and paste mistake, there are multiple degree chars that look the same
            Float fahrenheit = getNumberFromNumberAndUnit(possibleNumberString, "ºF");
            if(fahrenheit!=null){
                return convertFahrenheightToCelsiusAndGasMark(fahrenheit);
            }
        }
        if(possibleNumberString.toLowerCase().endsWith("°f")){ // note: this is not a copy and paste mistake, there are multiple degree chars that look the same
            Float fahrenheit = getNumberFromNumberAndUnit(possibleNumberString, "°F");
            if(fahrenheit!=null){
                return convertFahrenheightToCelsiusAndGasMark(fahrenheit);
            }
        }
        if(possibleNumberString.toLowerCase().endsWith("f")){ // note: this is not a copy and paste mistake, there are multiple degree chars that look the same
            Float fahrenheit = getNumberFromNumberAndUnit(possibleNumberString, "F");
            if(fahrenheit!=null){
                return convertFahrenheightToCelsiusAndGasMark(fahrenheit);
            }
        }
        if(possibleNumberString.toLowerCase().endsWith("° fahrenheit")){
            Float fahrenheit = getNumberFromNumberAndUnit(possibleNumberString, "° fahrenheit");
            if(fahrenheit!=null){
                return convertFahrenheightToCelsiusAndGasMark(fahrenheit);
            }
        }
        if(possibleNumberString.toLowerCase().endsWith("º fahrenheit")){
            Float fahrenheit = getNumberFromNumberAndUnit(possibleNumberString, "º fahrenheit");
            if(fahrenheit!=null){
                return convertFahrenheightToCelsiusAndGasMark(fahrenheit);
            }
        }

        if(possibleNumberString.startsWith("gas mark")) {
            String numString = possibleNumberString.substring("gas mark".length(), possibleNumberString.trim().length()).trim();
            try{
                int gasMark = Integer.parseInt(numString);
                return "gas mark "+gasMark+" = "+convertGasMarkToCelsius(gasMark)+"ºC or "+convertGasMarkToFahrenheit(gasMark)+"ºF";
            }catch (NumberFormatException e){

            }
        }
        /*converted = attemptRealWorldUnitConversion(possibleNumberString, " cup", " grams", 220f);
        if(converted!=null){
            return removePointZero(converted);
        }
        converted = attemptRealWorldUnitConversion(possibleNumberString, " cups", " grams", 120f);
        if(converted!=null){
            return removePointZero(converted);
        }*/
        return null;
    }

    public static String convertCelsiusToFahrenheitAndGasMark(Float celsius) {
        if(celsius==null){
            return null;
        }
        float fahrenheit = roundDecimal(celsius*1.8f+32f);
        String gasMark = convertCelsiusToGasMark(celsius, fahrenheit) > 0 ? " (gas mark "+convertCelsiusToGasMark(celsius, fahrenheit)+")" : "";
        return (roundDecimal(celsius)+"ºC = "+roundDecimal(fahrenheit)+"ºF").replace(".0º", "º")+gasMark;
    }

    private static String convertFahrenheightToCelsiusAndGasMark(Float fahrenheit){
        if(fahrenheit!=null){
            float celsius = roundDecimal((fahrenheit-32f) / 1.8f);
            String gasMark = convertCelsiusToGasMark(celsius, fahrenheit) > 0 ? " (gas mark "+convertCelsiusToGasMark(celsius, fahrenheit)+")" : "";
            return (roundDecimal(fahrenheit)+"ºF = "+roundDecimal(celsius)+"ºC").replace(".0º", "º")+gasMark;
        }
        return null;
    }

    private static int convertGasMarkToFahrenheit(int gasMark) {
        switch(gasMark){
            case 1:
                return 275;
            case 2:
                return 300;
            case 3:
                return 325;
            case 4:
                return 350;
            case 5:
                return 375;
            case 6:
                return 400;
            case 7:
                return 425;
            case 8:
                return 450;
            case 9:
                return 475;
            case 10:
                return 500;
        }
        return 0;
    }

    private static int convertGasMarkToCelsius(int gasMark) {
        switch(gasMark){
            case 1:
                return 140;
            case 2:
                return 150;
            case 3:
                return 165;
            case 4:
                return 177;
            case 5:
                return 190;
            case 6:
                return 200;
            case 7:
                return 220;
            case 8:
                return 230;
            case 9:
                return 245;
            case 10:
                return 260;
        }
        return 0;
    }

    public static int convertCelsiusToGasMark(float celsius, float fahrenheit) {
        if(Math.abs(celsius-140f) < 0.1f || Math.abs(fahrenheit-275f) < 0.1f){
            return 1;
        }
        if(Math.abs(celsius-150f) < 0.1f || Math.abs(fahrenheit-300f) < 0.1f){
            return 2;
        }
        if(Math.abs(celsius-165f) < 0.1f || Math.abs(fahrenheit-325f) < 0.1f){
            return 3;
        }
        if(Math.abs(celsius-177f) < 0.1f || Math.abs(fahrenheit-350f) < 0.1f){
            return 4;
        }
        if(Math.abs(celsius-190f) < 0.1f || Math.abs(fahrenheit-375f) < 0.1f){
            return 5;
        }
        if(Math.abs(celsius-200f) < 0.1f || Math.abs(fahrenheit-400f) < 0.1f){
            return 6;
        }
        if(Math.abs(celsius-220f) < 0.1f || Math.abs(fahrenheit-425f) < 0.1f){
            return 7;
        }
        if(Math.abs(celsius-230f) < 0.1f || Math.abs(fahrenheit-450f) < 0.1f){
            return 8;
        }
        if(Math.abs(celsius-245f) < 0.1f || Math.abs(fahrenheit-475f) < 0.1f){
            return 9;
        }
        if(Math.abs(celsius-260f) < 0.1f || Math.abs(fahrenheit-500f) < 0.1f){
            return 10;
        }
        return 0;
    }

    private static String removePointZero(String string) {
        return (string+" ").replace(".0 ", " ").trim();
    }

    private static String attemptRealWorldUnitConversion(String possibleNumberString, String sourceUnitPrefix, String targetUnitPrefix, float conversionRate){
        // CAREFUL WITH TRIMMING, sourceUnitPrefix intentionally has whitespace at the beginning for some units e.g. " km", and not for others "'" (feet)
        try {
            Float num = getNumberFromNumberAndUnit(possibleNumberString, sourceUnitPrefix);
            if(num!=null){
                float converted = roundDecimal(num*conversionRate);
                return roundDecimal(num)+sourceUnitPrefix+" = "+roundDecimal(converted)+targetUnitPrefix;
            }
        }catch(Exception e){
            return null;
        }
        return null;
    }

    /**
     * Return the number part from a string that has a number and a unit
     * e.g. given "1 1/2 cup" return 1.5
     * @param possibleNumberString
     * @param sourceUnitPrefix
     * @return
     */
    public static Float getNumberFromNumberAndUnit(String possibleNumberString, String sourceUnitPrefix){
        if(possibleNumberString.toLowerCase().trim().endsWith(sourceUnitPrefix.trim().toLowerCase())) {
            String numString = possibleNumberString.substring(0, possibleNumberString.trim().length() - sourceUnitPrefix.trim().length()).trim();
            numString = numString.replace(",", ".");
            return parseNumberOrFraction(numString);
        }
        return null;
    }

    public static Float parseNumberOrFraction(String numString){
        try {
            numString = numString.trim().replace(",", ".");
            String boneless = numString // the goal is to remove things that are valid in number strings to check if what remains is numeric
                    .replace(" ", "")
                    .replace("/", "")
                    .replace(",", "")
                    .replace(".", "")
                    .replace("½", "1")
                    .replace("⅓", "1")
                    .replace("⅔", "1")
                    .replace("¼", "1")
                    .replace("¾", "1")
                    .replace("⅕", "1")
                    .replace("⅖", "1")
                    .replace("⅗", "1")
                    .replace("⅘", "1")
            ;
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(boneless); // errors if not a number
            boolean isFraction = numString.contains("/");
            if (isFraction) {
                float baseNumber = 0f;
                String fractionPart = "";
                String[] baseAndFractionParts = numString.split(" ");
                if (baseAndFractionParts.length == 2) {
                    baseNumber = Integer.parseInt(baseAndFractionParts[0]);
                    fractionPart = baseAndFractionParts[1];
                } else if (baseAndFractionParts.length == 1) {
                    fractionPart = baseAndFractionParts[0];
                } else {
                    throw new NumberFormatException();
                }
                String[] rat = fractionPart.split("/");
                if (rat.length != 2) {
                    throw new NumberFormatException();
                }
                float fractionAsFloat = Float.parseFloat(rat[0]) / Float.parseFloat(rat[1]);
                return baseNumber + fractionAsFloat;
            } else {
                if(stringStartsWithFraction(numString)){
                    numString = "0"+numString;
                }
                String fractionsReplaced = numString
                        .replace("½", ".5")
                        .replace("⅓", ".33")
                        .replace("⅔", ".66")
                        .replace("¼", ".25")
                        .replace("¾", ".75")
                        .replace("⅕", ".2")
                        .replace("⅖", ".4")
                        .replace("⅗", ".6")
                        .replace("⅘", ".8")
                        ;
                return Float.parseFloat(fractionsReplaced);
            }
        }catch(NumberFormatException e){
            return null;
        }
    }

    private static boolean stringStartsWithFraction(String numString) {
        return numString.startsWith("½") ||
        numString.startsWith("⅓") ||
        numString.startsWith("⅔") ||
        numString.startsWith("¼") ||
        numString.startsWith("¾") ||
        numString.startsWith("⅕") ||
        numString.startsWith("⅖") ||
        numString.startsWith("⅗") ||
        numString.startsWith("⅘");
    }

    public static float roundDecimal(float n){
        return (float) Math.round(n * 100) / 100;
    }
}
