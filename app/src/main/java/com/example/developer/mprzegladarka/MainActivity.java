package com.example.developer.mprzegladarka;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements View.OnLongClickListener {


    //Wymiary 'inicjalne' elementow (nadawane przy uruchomianiu):
    public static int nazwaTVHeightInitialPortrait;
    public static int nazwaTVHeightInitialLandscape;

    public static int bNazadHeightInitialPortrait;

    public static int bNazadWidthInitialPortrait;
    public static int bNazadWidthInitialLanscape;

    public static int imageViewHeightInitialPortrait;
    public static int imageViewHeightInitialLandscape;


    public static final String CURR_IMAGE =   "currImage";      //na odzyskanie stanu po obrocie urzadzenia
    public static final String CURR_LICZNIK = "licznikWykonan"; //j.w.

    private KombinacjaOpcji staraKombinacja, nowaKombinacja;

    public static final String katalog  = "obrazki";        //w tym katalogu w Assets trzymane beda obrazki
    final String nagrania = "nagrania";       //w tym katalogu w Assets trzymane beda nagrania
    String listaObrazkowAssets[] = null;      //lista obrazkow z Assets/obrazki - dla werski demo )i nie tylko...)
    //File listaObrazkowSD[] = null;            //lista plikow do pokazania z kat. obrazki karty SD
    ArrayList<File> myObrazkiSD;    //lista obrazkow w SD (alternatywa dla zmiennej. Files[] listaObrazkowSD)
    String listaNagranAssets[] = null;        //j.w. - dźwieki mp3/ogg/inne nazywajace obrazki
    String sciezka_do_pliku_dzwiekowego;      //zmienna 'globalna', pookazujaca plik dzwiekowy do odegrania (Assets i SD)

    File dirObrazkiNaSD;  //uwaga (to dla Lenovo) - HARDWARE Dependent !!!!!! - co z innym sprzetem???

    boolean nieGraj;         //przelacznik(semafar) : grac/nie grac - jesli start apk. lub po obrocie, to ma nie grac slowa (bo glupio..)

    //licznik wykonan onResume - na mechanizm sensownego odgrywania slowa po uruchomieniu apki:
    int licznikWykonan;    //zeby po uruchomieniu apki. po 'zdjeciu' SplashEkranu odegralo sie slowo (bo przy starcie ma sie nie odgrywac -> patrz nieGraj)

    static TextView nazwaTV;   //na nazwe pod wyswietlanym obrazkiem (bez rozszerzenia .jpg), inicjacja referencji; public static - zeby mozna bylo dobrac sie w SplashKlasa - natychmiastowe On/Off vivibility
    ImageView imageView;       //z Layoutu, na obrazek
    Button bNazad;             //z Layoutu na klawisz
    Button bForward;           //z Layoutu, na klawisz

    private int currImage = 0;          //indeks na obrazek
    private int prevImage;              //poprzednio wyswietlany obrazek (istotne w wybieraniu losowym,do cofania)
    private int powrotnyImage;         //tryb losowy, byl klawisz bNazad i neciskamy bForward - zeby w tej sytuacji pokazac to, co trzeba

    static MediaPlayer mp = null;

    //Szerokosc i Wysokosc aktualnego Urządzenia (wyliczana w onCreate):
    static int width;
    static int height;

    protected Intent splashKlasa;

    private   long mLastClickTime;         //kiedy (czas) kliknalem ostatni raz na obrazku - do mechanizmu zabezpieczenia przez klikaniem jak opętany...
    protected long mLastClickTimeWpieriod; //j.w. dla klawisza Wpieriod
    protected long mLastClickTimeNazad;    //j.w. dla klawisza Nazad
    protected boolean bylObrot;             //czy 'wstajemy' po obrocie ekranu


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /* skladujemy zmienne potrzebne do odzyskania po obrocie ekranu */
        super.onSaveInstanceState(outState);
        outState.putInt(CURR_IMAGE,currImage);
        outState.putInt(CURR_LICZNIK,licznikWykonan);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Ustawienie widoku w zaleznosci "wypoziomowania" urządzenia:
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_landskejp);
        } else {
            setContentView(R.layout.activity_main);
        }

        imageView = (ImageView) findViewById(R.id.imageView);
        nazwaTV   = (TextView) findViewById(R.id.nazwaTV); //na nazwe pod wyswietlanym obrazkiem, inicjacja referencji
        bNazad    = (Button) findViewById(R.id.btnNazad);
        bForward  = (Button) findViewById(R.id.btnForward);


        //Modyfikuję wymiary elementow graficznych, tak aby pasowal do roznych urzadzen:
        //Pobieram wymiary ekranu
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        width = displaymetrics.widthPixels;
        height= displaymetrics.heightPixels;

        dostosujDoUrzadzen(width, height);

        setImageWpieriodListener();
        setImageNazadListener();

        //Na przechowywanie ustawień:
        nowaKombinacja  = new KombinacjaOpcji();
        staraKombinacja = new KombinacjaOpcji();
        nowaKombinacja.pobierzZeZmiennychGlobalnych();
        staraKombinacja.pobierzZeZmiennychGlobalnych();

        splashKlasa = new Intent("com.example.developer.mprzegladarka.SplashKlasa");

        //Pokazanie splash screena (ale tylko wtedy, kiedy startujemy od "zera"; kiedy restart spowodowany obrotem - nie robic nic:
        if (savedInstanceState == null) {
            licznikWykonan = 0;     //startujemy licznik wykonan dla onResume
            startActivity(splashKlasa);
            currImage = 0; //'startowy' obrazek; w przyszlosci Random(0..listaObrazkow.size()-1/Assets.licznosc()-1)
        } else { //byl obrot ekranu
            //odzyskanie (po obrocie ekranu) aktualnego obrazka i licznikaWykonan onResume:
            currImage      = savedInstanceState.getInt(CURR_IMAGE);
            licznikWykonan = savedInstanceState.getInt(CURR_LICZNIK);
            nieGraj = true;  //dajemy znac, zeby nie gral slowa, bo jestesmy po obrocie ekranu(i glupio...)
            bylObrot = true; //dajemy znac, ze 'wstajemy' po obrocie ekranu
            //setCurrentImage();
        }

        //Long Touch na obrazku - przywolanie menu:
        imageView.setOnLongClickListener(this);


        nazwaTV.setOnClickListener(new View.OnClickListener() {
           /* ********************************************* */
           /*Powiekszenie/pomniejszenie nazwy pod obrazkiem */
           /* ********************************************* */
            @Override
            public void onClick(View arg0) {
                if(licznikKlikow==0) { //wchodzimy 1-szy raz na danym obrazku
                    TextView pole = (TextView) findViewById(R.id.nazwaTV);
                    nazwaGlobStr = pole.getText().toString();
                }

                licznikKlikow++; //zeby wiedziec, ktory raz (przy zadanym obrazku) wchodzimy (=klikamy na napisie) do procedury

                if (licznikKlikow%2 == 0) { //parzyste klikniecie na nazwie oznacza chec jej pomniejszenia
                    TextView pole = (TextView) findViewById(R.id.nazwaTV);
                    pole.setText(nazwaGlobStr);  //odzyskujemy "starą" (małe litery lub imię) nazwę obrazka
                }
                else {                      //nieparzyste klikniecie na nazwie oznacza chec jej powiekszenia
                    String nazwaRobStr = nazwaGlobStr.toUpperCase(Locale.getDefault());
                    TextView pole = (TextView) findViewById(R.id.nazwaTV);
                    pole.setText(nazwaRobStr);
                }
            }
        });

    } //koniec metody onCreate


    private void setImageNazadListener() {

        final Button bnazad = (Button) findViewById(R.id.btnNazad);
        bnazad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //najpierw zabezpieczenie przed wscieklym klikaniem:
                //if (SystemClock.elapsedRealtime() - mLastClickTimeNazad < 1200){
                //    return;
                //}
                mLastClickTimeNazad = SystemClock.elapsedRealtime(); //rejestrujemy czas klikniecia

                dajPrevObrazek(); //ustawia currImage na poprzedni;
                setCurrentImage();  //wyswietlenie obrazka

            }
        });

    } //koniec metody setImageNazadListener()



    public static ArrayList<File> findObrazki(File katalog) {
    /* ******************************************************************************************************************* */
    /* Zwraca liste obrazkow (plikow graficznych .jpg .bmp .png) z katalogu katalog - uzywana tylko dla przypadku SD karty */
    /* ******************************************************************************************************************* */
        ArrayList<File> al = new ArrayList<File>(); //al znaczy "Array List"
        File[] files = katalog.listFiles(); //w files WSZYSTKIE pliki z katalogu (rowniez nieporządane)
        if (files != null) { //lepiej sprawdzic, bo wali sie w petli for na niektorych emulatorach...
            for (File singleFile : files) {
                if ((singleFile.getName().toUpperCase().endsWith(".JPG")) || (singleFile.getName().toUpperCase().endsWith(".PNG")) || (singleFile.getName().toUpperCase().endsWith(".BMP"))) {
                    al.add(singleFile);
                }
            }
        }
        return al;
    }  //koniec metody findObrazki


    /* Uwaga - wylaczam. Dziala tylko jezeli w manifest.xml jest odpowiedni wpis przy actiwities
     Jesli jest wlaczone, to problemy z layoutami - nie przelaczaja sie automatycznie Portrait <-> Landscape

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        dostosujKlawiszeImageNazwaTV(width, height);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }
    */


    private void dostosujKlawiszeImageNazwaTV(int width, int height) {
    /* Uzywana przy zmianie konfiguracji. Zmienia wysokosci bNazad, imageView, nazwaTV */

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (ZmienneGlobalne.getInstance().zNapisami == false) {
                nazwaTV.getLayoutParams().height = 0;
                bNazad.getLayoutParams().height += nazwaTVHeightInitialPortrait;
            }
            else {
                imageView.getLayoutParams().height = imageViewHeightInitialPortrait;
                nazwaTV.getLayoutParams().height   = nazwaTVHeightInitialPortrait;
                bNazad.getLayoutParams().height    = bNazadHeightInitialPortrait;
            }
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (ZmienneGlobalne.getInstance().zNapisami == false) {
                nazwaTV.getLayoutParams().height = 0;
                imageView.getLayoutParams().height += nazwaTVHeightInitialLandscape;
            }
            else {
                imageView.getLayoutParams().height = imageViewHeightInitialLandscape;
                nazwaTV.getLayoutParams().height   = nazwaTVHeightInitialLandscape;
            }
        }

        nazwaTV.requestLayout();
        bNazad.requestLayout();
        imageView.requestLayout();

    } //koniec metody dostosujKlawiszeiNazweTV()



    private void dostosujDoUrzadzen(int width, int height) {
/*
        Ustawiam wymiary poszczegolnych elementow w stosunku do calosci ekranu (mamy wtedy uniezaleznienie od urządzeń)
        Ustawiam proporcjonalnie do ekranu; podstawowy 'sznyt' nadawany jest jednak w Layoucie:
        Parametry width, height - rozmiary Urządzenia
*/
        nazwaTVHeightInitialPortrait    = (int) (height * (0.15));
        nazwaTVHeightInitialLandscape   = (int) (height * (0.30));//(0.30));

        bNazadWidthInitialPortrait      = (int) (width/2.0);
        bNazadWidthInitialLanscape      = (int) (width/5.0);

        imageViewHeightInitialPortrait  = (int) (height * (0.50));
        imageViewHeightInitialLandscape = (int) (height * (0.70));//(0.65));

        //klawisze:
        Button bNazad = (Button) findViewById(R.id.btnNazad);
        Button bForward = (Button) findViewById(R.id.btnForward);
        //nazwa pod obrazkiem:
        nazwaTV.getLayoutParams().height = nazwaTVHeightInitialLandscape;

        //PROPORCJE ELEMENTOW NA EKRANIE GLOWNYM:
        //ustawienie proporcji; powoduje to jednoczesnie "sztywne" trzymanie sie ekranu przez elementy (nie 'plywaja' w zaleznosci od wielkosci np tekstu):
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            imageView.getLayoutParams().height = imageViewHeightInitialPortrait;
            bNazad.getLayoutParams().width     = bNazadWidthInitialPortrait; //polowa szerokosci ekranu, bForward - dostanie z automatu na pdst. bNazad
            nazwaTV.getLayoutParams().height   = nazwaTVHeightInitialPortrait;
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageView.getLayoutParams().height = imageViewHeightInitialLandscape;
            nazwaTV.getLayoutParams().height   = nazwaTVHeightInitialLandscape;
        }

        //Ustawianie strzalek na klawiszach:
        //ustawiam paddingLeft_i_Right, zeby strzalka wypadla mniej wiecej na srodku (trudno z xml...):
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int szerButtona = width/2;
            bForward.setPadding((int)(szerButtona/2.5)-10 ,1,1,1);
            bNazad.setPadding(1,1,(int)(szerButtona/2.5),1);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int szerButtona = width/5;
            bForward.setPadding((int)(szerButtona/4.0) ,1,1,1);
            bNazad.setPadding(1,1,(int)(szerButtona/4.0) ,1);
        }

        imageView.requestLayout(); //teraz nastepuje zaaplikowanie zmian
        nazwaTV.requestLayout();
        bNazad.requestLayout();
        bForward.requestLayout();

        //sledzenie - wyrzucic ski ski :
