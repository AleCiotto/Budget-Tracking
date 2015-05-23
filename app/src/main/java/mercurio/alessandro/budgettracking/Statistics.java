package mercurio.alessandro.budgettracking;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.ValueDependentColor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import static com.jjoe64.graphview.GraphView.GraphViewData;
import static com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

/**
 * Created by Alessandro on 20/08/2014.
 */
public class Statistics extends ActionBarActivity {

    private static int GIORNO = 1;
    int graphSelected = GIORNO;
    private static int SETTIMANA = 7;
    private static int MESE = 31;
    //int WEEK_OF_GRAPH = 0; // 0 is this week
    int DAY_OF_BARGRAPH = 30; // 30 is today
    int selectedPage = 0;

    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat italianFormat = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat graphFormat = new SimpleDateFormat("dd-MM");

    //RelativeLayout layout;
    RelativeLayout giornoPage;// = new RelativeLayout(this);
    RelativeLayout settimanaPage;// = new RelativeLayout(this);
    RelativeLayout mesePage;// = new RelativeLayout(this);


    boolean budgetIsEnable;
    long budget;
    float totSpesa[] = new float[31];
    float VarieSpesa[] = new float[31];
    float CasaSpesa[] = new float[31];
    float BolletteSpesa[] = new float[31];
    float AbbigliamentoSpesa[] = new float[31];
    float CibieBevandeSpesa[] = new float[31];
    float DivertimentoSpesa[] = new float[31];
    float AutoSpesa[] = new float[31];
    float HobbySpesa[] = new float[31];
    float SpesePeriodicheSpesa[] = new float[31];

    int arrayStart;

