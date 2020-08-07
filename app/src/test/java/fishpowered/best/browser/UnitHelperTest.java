package fishpowered.best.browser;

import org.junit.Test;

import fishpowered.best.browser.utilities.UnitHelper;
import fishpowered.best.browser.utilities.UrlHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UnitHelperTest {


    @Test
    public void testParseNumberOrFraction(){
        assertEquals(1.5f, UnitHelper.parseNumberOrFraction(" 1,50 "), 0.01);
        assertEquals(1.5f, UnitHelper.parseNumberOrFraction(" 1.50 "), 0.01);
        assertEquals(1.51f, UnitHelper.parseNumberOrFraction(" 1.51 "), 0.01);
        assertEquals(0.6f, UnitHelper.parseNumberOrFraction(" 3/5 "), 0.01);
        assertEquals(22.6f, UnitHelper.parseNumberOrFraction(" 22 3/5 "), 0.01);
        assertEquals(2.1f, UnitHelper.parseNumberOrFraction(" 2 1/10 "), 0.01);
        assertEquals(2.1f, UnitHelper.parseNumberOrFraction(" 2 1/10 "), 0.01);
        assertEquals(0.5f, UnitHelper.parseNumberOrFraction(" ½ "), 0.01);
        assertEquals(2.5f, UnitHelper.parseNumberOrFraction(" 2½ "), 0.01);
        assertEquals(22.5f, UnitHelper.parseNumberOrFraction(" 22½ "), 0.01);
        assertEquals(22.75f, UnitHelper.parseNumberOrFraction(" 22¾ "), 0.01);
        assertEquals(100.66f, UnitHelper.parseNumberOrFraction(" 100⅔ "), 0.01);
        assertNull(UnitHelper.parseNumberOrFraction("2x1/10"));
        assertNull(UnitHelper.parseNumberOrFraction("2/1/10"));
        assertNull(UnitHelper.parseNumberOrFraction("2 1 1"));
    }

    @Test
    public void testGetRealWorldUnitConversion(){
        assertNull(UnitHelper.getRealWorldUnitConversion(null));
        assertNull(UnitHelper.getRealWorldUnitConversion("https://www.google.com"));
        assertNull(UnitHelper.getRealWorldUnitConversion(""));
        assertNull(UnitHelper.getRealWorldUnitConversion("foo 100kg"));
        assertEquals("100 g = 3.53 oz", UnitHelper.getRealWorldUnitConversion("100g"));

        assertEquals("1 kg = 2.2 lbs", UnitHelper.getRealWorldUnitConversion(" 1kg "));
        assertEquals("100 kg = 220.46 lbs", UnitHelper.getRealWorldUnitConversion(" 100kg "));
        assertEquals("100 kg = 220.46 lbs", UnitHelper.getRealWorldUnitConversion(" 100 kg "));
        assertEquals("100 kg = 220.46 lbs", UnitHelper.getRealWorldUnitConversion("100.0 kg"));
        assertEquals("100.5 kg = 221.56 lbs", UnitHelper.getRealWorldUnitConversion("100.5 kg"));
        assertEquals("100.5 kg = 221.56 lbs", UnitHelper.getRealWorldUnitConversion("100,50 kg"));

        assertEquals("1 lbs = 0.45 kg", UnitHelper.getRealWorldUnitConversion(" 1lbs "));
        assertEquals("100 lbs = 45.36 kg", UnitHelper.getRealWorldUnitConversion(" 100lbs "));
        assertEquals("100 lbs = 45.36 kg", UnitHelper.getRealWorldUnitConversion(" 100 lbs "));
        assertEquals("100 lbs = 45.36 kg", UnitHelper.getRealWorldUnitConversion("100.0 lbs"));
        assertEquals("100.5 lbs = 45.59 kg", UnitHelper.getRealWorldUnitConversion("100.5 lbs"));
        assertEquals("100.5 lbs = 45.59 kg", UnitHelper.getRealWorldUnitConversion("100,50 lbs"));
        assertEquals("1 pounds = 0.45 kg", UnitHelper.getRealWorldUnitConversion(" 1 pounds "));
        assertEquals("1 pound = 0.45 kg", UnitHelper.getRealWorldUnitConversion(" 1 pound "));

        assertEquals("10 miles = 16.09 km", UnitHelper.getRealWorldUnitConversion("10 miles"));
        assertEquals("10 km = 6.21 miles", UnitHelper.getRealWorldUnitConversion("10 km"));
        assertEquals("1.0\" = 2.54 cm", UnitHelper.getRealWorldUnitConversion("1\""));
        assertEquals("10.5\" = 26.67 cm", UnitHelper.getRealWorldUnitConversion("10.5\""));

        assertEquals("1 foot = 0.3 meters", UnitHelper.getRealWorldUnitConversion("1 Foot"));
        assertEquals("1.5 ft = 0.46 meters", UnitHelper.getRealWorldUnitConversion("1.5ft"));
        assertEquals("10 feet = 3.05 meters", UnitHelper.getRealWorldUnitConversion("10 feet"));
        assertEquals("10 yd = 9.14 meters", UnitHelper.getRealWorldUnitConversion("10 yd"));
        assertEquals("10 yds = 9.14 meters", UnitHelper.getRealWorldUnitConversion("10 yds"));
        assertEquals("10 yards = 9.14 meters", UnitHelper.getRealWorldUnitConversion("10 Yards"));

        assertEquals("10 oz = 2.96 decilitres (dl)", UnitHelper.getRealWorldUnitConversion("10oz"));
        assertEquals("10 litres = 2.64 gallons", UnitHelper.getRealWorldUnitConversion("10 litres"));
        assertEquals("10 m = 32.81 feet", UnitHelper.getRealWorldUnitConversion("10m"));
        assertEquals("10 cm = 3.94 inches", UnitHelper.getRealWorldUnitConversion("10cm"));
        assertEquals("100 g = 3.53 oz", UnitHelper.getRealWorldUnitConversion("100g"));

        assertEquals("0.33 pound = 0.15 kg", UnitHelper.getRealWorldUnitConversion(" 1/3 pound "));
        assertEquals("1.5 pound = 0.68 kg", UnitHelper.getRealWorldUnitConversion(" 1 1/2 pound "));
        assertEquals("10ºC = 50ºF", UnitHelper.getRealWorldUnitConversion(" 10ºC "));
        assertEquals("11ºC = 51.8ºF", UnitHelper.getRealWorldUnitConversion(" 11ºC "));
        assertEquals("11ºC = 51.8ºF", UnitHelper.getRealWorldUnitConversion(" 11º celsius "));
        assertEquals("11.5ºC = 52.7ºF", UnitHelper.getRealWorldUnitConversion(" 11.5ºc "));
        assertEquals("50ºF = 10ºC", UnitHelper.getRealWorldUnitConversion(" 50ºF "));
        assertEquals("70ºF = 21.11ºC", UnitHelper.getRealWorldUnitConversion(" 70ºf "));
        assertEquals("70ºF = 21.11ºC", UnitHelper.getRealWorldUnitConversion(" 70°f "));
        assertEquals("70ºF = 21.11ºC", UnitHelper.getRealWorldUnitConversion(" 70º fahrenheit "));

        assertEquals("150ºC = 302ºF (gas mark 2)", UnitHelper.getRealWorldUnitConversion(" 150ºC "));
        assertEquals("300ºF = 148.89ºC (gas mark 2)", UnitHelper.getRealWorldUnitConversion(" 300ºF "));
        assertEquals("200ºC = 392ºF (gas mark 6)", UnitHelper.getRealWorldUnitConversion(" 200ºC "));
        assertEquals("425ºF = 218.33ºC (gas mark 7)", UnitHelper.getRealWorldUnitConversion(" 425ºf "));
        assertEquals("gas mark 2 = 150ºC or 300ºF", UnitHelper.getRealWorldUnitConversion(" gas mark 2 "));
        assertEquals("gas mark 6 = 200ºC or 400ºF", UnitHelper.getRealWorldUnitConversion("gas mark 6"));
        assertEquals("gas mark 10 = 260ºC or 500ºF", UnitHelper.getRealWorldUnitConversion("Gas mark 10"));

        assertEquals("0.5 tbsp = 7.5 g", UnitHelper.getRealWorldUnitConversion("½ tbsp"));
        assertEquals("1.5 tbsp = 22.5 g", UnitHelper.getRealWorldUnitConversion("1½ tbsp"));

        assertEquals("__show_cup_conversion__", UnitHelper.getRealWorldUnitConversion("3 1/2 Cups"));
        assertEquals("__show_cup_conversion__", UnitHelper.getRealWorldUnitConversion("1/2 cups"));
        assertEquals("__show_cup_conversion__", UnitHelper.getRealWorldUnitConversion("1/2 cup"));
    }
}