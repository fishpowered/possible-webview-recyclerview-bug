package fishpowered.best.browser.services;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;

/**
 * Displays a foreground notification to inform the user they are in incognito mode
 * and when tapped will exit incognito mode
 */
public class IncognitoModeNotificationService extends Service {

    private final String NOTIFICATION_CHANNEL_INCOGNITO_MODE = "fp_incognito_mode";

    @Override
    public void onCreate() {
        createNotificationChannel();
        createNotification();
        super.onCreate();
    }

    private void createNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_INCOGNITO_MODE);
        notificationBuilder.setOnlyAlertOnce(true);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationBuilder.setSound(null);
        //notificationBuilder.setDeleteIntent(createSwipeToRemoveIntent()); //not sure what this does but doesn't allow for
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSmallIcon(R.drawable.ic_private_mode_mask_notification);
        //notificationBuilder.setLargeIcon(R.drawable.fishpowered_icon_legacy);
        notificationBuilder.setContentTitle("Fishpowered");
        notificationBuilder.setContentText(getString(R.string.exit_incognito_mode));
        notificationBuilder.setContentIntent(createExitIncognitoModeIntent());
        notificationBuilder.setColorized(true);
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.mediastyle_icon));
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // public=appear on lock screen
        final Notification notification = notificationBuilder.build();
        startForeground(1, notification);
    }

    private PendingIntent createExitIncognitoModeIntent() {
        Intent exitIncognito = new Intent(this, Browser.class);
        exitIncognito.setAction(Browser.EXIT_INCOGNITO_MODE_INTENT_ACTION);
        return PendingIntent.getActivity(
                this, 57294503, exitIncognito, 0); // not sure if needed: PendingIntent.FLAG_CANCEL_CURRENT
    }

    private void createNotificationChannel() {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIFICATION_CHANNEL_INCOGNITO_MODE,
                    getString(R.string.incognito_mode_notification_channel_title),
                    NotificationManager.IMPORTANCE_DEFAULT); // Don't set too high as it sets off noises and stuff when it changes in android x
            // the flags below don't seem to actually do anything... maybe because they get set on the notification too :(
            channel.enableVibration(false);
            channel.setShowBadge(false); // dot on icon in launcher
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(null, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                channel.setAllowBubbles(false);
            }
            if(getSystemService(Context.NOTIFICATION_SERVICE)!=null) {
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
