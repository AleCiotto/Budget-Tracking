package mercurio.alessandro.budgettracking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    ArrayList<Spesa> spesaToShowInAlertDialog = new ArrayList<Spesa>();
    ArrayList<Spesa> spesaList = new ArrayList<Spesa>();
    ListView listview;
    MyAdapter adapter;

    float totaleSpesa = 0;
    long budget;
    float budgetRimasto;
    int budgetDay;

    boolean resumed = false;

    // Necessario per il databse
    FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
    //SQLiteDatabase db = mDbHelper.getWritableDatabase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* === IN CASO DI CASINI === */
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //String TABLE_NAME = "cost";
        //db.execSQL("delete from "+ TABLE_NAME);
        /* === ================= === */

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean budgetIsEnable = prefs.getBoolean("checkbox_budget_preference", false);
        budget = Long.parseLong(prefs.getString("edittext_budget_preference", "0"));
        budgetDay = Integer.parseInt(prefs.getString("day_budget_preference", "0"));
        if (budgetDay > 31) {
            budgetDay = 1;
            Toast.makeText(this, "Giorno del mese non valido, inserire un valore compreso tra 0 e 1 nell' apposita sezione nelle preferenze", Toast.LENGTH_LONG).show();
        } // Se si inserisce un numero non valido considero il primo del mese
        populateListViewFromDB();

        listview = (ListView) findViewById(R.id.listview);
        adapter = new MyAdapter();
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Spesa clickedSpesa = spesaList.get(position);
                //String message = "You click the item on position " + position + " which is " +clickedSpesa.getNome();
                //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                intent.putExtra("name", clickedSpesa.getNome());
                intent.putExtra("date", clickedSpesa.getDataString());
                intent.putExtra("category", clickedSpesa.getCategoria());
                intent.putExtra("description", clickedSpesa.getDescrizione());
                intent.putExtra("image", clickedSpesa.getImg());
                intent.putExtra("image_custom", clickedSpesa.getImg_url_custom());
                intent.putExtra("location", clickedSpesa.getLuogo());
                intent.putExtra("alert", clickedSpesa.isAlert());
                intent.putExtra("price", clickedSpesa.getPrezzo());
                intent.putExtra("longid", clickedSpesa.getId());
                startActivity(intent);
            }
        });

        ImageButton fabImageButton = (ImageButton) findViewById(R.id.fab_image_button);
        fabImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newItem_intent = new Intent(MainActivity.this, NewItemActivity.class);
                startActivity(newItem_intent);
            }
        });

        showFutureExpensesDialog();


    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean budgetIsEnable = prefs.getBoolean("checkbox_budget_preference", false);
        budget = Long.parseLong(prefs.getString("edittext_budget_preference", "0"));
        budgetDay = Integer.parseInt(prefs.getString("day_budget_preference", "0"));

        totaleSpesa = 0;

        if (resumed) {
            // In questo modo la prima volta, ovvero quando l' activity viene creata
            // si va a popolare il database una sola volta
            populateListViewFromDB();
        } else {
            // Setta a true la variabili, in modo che dalla prossima volta, ad ogni onResume
            // la listView verra' aggiornata
            resumed = true;
        }
        listview = (ListView) findViewById(R.id.listview);
        adapter.notifyDataSetChanged();

        if (!budgetIsEnable) {
            TextView b_label = (TextView) findViewById(R.id.budget_label);
            b_label.setVisibility(View.INVISIBLE);
            TextView b_value = (TextView) findViewById(R.id.budget);
            b_value.setVisibility(View.INVISIBLE);
        } else {
            TextView b_label = (TextView) findViewById(R.id.budget_label);
            b_label.setVisibility(View.VISIBLE);
            TextView b_value = (TextView) findViewById(R.id.budget);
            b_value.setVisibility(View.VISIBLE);
        }
    }

    private void populateListViewFromDB() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Svuoto la listview
        spesaList.clear();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_ENTRY_ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_NAME,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_PRICE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_LOCATION,
                FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DESCRIPTION,
                FeedReaderContract.FeedEntry.COLUMN_NAME_IMG_URL,
                FeedReaderContract.FeedEntry.COLUMN_NAME_IMG_URL_CUSTOM,
                FeedReaderContract.FeedEntry.COLUMN_NAME_ALERT
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedReaderContract.FeedEntry._ID + " DESC";

        // Considero solo le date dopo budgetDay [preso dalle preferenze]
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " >= ?";

        Calendar calendar = Calendar.getInstance();
        Calendar todayC = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), budgetDay);

        // calendar e' settato con il mese corrente, ma se calendar > today allora imposto il mese scorso
        if (todayC.before(calendar)) {
            Log.e("Mese: ", "Mese: " + String.valueOf(calendar.get(Calendar.MONTH) - 1));
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) - 1, budgetDay);
        }

        Date dateFrom = calendar.getTime();
        String dateString = format.format(dateFrom);

        String[] selectionArgs = {dateString};

        Cursor c = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        if (c.getCount() > 0) {
            c.moveToFirst();

            for (int i = 0; i < c.getCount(); i++) {
                String itemName = c.getString(c.getColumnIndex("name"));
                // Gestione data
                String stringDate = c.getString(c.getColumnIndex("date"));
                //DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date;
                Date today = new Date();
                Calendar itemDate = Calendar.getInstance();
                try {
                    date = format.parse(stringDate);
                } catch (ParseException e) {
                    date = today;
                    Toast.makeText(this, "eccezione " + e, Toast.LENGTH_SHORT).show();
                }
                itemDate.setTime(date);

                String itemPrice = String.valueOf(c.getDouble(c.getColumnIndex("price")));
                String itemLocation = c.getString(c.getColumnIndex("location"));
                String itemCategory = c.getString(c.getColumnIndex("category"));
                String itemDescription = c.getString(c.getColumnIndex("description"));
                //int itemImg = setDefaultImg(itemCategory);
                int itemImg = Integer.valueOf(c.getString(c.getColumnIndex("img_url")));
                String itemImgCustom = c.getString(c.getColumnIndex("img_url_custom"));
                String itemAlert = c.getString(c.getColumnIndex("alert"));
                int itemId = c.getInt(c.getColumnIndex("_id"));

                spesaList.add(new Spesa(itemName, itemDate, itemPrice, itemLocation, itemCategory, itemDescription, itemImg, itemImgCustom, Boolean.valueOf(itemAlert), itemId));


                if (date.before(new Date())) {
                    // Aggiorno il totale spesa
                    totaleSpesa += Float.parseFloat(itemPrice);
                    budgetRimasto = budget - totaleSpesa;
                } else {
                    if (date.compareTo(today) < 2) {
                        if (Boolean.valueOf(itemAlert)) {
                            spesaToShowInAlertDialog.add(new Spesa(itemName, itemDate, itemPrice, itemLocation, itemCategory, itemDescription, itemImg, itemImgCustom, Boolean.valueOf(itemAlert), itemId));
                        }
                    }
                }

                // Aggiorno il totale spesa
                //totaleSpesa += Float.parseFloat(itemPrice);
                //budgetRimasto = budget - totaleSpesa;

                c.moveToNext();
            }

            TextView total = (TextView) findViewById(R.id.total);
            total.setText("€ " + totaleSpesa);

            TextView budget = (TextView) findViewById(R.id.budget);
            budget.setText("€ " + budgetRimasto);

        } else {
            Toast.makeText(this, "Nessuna spesa da visualizzare", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    public void showFutureExpensesDialog() {
        if (spesaToShowInAlertDialog.size() > 0) {

            ListView l = new ListView(this);
            MyAdapterDialog adapter = new MyAdapterDialog();
            l.setAdapter(adapter);

            l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Spesa clickedSpesa = spesaToShowInAlertDialog.get(position);

                    Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                    intent.putExtra("name", clickedSpesa.getNome());
                    intent.putExtra("date", clickedSpesa.getDataString());
                    intent.putExtra("category", clickedSpesa.getCategoria());
                    intent.putExtra("description", clickedSpesa.getDescrizione());
                    intent.putExtra("image", clickedSpesa.getImg());
                    intent.putExtra("image_custom", clickedSpesa.getImg_url_custom());
                    intent.putExtra("location", clickedSpesa.getLuogo());
                    intent.putExtra("alert", clickedSpesa.isAlert());
                    intent.putExtra("price", clickedSpesa.getPrezzo());
                    intent.putExtra("longid", clickedSpesa.getId());
                    startActivity(intent);
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(l);
            builder.setMessage(R.string.dialog_title)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (id) {
            case R.id.action_settings:
                Intent settings = new Intent(MainActivity.this, UserSettings.class);
                startActivity(settings);
                return true;
            case R.id.statistics:
                Intent stats = new Intent(MainActivity.this, Statistics.class);
                startActivity(stats);
                return true;
            /*case R.id.action_add:
                Intent newItem_intent = new Intent(MainActivity.this, NewItemActivity.class);
                startActivity(newItem_intent);
                return true;*/
            case R.id.advaced_view:
                Intent advancedView_intent = new Intent(MainActivity.this, AdvancedView.class);
                advancedView_intent.putExtra("budgetDay", budgetDay);
                advancedView_intent.putExtra("budget", budget);
                startActivity(advancedView_intent);
                return true;
            case R.id.make_report:
                Intent makeReport_intent = new Intent(MainActivity.this, MakeReport.class);
                startActivity(makeReport_intent);
                return true;
            case R.id.actiom_deleteAll:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final LayoutInflater inflater = this.getLayoutInflater();
                builder.setTitle(R.string.dialog_delete_all_title);
                builder.setMessage(R.string.dialog_delete_all_message);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        String TABLE_NAME = "cost";
                        db.execSQL("delete from " + TABLE_NAME);
                        spesaList.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                //String TABLE_NAME = "cost";
                //db.execSQL("delete from "+ TABLE_NAME);
                //listview.setAdapter(adapter);
                return true;
            case R.id.info:
                Intent openInfo = new Intent(MainActivity.this, Info.class);
                startActivity(openInfo);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ListView -->Adapter<--
    public class MyAdapter extends ArrayAdapter<Spesa> {
        //Context context;

        public MyAdapter() {
            //super(context, R.layout.list_view_layout, values);
            super(MainActivity.this, R.layout.item_layout, spesaList);
            //this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = getLayoutInflater().inflate(R.layout.item_layout, parent, false);
            }

            // Setto la spesa corrente
            Spesa cSpesa = spesaList.get(position);
            // Oggetto
            TextView textViewObjectName = (TextView) rowView.findViewById(R.id.settings_name);
            textViewObjectName.setText(cSpesa.getNome());
            // Data
            TextView textViewData = (TextView) rowView.findViewById(R.id.settings_value);
            textViewData.setText(cSpesa.getDataString());
            // Prezzo
            TextView textViewPrice = (TextView) rowView.findViewById(R.id.price);
            textViewPrice.setText(cSpesa.getPrezzo() + "€");
            // Immagine
            if (cSpesa.getImg_url_custom() == null) {
                ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
                imageView.setImageResource(cSpesa.getImg());
            } else {
                ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
                imageView.setImageBitmap(BitmapFactory.decodeFile(cSpesa.getImg_url_custom()));
                //imageView.setImageBitmap(decodeSampledBitmapFromFile(cSpesa.getImg_url_custom(), 250, 250));
            }
            //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            //imageView.setImageResource(cSpesa.getImg());

            return rowView;
        }
    }

    public class MyAdapterDialog extends ArrayAdapter<Spesa> {
        //Context context;

        public MyAdapterDialog() {
            //super(context, R.layout.list_view_layout, values);
            super(MainActivity.this, R.layout.item_layout, spesaToShowInAlertDialog);
            //this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = getLayoutInflater().inflate(R.layout.item_layout, parent, false);
            }

            // Setto la spesa corrente
            Spesa cSpesa = spesaToShowInAlertDialog.get(position);
            // Oggetto
            TextView textViewObjectName = (TextView) rowView.findViewById(R.id.settings_name);
            textViewObjectName.setText(cSpesa.getNome());
            // Data
            TextView textViewData = (TextView) rowView.findViewById(R.id.settings_value);
            textViewData.setText(cSpesa.getDataString());
            // Prezzo
            TextView textViewPrice = (TextView) rowView.findViewById(R.id.price);
            textViewPrice.setText(cSpesa.getPrezzo() + "€");
            // Immagine
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            imageView.setImageResource(cSpesa.getImg());

            // va anche qui? TODO: need test
            /*
            if(cSpesa.getImg_url_custom() == null) {
                ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
                imageView.setImageResource(cSpesa.getImg());
            } else {
                ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
                imageView.setImageBitmap(BitmapFactory.decodeFile(cSpesa.getImg_url_custom()));
            }
            * */

            return rowView;
        }
    }

}
