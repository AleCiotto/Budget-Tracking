package mercurio.alessandro.budgettracking;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alessandro on 09/12/2014.
 */
public class ReportManager extends ListActivity {
    private File file;
    private List<String> myList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_manager_layout);

        myList = new ArrayList<String>();

        file = new File(Environment.getExternalStorageDirectory() + File.separator + "/BudgetTracking/Reports");
        File list[] = file.listFiles();

        for (File aList : list) {
            myList.add(aList.getName());
        }

        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, myList));


    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "BudgetTracking/Reports/" + myList.get(position));
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        startActivity(intent);

    }

}
