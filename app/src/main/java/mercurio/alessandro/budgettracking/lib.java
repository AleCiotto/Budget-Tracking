package mercurio.alessandro.budgettracking;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alessandro on 06/12/2014.
 */
public class lib {
    public static Bitmap decodeSampledBitmapFromFile(String path,
                                                     int reqWidth, int reqHeight) { // BEST QUALITY MATCH
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;
        if (height > reqHeight) {
            inSampleSize = Math.round((float) height / (float) reqHeight);
        }
        int expectedWidth = width / inSampleSize;
        if (expectedWidth > reqWidth) {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float) width / (float) reqWidth);
        }
        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static String mycreateDirectoryAndSaveFile(Bitmap imageToSave, Context context) {

        File direct = new File(Environment.getExternalStorageDirectory() + "/BudgetTracking");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date d = new Date();
        String date = format.format(d);

        int i = 0;
        String fileName = "IMG-" + date + "-" + i + ".jpg";

        File file = new File(direct, fileName);
        while (file.exists()) {
            i++;
            fileName = "IMG-" + date + "-" + i + ".jpg";
            file = new File(direct, fileName);
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "" + e, Toast.LENGTH_LONG).show();
        }


        return file.getAbsolutePath();
    }

    public static int setDefaultImg(String itemCategory) {
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
        switch (Integer.parseInt(itemCategory)) {
            case 0: {
                return R.drawable.orange_v;
            }
            case 1: {
                return R.drawable.green_c;
            }
            case 2: {
                return R.drawable.violet_b;
            }
            case 3: {
                return R.drawable.blue_a;
            }
            case 4: {
                return R.drawable.light_red_c;
            }
            case 5: {
                return R.drawable.light_blue_d;
            }
            case 6: {
                return R.drawable.green_a;
            }
            case 7: {
                return R.drawable.yellow_h;
            }
            case 8: {
                return R.drawable.pink_s;
            }
            default:
                return R.drawable.ic_launcher;

        }
    }
}
