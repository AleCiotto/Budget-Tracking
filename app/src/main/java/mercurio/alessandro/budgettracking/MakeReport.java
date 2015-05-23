package mercurio.alessandro.budgettracking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Alessandro on 02/09/2014.
 */
public class MakeReport extends ActionBarActivity {

    static float totaleSpesa = 0;
    static float budgetRimasto = 0;
    static ArrayList<Spesa> spesaList = new ArrayList<Spesa>();
    static long budget;
    float totaleVarie = 0;
    float totaleCasa = 0;
    float totaleBollette = 0;
    float totaleAbbigliamento = 0;
    float totaleCibieBevande = 0;
    float totaleDivertimento = 0;
    float totaleAuto = 0;
    float totaleHobby = 0;
    float totaleSpesePeriodiche = 0;
    String[] datesFromPickers;
    String[] categoriesFromDialog;
    EditText fromEditText;
    EditText toEditText;
    Button categories;
    Button makeReport;
    Spinner orderBy;
    boolean vuoto = false;
    boolean budgetIsEnable;

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private static void addContent(Document document) throws DocumentException {
        // Second parameter is the number of the chapter
        Chapter catPart = new Chapter(new Paragraph("Report generated by: " + System.getProperty("user.name") + ", " + new Date()) + "\n\n", 0);

        // add a table
        createTable(catPart);

        Paragraph final_report1 = new Paragraph("\nBudget: " + budget);
        Paragraph final_report2 = new Paragraph("Totale: " + totaleSpesa);
        Paragraph final_report3 = new Paragraph("_________________");
        Paragraph final_report4 = new Paragraph("Budget rimasto: " + budgetRimasto);

        // now add all this to the document
        document.add(catPart);
        document.add(final_report1);
        document.add(final_report2);
        document.add(final_report3);
        document.add(final_report4);

    }

