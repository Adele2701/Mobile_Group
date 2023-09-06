package com.edu.carpool;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileFragment extends Fragment {

    private EditText accName, accPhoneNum;
    private Button saveProfile;
    private String Name, Gender, PhoneNum;
    private String phoneNum1Regex, phoneNum2Regex;
    private AutoCompleteTextView autoCompleteTextView;
    private DatabaseReference dbReference;
    private boolean changesDetected, hasError;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        accName = rootView.findViewById(R.id.editName);
        accPhoneNum = rootView.findViewById(R.id.editPhoneNum);
        autoCompleteTextView = rootView.findViewById(R.id.autoCompleteTextView);
        saveProfile = rootView.findViewById(R.id.saveButton);

        // Retrieve and display user data pass from profile
        Bundle arguments = getArguments();
        if (arguments != null) {
            Name = arguments.getString("name");
            Gender = arguments.getString("gender");
            PhoneNum = arguments.getString("phoneNum");

            accName.setText(Name);

            if(Gender.equals("Not set")) {
                autoCompleteTextView.setHint("Gender");
            } else {
                autoCompleteTextView.setText(Gender);
            }

            if(PhoneNum.equals("Not set")){
                accPhoneNum.setText("");
            } else {
                accPhoneNum.setText(PhoneNum);
            }

        }

        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // To hide the keyboard when clicked
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && getActivity().getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }
            }
        });

        // Set items to drop-down menu for gender
        String[] Gender = new String[]{"Male", "Female"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.dropdown_item, Gender);
        autoCompleteTextView.setAdapter(adapter);

        // Set format to phone number
        phoneNum1Regex = "^(\\d{3}-\\d{7})$";
        phoneNum2Regex = "^(\\d{3}-\\d{8})$";

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // To hide the keyboard when clicked
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    dbReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

                    changesDetected = false;
                    hasError = false;

                dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(snapshot.exists()) {

                            if (!Name.equals(accName.getText().toString().trim())) {
                                dbReference.child("name").setValue(accName.getText().toString());
                                changesDetected = true;
                            }

                            if (!Gender.equals(autoCompleteTextView.getText().toString().trim())) {
                                dbReference.child("gender").setValue(autoCompleteTextView.getText().toString());
                                changesDetected = true;
                            }

                            String number = accPhoneNum.getText().toString().trim();
                            if (number.matches(phoneNum1Regex) || number.matches(phoneNum2Regex)) {
                                if (!PhoneNum.equals(number)) {
                                    dbReference.child("phoneNum").setValue(number);

                                    changesDetected = true;
                                }
                            } else if (number.isEmpty()) {
                                accPhoneNum.setError("Required");
                                hasError = true;
                            } else {
                                accPhoneNum.setError("Invalid phone number.\nE.g. 011-1234567");
                                hasError = true;
                            }

                            if(hasError){
                                Toast.makeText(requireContext(), "Invalid data exists", Toast.LENGTH_SHORT).show();
                            } else if (changesDetected){
                                Toast.makeText(requireContext(), "Profile Successfully Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });

                }
            }
        });

        return rootView;
    }
}