package com.example.mangosweetnessdetection;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

public class HighlightWidgets {

    public static void highlightView(View dimLayout, View  viewToHighlight, Button button1, Button button2, Button button3, Button button4) {
        dimLayout.setVisibility(View.VISIBLE); // Show the dim effect
        button1.setTextColor(Color.parseColor("#A3887F5E"));
        button1.setBackgroundColor(Color.parseColor("#602E1F"));
        button2.setTextColor(Color.parseColor("#A3887F5E"));
        button2.setBackgroundColor(Color.parseColor("#602E1F"));
        button3.setTextColor(Color.parseColor("#A3887F5E"));
        button3.setBackgroundColor(Color.parseColor("#602E1F"));
        button4.setTextColor(Color.parseColor("#A3887F5E"));
        button4.setBackgroundColor(Color.parseColor("#602E1F"));



        viewToHighlight.bringToFront();  // Bring the target widget to front

    }
    public static void highlightView(View dimLayout, View  viewToHighlight, Button button, Button prev, Button next) {
        dimLayout.setVisibility(View.VISIBLE); // Show the dim effect
        button.setTextColor(Color.parseColor("#A3887F5E"));
        button.setBackgroundColor(Color.parseColor("#602E1F"));
        viewToHighlight.bringToFront();  // Bring the target widget to front
        prev.bringToFront();
        next.bringToFront();

    }
    public static void highlightView(View dimLayout) {
        dimLayout.setVisibility(View.VISIBLE); // Show the dim effect
    }


}
