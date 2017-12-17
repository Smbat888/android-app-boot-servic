package rest.htpp.com.webprint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * The broadcast receiver registered in manifest file which will listen/handle on device boot event
 * The class is public to be accessible by android system
 *
 */
public class StartServiceOnBootCompleted extends BroadcastReceiver {

    final String TOAST_TEXT = "Web Server is connected on Boot";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, TOAST_TEXT, Toast.LENGTH_LONG).show();
        final Intent serviceIntent = new Intent(context, WebServerService.class);
        context.startService(serviceIntent);
    }
}
