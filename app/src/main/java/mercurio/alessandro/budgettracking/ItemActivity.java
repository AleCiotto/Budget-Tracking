package mercurio.alessandro.budgettracking;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static mercurio.alessandro.budgettracking.FeedReaderContract.FeedEntry.COLUMN_NAME_ALERT;
import static mercurio.alessandro.budgettracking.FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY;
import static mercurio.alessandro.budgettracking.FeedReaderContract.FeedEntry.COLUMN_NAME_DESCRIPTION;
import static mercurio.alessandro.budgettracking.FeedReaderContract.FeedEntry.COLUMN_NAME_IMG_URL;
import static mercurio.alessandro.budgettracking.FeedReaderContract.FeedEntry.COLUMN_NAME_LOCATION;
import static mercurio.alessandro.budgettracking.FeedReaderContract.FeedEntry.COLUMN_NAME_PRICE;
import static mercurio.alessandro.budgettracking.FeedReaderContract.FeedEntry.TABLE_NAME;
import static mercurio.alessandro.budgettracking.lib.decodeSampledBitmapFromFile;
import static mercurio.alessandro.budgettracking.lib.setDefaultImg;

/**
 * Created by Alessandro on 06/08/2014.
 */
public class ItemActivity extends ActionBarActivity {

    // Variabili usate per mostrare/nascondere i pulsanti done/cancel nella Action Bar
    private static boolean HIDE_MENU = true;
    private static boolean SHOW_MENU = false;
    boolean mState = SHOW_MENU;
    TextView nome;// = (TextView) findViewById(R.id.item_name);
    TextView data;// = (TextView) findViewById(R.id.item_date);
    Spinner categoria;
    TextView descrizione;// = (TextView) findViewById(R.id.item_description);
    TextView luogo;// = (TextView) findViewById(R.id.item_location);
    TextView prezzo;// = (TextView) findViewById(R.id.item_price);
    EditText prezzoEditable;// = (EditText) findViewById(R.id.item_price_editable);
    ImageView immagine;
    CheckBox alert_checkbox;// = (CheckBox) findViewById(R.id.checkBox_alert);
    String array_spinner[];
    long longid;
    String img_custom;
    // Old values
    String oldLocation;
    String oldDescription;
    String oldPrice;
    int oldCategory;
    boolean oldAlert;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_details_layout);

        nome = (TextView) findViewById(R.id.item_name);
        data = (TextView) findViewById(R.id.item_date);
        categoria = (Spinner) findViewById(R.id.item_category);
        descrizione = (TextView) findViewById(R.id.item_description);
        luogo = (TextView) findViewById(R.id.item_location);
        prezzo = (TextView) findViewById(R.id.item_price);
        prezzoEditable = (EditText) findViewById(R.id.item_price_editable);
        immagine = (ImageView) findViewById(R.id.item_image);
        alert_checkbox = (CheckBox) findViewById(R.id.checkBox_alert);

        // Gestione Spinner categoria
        array_spinner = getResources().getStringArray(R.array.categoriesString);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, array_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoria.setAdapter(adapter);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String date = intent.getStringExtra("date");
        String category = intent.getStringExtra("category");
        String description = intent.getStringExtra("description");
        String location = intent.getStringExtra("location");
        int img = intent.getIntExtra("image", R.drawable.ic_launcher);
        img_custom = intent.getStringExtra("image_custom");
        Boolean alert = intent.getBooleanExtra("alert", false);
        String price = intent.getStringExtra("price");
        longid = intent.getLongExtra("longid", 999);


        // Controllo se era settato il promemoria
        if (alert) {
            alert_checkbox.setChecked(true);
        }
        alert_checkbox.setEnabled(false);

        // Setto tutti i parametri dell' oggetto selezionato

        if (img_custom == null) {
            immagine.setImageResource(img);
        } else {
            //immagine.setImageBitmap(BitmapFactory.decodeFile(img_custom));
            immagine.setImageBitmap(decodeSampledBitmapFromFile(img_custom, 250, 250));
        }

        nome.setText(name);

        data.setText(date);

        categoria.setSelection(Integer.parseInt(category));
        categoria.setEnabled(false);

        descrizione.setText(description);
        descrizione.setEnabled(false);

        luogo.setText(location);
        luogo.setEnabled(false);

        prezzo.setText(price + "€");
        prezzoEditable.setText(price);
        prezzoEditable.setEnabled(false);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_modify:
                setEditableButtons();
                saveOldValues();
                return true;
            case R.id.action_delete:
                deleteItem(longid);
                finish();
                return true;
            case R.id.details_cab_cancel:
                restoreOldValues();

                categoria.setEnabled(false);
                luogo.setEnabled(false);
                descrizione.setEnabled(false);
                alert_checkbox.setEnabled(false);
                prezzoEditable.setVisibility(View.INVISIBLE);
                prezzo.setVisibility(View.VISIBLE);

                mState = SHOW_MENU; // setting state = true
                Toast.makeText(this, "Modifiche non salvate", Toast.LENGTH_LONG).show();
                supportInvalidateOptionsMenu();

                return true;
            case R.id.details_cab_done:
                prezzo.setText(prezzoEditable.getText() + " €");

                categoria.setEnabled(false);
                luogo.setEnabled(false);
                descrizione.setEnabled(false);
                alert_checkbox.setEnabled(false);
                prezzoEditable.setVisibility(View.INVISIBLE);
                prezzo.setVisibility(View.VISIBLE);

                ContentValues cv = new ContentValues();
                cv.put(COLUMN_NAME_LOCATION, luogo.getText().toString());
                cv.put(COLUMN_NAME_DESCRIPTION, descrizione.getText().toString());
                cv.put(COLUMN_NAME_CATEGORY, String.valueOf(categoria.getSelectedItemPosition()));
                cv.put(COLUMN_NAME_ALERT, String.valueOf(alert_checkbox.isChecked()));
                cv.put(COLUMN_NAME_PRICE, prezzoEditable.getText().toString());
                cv.put(COLUMN_NAME_IMG_URL, setDefaultImg(String.valueOf(categoria.getSelectedItemPosition())));

                updateItem(longid, cv);

                mState = SHOW_MENU; // setting state = true
                //invalidateOptionsMenu();
                supportInvalidateOptionsMenu();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveOldValues() {
        oldLocation = luogo.getText().toString();
        oldDescription = descrizione.getText().toString();
        oldPrice = prezzo.getText().toString();
        oldCategory = categoria.getSelectedItemPosition();
        oldAlert = alert_checkbox.isChecked();
    }

    private void restoreOldValues() {
        luogo.setText(oldLocation);
        descrizione.setText(oldDescription);
        prezzo.setText(oldPrice);
        categoria.setSelection(oldCategory);
        alert_checkbox.setChecked(oldAlert);
    }

    //---deletes a particular item---
    public boolean deleteItem(long id) {

        /* GESTIONE DATABASE */
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        return db.delete(TABLE_NAME, FeedReaderContract.FeedEntry._ID + "=" + id, null) > 0;
    }

    //---update a particular item---
    public boolean updateItem(long id, ContentValues cv) {
        /* GESTIONE DATABASE */
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        return db.update(TABLE_NAME, cv, FeedReaderContract.FeedEntry._ID + "=" + id, null) > 0;
    }

    private void setEditableButtons() {
        categoria.setEnabled(true);
        luogo.setEnabled(true);
        descrizione.setEnabled(true);
        alert_checkbox.setEnabled(true);
        prezzoEditable.setEnabled(true);
        prezzoEditable.setVisibility(View.VISIBLE);
        prezzo.setVisibility(View.INVISIBLE);

        mState = HIDE_MENU; // setting state = true
        //invalidateOptionsMenu();
        supportInvalidateOptionsMenu();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details_menu, menu);
        if (mState) {
            menu.findItem(R.id.details_cab_done).setVisible(true);
            menu.findItem(R.id.details_cab_cancel).setVisible(true);
            menu.findItem(R.id.action_modify).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
        } else {
            menu.findItem(R.id.details_cab_done).setVisible(false);
            menu.findItem(R.id.details_cab_cancel).setVisible(false);
            menu.findItem(R.id.action_modify).setVisible(true);
            menu.findItem(R.id.action_delete).setVisible(true);
        }
        return true;
    }

    public void manageLocation(View view) {
        TextView loc = (TextView) findViewById(R.id.item_location);
        String locat = loc.getText().toString().replace(" ", "+");
        Uri loc_uri = Uri.parse("geo:0,0?q=" + locat);
        showMap(loc_uri);
    }

    // Funzione chiamata se si preme sul pulsante "mappa"
    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    // Funzione chiamata se si preme sulla imageView
    public void openImage(View view) {
        if (img_custom != null) {
            File file = new File(img_custom);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            startActivity(intent);
        }
    }

}
