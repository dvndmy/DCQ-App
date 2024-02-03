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

    //    private List<Category> categoryList;
//    private AVLoadingIndicatorView progressBar;
//    private DatabaseReference dbCategories;
//
//    private RecyclerView recyclerView;
//    private PhotoCategoriesAdapter adapter;
    Boolean noErrors = true;
    Button submit;
    EditText quote, person, search;
    CheckBox cperson, verse, other;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_quote, container, false);


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        quote = (EditText) view.findViewById(R.id.et_quote);
        search = (EditText) view.findViewById(R.id.et_search);
        person = (EditText) view.findViewById(R.id.et_person);
        cperson = (CheckBox) view.findViewById(R.id.cb_person);
        verse = (CheckBox) view.findViewById(R.id.cb_verse);
        other = (CheckBox) view.findViewById(R.id.cb_other);
        cperson.setOnClickListener(this::onPersonClick);
        verse.setOnClickListener(this::onVerseClick);
        other.setOnClickListener(this::onOtherClick);
        submit = (Button) view.findViewById(R.id.btn_save);
        submit.setOnClickListener(this::OnSubmit);
    }

    public void onPersonClick(View view) {
        cperson.setChecked(true);
        verse.setChecked(false);
        other.setChecked(false);
    }

    public void onVerseClick(View view) {

        cperson.setChecked(false);
        verse.setChecked(true);
        other.setChecked(false);
    }

    public void onOtherClick(View view) {

        cperson.setChecked(false);
        verse.setChecked(false);
        other.setChecked(true);
    }

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
        SharedPreferences sharedPref = getContext().getSharedPreferences("search", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("searchterm", str_search);
        editor.apply();

        //Verifies that users have entered in all the required fields before registering a quote
        if (checkEnteredData()) {
            DatabaseReference db = FirebaseDatabase.getInstance("https://dailycatholicquotes-2ef12-default-rtdb.europe-west1.firebasedatabase.app").getReference();
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("dbmax", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("max", String.valueOf(dataSnapshot.getChildrenCount() + 1));
                    editor.apply();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("dbmax", Context.MODE_PRIVATE);
            String no = sharedPreferences.getString("max", "5001");
            Quote quotetoadd = new Quote(no, str_quote, str_person, "User Added", category);
            db.child("quotes/"+no).setValue(quotetoadd);
            Toast.makeText(this.getActivity().getApplicationContext(), "Added to Database", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), NewQuoteActivity.class));

        } else {
            Toast.makeText(this.getActivity().getApplicationContext(), "Please make sure you have filled all fields correctly", Toast.LENGTH_SHORT).show();
        }


    }

    //Verifies that users have entered all required fields
    Boolean checkEnteredData() {
        noErrors = true;
        if (isEmpty(quote)) {
            noErrors = false;
            quote.setError("Quote is required!");
        }
        if (isEmpty(person)) {
            noErrors = false;
            person.setError("Person/verse is required!");
        }

        if (cperson.isChecked() == false && verse.isChecked() == false && other.isChecked() == false) {
            noErrors = false;
            cperson.setError("Select one!");
            verse.setError("Select one!");
            other.setError("Select one!");
        }
        return noErrors;
    }

    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }
}
