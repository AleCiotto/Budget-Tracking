package mercurio.alessandro.budgettracking;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.Vector;

/**
 * Created by Alessandro on 21/08/2014.
 */

// PagerAdapter utilizzato in Statistics
public class CustomPagerAdapter_GraphView extends PagerAdapter {

    private Context mContext;
    private Vector<View> pages;
    // Tab Titles
    private String tabtitles[] = new String[]{"Giorno", "Settimana", "Mese"};

    public CustomPagerAdapter_GraphView(Context context, Vector<View> pages) {
        this.mContext = context;
        this.pages = pages;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        //return false;
        return view.equals(o);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View page = pages.get(position);
        container.addView(page);
        return page;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }
}
