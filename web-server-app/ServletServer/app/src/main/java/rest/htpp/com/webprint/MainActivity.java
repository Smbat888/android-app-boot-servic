package rest.htpp.com.webprint;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.basewin.services.ServiceManager;

import rest.htpp.com.webprint.fiscal.SerialPortReader;

/**
 * The main activity with no UI, just for starting web server's service at once
 *
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent serviceIntent = new Intent(this, WebServerService.class);
        ServiceManager.getInstence().init(getApplicationContext());
        new SerialPortReader();
        startService(serviceIntent);
//        finish();
    }
}
