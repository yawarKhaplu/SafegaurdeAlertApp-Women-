package com.darkness.sparkwomen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LawDisplayerActivity extends AppCompatActivity implements View.OnClickListener {

    TextView big,oneLine;
    String[] laws, lawsContent;
    int counter;
    Button back, next;
    View closeBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_law_displayer);

        big = findViewById(R.id.bigLaws);
        oneLine = findViewById(R.id.lawString);
        counter = getIntent().getIntExtra("position",0);
        laws = new String[]{"Child Marriage Restraint Act, 1926","Prohibition of Force Marriage Act, 2011 ","Dowry and Bridal Gifts (Restriction) Act, 1976","Protection Against Harassment at workplace , 2010","West Pakistan Maternity Benefit Ordinance, 1958","Criminal Law (Amendment) (Offences in the name or pretext of Honour) Act, 2016"," Acid Control and Acid Crime Prevention Bill, 2011","The Family Courts Act, 1964","Legal Aid and Justice Authority Act, 2020.","" +
                "The Punjab Protection of Women Against Violence Act, 2016"};
        lawsContent = this.getResources().getStringArray(R.array.lawsBig);

        closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(view -> {
            onBackPressed();
            LawDisplayerActivity.this.finish();
        });

        back = findViewById(R.id.backBtn);
        next = findViewById(R.id.nextBtn);
        next.setOnClickListener(this);
        back.setOnClickListener(this);
        setData();
    }

    public void setData(){
        oneLine.setText(laws[counter]);
        big.setText(lawsContent[counter]);
        big.setMovementMethod(new ScrollingMovementMethod());
        big.scrollTo(0,0);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.nextBtn){
            if(counter<9){
                counter++;
            }else {
                counter = 0;
            }
        } else if (view.getId() == R.id.backBtn) {
            if(counter == 0){
                counter = (laws.length-1);
            }else {
                counter--;
            }
        }

        setData();
    }
}