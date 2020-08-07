package fishpowered.best.browser;

import fishpowered.best.browser.utilities.UrlHelper;

import org.junit.Test;

import static org.junit.Assert.*;

public class UrlHelperTest {

    @Test
    public void testGetQueryStringFromUrl(){
        assertEquals(null, UrlHelper.getQueryStringFromUrl("https://www.google.com"));
        assertEquals(null, UrlHelper.getQueryStringFromUrl("https://www.google.com/wewefwef"));
        assertEquals(null, UrlHelper.getQueryStringFromUrl("https://www.google.com/wewefwef#foo"));
        assertEquals(null, UrlHelper.getQueryStringFromUrl("https://www.google.com/wewefwef?"));
        assertEquals("a=1&b=2", UrlHelper.getQueryStringFromUrl("https://www.google.com/wewefwef?a=1&b=2"));
        assertEquals("a=1&b=2", UrlHelper.getQueryStringFromUrl("https://www.google.com/wewefwef?a=1&b=2#3"));
    }

    @Test
    public void testGetParamFromUrl(){
        assertEquals(null, UrlHelper.getParamFromUrl("https://www.google.com", "a"));
        assertEquals(null, UrlHelper.getParamFromUrl("https://www.google.com/wewefwef", "a"));
        assertEquals(null, UrlHelper.getParamFromUrl("https://www.google.com/wewefwef#a=1", "a"));
        assertEquals(null, UrlHelper.getParamFromUrl("https://www.google.com/wewefwef?", "a"));
        assertEquals("toast", UrlHelper.getParamFromUrl("https://www.google.com/wewefwef?a=toast&b=2", "a"));
        assertEquals("0", UrlHelper.getParamFromUrl("https://www.google.com/wewefwef?a=1&b=0#3", "b"));
    }

    @Test
    public void testGetDomain() {
        assertEquals("google.com", UrlHelper.getDomain("https://www.google.com/skejfnskejfn", true, true, false));
        assertEquals("www.google.com", UrlHelper.getDomain("https://www.google.com/skejfnskejfn", false, true, false));
        assertEquals("https://www.google.com", UrlHelper.getDomain("https://www.google.com/skejfnskejfn", false, false, false));

        assertEquals("google.com", UrlHelper.getDomain("https://www.google.com/?skejfnskejfn", true, true, false));
        assertEquals("www.google.com", UrlHelper.getDomain("https://www.google.com/?skejfnskejfn", false, true, false));
        assertEquals("https://www.google.com", UrlHelper.getDomain("https://www.google.com/?skejfnskejfn", false, false, false));

        assertEquals("google.com", UrlHelper.getDomain("https://www.google.com/#", true, true, false));
        assertEquals("www.google.com", UrlHelper.getDomain("https://www.google.com/#", false, true, false));
        assertEquals("https://www.google.com", UrlHelper.getDomain("https://www.google.com/#", false, false, false));

        assertEquals("127.0.0.1", UrlHelper.getDomain("http://127.0.0.1/skejfnskejfn", true, true, false));
        assertEquals("127.0.0.1", UrlHelper.getDomain("http://127.0.0.1", true, true, false));
    }

    @Test
    public void testGetDomain_HandleNonDomainsGracefully() {
        assertEquals("foreca.fi", UrlHelper.getDomain("foreca.fi", true, true, true));

        // Think it's fine if the original text gets returned here.. This affects ItemList.addIfDomainNotExists and search suggestion results
        assertEquals("something not a domain", UrlHelper.getDomain("something not a domain", true, true, true));
    }

