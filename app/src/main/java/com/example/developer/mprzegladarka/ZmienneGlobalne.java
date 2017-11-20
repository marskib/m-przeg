package com.example.developer.mprzegladarka;


import android.app.Application;

/**
 singleton na przechowywanie zmiennych globalnych
 */
public class ZmienneGlobalne extends Application {

    public boolean zNapisami;
    public boolean zGlosem;
    public boolean PELNA_WERSJA;
    public boolean ZRODLEM_JEST_KATALOG; //Co jest aktualnie źródlem obrazków - Asstes czy Katalog (np. katalog na karcie SD)
    public String  WYBRANY_KATALOG;      //katalog (if any) wybrany przez usera jako zrodlo obrazkow (z external SD lub Urządzenia)
    public boolean ZMIENIONO_ZRODLO;     //jakakolwiek zmiana zrodla obrazkow - Assets/Katalog JAK ROWNIEZ zmiana katalogu
    public boolean ALFABET;              //Czy obrazki mają być wyświetlano alfabetycznie czy losowo
    public boolean DLA_KRZYSKA;          //Czy dla Krzyska do testowania - jesli tak -> wylaczam logo i strone www

    private static ZmienneGlobalne ourInstance = new ZmienneGlobalne();

    public static ZmienneGlobalne getInstance() {
        return ourInstance;
    }


    //konstruktor tego singletona + ustawienia poczatkowe aplikacji:
    private ZmienneGlobalne() {
        zGlosem    = true;  //false - na potrzeby developmentu w 1.05 i na 232 Wawrz.40
        zNapisami  = false;
        ALFABET    = true;
        PELNA_WERSJA = true;
        ZRODLEM_JEST_KATALOG = false;  //startujemy ze zrodlem w Assets
        ZMIENIONO_ZRODLO = true;       //inicjacyjnie na true, zeby po uruchomieniu apki wykonala sie onResume() w calosci
        WYBRANY_KATALOG = "*^5%dummy"; //"nic jeszcze nie wybrano" - lepiej to niz null...
        DLA_KRZYSKA = false;
    } //konstruktor
}

