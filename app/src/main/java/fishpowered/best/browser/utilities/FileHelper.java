package fishpowered.best.browser.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.TabState;
import fishpowered.best.browser.TabViewHolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static android.content.Context.DOWNLOAD_SERVICE;

public class FileHelper {

    public static final int STORAGE_PERMISSIONS_REQUEST_CODE = 1232399;

    /**
     * We need to check for file storage permission before writing files in later versions of android
     * @param activity
     * @return
     */
    public static boolean getFileStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.e("Permission error","You have permission");
                return true;
            } else {
                //Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        else { //you dont need to worry about these stuff below api level 23
            //Log.e("Permission error","You already have the permission");
            return true;
        }
    }

    /**
     * Take a Base 64 encoded string (e.g. image) and turn into a file
     * @param base64EncodedData
     * @param downloadUsingFileName If we can scrape the filename from the download attribute, it will be here
     * @return
     */
    public static String createAndSaveFileFromBase64Url(Browser browser, String base64EncodedData, String mimeType, String downloadUsingFileName) {
        //Toast.makeText(browser, R.string.downloading, Toast.LENGTH_SHORT).show();

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //String fileType = base64EncodedData.substring(base64EncodedData.indexOf("/") + 1, base64EncodedData.indexOf(";"));
        String filename;
        if(downloadUsingFileName!=null && !downloadUsingFileName.trim().equals("")){
            filename = downloadUsingFileName;
        }else {
            filename = UrlHelper.getFileNameFromUrl(base64EncodedData, null, mimeType);
        }
        File file = new File(path, filename);
        try {
            //if(!path.exists())
            //    path.mkdirs();
           // if(!file.exists()) {
                file.createNewFile();
           // }

            String base64EncodedString = base64EncodedData.substring(base64EncodedData.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64EncodedString, Base64.DEFAULT);
            OutputStream os = new FileOutputStream(file);
            os.write(decodedBytes);
            os.close();


            //Set notification after download complete and add "click to view" action to that
            //FileHelper.showDownloads(file.toString(), browser);
            Uri fileUri = FileProvider.getUriForFile(browser, "fishpowered.best.browser.fileprovider", file);
            notifyThatDownloadHasFinished(browser, fileUri, filename, mimeType);

            //Set notification after download complete and add "click to view" action to that
            /*String mimeType = base64EncodedData.substring(base64EncodedData.indexOf(":") + 1, base64EncodedData.indexOf("/"));
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW, );
            //intent.setDataAndType(Uri.fromFile(file), (mimeType + "/*")); causes errors in newer android
            intent.setDataAndType(FileProvider.getUriForFile(browser,
                    browser.getPackageName()+".fishpowered.best.browser", file), (mimeType + "/*"));

            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PendingIntent pIntent = PendingIntent.getActivity(browser, 0, intent, 0);
            Notification notification = new NotificationCompat.Builder(browser, "something")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(browser.getString(R.string.msg_file_downloaded))
                    .setContentTitle(filename)
                    .setContentIntent(pIntent)
                    .build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            int notificationId = 85851;
            NotificationManager notificationManager = (NotificationManager) browser.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, notification); */
        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
            Toast.makeText(browser, R.string.failed_to_download_file, Toast.LENGTH_LONG).show();
        }

        return file.toString();
    }

    public static void showDownloads(String filePathOfNewlyAddedFile, Activity context) {
        //Tell the media scanner about the new file so that it is immediately available to the user.
        if(filePathOfNewlyAddedFile!=null) {
            MediaScannerConnection.scanFile(context,
                    new String[]{filePathOfNewlyAddedFile}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            //Log.i("ExternalStorage", "Scanned " + path + ":");
                            //Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        }
        context.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
    }

    public static boolean saveImageToInternalStorage(Bitmap image, String targetFileName, Context context) {
        try {
            // Use the compress method on the Bitmap object to write image to
            // the OutputStream
            FileOutputStream fos = context.openFileOutput(targetFileName, Context.MODE_PRIVATE);
            // Writing the bitmap to the output stream
            image.compress(Bitmap.CompressFormat.JPEG, 40, fos);
            fos.close();
            return true;
        } catch (Exception e) {
            Log.e("saveToInternalStorage()", "Err: "+e.getMessage());
            return false;
        }
    }

    public static boolean fileExistsInFilesDirectory(final Context context, final String filename){
        File file = new File(context.getFilesDir(), filename);
        return file.exists();
    }

    public static String getFilePathForSavingWebArchive(int readLaterItemId, Context context, boolean includeDirectory){
        final String fileName = "readLater_" + readLaterItemId + ".mht";
        return includeDirectory
            ? context.getFilesDir() + File.separator + fileName
            : fileName;
    }

    public static Bitmap getImageFromInternalStorage(String filename, Context context) {
        Bitmap thumbnail = null;
       /* UNCOMMENT IF YOU WANT IT TO CHECK EXTERNAL STORAGE FIRST...
       String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SD_CARD + APP_THUMBNAIL_PATH_SD_CARD;
        // Look for the file on the external storage
        try {
            if (tools.isExternalStorageReadable() == true) {
                thumbnail = BitmapFactory.decodeFile(fullPath + "/" + filename);
            }
        } catch (Exception e) {
            Log.e("getImageFromInternalStorage() on external storage", e.getMessage());
        }*/

        // If no file on external storage, look in internal storage
        //if (thumbnail == null) {
            try {
                File filePath = context.getFileStreamPath(filename);
                FileInputStream fi = new FileInputStream(filePath);
                thumbnail = BitmapFactory.decodeStream(fi);
            } catch (Exception ex) {
                //Log.e("FPgetThumbnail", ex.getMessage());
            }
        //}
        return thumbnail;
    }

    public static boolean isExternalStorageReadable() {
        boolean mExternalStorageAvailable;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
        } else {
            // Something else is wrong. It may be one of many other
            // states, but all we need to know is we can neither read nor write
            mExternalStorageAvailable = false;
        }

        return mExternalStorageAvailable;
    }


    public static void promptToStartDownload(final String url, final String userAgent, final String contentDisposition, final String mimeType, final long contentLength, final Browser browser) {
        AlertDialog.Builder builder = new AlertDialog.Builder(browser);
        String fileNameFromUrl = UrlHelper.getFileNameFromUrl(url, contentDisposition, mimeType);
        if(url.startsWith("blob:") && fileNameFromUrl!=null && fileNameFromUrl.endsWith("unknown")){
            fileNameFromUrl = "Download?";
        }
        final AlertDialog dialog = builder
                .setTitle(fileNameFromUrl)
                //.setMessage(privacyDescription)
                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final TabViewHolder currentTabViewHolder = browser.getCurrentTabViewHolder();
                        onDownloadStartConfirmed(url, userAgent, contentDisposition, mimeType, contentLength, currentTabViewHolder);
                        if(isBlankTabOnlyOpenedToTriggerDownload(currentTabViewHolder, url)) {

                            // Close blank tab if the download link was target="_blank"
                            browser.removeTabAtPosition(currentTabViewHolder.getAdapterPosition(), false, false, false);
                        }else if(requiresRefreshHack(url)){
                            // HACK - sometimes webview will error when triggering a download on the current page
                            currentTabViewHolder.getWebView().reload();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        browser.pendingDownload = null;
                        final TabViewHolder currentTabViewHolder = browser.getCurrentTabViewHolder();
                        if(isBlankTabOnlyOpenedToTriggerDownload(currentTabViewHolder, url)) {
                            // Close blank tab if the download link was target="_blank"
                            browser.removeTabAtPosition(currentTabViewHolder.getAdapterPosition(), false, false, false);
                        }else if(requiresRefreshHack(url)){
                            // HACK - sometimes webview will error when triggering a download on the current page
                            currentTabViewHolder.getWebView().reload();
                        }
                        dialog.cancel();
                    }
                })
                .create();
        // Style the dialog before showing
        Integer    iconResource = FileHelper.getDownloadIconForFileName(fileNameFromUrl);
        ThemeHelper.styleAlertDialog(dialog, iconResource, browser);
        dialog.show();
    }

    /**
     * Downloading certain file types will trigger a webview error that I canät seem to recover from
     * @param url
     * @return
     */
    private static boolean requiresRefreshHack(String url) {
        String ext = UrlHelper.getFileExtension(url);
        switch(ext){
            case "doc":
            case "docx":
            case "ods":
            case "odt":
            case "zip":
            case "7z":
            case "mov":
            case "wmv":
            case "ppt":
            case "pptx":
                return true;
        }
        return false;
    }

    public static boolean isBlankTabOnlyOpenedToTriggerDownload(@NonNull TabViewHolder currentTabViewHolder, String downloadUrl) {
        final TabState tabState = currentTabViewHolder.getTabState();
        return tabState!=null && !currentTabViewHolder.getWebView().canGoBack() && tabState.address!=null
                && (tabState.address.equals(downloadUrl) || tabState.address.equals("about:blank"));
    }

    public static void onDownloadStartConfirmed(String url, String userAgent, String contentDisposition, String mimeType, long contentLength, TabViewHolder tabViewHolder) {
    }

    private static String getBase64StringFromBlobUrl(String blobUrl, String mimeType) {
        return "";
    }

    /**
     * Download file, requires permissions to be checked first, @see CustomWebViewClient.onDownloadHeadersReceivedListener
     * @param downloadDetails
     */
    @NonNull
    public static void downloadFile(DownloadDetails downloadDetails, Browser browser){

    }

    /**
     * Attachment download complete receiver.
     * <p/>
     * 1. Receiver gets called once attachment download completed.
     * 2. Open the downloaded file.
     */
    private static BroadcastReceiver attachmentDownloadCompleteReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context browser, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                notifyThatDownloadHasFinished((Browser) browser, downloadId);
            }
        }
    };

    /**
     * Used to notify and open the downloaded attachment.
     *
     * @param browser    Content.
     * @param downloadId Id of the downloaded file to open.
     */
    private static void notifyThatDownloadHasFinished(final Browser browser, final long downloadId) {
        DownloadManager downloadManager = (DownloadManager) browser.getSystemService(Context.DOWNLOAD_SERVICE);
        if(downloadManager==null){
            Toast.makeText(browser, "Unable to load download manager", Toast.LENGTH_SHORT);
            return;
        }
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            // Using "DownloadManager.COLUMN_LOCAL_URI" we will get only FileUri hence we need to convert it into ContentUri & share with other application
            String downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String downloadMimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
            String fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
            if ((downloadStatus == DownloadManager.STATUS_SUCCESSFUL) && downloadLocalUri != null) {
                notifyThatDownloadHasFinished(browser, Uri.parse(downloadLocalUri), fileName, downloadMimeType);
            }else{
                // TODO improve
                Toast.makeText(browser, browser.getString(R.string.failed_to_download_file), Toast.LENGTH_LONG).show();
            }
        }
        cursor.close();
    }

    /**
     * Used to notify and open the downloaded attachment.
     * <p/>
     * 1. Fire intent to open download file using external application.
     *
     * 2. Note:
     * 2.a. We can't share fileUri directly to other application (because we will get FileUriExposedException from Android7.0).
     * 2.b. Hence we can only share content uri with other application.
     * 2.c. We must have declared FileProvider in manifest.
     * 2.c. Refer - https://developer.android.com/reference/android/support/v4/content/FileProvider.html
     *
     * @param context            Context.
     * @param attachmentUri      Uri of the downloaded attachment to be opened.
     * @param attachmentMimeType MimeType of the downloaded attachment.
     */
    private static void notifyThatDownloadHasFinished(final Browser context, Uri attachmentUri, String fileNameOrTitle, final String attachmentMimeType) {
        if(attachmentUri!=null && attachmentUri.getPath()!=null) {
            // Get Content Uri.
            if (ContentResolver.SCHEME_FILE.equals(attachmentUri.getScheme())) {
                // FileUri - Convert it to contentUri.
                File file = new File(attachmentUri.getPath());
                attachmentUri = FileProvider.getUriForFile(context, "fishpowered.best.browser.fileprovider", file);
            }

            Intent openAttachmentIntent = new Intent(Intent.ACTION_VIEW);
            openAttachmentIntent.setDataAndType(attachmentUri, attachmentMimeType);
            openAttachmentIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if(fileNameOrTitle==null){
                fileNameOrTitle = context.getString(R.string.downloads);
            }
            // Found a good activity, allow them to open it directly
            showDownloadCompleteWithOpenButtonNotification(context, openAttachmentIntent, fileNameOrTitle, R.string.open);
        }
    }

    private static void showDownloadCompleteWithOpenButtonNotification(final Browser browser, final Intent openFileIntent, String fileName, int buttonTextResId){
    }

    /**
     * Read file into String (uses standard app files directory)
     * @param privateFileName
     * @param context
     * @return
     */
    public static String readFileContentsAsString(@NonNull String privateFileName, @NonNull Context context) {
        String ret = "";
        try {
            InputStream inputStream = context.openFileInput(privateFileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                boolean firstLine = true;
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    if(firstLine){
                        stringBuilder.append(receiveString);
                        firstLine = false;
                    }else {
                        stringBuilder.append("\n").append(receiveString);
                    }
                }

                bufferedReader.close();
                inputStreamReader.close();
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
    }

    public static int getDownloadIconForFileName(String fileName) {
        return 0;
    }

    @NonNull
    public static String getFileExtension(String fileName) {
        return String.valueOf(UrlHelper.getFileExtension(fileName));
    }

    public static void downloadFileFromUrlInWebView(@NonNull TabViewHolder tabViewHolder, @NonNull String url) {
        String safeUrl = url.replace("'", "");

        tabViewHolder.webView.evaluateJavascript("(" +
                    "function(){" +
                    "\t\tvar fpXhr = new XMLHttpRequest();\n" +
                    "\t\t\n" +
                    "\t\tfpXhr.open(\"HEAD\", '"+safeUrl+"', true);\n" +
                    "\t\tfpXhr.onreadystatechange = function() {\n" +
                    "\t\t\tif (this.readyState == this.DONE) {\n" +
                    "\t\t\t   let fpContentType = fpXhr.getResponseHeader(\"Content-Type\");\n" +
                    "\t\t\t   var link = document.createElement('a');\n" +
                    "\t\t\t\tlink.href = '"+safeUrl+"';\n" +
               // NOTE: setting a new file name with the download attribute *should work* but doesn't currently (browser support?),
                // so Ive hacked it to send the mime type to the js hooks below
                    "\t\t\t\tswitch(fpContentType){\n" +
                    "\t\t\t\t\tcase 'image/png':\n" +
                    "\t\t\t\t\t\tlink.download = 'download.png';\n" +
                    "\t\t\t\t\t\tbreak;\n" +
                    "\t\t\t\t\tcase 'image/jpg':\n" +
                    "\t\t\t\t\tcase 'image/jpeg':\n" +
                    "\t\t\t\t\t\tlink.download = 'download.jpg';\n" +
                    "\t\t\t\t\t\tbreak;\n" +
                    "\t\t\t\t\tcase 'image/gif':\n" +
                    "\t\t\t\t\t\tlink.download = 'download.gif';\n" +
                    "\t\t\t\t\t\tbreak;\n" +
                    "\t\t\t\t\tcase 'image/bmp':\n" +
                    "\t\t\t\t\t\tlink.download = 'download.bmp';\n" +
                    "\t\t\t\t\t\tbreak;\n" +
                    "\t\t\t\t\tcase 'image/svg':\n" +
                    "\t\t\t\t\t\tlink.download = 'download.svg';\n" +
                    "\t\t\t\t\t\tbreak;\n" +
                    "\t\t\t\t\tcase 'image/tiff':\n" +
                    "\t\t\t\t\t\tlink.download = 'download.tiff';\n" +
                    "\t\t\t\t\t\tbreak;\n" +
                    "\t\t\t\t}\n" +
                "fpandroid.setSelectedItemMimeType(fpContentType); // download attribute doesn't seem to work with this approach so we have to send the mimetype to catch on the server" +
                    "\t\t\t\tdocument.body.appendChild(link);\n" +
                    "\t\t\t\tlink.click();\n" +
                    "\t\t\t\tdocument.body.removeChild(link);\n" +
                    "\t\t\t   \n" +
                    "\t\t\t}\n" +
                    "\t\t};\n" +
                    "\t\tfpXhr.send();" +
                    "}" +
                ")()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                // note you cannot rely on this here because we are fetching the mime type asynchronously.
                // See fpandroid.setSelectedItemMimeType above for how we communicate the image type to the client
            }
        });
    }

    public static String getTabThumbFileName(int tabId) {
        return "tabThumb"+ tabId +".jpg";
    }

    public static int getTabIdFromTabThumbFileName(String filename){
        return Integer.parseInt(filename.replace("tabThumb","").replace(".png","").replace(".jpg",""));
    }

    public static void removeTabThumb(int tabId, Context context) {
        try {
            //int tabId = getTabIdAtPosition(n);
            final String tabThumbFileName = FileHelper.getTabThumbFileName(tabId);
            Log.d("TABTHUMBB", "removing:"+tabThumbFileName);
            context.deleteFile(tabThumbFileName);
        }catch(Exception e){
            // Probably the cache doesn't exist in the first place
            Log.d("TABTHUMBB", "No tab cache to remove for tabId :"+tabId+": "+e.toString());
            Browser.silentlyLogException(e, context);
        }
    }

    public static void removeOfflineWebArchive(int id, Context context) {
        try {
            //int tabId = getTabIdAtPosition(n);
            final String fileName = FileHelper.getFilePathForSavingWebArchive(id, context, false);
            context.deleteFile(fileName);
        }catch(Exception e){
            // Probably the cache doesn't exist in the first place
            Browser.silentlyLogException(e, context);
        }
    }
}
