package fishpowered.best.browser;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

public class App extends Application {
    public static Context context;

    @Override public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }


}
