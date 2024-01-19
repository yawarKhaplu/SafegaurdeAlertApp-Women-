package com.darkness.sparkwomen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
public class SelfDefenseActivity extends AppCompatActivity {

    RelativeLayout logout,ShareApp,AboutUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_defense);
        logout = findViewById(R.id.second);
        ShareApp = findViewById(R.id.fourth);
        AboutUs = findViewById(R.id.third);

        AboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.about_us);
            }
        });

        ShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent with ACTION_SEND
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);

                // Set the type of data you are sending (text/plain in this case)
                sendIntent.setType("text/plain");

                // Add the message you want to share
                String message = "Check My Application Women Security App\nhttps://play.google.com/store/search?q=women+security+app&c=apps";
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);

                // Create a chooser to let the user choose where to share the message
                Intent chooserIntent = Intent.createChooser(sendIntent, "Share via");

                // Verify that the intent will resolve to an activity
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    // Start the chooser activity
                    startActivity(chooserIntent);
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }
}