    @Test
    public void testIsUrlAttempt(){
        assertTrue(UrlHelper.isUrlAttempt("http://foo.com"));
        assertTrue(UrlHelper.isUrlAttempt("Foo.com"));
        assertTrue(UrlHelper.isUrlAttempt("https://foo.com"));
        assertTrue(UrlHelper.isUrlAttempt("data://foo.com"));
        assertTrue(UrlHelper.isUrlAttempt("foo.com/awdawd/awdawd.foo?sefsef#sefsef"));
        assertTrue(UrlHelper.isUrlAttempt("www.foo.co/awdawd/awdawd.foo?sefsef#sefsef"));
        assertTrue(UrlHelper.isUrlAttempt("offline://123/https://foo.com"));

        assertFalse(UrlHelper.isUrlAttempt("foo"));
        assertFalse(UrlHelper.isUrlAttempt("foo.com bar"));

        // Important for security we don't allow people to type file addresses...
        assertFalse(UrlHelper.isUrlAttempt("file://foo.com"));
        assertFalse(UrlHelper.isUrlAttempt("files://foo.com"));
    }

    @Test
    public void testIsImage() {
        assertTrue(UrlHelper.isImage("someimage.JPG"));
        assertTrue(UrlHelper.isImage("http://cats.com/someimage.JPG?foo#123"));
        assertTrue(UrlHelper.isImage("http://cats.com/someimage.JPG"));
        assertTrue(UrlHelper.isImage("http://cats.com/someimage.Bmp?foo#123"));
        assertTrue(UrlHelper.isImage("http://cats.com/someimage.PnG?foo#123"));
        assertTrue(UrlHelper.isImage("http://cats.com/someimage.gif?foo#123"));
        assertFalse(UrlHelper.isImage("http://cats.com/sometext.txt?foo=foo.jpg#123"));
        assertFalse(UrlHelper.isImage("http://cats.com/somevid.mp4?foo#123"));
        assertFalse(UrlHelper.isImage("http://cats.com/somevid.ogg"));
    }

    @Test
    public void testIsImageOrVideo() {
        assertTrue(UrlHelper.isImageOrVideo("someimage.JPG"));
        assertTrue(UrlHelper.isImageOrVideo("somevid.avi"));
        assertTrue(UrlHelper.isImageOrVideo("http://cats.com/someimage.JPG?foo#123"));
        assertTrue(UrlHelper.isImageOrVideo("http://cats.com/someimage.JPG"));
        assertTrue(UrlHelper.isImageOrVideo("http://cats.com/someimage.Bmp?foo#123"));
        assertTrue(UrlHelper.isImageOrVideo("http://cats.com/someimage.PnG?foo#123"));
        assertTrue(UrlHelper.isImageOrVideo("http://cats.com/someimage.gif?foo#123"));
        assertFalse(UrlHelper.isImageOrVideo("http://cats.com/sometext.txt?foo=foo.jpg#123"));
        assertTrue(UrlHelper.isImageOrVideo("http://cats.com/somevid.mp4?foo#123"));
        assertTrue(UrlHelper.isImageOrVideo("http://cats.com/somevid.ogg"));
    }

    @Test
    public void testAppendHttpProtocolToUrl() {
    }

    @Test
    public void testGetFileExtension() {
        assertEquals("jpg", UrlHelper.getFileExtension("http://cats.com/someimage.JPG?foo#123"));
        assertEquals("png", UrlHelper.getFileExtension("some.image.Png#foo"));
    }

    @Test
    public void testGetTrustedAddress() {
        assertEquals("cats.com", UrlHelper.getTrustedAddress("http://cats.com"));
        assertEquals("cats.com", UrlHelper.getTrustedAddress(" http://cats.com "));
        assertEquals("cats.com", UrlHelper.getTrustedAddress("http://cats.com/"));
        assertEquals("cats.com", UrlHelper.getTrustedAddress("http://cats.com?"));
        assertEquals("cats.com", UrlHelper.getTrustedAddress("http://cats.com/?"));
        assertEquals("cats.com", UrlHelper.getTrustedAddress("http://cats.com/#foo"));
        assertEquals("cats.com/foo.html", UrlHelper.getTrustedAddress("http://cats.com/foo.html?"));
        assertEquals("cats.com/formula1", UrlHelper.getTrustedAddress("http://cats.com/formula1"));
        assertEquals("cats.com/formula1", UrlHelper.getTrustedAddress("http://cats.com/formula1/"));
        assertEquals("cats.com/formula1", UrlHelper.getTrustedAddress("http://cats.com/formula1/?foo=foo/foo"));
        assertEquals("cats.com/formula1/foo.html", UrlHelper.getTrustedAddress("http://cats.com/formula1/foo.html"));
        assertEquals("cats.com/formula1/foo.html", UrlHelper.getTrustedAddress("http://cats.com/formula1/foo.html?foooo"));
        assertEquals("cats.com/formula1/foo", UrlHelper.getTrustedAddress("http://cats.com/formula1/foo"));
        assertEquals("cats.com/formula1/foo", UrlHelper.getTrustedAddress("http://cats.com/formula1/foo/"));
        assertEquals("cats.com/formula1/foo", UrlHelper.getTrustedAddress("http://cats.com/formula1/foo/?foo=foo/foo"));
        assertEquals("cats.com/formula1/a/b/c", UrlHelper.getTrustedAddress("http://cats.com/formula1/a/b/c?foo=foo/foo"));
        assertEquals("w3.org/blog/news/archives/7724", UrlHelper.getTrustedAddress("https://www.w3.org/blog/news/archives/7724"));
    }

