package rest.taron.com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class StartServiceOnBootCompleted extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        for (int i = 0; i < 30; i++) {
            Toast.makeText(context, "Hello from Server on Boot", Toast.LENGTH_LONG).show();
        }
        final Intent serviceIntent = new Intent(context, WebServerService.class);
        context.startService(serviceIntent);
    }
}
