package com.dcq.quotesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

    private static final String CATEGORY_PERSON = "1";
    private static final String CATEGORY_VERSE = "2";
    private static final String CATEGORY_OTHER = "0";

    private Button submit, clear;
    private EditText quote, person, search;
    private CheckBox cperson, verse, other;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_quote, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        quote = view.findViewById(R.id.et_quote);
        search = view.findViewById(R.id.et_search);
        person = view.findViewById(R.id.et_person);
        cperson = view.findViewById(R.id.cb_person);
        verse = view.findViewById(R.id.cb_verse);
        other = view.findViewById(R.id.cb_other);
        submit = view.findViewById(R.id.btn_save);
        clear = view.findViewById(R.id.btn_clear);

        cperson.setOnClickListener(this::onPersonClick);
        verse.setOnClickListener(this::onVerseClick);
        other.setOnClickListener(this::onOtherClick);
        submit.setOnClickListener(this::OnSubmit);
        clear.setOnClickListener(this::OnClear);
    }

    public void onPersonClick(View view) {
        setCategoryCheckBoxes(true, false, false);
    }

    public void onVerseClick(View view) {
        setCategoryCheckBoxes(false, true, false);
    }

    public void onOtherClick(View view) {
        setCategoryCheckBoxes(false, false, true);
    }

    public void OnClear(View view) {
        clearFields(quote, person, search);
    }

    private void setCategoryCheckBoxes(boolean personChecked, boolean verseChecked, boolean otherChecked) {
        cperson.setChecked(personChecked);
        verse.setChecked(verseChecked);
        other.setChecked(otherChecked);
    }

    private void clearFields(EditText... fields) {
        for (EditText field : fields) {
            field.setText("");
        }
    }

    public void OnSubmit(View view) {
        String str_quote = quote.getText().toString();
        String str_person = person.getText().toString();
        String str_search = search.getText().toString();

        String category = cperson.isChecked() ? CATEGORY_PERSON : verse.isChecked() ? CATEGORY_VERSE : CATEGORY_OTHER;

        SharedPreferences sharedPref = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        sharedPref.edit().putString("searchterm", str_search).apply();

        if (!checkEnteredData()) {
            Toast.makeText(requireActivity().getApplicationContext(), "Please make sure you have filled all fields correctly", Toast.LENGTH_SHORT).show();
            return;
        }

        submit.setEnabled(false);
        DatabaseReference db = FirebaseDatabase.getInstance("https://dailycatholicquotes-2ef12-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference().child("quotes");

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String no = String.valueOf(dataSnapshot.getChildrenCount() + 1);

                Quote quotetoadd = new Quote(str_person, no, str_quote, "1", category);
                db.child(no).setValue(quotetoadd).addOnCompleteListener(task -> {
                    submit.setEnabled(true);
                    if (task.isSuccessful()) {
                        hideKeyboard();
                        Toast.makeText(getActivity().getApplicationContext(), "Added to Database", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), NewQuoteActivity.class));
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Failed to add quote!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                submit.setEnabled(true);
                Toast.makeText(getActivity().getApplicationContext(), "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkEnteredData() {
        boolean noErrors = true;

        if (isEmpty(quote)) {
            noErrors = false;
            quote.setError("Quote is required!");
        }

        if (isEmpty(person)) {
            noErrors = false;
            person.setError("Person/verse is required!");
        }

        if (!cperson.isChecked() && !verse.isChecked() && !other.isChecked()) {
            noErrors = false;
            Toast.makeText(getContext(), "Please select at least one category!", Toast.LENGTH_SHORT).show();
        }

        return noErrors;
    }

    private boolean isEmpty(EditText text) {
        return TextUtils.isEmpty(text.getText().toString());
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}