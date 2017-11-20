package com.example.developer.mprzegladarka;

/*

 Pokazanie splash screena, ale tak, zeby nie zaslanial - zasluga THEME w manifescie
 Spalsh'a gasimy kliknieciem na niej lub poza nia)

 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class SplashKlasa extends Activity implements View.OnClickListener {

    public static final int REQUEST_CODE_WRACAM_Z_APKA_INFO = 222;
    CheckBox cbSound;
    CheckBox cbNapisy;
    CheckBox cbAlfabet;
    RadioButton rbAssets;
    RadioButton rbKatalog;
    TextView sciezka; //informacyjny teksci pokazujacy biezacy katalog (if any)

    Button bInfo,bStart1;


    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    /* Wywolywana po udzieleniu/odmowie zezwolenia na dostęp do karty (od API 23 i wyzej) */
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)  {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //reload my activity with permission granted or use the features what required the permission
                } else  {
                    //toast("Nie udzieliłeś zezwolenia na odczyt. Opcja 'obrazki z mojego katalogu' nie będzie działać. Możesz zainstalować aplikacje ponownie lub zmienić zezwolenie w Menadżerze aplikacji.");
                    wypiszOstrzezenie("Nie udzieliłeś zezwolenia na odczyt. Opcja 'obrazki z mojego katalogu' nie będzie działać. Możesz zainstalować aplikację ponownie lub zmienić zezwolenie w Menadżerze aplikacji.");
                    rbKatalog = (RadioButton) findViewById(R.id.rb_zKatalogu);
                    rbKatalog.setEnabled(false);
                }
            }
        }
    } //koniec Metody

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        /* ZEZWOLENIA NA KARTE _ WERSJA na MARSHMALLOW, jezeli dziala na starszej wersji, to ten kod wykona sie jako dummy */
        int jestZezwolenie = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (jestZezwolenie != PackageManager.PERMISSION_GRANTED) {
              ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
        }
        /* KONIEC **************** ZEZWOLENIA NA KARTE _ WERSJA na MARSHMALLOW */

        setContentView(R.layout.splash);
        czyscDlaKrzyska();

        cbSound     = (CheckBox) findViewById(R.id.cb_sound);
        cbNapisy    = (CheckBox) findViewById(R.id.cb_napisy);
        cbAlfabet   = (CheckBox) findViewById(R.id.cb_alfabet);
        sciezka     = (TextView) findViewById(R.id.sciezkaKatalog);

        rbAssets = (RadioButton) findViewById(R.id.rb_zAssets);
        rbAssets.setOnClickListener(this);

        rbKatalog = (RadioButton) findViewById(R.id.rb_zKatalogu);
        rbKatalog.setOnClickListener(this);