/*

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenDensity = metrics.densityDpi;
        bForward.setTextSize(20);
        bForward.setText(Integer.toString(width)+" x "+Integer.toString(height)+" : "+Integer.toString(screenDensity)+" dpi");

*/

    } //konie metody dostosujDoUrzadzen



    /*
    private void initializeImageSwitcher() {
        final ImageView imageView = (ImageView) findViewById(imageView);
        imageView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(MainActivity.this);
                return imageView;
            }
        });

//        imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
//        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right));

          imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
          imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_out));
    }
*/
    private void setImageWpieriodListener() {

        final Button rotatebutton = (Button) findViewById(R.id.btnForward);
        rotatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //najpierw zabezpieczenie przed wscieklym klikaniem:
                //if (SystemClock.elapsedRealtime() - mLastClickTimeWpieriod < 1200){
                //    return;
                //}
                mLastClickTimeWpieriod = SystemClock.elapsedRealtime(); //rejestrujemy czas klikniecia
                dajNextObrazek();
                setCurrentImage();  //wyswietlenie obrazka
            }
        });
    } //koniec metody setImageWpieriodListener


    private void dajNextObrazek() {
        //"Przywrocenie" klawisza bNazad (bo moglem go "znieczulic":
        if (!bNazad.isEnabled()) {
            bNazad.setEnabled(true);
            bNazad.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.arrow_left,0);
            //tryb losowy, byl klawisz bNazad, a potem bForward - zeby w takiej syt. pokazac to, co trzeba
            if (!ZmienneGlobalne.getInstance().ALFABET) {
                currImage = powrotnyImage;
                return;
            }
        }
        //Kolejnosc alfabetyczna:
        if (ZmienneGlobalne.getInstance().ALFABET) {
            currImage++;
            if (!ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
                if (currImage == listaObrazkowAssets.length) {
                    currImage = 0;
                }
            }
            if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
                if (currImage == myObrazkiSD.size()) {
                    currImage = 0;
                }
            }
        }
        //Kolejnosc losowa:
        else {
            currImage = dajLosowyNumerObrazka();
        }
        return;
    } //koniec metody dajNextObrazek()


    private void dajPrevObrazek() {
        //Kolejnosc alfabetyczna:
        if (ZmienneGlobalne.getInstance().ALFABET) {
            currImage--;
            if (!ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
                if (currImage == -1) {
                    currImage = listaObrazkowAssets.length - 1;
                }
            }
            if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
                if (currImage == -1) {
                    currImage = myObrazkiSD.size() - 1;
                }
            }
        }
        //Kolejnosc losowa:
        else {
            powrotnyImage = currImage;
            currImage = prevImage;
            //Klawisz staje sie nieaktywny i bez strzalki (znieczulony):
            bNazad.setEnabled(false);
            bNazad.setCompoundDrawables(null,null,null,null);
        }
        return;
    } //koniec metody dajNextObrazek()


    private int dajLosowyNumerObrazka() {
        int rob;
        int rozmiar_tab;
        prevImage = currImage; //do cofania sie, gdy tryb pracy losowy
        if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG)
             rozmiar_tab = myObrazkiSD.size();
        else
            rozmiar_tab = listaNagranAssets.length;
        //Generujemy losowy numer, ale tak, zeby nie wypadl ten sam:
        if (rozmiar_tab !=1 ) { //przy tylko jednym obrazku kod ponizej jest petla nieskonczona, więc if
            do {
                rob = (int) (Math.random() * rozmiar_tab);
            } while (rob == currImage);
        }
        else
            rob = 0; //bo 0-to jest de facto numer obrazka

        return rob;
    } //koniec metody dajLosowyNumerObrazka()


    /* ***************************************** */
    /* Na potrzeby dialogu - directory chooser : */
    /* ***************************************** */
    static final int CUSTOM_DIALOG_ID = 0;
    Button buttonOpenDialog;
    Button buttonUp;
    TextView textFolder;
    ListView dialog_ListView;
    private List<String> fileList = new ArrayList<String>();

