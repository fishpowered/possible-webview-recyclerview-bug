package fishpowered.best.browser;

import fishpowered.best.browser.utilities.ColourHelper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColourHelperTest {

    @Test
    public void testHexStringToHex() {
        assertEquals(0x9, ColourHelper.hexStringToHex("#000009"));
        assertEquals(0xf9, ColourHelper.hexStringToHex("#0000f9"));
        assertEquals(0xff00f9, ColourHelper.hexStringToHex("#FF00f9"));
        assertEquals(0xff00f9, ColourHelper.hexStringToHex("FF00f9"));
        assertEquals(0xffffff, 16777215);
    }
}