/*
        //Przy b. malych rozdzielczosciach i ekranie poziomym "zdejmujemy' obrazek, bo reszta sie nie miesci...:
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenDensity = metrics.densityDpi;
        if (screenDensity < 170) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LinearLayout splashEkran = (LinearLayout) findViewById(R.id.SplashEkran);
                ImageView obrazek = (ImageView) findViewById(R.id.imageView1);
                if (obrazek != null)
                    splashEkran.removeView(obrazek);
                //dodatkowo pomniejszamy teksty, bo nadal sie nie miesci:
                rbAssets.setTextSize(rbAssets.getTextSize() - 5);
                rbKatalog.setTextSize(rbKatalog.getTextSize() - 5);

                cbSound.setTextSize(cbSound.getTextSize() - 10);
                cbNapisy.setTextSize(cbNapisy.getTextSize() - 10);
                cbAlfabet.setTextSize(cbAlfabet.getTextSize() - 10);

                TextView autyzmsoftpl = (TextView) findViewById(R.id.autyzmsoftpl);
                autyzmsoftpl.setTextSize(autyzmsoftpl.getTextSize() - 10);

                TextView ustaw = (TextView) findViewById(R.id.ustawienia_text);
                ustaw.setTextSize(ustaw.getTextSize() - 10);
            }
        }
*/
        //Przy malej gestosci i Landscape usuwam obrazek, zeby reszta sie zmiescila:
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenDensity = metrics.densityDpi;
        if (screenDensity < 210) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ImageView obrazek = (ImageView) findViewById(R.id.imageView1);
                if (obrazek != null) //bo moze go nie byc przy jakims konkretnym layoucie
                    obrazek.setVisibility(View.GONE);
            }
        }

        bInfo   = (Button) findViewById(R.id.bINFO);
        bStart1 = (Button) findViewById(R.id.bStart1);

        //Zeby klikniecie na SplaszScreenie zamykalo go :
        //View Obraz = findViewById(R.id.SplashEkran);
        //Obraz.setOnClickListener(this);

        //Pobranie zapisanych ustawien, (if any) gdy startujemy aplikacje :
        if (savedInstanceState == null) { //ten warunek oznacza, ze to nie obrot, tylko startujemy odpoczatku
            //Zapisane ustawienia wrzucane sa do zm. glob., by je potem pobrac ze zm. glob. na onResume:
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); //na zapisanie ustawien na next. sesję
            //Rozpoczynamy aplikacje od wyswietleni 'startowego' zestawu (korzystam z ewentualnych ustawien z ostatniej sesji) :
            ZmienneGlobalne.getInstance().zGlosem  =  sharedPreferences.getBoolean("zGlosem",  true); //2-gi parametr na wypadek, gdyby w SharedPref. nic jeszcze nie bylo
            ZmienneGlobalne.getInstance().zNapisami = sharedPreferences.getBoolean("zNapisami",true);
            ZmienneGlobalne.getInstance().ALFABET = sharedPreferences.getBoolean("alfabet",true);

            ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG = sharedPreferences.getBoolean("zKatalogu", false);
            ZmienneGlobalne.getInstance().WYBRANY_KATALOG = sharedPreferences.getString("katalog","");
            ZmienneGlobalne.getInstance().ALFABET = sharedPreferences.getBoolean("alfabet",true);
            //Gdyby pomiedzy uruchomieniami zlikwidowano wybrany katalog, przelaczamy sie na zrodlo z zasobow aplikacji:
            if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
                String katalog = ZmienneGlobalne.getInstance().WYBRANY_KATALOG;
                File file = new File(katalog);
                if (!file.exists()) {
                    ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG = false;
                }
                //gdyby nie zlikwidowano katalogu, ale tylko 'wycieto' obrazki - przelaczenie na Zasoby applikacji:
                else {
                    if (policzObrazki(katalog) == 0) {
                        ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG = false;
                    }
                }
            }
        }
    }  //onCreate

    private int policzObrazki(String strKatalog) {
    /* ******************************************************** */
    /* Liczy obrazki (=pliki .jpg .bmp .png) w zadanym katalogu */
    /* zwraca po prostu rozmiar kolekcji                        */
    /* ******************************************************** */

        return MainActivity.findObrazki(new File(strKatalog)).size();

    /*
        int licznik = 0;
        File fileKatalog = new File(strKatalog);

            File[] files = fileKatalog.listFiles();
            if (files == null) return 0;  //robie tak, bo na niektorych emulatorach wyklada sie w petli for ...
            for (File plik : files) {
                if ((plik.getName().endsWith(".jpg")) || (plik.getName().endsWith(".bmp")) || (plik.getName().endsWith(".png"))) {
                    licznik++;
                }
            }
            return licznik;
        */

    } //policzObrazki


    @Override
    protected void onResume() {
    /* ******************************************************************************************/
    /* Na ekranie (splashScreenie) pokazywane sa aktualne ustawienia.                           */
    /* Wywolywana (nie bezposrednio, ale jako skutek) na long touch na obrazku - wtedy          */
    /* przywolywana jest SplashKlasa z pokazanymi ustawieniami - patrz MAinActivity.onLOngClick */
    /* Wywolywana rowniez przy starcie aplikacji(!)                                             */
    /* **************************************************************************************** */
        super.onResume();

        cbSound.setChecked(ZmienneGlobalne.getInstance().zGlosem);
        cbNapisy.setChecked(ZmienneGlobalne.getInstance().zNapisami);
        cbAlfabet.setChecked(ZmienneGlobalne.getInstance().ALFABET);

        //Dostosowanie SplashEkranu (splash.xml) w przypadku LANDSCAPE (poszerzenie):
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Pobieram wymiary ekranu
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int szer = displaymetrics.widthPixels;
            View layoutSki = findViewById(R.id.SplashEkran);
            layoutSki.getLayoutParams().width = (int) (szer*0.90f);
            layoutSki.requestLayout(); //teraz nastepuje zaaplikowanie zmian
        };

        if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
            String strKatalog = ZmienneGlobalne.getInstance().WYBRANY_KATALOG;
            int liczbaObrazkow = policzObrazki(strKatalog);
            if (liczbaObrazkow>0) {
                if (!ZmienneGlobalne.getInstance().PELNA_WERSJA) {
                    if (liczbaObrazkow>5) {  //werja Demo, a wybrano katalog z wiecej niz 5 obrazkami
                        ostrzegajPowyzej5();
                        //przywrócenie wyboru 'domyslnego' - z zasobów aplikacji:
                        onClick(rbAssets);
                        rbAssets.setChecked(true);
                    }
                    else { //wersja Demo, wybór OK
                        toast("Liczba obrazków: " + liczbaObrazkow);
                        rbKatalog.setChecked(true);
                        sciezka.setText(strKatalog);
                    }
                }
                else {  //wersja Pełna, wybór OK
                    toast("Liczba obrazków: " + liczbaObrazkow);
                    rbKatalog.setChecked(true);
                    sciezka.setText(strKatalog);
                }
            }
            else { //nie ma obrazkow w wybranym katalogu (dot. werski Pelnej i Demo)
                ostrzegajBrakObrazkow();
                //przywrócenie wyboru 'domyslnego' - z zasobów aplikacji:
                onClick(rbAssets);
                rbAssets.setChecked(true);
            }
        }
        else { //wybrano zasoby aplikacji
            rbAssets.setChecked(true);
            sciezka.setText("");  //zeby nie śmiecił na ekranie
            rbAssets.setChecked(true);
        }

    } //onResume - koniec

    private void ostrzegajBrakObrazkow(){
    /* **************************************************************** */
    /* Wyswietlany, gdy user wybierze katalog nie zawierajacy obrazkow. */
    /* **************************************************************** */
        wypiszOstrzezenie("Brak obrazków w wybranym katalogu.\nZostanie zastosowany wybór\nz zasobów aplikacji.");
    }

    private void ostrzegajPowyzej5() {
    /* ************************************************************************************ */
    /* Wyswietlany, gdy user wybierze katalog z wiecej niz 5 obrazkami, a wersja jest Demo. */
    /* ************************************************************************************ */
        wypiszOstrzezenie("Uwaga - używasz wersji Demo.\nWybrano katalog zawierający więcej niż 5 obrazków.\nZostanie przywrócony wybór\nz zasobów aplikacji.");
    }


    private void wypiszOstrzezenie(String tekscik) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashKlasa.this, R.style.MyDialogTheme);
        builder1.setMessage(tekscik);
        builder1.setCancelable(true);

        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


       /* builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });*/

        AlertDialog alert11 = builder1.create();
        alert11.show();
    } //ostrzegajBrakObrazkow

    @Override
    protected void onPause() {
        //Sprawdzam checkbox'y i przekazuje ich stan do zmiennych globalnych,
        //bo obiekt zwiazany ze SplashKlasa przestanie za chwile istniec ...
        super.onPause();
        ZmienneGlobalne.getInstance().zGlosem   = cbSound.isChecked();
        ZmienneGlobalne.getInstance().zNapisami = cbNapisy.isChecked();
        ZmienneGlobalne.getInstance().ALFABET   = cbAlfabet.isChecked();

        //zeby zmiana wyswietlania nazwy On/Off byla widoczna natychmiast:
        if (!cbNapisy.isChecked()) {
            MainActivity.nazwaTV.setVisibility(View.INVISIBLE);
        }
        else {
            MainActivity.nazwaTV.setVisibility(View.VISIBLE);
        }
    } //onPause


    public void onClick(View arg0) {
    /* ********************************************************************************************** */
    /* Obsluga klikniec na radio buttony 'Obrazki z zasobow aplikacji', 'Obrazki z wlasnego katalogu' */
    /* ********************************************************************************************** */

        if (arg0==rbAssets) {
            sciezka.setText(""); //kosmetyka - znika z ekranu
            //jesli kliknieto na "z zasobow aplikacji", to przełączam się na to nowe źródło:
            if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG==true) {
                ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG = false;
                ZmienneGlobalne.getInstance().ZMIENIONO_ZRODLO     = true;
            }

            //policzenie obrazkow w zasobach aplikacji (zeby uswiadomic usera...):
            AssetManager mgr = getAssets();
            try {
                toast("Liczba obrazków: "+Integer.toString(mgr.list(MainActivity.katalog).length));
            } catch (Exception io) {};


            return;
        }
        if (arg0==rbKatalog) {
            /*Wywolanie activity do wyboru miedzy karta zewnetrzna SD, a pamiecia urzadzenia:*/

            if (ZmienneGlobalne.getInstance().PELNA_WERSJA) {
                Intent intent = new Intent(this, InternalExternalKlasa.class);
                this.startActivity(intent);
            }
            //Wersja demo::
            else {
                Intent intent = new Intent(this, WersjaDemoOstrzez.class);
                this.startActivity(intent); //w srodku zostanie wywolana InternalExternalKlasa
            }

            /*if (!ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) { //wybieralismy, ale byla rezygnacja z wybierania katalogu
                rbAssets.setChecked(true);
            }*/

            return;
        }
    }


    public void bInfoStartClick(View arg0) {

        if (arg0 == bInfo) {
            Intent intent = new Intent(this, ApkaInfo.class);
            this.startActivityForResult(intent, REQUEST_CODE_WRACAM_Z_APKA_INFO);
        }
        if (arg0 == bStart1) { //zamkniecie SplashScreena (spowoduje wejscie do MainActivity)
            finish();
        }
        return;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_WRACAM_Z_APKA_INFO) {
            //toast("Wrocilem z apkaInfo");
            if (resultCode == Activity.RESULT_OK) { //to musi byc na wypadek powrotu przez klawisz Back (zeby kod ponizej sie nie wykonal, bo error..)
                String message = data.getStringExtra("MESSAGE");
                if (message.equals("KL_START"))
                    this.finish();
            }
        }
        //toast(Integer.toString(resultCode));
    } //koniec onActivityResult


    public void toast(String napis) {
        Toast.makeText(getApplicationContext(),napis,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
	/* Zapisanie ustawienia w SharedPreferences na przyszła sesję */
        super.onDestroy();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); //na zapisanie ustawien na next. sesję
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("zGlosem", ZmienneGlobalne.getInstance().zGlosem);
        edit.putBoolean("zNapisami", ZmienneGlobalne.getInstance().zNapisami);
        edit.putBoolean("zKatalogu", ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG);
        edit.putString("katalog", ZmienneGlobalne.getInstance().WYBRANY_KATALOG);
        edit.putBoolean("alfabet", ZmienneGlobalne.getInstance().ALFABET);
        edit.apply();
    } //onDestroy

    public void czyscDlaKrzyska() {
    /* Ukrywanie obrazkow i 'śladów' do strony www - przed przekazanie do Krzyska; Potem usunac */
        if (ZmienneGlobalne.getInstance().DLA_KRZYSKA) {
            ImageView obrazek = (ImageView) findViewById(R.id.imageView1);
            if (obrazek != null) obrazek.setVisibility(View.INVISIBLE);
            TextView link = (TextView) findViewById(R.id.autyzmsoftpl); //bo na niektorych konfiguracjach nie pokazuje tego linku
            if (link != null) link.setVisibility(View.INVISIBLE);
        }
    } //koniec Metody

}
