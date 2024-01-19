package com.darkness.sparkwomen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

public class LawsActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(LawsActivity.this,MainActivity.class));
        LawsActivity.this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laws);
        RecyclerView recyclerView = findViewById(R.id.recycleLaws);
        String[] laws = new String[]{"Child Marriage Restraint Act, 1929","Prohibition of Force Marriage Act, 2011","Dowry and Bridal Gifts (Restriction) Act, 1976","Protection Against Harassment at workplace , 2010","West Pakistan Maternity Benefit Ordinance, 1958","Criminal Law (Amendment) (Offences in the name or pretext of Honour) Act, 2016","Acid Control and Acid Crime Prevention Bill, 2011","The Family Courts Act, 1964","Legal Aid and Justice Authority Act, 2020.","" + "Protection of Women Against Violence Act, 2016"};

        MyAdapter adapter = new MyAdapter(this, laws, position -> {
            Intent intent = new Intent(LawsActivity.this,LawDisplayerActivity.class);
            intent.putExtra("position",position);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        findViewById(R.id.backBtn).setOnClickListener(view -> {
            startActivity(new Intent(LawsActivity.this,MainActivity.class));
            LawsActivity.this.finish();
        });
    }
}