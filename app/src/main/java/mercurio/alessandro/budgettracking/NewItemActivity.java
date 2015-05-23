package mercurio.alessandro.budgettracking;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static mercurio.alessandro.budgettracking.lib.decodeSampledBitmapFromFile;
import static mercurio.alessandro.budgettracking.lib.mycreateDirectoryAndSaveFile;
import static mercurio.alessandro.budgettracking.lib.setDefaultImg;

/**
 * Created by Alessandro on 07/08/2014.
 */
public class NewItemActivity extends ActionBarActivity {

    private static final int SELECT_PHOTO = 100;
    private static final int CAMERA_IMAGE_REQUEST = 1;
    static TextView data;
    static Calendar itemDate = Calendar.getInstance();
    String array_spinner[];
    String selectedImagePath;
    String img = null;
    int img_default = R.drawable.ic_launcher;

    // Called when we tap on Date's EditText
    public static void onFinishEditDialog(int day, int month, int year) {
        String displayedDate, stringMonth;
        switch (month) {
            case 0:
                stringMonth = "Gennaio";
                break;
            case 1:
                stringMonth = "Febbraio";
                break;
            case 2:
                stringMonth = "Marzo";
                break;
            case 3:
                stringMonth = "Aprile";
                break;
            case 4:
                stringMonth = "Maggio";
                break;
            case 5:
                stringMonth = "Giugno";
                break;
            case 6:
                stringMonth = "Luglio";
                break;
            case 7:
                stringMonth = "Agosto";
                break;
            case 8:
                stringMonth = "Settembre";
                break;
            case 9:
                stringMonth = "Ottobre";
                break;
            case 10:
                stringMonth = "Novembre";
                break;
            case 11:
                stringMonth = "Dicembre";
                break;
            default:
                stringMonth = "undefined";
        }
        displayedDate = day + " " + stringMonth + " " + year;
        data.setText(displayedDate);
        itemDate.set(year, month, day);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_add_layout_2);

        data = (TextView) findViewById(R.id.addItem_date_edit);

        array_spinner = getResources().getStringArray(R.array.categoriesString);
        Spinner s = (Spinner) findViewById(R.id.addItem_category_spinner);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, array_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add:
                boolean ok = saveItem();
                if (ok) {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean saveItem() {

        TextView nome = (TextView) findViewById(R.id.addItem_name_edit);
        // Dichiarata globalmente
        // EditText data = (EditText) findViewById(R.id.addItem_date_edit);
        Spinner categoria = (Spinner) findViewById(R.id.addItem_category_spinner);
        TextView descrizione = (TextView) findViewById(R.id.addItem_description_edit);
        TextView luogo = (TextView) findViewById(R.id.addItem_location_edit);
        TextView prezzo = (TextView) findViewById(R.id.addItem_price_edit);
        CheckBox alert_checkbox = (CheckBox) findViewById(R.id.addItem_alert);


        String name = nome.getText().toString();

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date d = itemDate.getTime();
        String date = format.format(d);

        String category = String.valueOf(categoria.getSelectedItemPosition());
        String description = descrizione.getText().toString();
        String location = luogo.getText().toString();
        String price = prezzo.getText().toString();
        String alert = String.valueOf(alert_checkbox.isChecked());

        img_default = setDefaultImg(category);


        //TODO: need debug
        if (name.equals("") || price.equals("") || nome.getText() == null || prezzo.getText() == null) {
            Toast.makeText(this, "Nome e prezzo sono obbligatori. Se non selezioni una data verrà impostata quella odierna.", Toast.LENGTH_LONG).show();
            return false;
        }


        /* GESTIONE DATABASE */
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_NAME, name);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE, date);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_PRICE, price);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_LOCATION, location);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY, category);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DESCRIPTION, description);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_ALERT, alert);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_IMG_URL, img_default);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_IMG_URL_CUSTOM, img);


        // Insert the new row, returning the primary key value of the new row
        Long newRowId = db.insert(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                values);

        db.close();

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_menu, menu);
        return true;
    }

    // Called when we tap on date EditText
    public void onDataClick(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void createDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scegli:")
                .setItems(R.array.camera_or_gallery, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        // Fotocamera:0 Galleria:1
                        if (which == 0) {
                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "/BudgetTracking/" + "image.jpg");
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
                        } else {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                        }
                    }
                });
        AlertDialog imageDialog = builder.create();
        imageDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {

                    Uri selectedImageUri = data.getData();

                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
                    int column_index = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();

                    selectedImagePath = cursor.getString(column_index);
                    img = selectedImagePath;
                    ImageView imageView = (ImageView) findViewById(R.id.addItem_imgTest);
                    imageView.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                }
                break;
            case CAMERA_IMAGE_REQUEST:
                File newPhoto = new File(Environment.getExternalStorageDirectory() + File.separator + "/BudgetTracking/" + "image.jpg");

                Bitmap bitmap = decodeSampledBitmapFromFile(newPhoto.getAbsolutePath(), 250, 250);
                img = mycreateDirectoryAndSaveFile(bitmap, this);

                ImageView imageView = (ImageView) findViewById(R.id.addItem_imgTest);
                imageView.setImageBitmap(bitmap);

                break;

        }
    }
}
