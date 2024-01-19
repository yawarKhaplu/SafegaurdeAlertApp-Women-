package com.darkness.sparkwomen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class  ContactActivity extends AppCompatActivity {


    EditText contact;
    Button addContact;

    RecyclerView recyclerView;

    HashMap<String,String> contacts;

    ArrayList<String> send;

    ContactsAdapter adapter;
    MyOnClickListener onClickListener;

    ImageView edit;

    TextView callerInfo;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,MainActivity.class));
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);


        edit = findViewById(R.id.editCallButton);



        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(ContactActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog);

                Button close,save;
                close = dialog.findViewById(R.id.dialogCancel);
                save = dialog.findViewById(R.id.dialogSave);
                EditText  number = dialog.findViewById(R.id.dialogEditText);

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String contactId = "ID";
                        DatabaseReference contactRef = FirebaseDatabase.getInstance().getReference("users").child(userUid).child("callingContact").child(contactId);
                        String numberText = number.getText().toString();
                        if (numberText.equals("")){

                            contactRef.removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("firstNumber",null);
                                    editor.apply();
                                    setCallingInformation();
                                    dialog.dismiss();
                                }
                                SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("firstNumber",null);
                                editor.apply();
                                setCallingInformation();
                                dialog.dismiss();
                            });

                        }else if(numberText.length() == 11){
                            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("firstNumber",numberText);
                            editor.apply();
                            updateCallingContactInFirebase(numberText);
                            setCallingInformation();
                            dialog.dismiss();
                        }else {
                            Toast.makeText(ContactActivity.this, "Enter valid number!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });


                dialog.show();

            }
        });




        callerInfo = findViewById(R.id.callText);


        setCallingInformation();


        contacts = new HashMap<>();
        send = new ArrayList<>();

        adapter = new ContactsAdapter(this, send, new MyOnClickListener() {
            @Override
            public void onItemClicked(int position) {
                deleteItemFromDatabase(position);
            }
        });

        recyclerView = findViewById(R.id.contacts);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getData();

        contact = findViewById(R.id.contactGet);
        addContact = findViewById(R.id.addContact);

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createContact(contact.getText().toString());
            }
        });
    }

    private void createContact(String contactString) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> oldNumbers = sharedPreferences.getStringSet("enumbers", new LinkedHashSet<>());
        oldNumbers.add(contactString);
        editor.remove("enumbers");
        editor.putStringSet("enumbers",oldNumbers);
        editor.apply();

        contact.setText("");
        editor.apply();
        getData();
        checkAndUpdateFirebase();
    }

    private void checkAndUpdateFirebase() {
        if (isConnectedToInternet()) {
            uploadLocalContactsToFirebase();
        }
        getData();
    }
    private void uploadLocalContactsToFirebase() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        Set<String> localContacts = sharedPreferences.getStringSet("enumbers", new LinkedHashSet<>());

        // Assuming each user has a unique ID (uid) for Firebase
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("contacts");

        // Clear existing contacts on Firebase
        userRef.removeValue();

        // Upload local contacts to Firebase
        for (String contact : localContacts) {
            String contactKey = userRef.push().getKey();
            userRef.child(contactKey).setValue(contact);
        }
    }
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }


    private void setCallingInformation(){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        String firstNumber = sharedPreferences.getString("firstNumber","null");
        if (firstNumber.isEmpty()||firstNumber.equalsIgnoreCase("null")){
            callerInfo.setText("Please add number.");
        }  else {
            callerInfo.setText(firstNumber);
        }
        updateCallingContactInFirebase(firstNumber);
    }

    private void updateCallingContactInFirebase(String contactID) {
        // Assuming each user has a unique ID (uid) for Firebase
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        // Set the callingContact node with the contact ID
        userRef.child("callingContact").child("ID").setValue(contactID);
    }




    private void deleteItemFromDatabase(int position) {
        // Get the SharedPreferences object for storing local data
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        // Get the editor to modify the SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve the set of existing contact numbers from SharedPreferences
        Set<String> oldNumbers = sharedPreferences.getStringSet("enumbers", new LinkedHashSet<>());

        // Remove the contact at the specified position from the set
        oldNumbers.remove(send.get(position));

        // Put the updated set back into SharedPreferences
        editor.putStringSet("enumbers", oldNumbers);

        // Apply the changes to SharedPreferences
        editor.apply();

        // Update your local list and notify the adapter
        getData();
        adapter.notifyDataSetChanged();
        checkAndUpdateFirebase();
    }




    private void getData() {
        // Clear the existing data in the 'send' ArrayList
        send.clear();

        // Access the SharedPreferences object named "MySharedPref"
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        // Retrieve the set of existing contact numbers from SharedPreferences
        Set<String> localContacts = sharedPreferences.getStringSet("enumbers", new LinkedHashSet<>());

        // If there are no contacts in local SharedPreferences, check Firebase
        if (localContacts.isEmpty() && isConnectedToInternet()) {
            // Fetch data from Firebase and update local data
            getDataFromFirebase();
        } else {
            // Use the contacts from local SharedPreferences
            send.addAll(localContacts);
            adapter.notifyDataSetChanged();
        }
    }


    private void getDataFromFirebase() {
        // Assuming each user has a unique ID (uid) for Firebase
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("contacts");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Assuming phone numbers are stored as values
                    String phoneNumber = snapshot.getValue(String.class);
                    send.add(phoneNumber);
                }

                // Update local SharedPreferences with fetched data
                updateLocalSharedPreferences();

                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateLocalSharedPreferences() {
        // Access the SharedPreferences object named "MySharedPref"
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        // Get the editor to modify the SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Clear the existing "enumbers" key from SharedPreferences
        editor.remove("enumbers");

        // Put the updated set back into SharedPreferences
        editor.putStringSet("enumbers", new LinkedHashSet<>(send));

        // Apply the changes to SharedPreferences
        editor.apply();
    }
}