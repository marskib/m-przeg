package com.example.developer.mprzegladarka;

/*
/* Klasa do zrobienia TextView'a, który zmniejsza wielkość tekstu, jeśliby miało dojść do zawijania tegoż tekstu */
/* na podstawie: http://android-smumn.blogspot.com/2013/09/i-noticed-that-when-my-text-in-textview.html
/* (ostateczne źródło: http://stackoverflow.com/questions/5033012/auto-scale-textview-text-to-fit-within-bounds/5280436#5280436
/* Uwaga: text jest zmniejszany do pewnej granicy, potem juz przycinany na końcu
/* Kontrolkę(i) typu ResizeTextView umieszcza sie w Layoutach. Istotny jest parametr android:maxLines="1"; można też dawać "2" - bezpieczniej...
/* Mozna zastosowac w Sylaby, bo tam niektore sie łamię...
/* 2017-03-22
*/



import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;


public class ResizeTextView extends TextView {


    private final int mOriginalTextSize;
    private final int mMinTextSize;
    private final static int sMinSize = 10;//20;

    public ResizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mOriginalTextSize = (int) getTextSize();
        mMinTextSize = (int) sMinSize;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        ViewUtil.resizeText(this, mOriginalTextSize, mMinTextSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ViewUtil.resizeText(this, mOriginalTextSize, mMinTextSize);
    }
}

class ViewUtil {

    private ViewUtil() {}

    public static void resizeText(TextView textView, int originalTextSize, int minTextSize) {
        final Paint paint = textView.getPaint();
        final int width = textView.getWidth();
        if (width == 0) return;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
        float ratio = width / paint.measureText(textView.getText().toString());
        if (ratio <= 1.0f) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    Math.max(minTextSize, originalTextSize * ratio));
        }
    }
}

//**************************** ski ski 2017.03.21 - proby z lamaczem linii: inne, moje proby... :
//Layout layout = new StaticLayout()   //(text, /* other stuff--see docs */);
//int nLines = nazwaTV.getLineCount(); //TUTAJ SEDNO SKI SKI ski ski, ale post factum (na wyswietlonym widoku...  :(      )
//String rob = (String) nazwaTV.getText();
//rob = rob + Integer.toString(nLines);
//nazwaTV.setText(rob);
//****************************
