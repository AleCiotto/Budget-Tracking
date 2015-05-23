package mercurio.alessandro.budgettracking;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Alessandro on 19/08/2014.
 */
public class UserSettings extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // gingerbread non supporta alternative!
        addPreferencesFromResource(R.xml.settings_layout_v2);
    }

}
