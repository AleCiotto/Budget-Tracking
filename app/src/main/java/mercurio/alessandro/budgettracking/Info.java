package mercurio.alessandro.budgettracking;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

/**
 * Created by Alessandro on 03/09/2014.
 */
public class Info extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_layout);
    }

    public void openLink(View view) {
        switch (view.getId()) {
            case R.id.linkGoogle: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/101630037484322983980/about"));
                startActivity(intent);
                break;
            }
            case R.id.linkIcon: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.iconarchive.com/"));
                startActivity(intent);
                break;
            }
            case R.id.linkGraph: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://android-graphview.org/"));
                startActivity(intent);
                break;
            }
            case R.id.linkItext: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://itextpdf.com/"));
                startActivity(intent);
                break;
            }
            case R.id.okButton: {
                finish();
                break;
            }
        }
    }
}