    Menu mainMenu;
    MenuItem previousItem;
    MenuItem nextItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_layout);

        populateListFromDB();

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Prendo il budget dalle impostazioni (se abilitato)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        budgetIsEnable = prefs.getBoolean("checkbox_budget_preference", false);
        if (budgetIsEnable) {
            budget = Long.parseLong(prefs.getString("edittext_budget_preference", "0"));
        }

        Vector<View> pages = new Vector<View>();

        // Init RelativeLayout
        giornoPage = new RelativeLayout(this);
        settimanaPage = new RelativeLayout(this);
        mesePage = new RelativeLayout(this);
        // Set padding
        giornoPage.setPadding(10, 10, 10, 10);
        settimanaPage.setPadding(10, 10, 10, 10);
        mesePage.setPadding(10, 10, 10, 10);

        // Aggiungo le viste/pagine
        pages.add(giornoPage);
        pages.add(settimanaPage);
        pages.add(mesePage);

        final ViewPager vp = (ViewPager) findViewById(R.id.pager);
        CustomPagerAdapter_GraphView adapter = new CustomPagerAdapter_GraphView(this, pages);
        vp.setAdapter(adapter);

        vp.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        selectedPage = position;
                        if (position == 1 || position == 2) {
                            showPreviousNext(false);
                            //mainMenu.findItem(R.id.legenda).setVisible(true);
                            //mainMenu.findItem(R.id.next).setVisible(false);
                            //mainMenu.findItem(R.id.previous).setVisible(false);
                        } else {
                            showPreviousNext(true);
                            //mainMenu.findItem(R.id.legenda).setVisible(false);
                            //mainMenu.findItem(R.id.next).setVisible(true);
                            //mainMenu.findItem(R.id.previous).setVisible(true);
                        }
                    }
                });



        /* === PARTE RELATIVA AL GRAFICO === */
        //graficoSettimanaleMensile(SETTIMANA);
        graficoGiornaliero(30);
        graficoSettimanaleMensile(SETTIMANA);
        graficoSettimanaleMensile(MESE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*getMenuInflater().inflate(R.menu.statistics_menu, menu);
        mainMenu = menu;
        mainMenu.findItem(R.id.legenda).setVisible(false);
        return true;*/

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.statistics_menu, menu);
        //mainMenu = menu;
        previousItem = menu.findItem(R.id.previous);
        nextItem = menu.findItem(R.id.next);

        if (selectedPage == 0) {
            previousItem.setVisible(true);
            nextItem.setVisible(true);
        } else {
            previousItem.setVisible(false);
            nextItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.previous:
                if (DAY_OF_BARGRAPH == 0) { // disabilito il pulsante indietro se sto visualizzando il grafico di 30 giorni fa [pos 0]
                    item.setEnabled(false);
                    Toast.makeText(this, "Impossibile andare oltre", Toast.LENGTH_SHORT).show();
                    return true;
                }
                DAY_OF_BARGRAPH--;
                giornoPage.removeAllViewsInLayout();
                graficoGiornaliero(DAY_OF_BARGRAPH);

                nextItem.setEnabled(true); // riabilito il pulsante avanti
                return true;
            case R.id.next:
                if (DAY_OF_BARGRAPH == 30) { // disabilito il pulsante indietro se sto visualizzando il grafico di 30 giorni fa [pos 0]
                    item.setEnabled(false);
                    Toast.makeText(this, "Impossibile andare oltre", Toast.LENGTH_SHORT).show();
                    return true;
                }
                DAY_OF_BARGRAPH++;
                giornoPage.removeAllViewsInLayout();
                graficoGiornaliero(DAY_OF_BARGRAPH);

                previousItem.setEnabled(true); // riabilito il pulsante indietro
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void graficoGiornaliero(int day) { // 30 is today
                 /* 0: Varie
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

        //String[] categories = new String[]{"Budget", "", "Tot.", "Varie", "Casa", "Boll.", "Abb.", "C.&B.", "Div.", "Auto", "Hobby", "Sp. Per.", ""};
        String[] categories = new String[]{"Budget", "", "Tot.", "Varie", "Casa", "Boll.", "Abb.", "C.&B.", "Div.", "Auto", "Hobby", "Sp. Per."};

        GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle();
        seriesStyle.setValueDependentColor(new ValueDependentColor() {
            @Override
            public int get(GraphViewDataInterface data) {
                switch ((int) data.getX()) {
                    case 0: {
                        return Color.rgb(200, 50, 0); // red
                    }
                    case 1: {
                        return Color.rgb(0, 76, 153); // blue
                    }
                    case 2: {
                        return Color.parseColor("#fff8b566"); // varie
                    }
                    case 3: {
                        return Color.parseColor("#ff67be74"); // casa
                    }
                    case 4: {
                        return Color.parseColor("#ffac63a6"); // bollette
                    }
                    case 5: {
                        return Color.parseColor("#ff2092cc"); // abbigliamento
                    }
                    case 6: {
                        return Color.parseColor("#fff48459"); // cibi e bevande
                    }
                    case 7: {
                        return Color.parseColor("#ff73b0c7"); // divertimento
                    }
                    case 8: {
                        return Color.parseColor("#ff67be74"); // auto
                    }
                    case 9: {
                        return Color.parseColor("#ffe3c52e"); // hobby
                    }
                    case 10: {
                        return Color.parseColor("#fff06364"); // spese periodiche
                    }
                    default:
                        return Color.DKGRAY;
                }
                // the higher the more red
                //return Color.rgb((int)(150+((data.getY()/3)*100)), (int)(150-((data.getY()/3)*150)), (int)(150-((data.getY()/3)*150)));
            }
        });
        GraphViewSeries exampleSeries2 = new GraphViewSeries("", seriesStyle, new GraphViewData[]{
                new GraphViewData(0, budget)
                , new GraphViewData(0, 0)
                , new GraphViewData(1, totSpesa[day]) // totSpesa[30]
                , new GraphViewData(2, VarieSpesa[day])
                , new GraphViewData(3, CasaSpesa[day])
                , new GraphViewData(4, BolletteSpesa[day])
                , new GraphViewData(5, AbbigliamentoSpesa[day])
                , new GraphViewData(6, CibieBevandeSpesa[day])
                , new GraphViewData(7, DivertimentoSpesa[day])
                , new GraphViewData(8, AutoSpesa[day])
                , new GraphViewData(9, HobbySpesa[day])
                , new GraphViewData(10, SpesePeriodicheSpesa[day])
                //, new GraphViewData(11, 0)
        });

        String nomeGrafico = getDateItalianFormat(30 - day); // 30 - 30 oggi, 30 - 0 primo giorno del mese

        /* create graph */
        GraphView graphView = new BarGraphView(
                this
                , nomeGrafico
        );
        // add style
        graphView.setHorizontalLabels(categories);
        graphView.getGraphViewStyle().setNumVerticalLabels(9);
        graphView.getGraphViewStyle().setTextSize(25);
        // add data
        graphView.addSeries(exampleSeries2);

        giornoPage.addView(graphView);
    }


    public void graficoSettimanaleMensile(int num) {

        String[] days;
        String nomeGrafico;
        if (num == SETTIMANA) {
            days = getLastDays(7); // Chiamare sempre un giorno in piu' TODO: controlla 32
            arrayStart = 24;
            nomeGrafico = "Statistiche dell' ultima settimana";
        } else {
            days = getLastDays(32); // Chiamare sempre un giorno in piu'
            arrayStart = 0;
            nomeGrafico = ""; //TODO: lascio vuoto o metto nome??
        }

        // first init data
        GraphViewData[] data = new GraphViewData[num];

        /* === budget line === */
        GraphViewSeries budgetLine = null;
        if (budgetIsEnable) {
            for (int i = 0; i < num; i++) {
                data[i] = new GraphViewData(i, budget);
            }
            budgetLine = new GraphViewSeries("Budget", new GraphViewSeriesStyle(Color.rgb(200, 50, 0), 3), data);
        }

        /* === total graph === */
        data = new GraphViewData[num];
        float spesePrecedentiTotale = 0;
        float spesaIesimaTotale = 0;
        for (int i = 0; i < num; i++) {
            spesaIesimaTotale = totSpesa[arrayStart + i] + spesePrecedentiTotale;
            data[i] = new GraphViewData(i, spesaIesimaTotale);
            spesePrecedentiTotale = spesaIesimaTotale;

        }
        GraphViewSeries totale = new GraphViewSeries("Totale Spesa", new GraphViewSeriesStyle(Color.BLUE, 3), data);

        /* === Varie graph === */
        data = new GraphViewData[num];
        float spesePrecedentiVarie = 0;
        float spesaIesimaVarie = 0;
        for (int i = 0; i < num; i++) {
            spesaIesimaVarie = VarieSpesa[arrayStart + i] + spesePrecedentiVarie;
            data[i] = new GraphViewData(i, spesaIesimaVarie);
            spesePrecedentiVarie = spesaIesimaVarie;

        }
        GraphViewSeries varie = new GraphViewSeries("Varie", new GraphViewSeriesStyle(Color.parseColor("#fff8b566"), 2), data);

        /* === Casa graph === */
        data = new GraphViewData[num];
        float spesePrecedentiCasa = 0;
        float spesaIesimaCasa = 0;
        for (int i = 0; i < num; i++) {
            spesaIesimaCasa = CasaSpesa[arrayStart + i] + spesePrecedentiCasa;
            data[i] = new GraphViewData(i, spesaIesimaCasa);
            spesePrecedentiCasa = spesaIesimaCasa;

        }
        GraphViewSeries casa = new GraphViewSeries("Casa", new GraphViewSeriesStyle(Color.parseColor("#ff275936"), 2), data);

        /* === Bollette graph === */
        data = new GraphViewData[num];
        float spesePrecedentiBollette = 0;
        float spesaIesimaBollette = 0;
        for (int i = 0; i < num; i++) {
            spesaIesimaBollette = BolletteSpesa[arrayStart + i] + spesePrecedentiBollette;
            data[i] = new GraphViewData(i, spesaIesimaBollette);
            spesePrecedentiBollette = spesaIesimaBollette;

        }
        GraphViewSeries bollette = new GraphViewSeries("Bollette", new GraphViewSeriesStyle(Color.parseColor("#ffac63a6"), 2), data);

        /* === Abbigliamento graph === */
        data = new GraphViewData[num];
        float spesePrecedentiAbbigliamento = 0;
        float spesaIesimaAbbigliamento = 0;
        for (int i = 0; i < num; i++) {
            spesaIesimaAbbigliamento = AbbigliamentoSpesa[arrayStart + i] + spesePrecedentiAbbigliamento;
            data[i] = new GraphViewData(i, spesaIesimaAbbigliamento);
            spesePrecedentiAbbigliamento = spesaIesimaAbbigliamento;

        }
        GraphViewSeries abbigliamento = new GraphViewSeries("Abbigliamento", new GraphViewSeriesStyle(Color.parseColor("#ff2092cc"), 2), data);

        /* === Cibi e Bevande graph === */
        data = new GraphViewData[num];
        float spesePrecedentiCeB = 0;
        float spesaIesimaCeB = 0;
        for (int i = 0; i < num; i++) {
            spesaIesimaCeB = CibieBevandeSpesa[arrayStart + i] + spesePrecedentiCeB;
            data[i] = new GraphViewData(i, spesaIesimaCeB);
            spesePrecedentiCeB = spesaIesimaCeB;

        }
        GraphViewSeries ceb = new GraphViewSeries("C. e B.", new GraphViewSeriesStyle(Color.parseColor("#fff48459"), 2), data);

        /* === Divertimento graph === */
        data = new GraphViewData[num];
        float spesePrecedentiDivertimento = 0;
        float spesaIesimaDivertimento = 0;
        for (int i = 0; i < num; i++) {
            spesaIesimaDivertimento = DivertimentoSpesa[arrayStart + i] + spesePrecedentiDivertimento;
            data[i] = new GraphViewData(i, spesaIesimaDivertimento);
            spesePrecedentiDivertimento = spesaIesimaDivertimento;

        }
        GraphViewSeries divertimento = new GraphViewSeries("Divertimento", new GraphViewSeriesStyle(Color.parseColor("#ff73b0c7"), 2), data);

        /* === Auto graph === */
        data = new GraphViewData[num];
        float spesePrecedentiAuto = 0;
        float spesaIesimaAuto = 0;
        for (int i = 0; i < num; i++) {
            spesaIesimaAuto = AutoSpesa[arrayStart + i] + spesePrecedentiAuto;
            data[i] = new GraphViewData(i, spesaIesimaAuto);
            spesePrecedentiAuto = spesaIesimaAuto;

        }
        GraphViewSeries auto = new GraphViewSeries("Auto", new GraphViewSeriesStyle(Color.parseColor("#ff67be74"), 2), data);

        /* === Hobby graph === */
        data = new GraphViewData[num];
        float spesePrecedentiHobby = 0;
        float spesaIesimaHobby = 0;
        for (int i = 0; i < num; i++) {
            spesaIesimaHobby = HobbySpesa[arrayStart + i] + spesePrecedentiHobby;
            data[i] = new GraphViewData(i, spesaIesimaHobby);
            spesePrecedentiHobby = spesaIesimaHobby;

        }
        GraphViewSeries hobby = new GraphViewSeries("Hobby", new GraphViewSeriesStyle(Color.parseColor("#ffe3c52e"), 2), data);

        /* === Spese Periodiche graph === */
        data = new GraphViewData[num];
        float spesePrecedentiSP = 0;
        float spesaIesimaSP = 0;
        for (int i = 0; i < num; i++) {
            spesaIesimaSP = SpesePeriodicheSpesa[arrayStart + i] + spesePrecedentiSP;
            data[i] = new GraphViewData(i, spesaIesimaSP);
            spesePrecedentiSP = spesaIesimaSP;

        }
        GraphViewSeries sp = new GraphViewSeries("Spese Periodiche", new GraphViewSeriesStyle(Color.parseColor("#fff06364"), 2), data);

        /* === create graph === */
        GraphView graphView = new LineGraphView(
                this
                , nomeGrafico
        );
        graphView.setHorizontalLabels(days);
        // add data
        graphView.addSeries(totale);
        graphView.addSeries(varie);
        graphView.addSeries(casa);
        graphView.addSeries(bollette);
        graphView.addSeries(abbigliamento);
        graphView.addSeries(ceb);
        graphView.addSeries(divertimento);
        graphView.addSeries(auto);
        graphView.addSeries(hobby);
        graphView.addSeries(sp);
        if (budgetIsEnable) {
            graphView.addSeries(budgetLine);
        }
        // optional - legend
        graphView.setShowLegend(true);
        graphView.setLegendAlign(GraphView.LegendAlign.TOP);
        graphView.setLegendWidth(230);
        // label text size
        graphView.getGraphViewStyle().setTextSize(25);
        graphView.getGraphViewStyle().setNumVerticalLabels(9);

        if (num == SETTIMANA) {
            settimanaPage.addView(graphView);
        } else { // MESE
            mesePage.addView(graphView);
        }
    }


    private void populateListFromDB() {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_ENTRY_ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_PRICE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_CATEGORY,
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedReaderContract.FeedEntry.COLUMN_NAME_NAME + " DESC";


        //if(mode == SETTIMANA) {
        for (int j = 0; j < MESE; j++) {

            //String selection = null;
            String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = ?";
            String queryDate = getDateForDB(j);
            String[] selectionArgs = {queryDate};

            Cursor c = db.query(
                    FeedReaderContract.FeedEntry.TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                                     // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );

            //Toast.makeText(this, "" + c.getCount(), Toast.LENGTH_SHORT).show();

            if (c.getCount() > 0) {
                c.moveToFirst();

                for (int i = 0; i < c.getCount(); i++) {
                    // Gestione data
                    String stringDate = c.getString(c.getColumnIndex("date"));
                    Date date;
                    Calendar itemDate = Calendar.getInstance();
                    try {
                        date = format.parse(stringDate);
                    } catch (ParseException e) {
                        date = new Date();
                        Toast.makeText(this, "eccezione " + e, Toast.LENGTH_SHORT).show();
                    }
                    itemDate.setTime(date);

                    String itemPrice = c.getString(c.getColumnIndex("price"));
                    String itemCategory = c.getString(c.getColumnIndex("category"));
                    int itemId = c.getInt(c.getColumnIndex("_id"));

                    // Aggiorno il totale spesa
                    float prezzo = Float.parseFloat(itemPrice);
                    totSpesa[30 - j] += prezzo;
                    switch (Integer.parseInt(itemCategory)) {
                        case 0: {
                            VarieSpesa[30 - j] += prezzo;
                            break;
                        }
                        case 1: {
                            CasaSpesa[30 - j] += prezzo;
                            break;
                        }
                        case 2: {
                            BolletteSpesa[30 - j] += prezzo;
                            break;
                        }
                        case 3: {
                            AbbigliamentoSpesa[30 - j] += prezzo;
                            break;
                        }
                        case 4: {
                            CibieBevandeSpesa[30 - j] += prezzo;
                            break;
                        }
                        case 5: { //Varie
                            DivertimentoSpesa[30 - j] += prezzo;
                            break;
                        }
                        case 6: {
                            AutoSpesa[30 - j] += prezzo;
                            break;
                        }
                        case 7: {
                            HobbySpesa[30 - j] += prezzo;
                            break;
                        }
                        case 8: {
                            SpesePeriodicheSpesa[30 - j] += prezzo;
                            break;
                        }
                    }


                    c.moveToNext();
                }

            } else {
                //Toast.makeText(this, "Non ci sono spese da visualizzare", Toast.LENGTH_LONG).show();
            }

        } // fine for
        //} // fine if

        //updateSpesaArray(mode);

    }

    // Questa funzione fa in modo che ogni spesa corrispondente alla propria data, include anche quelle dei giorni precedenti
    private void updateSpesaArray(int mode) { //TODO da cancellare
        int start;
        if (mode == SETTIMANA) {
            start = 24;
        } else {
            start = 0;
        }
        for (int i = 1; i < mode; i++) {
            totSpesa[start + i] = totSpesa[start + i] + totSpesa[start + i - 1];
            VarieSpesa[start + i] = VarieSpesa[start + i] + VarieSpesa[start + i - 1];
            //Toast.makeText(this, "start+i= " + (start+i) + "," + totSpesa[start+i]+ " -1= " + (start+i-1) + "," + totSpesa[start+i-1], Toast.LENGTH_LONG).show();
        }
        ;
    }

    @Override
    protected void onStop() { //TODO da cancellare
        super.onStop();
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    // Restituisce un vettore di stringhe utilizzate come label nel grafico
    public String[] getLastDays(int numDays) {
        if (numDays == SETTIMANA) {
            String days[] = new String[numDays];
            for (int i = 0; i < numDays; i++) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1 * i);
                days[numDays - i - 1] = graphFormat.format(cal.getTime());
                // days[numDays] e' oggi
            }
            return days;
        } else {
            numDays = 16;
            String days[] = new String[numDays];
            for (int i = 0; i < numDays; i++) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -2 * i);
                // altrimenti si sovrappongono le ultime e le prime due scritte
                if (i == 0 || i == numDays - 1) {
                    days[numDays - i - 1] = "";
                } else {
                    days[numDays - i - 1] = graphFormat.format(cal.getTime());
                }
                // days[numDays] e' oggi
            }
            return days;
        }
    }

    // Restituisce la data odierna - x giorni
    public String getDateForDB(int num) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1 * num);
        return format.format(cal.getTime());
    }

    // Restituisce la data odierna - x giorni (formato dd-MM-yyyy)
    public String getDateItalianFormat(int num) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1 * num);
        return italianFormat.format(cal.getTime());
    }

    // mostra/nasconde i pulsanti avanti e indietro della action bar
    public void showPreviousNext(boolean bool) {
        // serve perche' altrimenti quando si gira il device mainMenu diventa null (e quindi anche nextItem,previousItem e legendaItem)
        if (nextItem == null || previousItem == null) {
            //Toast.makeText(this, "menu= " + mainMenu, Toast.LENGTH_SHORT).show();
            return;
        } else {
            nextItem.setVisible(bool);
            previousItem.setVisible(bool);
        }
    }
}