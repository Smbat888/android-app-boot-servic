package rest.taron.com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * The main activity with no UI, just for starting web server's service at once
 *
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent serviceIntent = new Intent(this, WebServerService.class);
        startService(serviceIntent);
    }
}
