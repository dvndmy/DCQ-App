package com.dcq.quotesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dcq.quotesapp.models.Quote;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddQuoteFragment extends Fragment {

    // Flag to track errors during form submission
    Boolean noErrors = true;

    // UI elements
    Button submit;
    EditText quote, person, search;
    CheckBox cperson, verse, other;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_add_quote, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        quote = view.findViewById(R.id.et_quote);
        search = view.findViewById(R.id.et_search);
        person = view.findViewById(R.id.et_person);
        cperson = view.findViewById(R.id.cb_person);
        verse = view.findViewById(R.id.cb_verse);
        other = view.findViewById(R.id.cb_other);
        submit = view.findViewById(R.id.btn_save);

        // Set click listeners for checkboxes and submit button
        cperson.setOnClickListener(this::onPersonClick);
        verse.setOnClickListener(this::onVerseClick);
        other.setOnClickListener(this::onOtherClick);
        submit.setOnClickListener(this::OnSubmit);
    }

    // Handle checkbox clicks
    public void onPersonClick(View view) {
        setCategoryCheckBoxes(true, false, false);
    }

    public void onVerseClick(View view) {
        setCategoryCheckBoxes(false, true, false);
    }

    public void onOtherClick(View view) {
        setCategoryCheckBoxes(false, false, true);
    }

    // Helper method to set category checkboxes
    private void setCategoryCheckBoxes(boolean personChecked, boolean verseChecked, boolean otherChecked) {
        cperson.setChecked(personChecked);
        verse.setChecked(verseChecked);
        other.setChecked(otherChecked);
    }

    // Handle form submission
    public void OnSubmit(View view) {
        String str_quote = quote.getText().toString();
        String str_person = person.getText().toString();
        String str_search = search.getText().toString();

        String category;
        if (cperson.isChecked()) {
            category = "1";
        } else if (verse.isChecked()) {
            category = "2";
        } else {
            category = "0";
        }

        // Save the search term in SharedPreferences
        SharedPreferences sharedPref = getContext().getSharedPreferences("search", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("searchterm", str_search);
        editor.apply();

        // Verifies that users have entered all the required fields before registering a quote
        if (checkEnteredData()) {
            DatabaseReference db = FirebaseDatabase.getInstance("https://dailycatholicquotes-2ef12-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference().child("quotes");

            // Retrieve the count of existing quotes for generating a new quote number
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Update the maximum quote number in SharedPreferences
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("dbmax", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("max", String.valueOf(dataSnapshot.getChildrenCount() + 1));
                    editor.apply();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error if needed
                }
            });

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("dbmax", Context.MODE_PRIVATE);
            String no = sharedPreferences.getString("max", "5001");

            // Create a new Quote object
            Quote quotetoadd = new Quote(str_person, no, str_quote, "1", category);

            // Add the new quote to the database
            db.child(no).setValue(quotetoadd);

            // Display success message and start a new activity
            Toast.makeText(getActivity().getApplicationContext(), "Added to Database", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), NewQuoteActivity.class));
        } else {
            // Display an error message if required fields are not filled
            Toast.makeText(getActivity().getApplicationContext(), "Please make sure you have filled all fields correctly", Toast.LENGTH_SHORT).show();
        }
    }

    // Verifies that users have entered all required fields
    Boolean checkEnteredData() {
        noErrors = true;

        // Check if the quote field is empty
        if (isEmpty(quote)) {
            noErrors = false;
            quote.setError("Quote is required!");
        }

        // Check if the person/verse field is empty
        if (isEmpty(person)) {
            noErrors = false;
            person.setError("Person/verse is required!");
        }

        // Check if at least one category checkbox is selected
        if (!cperson.isChecked() && !verse.isChecked() && !other.isChecked()) {
            noErrors = false;
            cperson.setError("Select one!");
            verse.setError("Select one!");
            other.setError("Select one!");
        }

        return noErrors;
    }

    // Helper method to check if an EditText is empty
    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }
}
