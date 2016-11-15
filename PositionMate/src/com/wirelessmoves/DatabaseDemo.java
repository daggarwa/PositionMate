package com.wirelessmoves;




import android.app.TabActivity;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;



public class DatabaseDemo extends TabActivity {
	DatabaseHelper dbHelper;
	GridView grid;
	TextView txtTest;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
    }
  
   
    
      
}