/*

    private void setInitialImage() {
        currImage = 0;  //w przyszlosci random 0..listaObrazkow.size()-1/Assets.licznosc()-1
        setCurrentImage();
    }
*/

    private void setCurrentImage() {
    /* ************************************ */
    /* metoda wyswietlajaca biezacy obrazek */
    /* ************************************ */

        //Ustawienie zm. globalnej zapewniajacej prawidlowe dzialanie mechanizmu powieksania/pomniejszania napisow (toUpperCase) na  klik na napisie:
        licznikKlikow = 0;
        //koniec ustawienia zm. glob. na klik na napisie

        //--------------------------------//
        // Wyswietlenie biezacego obrazka //
        //--------------------------------//
        //final ImageSwitcher imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);

        String imageName = "";
        final String nazwaObrazka;
        nazwaTV.setText(""); //nazwa pod obrazkiem - najpierw czyscimy stara nazwe

        //Wyswietlanie obrazka :
        try {
            if (!ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) { //pobranie z Assets
                nazwaObrazka = listaObrazkowAssets[currImage]; //z rozszerzeniem, np. cukierek.jpg ; jeszcze niepozbywam sie tego rozszerzenia, bo bedzie potrzebne
                InputStream stream = getAssets().open(katalog + "/" + nazwaObrazka);
                Drawable drawable = Drawable.createFromStream(stream, null);
                imageView.setImageDrawable(drawable);
            } else {  //pobranie z directory
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                //Bitmap bm = BitmapFactory.decodeFile(listaObrazkowSD[currImage].getAbsolutePath(), options); rozwiazanie alternatywne do ponizej:

                String robAbsolutePath = myObrazkiSD.get(currImage).getAbsolutePath();

                //toast("licznosc katalogu: "+Integer.toString(myObrazkiSD.size()));

                Bitmap bm = BitmapFactory.decodeFile(robAbsolutePath, options);  //[currImage].getAbsolutePath(), options);

                imageView.setImageBitmap(bm);
                nazwaObrazka = myObrazkiSD.get(currImage).getName();  //nazwa z rozszerzeniem, np. cukierek.jpg    //listaObrazkowSD[currImage].getName();
            }

            //Wypisanie nazwy pod obrazkiem:
            int dlg = nazwaObrazka.length();  //wyciecie rozszerzenia:
            String robStr = (String) nazwaObrazka.subSequence(0, dlg - 4); // podobno sprawdza zawijanie linii -> +Integer.toString(nazwaTV.getLineCount())); //podpis pod obrazkiem (z wycieciem rozszerzenia)
            //gdyby nazwa konczyla sie na '1'-'9' - tniemy dalej... :
            if (robStr.matches("^.*\\d$")) {  //regEx - cz string
                // konczy sie cyfra
                robStr = (String) robStr.subSequence(0, dlg - 5);
            }
            final String robStrFinal = robStr; //final - zeby przekazac nizej

            int opozniacz1 = 600;  //milisekundy
                Handler handler1 = new Handler();
                handler1.postDelayed(new Runnable() {
                    public void run() {
                        //Wypisanie nazwy (z lekkim opoznieniem):
                        nazwaTV.setText(robStrFinal);
                    }
                }, opozniacz1);


            //ODEGRANIE DŹWIĘKU:
            if (!ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
                //odeggranie z Assets (tam ogg):
                sciezka_do_pliku_dzwiekowego = nagrania + "/" + robStrFinal + ".ogg"; //ustawienie zmiennej glob. - glob., bo bedzie potrzebna rowniez w onCliknaImage...
                odegrajZAssets(sciezka_do_pliku_dzwiekowego,300);
            };
            if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
                //odegranie z SD (na razie nie zajmujemy sie rozszerzeniem=typ pliku dzwiekowego jest (prawie) dowolny):
                sciezka_do_pliku_dzwiekowego = dirObrazkiNaSD+"/"+robStrFinal; //tutaj przekazujemy rdzen nazwy, bez rozszerzenia, bo mogą być różne (.mp3, ogg, .wav...)
                odegrajZkartySD(sciezka_do_pliku_dzwiekowego,300);
            }


        } catch (Exception e) {
            Log.e("4321", e.getMessage());
            Toast.makeText(this, "Problem z wyswietleniem obrazka...", Toast.LENGTH_SHORT).show();
        }

    } // koniec metody setCurrentImage



    private void toast(String napis) {
    Toast.makeText(getApplicationContext(),napis,Toast.LENGTH_SHORT).show();
}

