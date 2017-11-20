package com.example.developer.mprzegladarka;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Created by Administrator on 2017-04-13.
 */

public class WersjaDemoOstrzez  extends Activity  implements OnClickListener {
/*Uwaga - listenera definije na 'piechote' (poprzez implemets OkClickListener),
  bo dla wersji Androida 4.2 nie dziala definiowanie android:OnClik="" w layoutcie - apka sie 'wali'...
*/
    public Button bOK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wersja_demo_ostrz);

        bOK = (Button) findViewById(R.id.buttonOk);
        bOK.setOnClickListener(this);
    }

    void ButtonOkOnClick(View v) {

       // finish(); //zeby activity ponizej nie przeslamnialo, a WersjaDemoOstrzez nie zostawala na ekranie (najpier finis(), potem kodu ciag dalszy...)

       // Intent intent2 = new Intent(this, InternalExternalKlasa.class);
       // this.startActivity(intent2);

    }

    @Override
    public void onClick(View v) {
      finish();  //zeby activity ponizej nie przeslamnialo, a WersjaDemoOstrzez nie zostawala na ekranie (najpier finis(), potem kodu ciag dalszy...)
      Intent intent2 = new Intent(this, InternalExternalKlasa.class);
      this.startActivity(intent2);
      return; //pro froma ;)
    }
}
