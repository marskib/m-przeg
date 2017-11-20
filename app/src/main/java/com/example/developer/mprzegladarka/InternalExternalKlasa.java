package com.example.developer.mprzegladarka;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by developer on 2017-03-06.
 */

public class InternalExternalKlasa extends Activity {

    private static final String LOG_TAG = "SKIB_LOG_KATALOGI";
    public static String rootToExtSD = null;
    public Intent intent;  //na wywolania fole browsera/chosera
    Button bExternal;
    Button bInternal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.internal_external);

        //sciezka do karty SD zewnetrzneJ (hardware dependent):
        //rootToExtSD = dajRootDoExternalSD_Bathis_Krzysiek();

               //taki podobno jest na Kasi telefonie...
        //rootToExtSD = "/storage/6089-903D/DCIM/Camera";

        //Odnalezienie (if any) sciezki do zewnetrznej karty SD:
        try {
            String[] punktyMontowan;
            /**/
            //Rozwiazanie z internetu: https://stackoverflow.com/questions/36766016/how-to-get-sd-card-path-in-android6-0-programmatically
            punktyMontowan = getExternalStorageDirectoriesSkib();
            /**/
            rootToExtSD = punktyMontowan[0]; //tutaj (prawdopodobnie) jest karta SD
        } catch (Exception e) {
            rootToExtSD = null;
            //W akcie desperacji probuję okreslic te sciezke jeszcze raz, tym razem na piechote... :
            String specialPath = null;
            if (new File("/ext_card/").exists()) {
                specialPath = "/ext_card/";
            } else if (new File("/mnt/sdcard/external_sd/").exists()) {
                specialPath = "/mnt/sdcard/external_sd/";
            } else if (new File("/storage/extSdCard/").exists()) {
                specialPath = "/storage/extSdCard/";
            } else if (new File("/mnt/extSdCard/").exists()) {
                specialPath = "/mnt/extSdCard/";
            } else if (new File("/mnt/sdcard/external_sd/").exists()) {
                specialPath = "/mnt/sdcard/external_sd/";
            } else if (new File("storage/sdcard1/").exists()) {
                specialPath = "storage/sdcard1/";
            }
            if (specialPath != null) {
                rootToExtSD = specialPath;
            }
        }

        //*********************************************
        //Jak nie ma SD zewn., to nie pokazuje klawisza na wklikniecie do niej (i blokuje klawisz na pamiec wewnetrzna - 2017.05.19 - zeby nie bylo niedomowien i problemow):
      //  if (rootToExtSD == null) {
            bExternal = (Button) findViewById(R.id.bExternal);
            //bExternal.setVisibility(View.INVISIBLE);
            //Informacja na bInternal i zamykanie interesu na klik (rozwiazanie tymczasowe - 2017.05.19):
            bInternal = (Button) findViewById(R.id.bInternal);
           // bInternal.setText("Aplikacja nie odnalazła zewnętrznej karty SD."+"\n"+"Bez karty SD wybrana opcja nie działa.");
/*
            bInternal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
*/
        //}
        //Przygotowanie do wywolania wybieracza katalogow:
      //  else
        {
            intent = new Intent(this, FileChooserActivity.class);
            intent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, true);  //tryb wyboru katalogu
        }
    } //koniec metody

    public void toast(String napis, boolean isLong) {
        int dlugosc = Toast.LENGTH_SHORT;
        if (isLong) { dlugosc = Toast.LENGTH_LONG;}
        Toast.makeText(getApplicationContext(),napis,dlugosc).show();
    }


    public void bInternalClick(View view) {
        String wewnetrznaSD = Environment.getExternalStorageDirectory().getPath();
        //wywolania browsera:
        // intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, "/storage/emulated/0"); //ostatni parametr powinien byc, bo problemy...
        intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, wewnetrznaSD); //ostatni parametr powinien byc, bo problemy...
        this.startActivityForResult(intent, 0);
    }

    public void bExternalClick(View view) {
        if (rootToExtSD != null) {
            //wywolanie browsera:
            intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, rootToExtSD);
            this.startActivityForResult(intent, 0);
        } else
            toast("Aplikacja nie odnalazła zewnętrznej karty SD.",true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            boolean fileCreated = false;
            String filePath = "";

            Bundle bundle = data.getExtras();
            if(bundle != null)
            {
                if(bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
                    fileCreated = true;
                    File folder = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                    String name = bundle.getString(FileChooserActivity.OUTPUT_NEW_FILE_NAME);
                    filePath = folder.getAbsolutePath() + "/" + name;
                } else {
                    fileCreated = false;
                    File file = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                    filePath = file.getAbsolutePath();
                }
            }

            ZmienneGlobalne.getInstance().WYBRANY_KATALOG = filePath;  //przekazanie wybranego katalogu (if any) do zm. globalnych
            ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG = true;

            finish();
/*
            String message = fileCreated? "File created" : "File opened";
            message += ": " + filePath;
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
            toast.show();
*/

        }
    } //koniec Metody



    public String[] getExternalStorageDirectoriesSkib() {
    /* returns external storage paths (directory of external memory card) as array of Strings
     (dziala rowniez na najnowszym sprzecie (05.2017)
     */

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = getExternalFilesDirs(null);

            for (File file : externalDirs) {
                String path = file.getPath().split("/Android")[0];

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                }
                else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(path);
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d(LOG_TAG, results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    Log.d(LOG_TAG, results.get(i)+" might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }  //koniec Metody


}