public void odegrajZkartySD(final String sciezka_do_pliku_parametr, int delay_milisek) {
/* ************************************** */
/* Odegranie pliku dzwiekowego z karty SD */
/* ************************************** */

    //Jezeli w ustawieniech jest, zeby nie grac - to wychodzimy:
    if (ZmienneGlobalne.getInstance().zGlosem != true) {
        return;
    }


//    if (ZmienneGlobalne.getInstance().pierwszeWejscie) {
//        return;
//    }

    //zeby nie gral po obrocie i przy starcie(?) apki:
    if (nieGraj) {
        nieGraj = false;
        return;
    }

    //Na pdst. parametru metody szukam odpowiedniego pliku do odegrania:
    //(typuję, jak moglby sie nazywac plik i sprawdzam, czy istbieje. jezeli istnieje - OK, wychodze ze sprawdzania majac wytypowaną nazwe pliku)
    String pliczek;
    pliczek=sciezka_do_pliku_parametr+".m4a";
    File file = new File(pliczek);
    if (!file.exists()) {
        pliczek = sciezka_do_pliku_parametr+".mp3";
        file = new File(pliczek);
        if (!file.exists()) {
            pliczek = sciezka_do_pliku_parametr+".ogg";
            file = new File(pliczek);
            if (!file.exists()) {
                pliczek = sciezka_do_pliku_parametr+".wav";
                file = new File(pliczek);
                if (!file.exists()) {
                    pliczek = sciezka_do_pliku_parametr+".amr";
                    file = new File(pliczek);
                    if (!file.exists()) {
                        pliczek=""; //to trzeba zrobic, zeby 'gracefully wyjsc z metody (na Android 4.4 sie wali, jesli odgrywa plik nie istniejacy...)
                        //dalej nie sprawdzam/nie typuję... (na razie) (.wma nie sa odtwarzane na Androidzie)
                    }
                }
            }
        }
    }
    //Odegranie znalezionego (if any) poliku:
    if (pliczek.equals("")) {
        return;  //bo Android 4.2 wali sie, kiedy próbujemy odegrac plik nie istniejący
    }
    Handler handler = new Handler();
    final String finalPliczek = pliczek; //klasa wewnetrzna ponizej - trzeba "kombinowac"...
    handler.postDelayed(new Runnable() {
        public void run() {
            try {
                Uri u = Uri.parse(finalPliczek); //parse(file.getAbsolutePath());
                mp = MediaPlayer.create(getApplicationContext(), u);
                mp.start();
            } catch (Exception e) {
                //toast("Nie udalo się odegrać pliku z podanego katalogu...");
                Log.e("4321", e.getMessage()); //"wytłumiam" komunikat
            }
            finally {
                //Trzeba koniecznie zakonczyc Playera, bo inaczej nie slychac dzwieku:
                //mozna tak:
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
                //albo mozna tak:
                //mPlayer.setOnCompletionListener(getApplicationContext()); ,
                //a dalej w kodzie klasy zdefiniowac tego listenera, czyli public void onCompletion(MediaPlayer xx) {...}
            }
        }
    }, delay_milisek);
} //koniec metody odegrajZkartySD