    private static void createTable(Section subCatPart)
            throws BadElementException {
        PdfPTable table = new PdfPTable(4);

        // t.setBorderColor(BaseColor.GRAY);
        // t.setPadding(4);
        // t.setSpacing(4);
        // t.setBorderWidth(1);

        PdfPCell c1 = new PdfPCell(new Phrase("Nome"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Data"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Categoria"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Prezzo"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        table.setHeaderRows(1);

        for (Spesa aSpesaList : spesaList) {
            table.addCell(aSpesaList.getNome());
            table.addCell(aSpesaList.getDataString());

            String categoria;
            switch (Integer.parseInt(aSpesaList.getCategoria())) {
                case 0: {
                    categoria = "Varie";
                    break;
                }
                case 1: {
                    categoria = "Casa";
                    break;
                }
                case 2: {
                    categoria = "Bollette";
                    break;
                }
                case 3: {
                    categoria = "Abbigliamento";
                    break;
                }
                case 4: {
                    categoria = "Cibi e Bevande";
                    break;
                }
                case 5: {
                    categoria = "Divertimento";
                    break;
                }
                case 6: {
                    categoria = "Auto";
                    break;
                }
                case 7: {
                    categoria = "Hobby";
                    break;
                }
                case 8: {
                    categoria = "Spese Periodiche";
                    break;
                }
                default:
                    categoria = "undefined";
            }
            table.addCell(categoria);
            table.addCell(aSpesaList.getPrezzo());
        }

        subCatPart.add(table);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_report_layout);

        fromEditText = (EditText) findViewById(R.id.fromEditText);
        toEditText = (EditText) findViewById(R.id.toEditText);
        categories = (Button) findViewById(R.id.categories_button);
        orderBy = (Spinner) findViewById(R.id.order_by_spinner);
        makeReport = (Button) findViewById(R.id.make_button);

        String[] arrayOrderBy_spinner = getResources().getStringArray(R.array.orderBy_spinner);
        ArrayAdapter adapterO = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, arrayOrderBy_spinner);
        adapterO.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderBy.setAdapter(adapterO);

        makeReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeQuery(datesFromPickers, categoriesFromDialog, orderBy.getSelectedItemPosition());
            }
        });


    }

    private void makeQuery(String[] dates, String[] category, int order) {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        budgetIsEnable = prefs.getBoolean("checkbox_budget_preference", false);
        budget = Long.parseLong(prefs.getString("edittext_budget_preference", "0"));

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
        };
        // How you want the results sorted in the resulting Cursor
        String sortOrder;
        if (order == 0) { // Date
            sortOrder =
                    FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " DESC";
        } else if (order == 1) { // Price
            sortOrder =
                    "CAST(" + FeedReaderContract.FeedEntry.COLUMN_NAME_PRICE + " AS REAL)" + " DESC";
        } else if (order == 2) { // Alfabetico
            sortOrder =
                    FeedReaderContract.FeedEntry.COLUMN_NAME_NAME + " DESC";
        } else { // Category
            sortOrder =
                    FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY + " DESC";
        }


        String selection = null;
        int argsSize = dates.length + category.length;
        String[] selectionArgs = new String[argsSize];

        // DATE_PARAMETERS
        selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " >= ? AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " <= ?";
        selectionArgs[0] = dates[0];
        selectionArgs[1] = dates[1];

        // CATEGORY_PARAMETERS
        for (int i = 0; i < category.length; i++) {
            if (i == 0) {
                selection += " AND (" + FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY + " = ?";
            } else {
                selection += " OR " + FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY + " = ?";
            }
            selectionArgs[2 + i] = category[i];
        }
        selection += ")";

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


                int itemId = c.getInt(c.getColumnIndex("_id"));

                spesaList.add(new Spesa(itemName, itemDate, itemPrice, itemLocation, itemCategory, itemDescription, itemId));
                // Aggiorno i totali parziali
                switch (Integer.parseInt(itemCategory)) {
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

                c.moveToNext();
            } // fine for 0 < i < c.getCount()

        } else {
            vuoto = true;
        }

        if (vuoto) {
            Toast.makeText(this, "Nessun risultato", Toast.LENGTH_SHORT).show();
        } else {
            try {
                createPDF();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                Toast.makeText(this, "Errore nel creare il file PDF", Toast.LENGTH_LONG).show();
            }
        }


        db.close();
    }

    public void createPDF() throws FileNotFoundException, DocumentException {

        File reportsFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "BudgetTracking/Reports");

        Date d = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        File path = new File(Environment.getExternalStorageDirectory() + File.separator + "BudgetTracking" + File.separator + "Reports");
        if (!reportsFolder.exists()) {
            reportsFolder.mkdirs();
        }

        int i = 0;
        String fileName = format.format(d) + "-" + i + ".pdf";

        File file = new File(path, fileName);
        while (file.exists()) {
            i++;
            fileName = format.format(d) + "-" + i + ".pdf";
            file = new File(path, fileName);
        }

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            addContent(document);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Documento creato con successo.", Toast.LENGTH_LONG).show();
    }

    public void createDateDialog(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = this.getLayoutInflater();

        final ArrayList<String> args = new ArrayList<String>();

        final View dialogView = inflater.inflate(R.layout.dialog_filter_by_name, null);
        View datePickers = getLayoutInflater().inflate(R.layout.dialog_date_filter_layout, null);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.select_dates);
        builder.setView(datePickers);

        // Add the buttons
        final View finalDatePickers = datePickers;
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                DatePicker datePFrom = (DatePicker) finalDatePickers.findViewById(R.id.fromDatePicker);
                DatePicker datePTo = (DatePicker) finalDatePickers.findViewById(R.id.toDatePicker);
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat italianFormat = new SimpleDateFormat("dd-MM-yyyy");

                Calendar calendarFrom = Calendar.getInstance();
                Calendar calendarTo = Calendar.getInstance();

                calendarFrom.set(datePFrom.getYear(), datePFrom.getMonth(), datePFrom.getDayOfMonth());
                calendarTo.set(datePTo.getYear(), datePTo.getMonth(), datePTo.getDayOfMonth());

                Date dFrom = calendarFrom.getTime();
                Date dTo = calendarTo.getTime();

                String dateStringFrom = format.format(dFrom);
                String dateStringTo = format.format(dTo);
                fromEditText.setText(italianFormat.format(dFrom));
                toEditText.setText(italianFormat.format(dTo));

                if (dFrom.after(dTo)) { // dFrom > dTo
                    datesFromPickers = new String[]{dateStringTo, dateStringFrom};
                } else { // dFrom < dTo
                    datesFromPickers = new String[]{dateStringFrom, dateStringTo};
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

    public void createCategoriesDialog(View view) {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final ArrayList<String> args = new ArrayList<String>();

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

        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                String[] category = new String[args.size()];
                for (int i = 0; i < args.size(); i++) {
                    category[i] = args.get(i);
                }
                categoriesFromDialog = category;
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

    public void openFolder(View view) {
        Intent manageReports_intent = new Intent(MakeReport.this, ReportManager.class);
        startActivity(manageReports_intent);

    }
}