    @Test
    public void testGetSimplifiedAddress_CollapseFolderStructure() {
        assertEquals("cats.com", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com", null, false));
        assertEquals("cats.com", UrlHelper.getSimplifiedAddressForUseAsTitle(" http://cats.com ", null, false));
        assertEquals("cats.com", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/", null, false));
        assertEquals("cats.com", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com?", null, false));
        assertEquals("cats.com", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/?", null, false));
        assertEquals("cats.com", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/#foo", null, false));
        assertEquals("cats.com/foo", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/foo.html?", null, false));
        assertEquals("cats.com/formula1", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/formula1", null, false));
        assertEquals("cats.com/formula1", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/formula1/", null, false));
        assertEquals("cats.com/formula1", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/formula1/?foo=foo/foo", null, false));
        assertEquals("cats.com/../foo", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/formula1/foo.html", null, false));
        assertEquals("cats.com/../foo", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/formula1/foo.html?foooo", null, false));
        assertEquals("cats.com/../foo", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/formula1/foo", null, false));
        assertEquals("cats.com/../foo", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/formula1/foo/", null, false));
        assertEquals("cats.com/../fooz", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/formula1/fooz/?foo=foo/foo", null, false));
        assertEquals("cats.com/../c", UrlHelper.getSimplifiedAddressForUseAsTitle("http://cats.com/formula1/a/b/c?foo=foo/foo", null, false));
    }

    @Test
    public void testGetSimplifiedAddress_GoogleSearch() {
        // Google
        assertEquals("google.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.com/", null, false));
        assertEquals("google.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.com/?q=", null, false));
        assertEquals("google.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.com/?q=&foo", null, false));
        assertEquals("google.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.com/?foo&q=", null, false));
        assertEquals("google: \"Test query string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.com/?q=Test+query+string&oq=Some+query+string&gs_l=psy-ab.3..0j0i30l2j0i5i30l7.94897.95127..95337...0.0..0.50.98.2......0....1..gws-wiz.......0i71j0i8i30.wUiHh8K06jk", null, false));
        assertEquals("google: \"Test query string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.com/search?ei=GBR5XMO9KOGgrgTjj77YDA&q=Test+query+string&oq=Some+query+string&gs_l=psy-ab.3..0j0i30l2j0i5i30l7.94897.95127..95337...0.0..0.50.98.2......0....1..gws-wiz.......0i71j0i8i30.wUiHh8K06jk", null, false));
        assertEquals("google: \"Test image search\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.com/search?biw=1176&bih=701&tbm=isch&sa=1&ei=khR5XMzVB-3SrgSyhJaoDg&q=Test+image+search&oq=Test+image+search&gs_l=img.3...5106.7778..10479...0.0..0.63.505.11......0....1..gws-wiz-img.......0j0i30j0i8i30j0i24.2RVW4aQZQqo:123", null, false));
        assertEquals("google: \"q=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/search?client=opera&q=q%3Dfoo&sourceid=opera&ie=UTF-8&oe=UTF-8", null, false)); // tried searching for "q=foo" to see if i can mess up my logic
        assertEquals("google: \"&q=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/search?client=opera&hs=Fj4&ei=jCF5XNjRGtyVjgb7h67oBA&q=%26q%3Dfoo&oq=%26q%3Dfoo&gs_l=psy-ab.3..0i30l10.72243.72243..72944...0.0..0.57.57.1......0....1..gws-wiz.UshxFttGw7A", null, false)); // tried searching for "&q=foo" to see if i can mess up my logic
        assertEquals("google: \"testete\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.com/search?client=handlebrowser&q=testete&sourceid=handlebrowser&ie=UTF-8&oe=UTF-8&ei=&ved=1t:11884", null, false));

        assertEquals("google.fi", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/", null, false));
        assertEquals("google.fi", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/?q=", null, false));
        assertEquals("google.fi", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/?q=&foo", null, false));
        assertEquals("google.fi", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/?foo&q=", null, false));
        assertEquals("google: \"Test query string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/?q=Test+query+string&oq=Some+query+string&gs_l=psy-ab.3..0j0i30l2j0i5i30l7.94897.95127..95337...0.0..0.50.98.2......0....1..gws-wiz.......0i71j0i8i30.wUiHh8K06jk", null, false));
        assertEquals("google: \"Test query string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/search?ei=GBR5XMO9KOGgrgTjj77YDA&q=Test+query+string&oq=Some+query+string&gs_l=psy-ab.3..0j0i30l2j0i5i30l7.94897.95127..95337...0.0..0.50.98.2......0....1..gws-wiz.......0i71j0i8i30.wUiHh8K06jk", null, false));
        assertEquals("google: \"Test image search\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/search?biw=1176&bih=701&tbm=isch&sa=1&ei=khR5XMzVB-3SrgSyhJaoDg&q=Test+image+search&oq=Test+image+search&gs_l=img.3...5106.7778..10479...0.0..0.63.505.11......0....1..gws-wiz-img.......0j0i30j0i8i30j0i24.2RVW4aQZQqo:123", null, false));
        assertEquals("google: \"q=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/search?client=opera&q=q%3Dfoo&sourceid=opera&ie=UTF-8&oe=UTF-8", null, false)); // tried searching for "q=foo" to see if i can mess up my logic
        assertEquals("google: \"&q=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.google.fi/search?client=opera&hs=Fj4&ei=jCF5XNjRGtyVjgb7h67oBA&q=%26q%3Dfoo&oq=%26q%3Dfoo&gs_l=psy-ab.3..0i30l10.72243.72243..72944...0.0..0.57.57.1......0....1..gws-wiz.UshxFttGw7A", null, false)); // tried searching for "&q=foo" to see if i can mess up my logic
    }

    @Test
    public void testGetSimplifiedAddress_BingSearch() {
        assertEquals("bing.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.com/", null, false));
        assertEquals("bing.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.com/?q=", null, false));
        assertEquals("bing.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.com/?q=&foo", null, false));
        assertEquals("bing.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.com/?foo&q=", null, false));
        assertEquals("bing: \"Test search string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.com/search?q=Test+search+string&qs=n&form=QBLH&sp=-1&pq=test+sear&sc=8-9&sk=&cvid=0B2FB770225F4D32B584A4CEEB803956", null, false));
        assertEquals("bing: \"Test search string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.com/search?q=Test+search+string&qs=n&form=QBLH&sp=-1&pq=test+search+string&sc=0-18&sk=&cvid=B1D94C6161BD4B97873223B75E7BAAB9", null, false));
        assertEquals("bing: \"Test image search\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.com/images/search?q=Test+image+search&qs=n&form=QBIR&sp=-1&pq=test+image+search&sc=3-17&sk=&cvid=2BB6940C899847C88994298D3FF539A0", null, false));
        assertEquals("bing: \"q=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.com/search?q=q%3Dfoo&qs=n&form=QBRE&sp=-1&pq=q%3Dfoo&sc=4-5&sk=&cvid=AA8FF1CD3C81402DB3FAB0DC64A53A67", null, false));
        assertEquals("bing: \"&q=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.com/images/search?q=%26q%3Dfoo&qs=n&form=QBIR&sp=-1&pq=%26q%3Dfoo&sc=2-6&sk=&cvid=1D0E4666184948B89641F718B3D0F668", null, false));

        assertEquals("bing.co.uk", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.co.uk/", null, false));
        assertEquals("bing.co.uk", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.co.uk/?q=", null, false));
        assertEquals("bing.co.uk", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.co.uk/?q=&foo", null, false));
        assertEquals("bing.co.uk", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.co.uk/?foo&q=", null, false));
        assertEquals("bing: \"Test search string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.co.uk/search?q=Test+search+string&qs=n&form=QBLH&sp=-1&pq=test+sear&sc=8-9&sk=&cvid=0B2FB770225F4D32B584A4CEEB803956", null, false));
        assertEquals("bing: \"Test search string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.co.uk/search?q=Test+search+string&qs=n&form=QBLH&sp=-1&pq=test+search+string&sc=0-18&sk=&cvid=B1D94C6161BD4B97873223B75E7BAAB9", null, false));
        assertEquals("bing: \"Test image search\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.co.uk/images/search?q=Test+image+search&qs=n&form=QBIR&sp=-1&pq=test+image+search&sc=3-17&sk=&cvid=2BB6940C899847C88994298D3FF539A0", null, false));
        assertEquals("bing: \"q=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.co.uk/search?q=q%3Dfoo&qs=n&form=QBRE&sp=-1&pq=q%3Dfoo&sc=4-5&sk=&cvid=AA8FF1CD3C81402DB3FAB0DC64A53A67", null, false));
        assertEquals("bing: \"&q=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.bing.co.uk/images/search?q=%26q%3Dfoo&qs=n&form=QBIR&sp=-1&pq=%26q%3Dfoo&sc=2-6&sk=&cvid=1D0E4666184948B89641F718B3D0F668", null, false));
    }

    @Test
    public void testGetSimplifiedAddress_DuckDuckSearch() {
        assertEquals("duckduckgo.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.duckduckgo.com/", null, false));
        assertEquals("duckduckgo.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.duckduckgo.com/?q=", null, false));
        assertEquals("duckduckgo.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.duckduckgo.com/?q=&foo", null, false));
        assertEquals("duckduckgo.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.duckduckgo.com/?foo&q=", null, false));
        assertEquals("duckduck: \"Test search string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://duckduckgo.com/?q=Test+search+string&t=h_&ia=web", null, false));
        assertEquals("duckduck: \"Test search string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.duckduckgo.com/search?q=Test+search+string&qs=n&form=QBLH&sp=-1&pq=test+search+string&sc=0-18&sk=&cvid=B1D94C6161BD4B97873223B75E7BAAB9", null, false));
        assertEquals("duckduck: \"Test image search\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.duckduckgo.com/images/search?q=Test+image+search&qs=n&form=QBIR&sp=-1&pq=test+image+search&sc=3-17&sk=&cvid=2BB6940C899847C88994298D3FF539A0", null, false));
        assertEquals("duckduck: \"q=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://duckduckgo.com/?q=q%3Dfoo&t=h_&ia=web", null, false));
        assertEquals("duckduck: \"?q=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://duckduckgo.com/?q=%3Fq%3Dfoo&t=h_&ia=web", null, false));
    }

    @Test
    public void testGetSimplifiedAddress_YandexSearch() {
        // yandex.com
        assertEquals("yandex.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.yandex.com/", null, false));
        assertEquals("yandex.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.yandex.com/?text=", null, false));
        assertEquals("yandex.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.yandex.com/?text=&foo", null, false));
        assertEquals("yandex.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.yandex.com/?foo&text=", null, false));
        assertEquals("yandex: \"Test search string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.yandex.com/search/?text=Test%20search%20string&lr=10493", null, false));
        assertEquals("yandex: \"Test image search\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://yandex.com/images/search?text=Test%20image%20search", null, false));
        assertEquals("yandex: \"?text=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://yandex.com/images/search?text=%3Ftext%3Dfoo", null, false));

        // yandex.ru
        assertEquals("yandex.ru", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.yandex.ru/", null, false));
        assertEquals("yandex.ru", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.yandex.ru/?text=", null, false));
        assertEquals("yandex.ru", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.yandex.ru/?text=&foo", null, false));
        assertEquals("yandex.ru", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.yandex.ru/?foo&text=", null, false));
        assertEquals("yandex: \"Test search string\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.yandex.ru/search/?text=Test%20search%20string&lr=10493", null, false));
        assertEquals("yandex: \"Test image search\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://yandex.ru/images/search?text=Test%20image%20search", null, false));
        assertEquals("yandex: \"?text=foo\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://yandex.ru/images/search?text=%3Ftext%3Dfoo", null, false));
    }

    @Test
    public void testGetSimplifiedAddress_PopularSiteSearches() {
        assertEquals("youtube.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.youtube.com", null, false));
        assertEquals("youtube.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.youtube.com/?foo", null, false));
        assertEquals("youtube.com/results", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.youtube.com/results?search_query=", null, false));
        assertEquals("youtube: \"formula 1\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.youtube.com/results?search_query=formula+1", null, false));
        assertEquals("youtube.com/watch", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.youtube.com/watch?v=123", null, false));
        assertEquals("youtube: \"Page title\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.youtube.com/watch?v=123", "Page title", false));

        assertEquals("wikipedia.org", UrlHelper.getSimplifiedAddressForUseAsTitle("https://en.wikipedia.org", null, false));
        assertEquals("wikipedia.org", UrlHelper.getSimplifiedAddressForUseAsTitle("https://en.wikipedia.org/?foo", null, false));
        assertEquals("wiki: \"i don't know what i' searching for\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://en.wikipedia.org/wiki/Special:Search?search=i+don%27t+know+what+i%27+searching+for&go=Go", null, false));

        assertEquals("twitter: \"test search\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://twitter.com/search?q=test%20search&src=typd", null, false));

        // Catch all
        assertEquals("madeupdomain.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://madeupdomain.com/?SearChTerM=", null, false));
        assertEquals("madeupdomain.com: \"test search\"", UrlHelper.getSimplifiedAddressForUseAsTitle("https://madeupdomain.com/search?SearChTerM=test%20search&src=typd", null, false));
    }

    @Test
    public void testGetSimplifiedAddress_Other() {
        assertEquals("twitter.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.twitter.com/home", null, false));
        assertEquals("foo.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.foo.com/index.html", null, false));
        assertEquals("foo.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.foo.com/index.htm", null, false));
        assertEquals("foo.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.foo.com/index.php", null, false));
        assertEquals("foo.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.foo.com/index.asp", null, false));
        assertEquals("foo.com", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.foo.com/index.aspx", null, false));
        assertEquals("foo.com/some-thing", UrlHelper.getSimplifiedAddressForUseAsTitle("https://www.foo.com/some-thing.php", null, false));
        assertEquals("r/formula1", UrlHelper.getSimplifiedAddressForUseAsTitle("https://reddit.com/r/formula1", null, false));
    }

    @Test
    public void testShouldLoadPageInRestrictedMode() {
    }

    @Test
    public void testRemoveUnnecessaryPartOfImageUrl() {
        assertEquals("image.png", UrlHelper.removeUnnecessaryPartOfImageUrl("image.png"));
        assertEquals("image.png?awdkjnawd", UrlHelper.removeUnnecessaryPartOfImageUrl("http://foo.com/image.png?awdkjnawd"));
        assertEquals("?dynamic-image", UrlHelper.removeUnnecessaryPartOfImageUrl("http://foo.com/?dynamic-image"));
        assertEquals("index.php", UrlHelper.removeUnnecessaryPartOfImageUrl("https://www.foo.com/index.php"));
        assertEquals("image.png?awdkjnawd", UrlHelper.removeUnnecessaryPartOfImageUrl("http://foo.com/test/a/image.png?awdkjnawd"));
    }
}