public void odegrajZAssets(final String sciezka_do_pliku_parametr, int delay_milisek) {
/* ***************************************************************** */
/* Odegranie dzwieku umieszczonego w Assets (w katalogu 'nagrania'): */
/* ***************************************************************** */

    //Jezeli w ustawieniech jest, zeby nie grac - to wychodzimy:
    if (ZmienneGlobalne.getInstance().zGlosem != true) {
        return;
    }


//    if (ZmienneGlobalne.getInstance().pierwszeWejscie) {
//        return;
//    }

    //zeby nie gral po obrocie i przy starcie apki:
    if (nieGraj) {
        nieGraj = false;
        return;
    }

    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
        public void run() {
            try {
                if (mp != null) {
                    mp.release();
                    mp = new MediaPlayer();
                } else {
                    mp = new MediaPlayer(); //wykona sie przy starcie aplikacji
                }
                final String sciezka_do_pliku = sciezka_do_pliku_parametr; //udziwniam, bo klasa wewn. i kompilator sie czepia....
                AssetFileDescriptor descriptor = getAssets().openFd(sciezka_do_pliku);
                mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                mp.prepare();
                mp.setVolume(1f, 1f);
                mp.setLooping(false);
                mp.start();
                //Toast.makeText(getApplicationContext(),"Odgrywam: "+sciezka_do_pliku,Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                //Toast.makeText(getApplicationContext(), "Nie można odegrać pliku z dźwiękiem.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }, delay_milisek);
} //koniec metody odegrazZAsstets


    public void  coNaKlikOnimageView(View v) {
    /* Odegranie nazwy obrazka */

        //Jezeli klika ze zbyt wielka czestotliwoscia - nie robie nic:
        //Mechanizm:  saving a last click time when clicking will prevent this problem (najlepsze rozwiazanie za znalezionych - setEnabled(false) - slabe...
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1200){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime(); //rejestrujemy czas klikniecia

        //toast("kliklem OBRAZEK");
        if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
            odegrajZkartySD(sciezka_do_pliku_dzwiekowego,5);
        } else {
            odegrajZAssets(sciezka_do_pliku_dzwiekowego,5);
        }
    } //koniec metody coNaKlikOnimageView



    @Override
    public boolean onLongClick(View v) {
    /* Pokazanie SplashEkranu (=Ustawienia) na dlugie dotkniecie na obrazku*/
        startActivity(splashKlasa);
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        dostosujKlawiszeImageNazwaTV(width,height);

        //Okreslam, czy zmieniono zrodlo:
        nowaKombinacja.pobierzZeZmiennychGlobalnych();
        if (nowaKombinacja.takaSamaJak(staraKombinacja))
            ZmienneGlobalne.getInstance().ZMIENIONO_ZRODLO = false;
        else
            ZmienneGlobalne.getInstance().ZMIENIONO_ZRODLO = true;

        //2-gie wywolanie - szczegolny przypadek - start aplikacji:
        licznikWykonan += 1;
        //przy 2-gim wykonaniu onResume ma odegrac sie slowo (bo 2-gie wykonanie tej proc., to de facto zamkniecie SplashEkranu po 1-szym uruchomieniu apki):
        if (licznikWykonan == 2) {
            ZmienneGlobalne.getInstance().ZMIENIONO_ZRODLO = true;  //zeby w tym szczegolnym przyp. zachowalo sie jak nalezy... (wymuszam naczytanie i odegranie)
            if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
                odegrajZkartySD(sciezka_do_pliku_dzwiekowego,5);
            } else {
                odegrajZAssets(sciezka_do_pliku_dzwiekowego,5);
            }
        }

        if (bylObrot) {  //do onPause weszlismy po obrocie ekranu - wymuszam wiec ponowne naczytanie :
            ZmienneGlobalne.getInstance().ZMIENIONO_ZRODLO = true; //wymuszam ponowne naczytanie listy obrazkow (patrz nizej)
        }

        //Jak nie zmieniono zrodla na SplashKlasa, to nie ma co zaczynac na nowo naczytywania itp.:
        if (!ZmienneGlobalne.getInstance().ZMIENIONO_ZRODLO) {
            return;
        }

        //ZMIENIONO ŹRÓDŁO OBRAZKÓW:

        //Tworzenie listy obrazków z Assets:
        if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG != true) {
            //Tworzenie listy obrazkow z Assets:
            AssetManager mgr = getAssets();
            try {
                listaObrazkowAssets = mgr.list(katalog);
                //sortuję po polsku:
                Arrays.sort(listaObrazkowAssets, new Comparator<String>() {
                    private Collator c = Collator.getInstance(Locale.getDefault()); //getDefault() - da polskie/bulgarskie/chnskie... znaki
                    @Override
                    public int compare(String lhs, String rhs) {
                        return c.compare(lhs,rhs);
                    }
                });


                listaNagranAssets   = mgr.list(nagrania);
                if (!bylObrot) currImage = 0;  //byla zmiana zrodla, od czegos trzeba zaczac... ;jesli byl obrot, to currImage bez zmian
                setCurrentImage();
                //Toast.makeText(this,"dlugosc : "+listaObrazkowAssets.length,Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                toast("Problem ze znalezieniem plików");
            }
        }

        //Tworzenie listy obrazków z Katalogu:
        if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG == true) {
            dirObrazkiNaSD = new File(ZmienneGlobalne.getInstance().WYBRANY_KATALOG);
            myObrazkiSD = findObrazki(dirObrazkiNaSD);
            //sortowanie po polsku:
            Collections.sort(myObrazkiSD, new Comparator<File>() {
                private Collator c = Collator.getInstance(Locale.getDefault());
                @Override
                public int compare(File lhs, File rhs) {
                    // ok, ale nie po polsku... return  lhs.getName().toString().compareTo(rhs.getName().toString());
                   return c.compare(lhs.getName().toString(), rhs.getName().toString()); //teraz po polsku(!)
                }
            });


            if (!bylObrot) currImage = 0; //byla zmiana zrodla, od czegos trzeba zaczac... ;jesli byl obrot, to currImage bez zmian
            setCurrentImage();
        }

        bylObrot = false; //na przyszlosc i na wszelki wypadek...

    }//koniec onResume


    @Override
    protected void onPause() {
    /*Nastepuje wejscie do SplashKlasy - na pobranie nowej kombinacji opcji*/
        super.onPause();
        staraKombinacja.pobierzZeZmiennychGlobalnych();
    }

    /*Klasa do sprawdzania czy podczas uzytkownik zmienil (klikaniem) zrodlo obrazkow */
    /* dotyczy (na razie) tylko zródła i ścieżki */
    class KombinacjaOpcji {

        private boolean ZRODLEM_JEST_KATALOG;
        private String  WYBRANY_KATALOG;

        void pobierzZeZmiennychGlobalnych() {
            ZRODLEM_JEST_KATALOG = ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG;
            WYBRANY_KATALOG = ZmienneGlobalne.getInstance().WYBRANY_KATALOG;
        }

        /*przepisanie z Nowej na starą*/
        void przepiszZNowej(KombinacjaOpcji nowa) {
            this.ZRODLEM_JEST_KATALOG = nowa.ZRODLEM_JEST_KATALOG;
            this.WYBRANY_KATALOG      = nowa.WYBRANY_KATALOG;
        }


        /*Sprawdza, czy kombinacje wybranych opcji sa takie same*/
        boolean takaSamaJak(KombinacjaOpcji nowaKombinacja) {
            if (this.ZRODLEM_JEST_KATALOG != nowaKombinacja.ZRODLEM_JEST_KATALOG)
                return false;
            if (!this.WYBRANY_KATALOG.equals(nowaKombinacja.WYBRANY_KATALOG))
                return  false;
            return true;
        }
    } //class wewnetrzna

    //Zmienne globalne zapewniejace prawidlowe dzialanie mechanizmu klkania na napisie (wielki/male litery). Trzeba uwzac na pomniejszanie imion!!!
    int licznikKlikow = 0;          //ile razy kliknelismy (przy zadanym obrazku) (=liczba wywolan nazwaTvClick() przy zadanym obrazku)
    String nazwaGlobStr;            //na przechowywanie oryginalnej nazwy spod obrazka (bo moze to byc Imię i jego pomniejszenie nie moe odbywac sie poprzez toLowerCase() )

