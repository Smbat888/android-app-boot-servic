package rest.htpp.com.webprint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import rest.htpp.com.webprint.constants.ServerConstants;

/**
 * The broadcast receiver registered in manifest file which will listen/handle on device restart event
 * The class is public to be accessible by android system
 *
 */
public class RestartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, ServerConstants.TOAST_TEXT, Toast.LENGTH_LONG).show();
        context.startService(new Intent(context.getApplicationContext(), WebServerService.class));
    }
}
