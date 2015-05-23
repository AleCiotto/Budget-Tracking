package mercurio.alessandro.budgettracking;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
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
import java.util.Vector;

/**
 * Created by Alessandro on 09/08/2014.
 */
public class AdvancedView extends ActionBarActivity {

    private static int NO_FILTER = 0;
    private static int FILTER_BY_DATE = 1;
    private static int FILTER_BY_NAME = 2;
    private static int FILTER_BY_CATEGORY = 3;
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    ArrayList<Spesa> spesaList_byDate = new ArrayList<Spesa>();
    ArrayList<Spesa> spesaList_byPrice = new ArrayList<Spesa>();
    ArrayList<Spesa> spesaList_byCategory = new ArrayList<Spesa>();
    ListViewAdapterCategory myadapterCategory;
    ListViewAdapterPrice myadapterPrice;
    ListViewAdapterDate myadapterDate;
    float totaleSpesa = 0;
    long budget = 0;
    int budgetDay = 0;
    float budgetRimasto = 0;
    float totaleVarie = 0;
    float totaleCasa = 0;
    float totaleBollette = 0;
    float totaleAbbigliamento = 0;
    float totaleCibieBevande = 0;
    float totaleDivertimento = 0;
    float totaleAuto = 0;
    float totaleHobby = 0;
    float totaleSpesePeriodiche = 0;
    TextView totalTextView;
    TextView budgetTextView;
    TextView varieTextView;
    TextView casaTextView;
    TextView bolletteTextView;
    TextView abbiglTextView;
    TextView cebTextView;
    TextView divertTextView;
    TextView autoTextView;
    TextView hobbyTextView;
    TextView spTextView;
    // Variabile usata per sapere da quale listView proviene la selezione dell' item
    int selectedPage = 0;
    Menu mainMenu;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.advancedview_v2_layout);

        totalTextView = (TextView) findViewById(R.id.total);
        budgetTextView = (TextView) findViewById(R.id.budget);

        Intent intent = getIntent();
        budget = intent.getLongExtra("budget", 0);
        budgetDay = intent.getIntExtra("budgetDay", 0);

        // Contenuto delle viste/pagine
        ListView listview1 = new ListView(mContext);
        ListView listview2 = new ListView(mContext);
        ListView listview3 = new ListView(mContext);
        View details = getLayoutInflater().inflate(R.layout.advancedview_details_layout_test, null);

        varieTextView = (TextView) details.findViewById(R.id.varie_total);
        casaTextView = (TextView) details.findViewById(R.id.casa_total);
        bolletteTextView = (TextView) details.findViewById(R.id.bollette_total);
        abbiglTextView = (TextView) details.findViewById(R.id.abbigl_total);
        cebTextView = (TextView) details.findViewById(R.id.ceb_total);
        divertTextView = (TextView) details.findViewById(R.id.divert_total);
        autoTextView = (TextView) details.findViewById(R.id.auto_total);
        hobbyTextView = (TextView) details.findViewById(R.id.hobby_total);
        spTextView = (TextView) details.findViewById(R.id.sp_total);

        Vector<View> pages = new Vector<View>();

        // Aggiungo le viste/pagine
        pages.add(listview1);
        pages.add(listview2);
        pages.add(listview3);
        pages.add(details);

        final ViewPager vp = (ViewPager) findViewById(R.id.pager);
        CustomPagerAdapter adapter = new CustomPagerAdapter(mContext, pages);
        vp.setAdapter(adapter);

        String[] nullArgs = {null};
        populateListViewFromDB(NO_FILTER, nullArgs);

        myadapterDate = new ListViewAdapterDate();
        myadapterPrice = new ListViewAdapterPrice();
        myadapterCategory = new ListViewAdapterCategory();

        listview1.setAdapter(myadapterDate);
        listview2.setAdapter(myadapterPrice);
        listview3.setAdapter(myadapterCategory);
        //listview1.setAdapter(new ArrayAdapter<Spesa>(mContext, R.layout.item_layout, spesaList));


        vp.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        selectedPage = position;
                    }
                });


        // Aggiungo il click listener per le 3 listview
        listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startItemActivity(position);
            }
        });
        listview2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startItemActivity(position);
            }
        });
        listview3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startItemActivity(position);
            }
        });


    }


    /* === GESTIONE MENU === */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.advancedview_menu, menu);
        mainMenu = menu;
        mainMenu.findItem(R.id.show_all).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.search_by_name:
                createDialog(FILTER_BY_NAME);
                return true;
            case R.id.search_by_date:
                createDialog(FILTER_BY_DATE);
                return true;
            case R.id.search_by_category:
                createDialog(FILTER_BY_CATEGORY);
                return true;
            case R.id.show_all:
                populateListViewFromDB(NO_FILTER, null);
                myadapterDate.notifyDataSetChanged();
                myadapterPrice.notifyDataSetChanged();
                myadapterCategory.notifyDataSetChanged();
                mainMenu.findItem(R.id.show_all).setVisible(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createDialog(final int mode) {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final LayoutInflater inflater = this.getLayoutInflater();

        final ArrayList<String> args = new ArrayList<String>();

        // Necessario tenerla globale.
        // Usato per mettere il layout con l'editText per inserire il nome
        final View dialogView = inflater.inflate(R.layout.dialog_filter_by_name, null);
        //final View datePicker = inflater.inflate(R.layout.dialog_filter_by_date, null);
        View datePickers = getLayoutInflater().inflate(R.layout.dialog_date_filter_layout, null);

        // 2. Chain together various setter methods to set the dialog characteristics
        switch (mode) {
            case 1: { // Data
                builder.setTitle(R.string.dialog_filter_by_date_title);
                //builder.setView(datePicker); CANCELLA
                //datePickers = getLayoutInflater().inflate(R.layout.dialog_date_filter_layout, null);
                builder.setView(datePickers);
                break;
            }
            case 2: { // Nome
                builder.setTitle(R.string.dialog_filter_by_name_title);
                builder.setView(dialogView);
                break;
            }
            case 3: { // Categoria
                builder.setTitle(R.string.dialog_filter_by_category_title);
                builder.setMultiChoiceItems(R.array.categoriesString, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    args.add(String.valueOf(which));
                                } else if (args.contains(String.valueOf(which))) {
                                    // Else, if the item is already in the array, remove it
                                    args.remove(String.valueOf(which));
                                }
                            }
                        });
                break;
            }
        }

        // Add the buttons
        final View finalDatePickers = datePickers;
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                switch (mode) {
                    case 1: { // Date
                        DatePicker datePFrom = (DatePicker) finalDatePickers.findViewById(R.id.fromDatePicker);
                        DatePicker datePTo = (DatePicker) finalDatePickers.findViewById(R.id.toDatePicker);
                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                        Calendar calendarFrom = Calendar.getInstance();
                        Calendar calendarTo = Calendar.getInstance();

                        calendarFrom.set(datePFrom.getYear(), datePFrom.getMonth(), datePFrom.getDayOfMonth());
                        calendarTo.set(datePTo.getYear(), datePTo.getMonth(), datePTo.getDayOfMonth());

                        Date dFrom = calendarFrom.getTime();
                        Date dTo = calendarTo.getTime();

                        String dateStringFrom = format.format(dFrom);
                        String dateStringTo = format.format(dTo);

                        String[] date;
                        if (dFrom.after(dTo)) { // dFrom > dTo
                            date = new String[]{dateStringTo, dateStringFrom};
                        } else { // dFrom < dTo
                            date = new String[]{dateStringFrom, dateStringTo};
                        }
                        populateListViewFromDB(FILTER_BY_DATE, date);
                        myadapterDate.notifyDataSetChanged();
                        myadapterPrice.notifyDataSetChanged();
                        myadapterCategory.notifyDataSetChanged();

                        mainMenu.findItem(R.id.show_all).setVisible(true);
                        break;
                    }
                    case 2: { // Name
                        TextView editText = (TextView) dialogView.findViewById(R.id.dialog_nameEditText);
                        String[] name = {editText.getText().toString()};

                        populateListViewFromDB(FILTER_BY_NAME, name);
                        myadapterDate.notifyDataSetChanged();
                        myadapterPrice.notifyDataSetChanged();
                        myadapterCategory.notifyDataSetChanged();

                        mainMenu.findItem(R.id.show_all).setVisible(true);
                        break;
                    }
                    case 3: { // Category
                        String[] category = new String[args.size()];
                        for (int i = 0; i < args.size(); i++) {
                            category[i] = args.get(i);
                        }

                        populateListViewFromDB(FILTER_BY_CATEGORY, category);
                        myadapterDate.notifyDataSetChanged();
                        myadapterPrice.notifyDataSetChanged();
                        myadapterCategory.notifyDataSetChanged();

                        mainMenu.findItem(R.id.show_all).setVisible(true);
                        break;
                    }
                }

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void populateListViewFromDB(int filter, String[] args) {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Date today = new Date();

        // Svuoto le liste
        spesaList_byCategory.clear();
        spesaList_byPrice.clear();
        spesaList_byDate.clear();
        // Azzerro le variabili
        totaleSpesa = 0;
        budgetRimasto = 0;
        totaleVarie = 0;
        totaleCasa = 0;
        totaleBollette = 0;
        totaleAbbigliamento = 0;
        totaleCibieBevande = 0;
        totaleDivertimento = 0;
        totaleAuto = 0;
        totaleHobby = 0;
        totaleSpesePeriodiche = 0;

        boolean vuoto = false;

        for (int j = 0; j < 3; j++) {

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
            String sortOrder;
            if (j == 0) { // Date
                sortOrder =
                        FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " DESC";
            } else if (j == 1) { // Price
                sortOrder =
                        "CAST(" + FeedReaderContract.FeedEntry.COLUMN_NAME_PRICE + " AS REAL)" + " DESC";
            } else { // Category
                sortOrder =
                        FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY + " DESC";
            }


            String selection = null;
            String[] selectionArgs = {null};
            switch (filter) {
                case 0: { // NO_FILTER
                    // Considero solo le date dopo budgetDay [preso dalle preferenze]
                    selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " >= ?";

                    Calendar calendar = Calendar.getInstance();
                    Calendar todayC = Calendar.getInstance();
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), budgetDay);

                    // calendar e' settato con il mese corrente, ma se calendar > today allora imposto il mese scorso
                    if (todayC.before(calendar)) {
                        Toast.makeText(this, "Mese: " + String.valueOf(calendar.get(Calendar.MONTH) - 1), Toast.LENGTH_LONG).show();
                        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) - 1, budgetDay);
                    }

                    Date dateFrom = calendar.getTime();
                    String dateString = format.format(dateFrom);

                    String[] datesArray = {dateString};
                    selectionArgs = datesArray;
                    break;
                }
                case 1: { // FILTER_BY_DATE
                    selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " >= ? AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " <= ?";
                    selectionArgs = args;
                    break;
                }
                case 2: { // FILTER_BY_NAME
                    selection = FeedReaderContract.FeedEntry.COLUMN_NAME_NAME + " = ?";
                    selectionArgs = args;
                    break;
                }
                case 3: { // FILTER_BY_CATEGORY
                    for (int i = 0; i < args.length; i++) {
                        if (i == 0) {
                            selection = FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY + " = ?";
                        } else {
                            selection += " OR " + FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY + " = ?";
                        }
                    }
                    //selection = FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY + " = ? OR " + FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY + " = ?";
                    selectionArgs = args;
                    break;
                }
            }

            /*Cursor c = db.query(
                    FeedReaderContract.FeedEntry.TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    null,                                     // The columns for the WHERE clause
                    null,                                     // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
                );
            if(filter != NO_FILTER) {*/
            Cursor c = db.query(
                    FeedReaderContract.FeedEntry.TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );


            //}

            if (c.getCount() > 0) {
                c.moveToFirst();

                for (int i = 0; i < c.getCount(); i++) {
                    String itemName = c.getString(c.getColumnIndex("name"));
                    // Gestione data
                    String stringDate = c.getString(c.getColumnIndex("date"));
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date date;
                    Calendar itemDate = Calendar.getInstance();
                    try {
                        date = format.parse(stringDate);
                    } catch (ParseException e) {
                        date = new Date();
                        Toast.makeText(this, "eccezione " + e, Toast.LENGTH_SHORT).show();
                    }
                    itemDate.setTime(date);

                    String itemPrice = String.valueOf(c.getDouble(c.getColumnIndex("price")));
                    String itemLocation = c.getString(c.getColumnIndex("location"));
                    String itemCategory = c.getString(c.getColumnIndex("category"));
                    String itemDescription = c.getString(c.getColumnIndex("description"));
                    int itemImg = Integer.valueOf(c.getString(c.getColumnIndex("img_url")));
                    String itemImgCustom = c.getString(c.getColumnIndex("img_url_custom"));
                    String itemAlert = c.getString(c.getColumnIndex("alert"));
                    int itemId = c.getInt(c.getColumnIndex("_id"));

                    if (j == 0) {
                        spesaList_byDate.add(new Spesa(itemName, itemDate, itemPrice, itemLocation, itemCategory, itemDescription, itemImg, itemImgCustom, Boolean.valueOf(itemAlert), itemId));
                        if (!date.after(today)) {
                            // Aggiorno i totali parziali
                            switch (Integer.parseInt(itemCategory)) {
                            /*  0: Varie
                                1: Casa
                                2: Bollette
                                3: Abbigliamento
                                4: Cibi e Bevande
                                5: Divertimento
                                6: Auto
                                7: Hobby
                                8: Spese Periodiche
                                9:
                            */
                                case 0: {
                                    totaleVarie += Float.parseFloat(itemPrice);
                                    break;
                                }
                                case 1: {
                                    totaleCasa += Float.parseFloat(itemPrice);
                                    break;
                                }
                                case 2: {
                                    totaleBollette += Float.parseFloat(itemPrice);
                                    break;
                                }
                                case 3: {
                                    totaleAbbigliamento += Float.parseFloat(itemPrice);
                                    break;
                                }
                                case 4: {
                                    totaleCibieBevande += Float.parseFloat(itemPrice);
                                    break;
                                }
                                case 5: {
                                    totaleDivertimento += Float.parseFloat(itemPrice);
                                    break;
                                }
                                case 6: {
                                    totaleAuto += Float.parseFloat(itemPrice);
                                    break;
                                }
                                case 7: {
                                    totaleHobby += Float.parseFloat(itemPrice);
                                    break;
                                }
                                case 8: {
                                    totaleSpesePeriodiche += Float.parseFloat(itemPrice);
                                    break;
                                }
                            }

                            // Aggiorno il totale spesa [basta farlo una volta sola]
                            totaleSpesa += Float.parseFloat(itemPrice);
                            budgetRimasto = budget - totaleSpesa;
                        }
                    } else if (j == 1) {
                        spesaList_byPrice.add(new Spesa(itemName, itemDate, itemPrice, itemLocation, itemCategory, itemDescription, itemImg, itemImgCustom, Boolean.valueOf(itemAlert), itemId));
                    } else {
                        spesaList_byCategory.add(new Spesa(itemName, itemDate, itemPrice, itemLocation, itemCategory, itemDescription, itemImg, itemImgCustom, Boolean.valueOf(itemAlert), itemId));
                    }

                    c.moveToNext();
                } // fine for 0 < i < c.getCount()

            } else {
                vuoto = true;
            }

        } // fine for

        if (vuoto) {
            Toast.makeText(this, "Nessun risultato", Toast.LENGTH_SHORT).show();
        }

        totalTextView.setText("€ " + String.valueOf(totaleSpesa));
        budgetTextView.setText("€ " + String.valueOf(budgetRimasto));
        varieTextView.setText("€ " + totaleVarie);
        casaTextView.setText("€ " + totaleCasa);
        bolletteTextView.setText("€ " + totaleBollette);
        abbiglTextView.setText("€ " + totaleAbbigliamento);
        cebTextView.setText("€ " + totaleCibieBevande);
        divertTextView.setText("€ " + totaleDivertimento);
        autoTextView.setText("€ " + totaleAuto);
        hobbyTextView.setText("€ " + totaleHobby);
        spTextView.setText("€ " + totaleSpesePeriodiche);


    }

    // Funzione che gestisce il tocco su un elemento della lista.
    // Fa un controllo sul tab selezionato per capire da quale lista proviene la selezione.
    public void startItemActivity(int position) {

        Spesa clickedSpesa;

        if (selectedPage == 0) {
            clickedSpesa = spesaList_byDate.get(position);
        } else if (selectedPage == 1) {
            clickedSpesa = spesaList_byPrice.get(position);
        } else {
            clickedSpesa = spesaList_byCategory.get(position);
        }

        //Spesa clickedSpesa = spesaList.get(position);

        Intent intent = new Intent(AdvancedView.this, ItemActivity.class);
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


    // ListView -->Adapter<--
    public class ListViewAdapterDate extends ArrayAdapter<Spesa> {

        public ListViewAdapterDate() {
            super(AdvancedView.this, R.layout.item_layout, spesaList_byDate);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = getLayoutInflater().inflate(R.layout.item_layout, parent, false);
            }

            // Setto la spesa corrente
            Spesa cSpesa = spesaList_byDate.get(position);

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

            return rowView;
        }
    }

    public class ListViewAdapterPrice extends ArrayAdapter<Spesa> {

        public ListViewAdapterPrice() {
            super(AdvancedView.this, R.layout.item_layout, spesaList_byPrice);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = getLayoutInflater().inflate(R.layout.item_layout, parent, false);
            }

            // Setto la spesa corrente
            Spesa cSpesa = spesaList_byPrice.get(position);

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

            return rowView;
        }
    }

    public class ListViewAdapterCategory extends ArrayAdapter<Spesa> {

        public ListViewAdapterCategory() {
            super(AdvancedView.this, R.layout.item_layout, spesaList_byCategory);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = getLayoutInflater().inflate(R.layout.item_layout, parent, false);
            }

            // Setto la spesa corrente
            Spesa cSpesa = spesaList_byCategory.get(position);

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

            return rowView;
        }
    }

}