/*

to ponizej dzialalo, ale nie na Android 4.2 - przenioslem wiec do wnetrza jako klasa anonimowa - 04.2017:

    public void nazwaTvClick (View arg0) {
    */
/* ********************************************* */
/*Powiekszenie/pomniejszenie nazwy pod obrazkiem */
/* ********************************************* */

/*

        if(licznikKlikow==0) { //wchodzimy 1-szy raz na danym obrazku
            TextView pole = (TextView) findViewById(R.id.nazwaTV);
            nazwaGlobStr = pole.getText().toString();
        }

        licznikKlikow++; //zeby wiedziec, ktory raz (przy zadanym obrazku) wchodzimy (=klikamy na napisie) do procedury

        if (licznikKlikow%2 == 0) { //parzyste klikniecie na nazwie oznacza chec jej pomniejszenia
            TextView pole = (TextView) findViewById(R.id.nazwaTV);
            pole.setText(nazwaGlobStr);  //odzyskujemy "starą" (małe litery lub imię) nazwę obrazka
        }
        else {                      //nieparzyste klikniecie na nazwie oznacza chec jej powiekszenia
           String nazwaRobStr = nazwaGlobStr.toUpperCase(Locale.getDefault());
           TextView pole = (TextView) findViewById(R.id.nazwaTV);
           pole.setText(nazwaRobStr);
        }
    } //nazwaTvClick

*/




}