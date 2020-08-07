package fishpowered.best.browser.utilities;

/**
 * Used to store download details until permissions is granted to download file
 */
public class DownloadDetails {
    public String url;
    public String userAgent;
    public String contentDisposition;
    public String mimeType;
    public long contentLength;
}
