package com.edu.carpool;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class driverSignupFragment extends Fragment {

    private EditText identityNum, studentID, plateNum, model, colour;
    private Button signUpBtn;
    private String icRegex, studentIDRegex, carModelAllUpper, carColourAllUpper, formattedCarPlate;
    private driverModelClass driverData;
    private DatabaseReference dbReference;
    private boolean hasError = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_driver_signup, container, false);

        identityNum = rootView.findViewById(R.id.driver_icNum);
        studentID = rootView.findViewById(R.id.driver_studentID);
        plateNum = rootView.findViewById(R.id.driver_carPlateNum);
        model = rootView.findViewById(R.id.driver_carModel);
        colour = rootView.findViewById(R.id.driver_carColour);
        signUpBtn = rootView.findViewById(R.id.signup_button);

        // Set format to user IC number and student ID
        icRegex = "^(\\d{6}\\-\\d{2}\\-\\d{4})$";
        studentIDRegex = "^(\\d{2}[A-Z]{3}\\d{5})$";

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    dbReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
                }

                hasError = false;

                String IcNum = identityNum.getText().toString();
                if(IcNum.isEmpty()) {
                    identityNum.setError("Required");
                    hasError = true;
                } else if (!IcNum.matches(icRegex)) {
                    identityNum.setError("Invalid format. E.g.000123-08-1234");
                    hasError = true;
                }

                String studID = studentID.getText().toString();
                if(studID.isEmpty()) {
                    studentID.setError("Required");
                    hasError = true;
                } else if (!studID.matches(studentIDRegex)) {
                    studentID.setError("Invalid format. E.g.20ABC01234");
                    hasError = true;
                }

                String carPlate = plateNum.getText().toString();
                if(carPlate.isEmpty()) {
                    plateNum.setError("Required");
                    hasError = true;
                } else {
                    formattedCarPlate = carPlate.toUpperCase();
                }

                String carModel = model.getText().toString();
                if(carModel.isEmpty()) {
                    model.setError("Required");
                    hasError = true;
                } else {
                    carModelAllUpper = capitalizeFirst(carModel);
                }

                String carColour = colour.getText().toString();
                if(carColour.isEmpty()) {
                    colour.setError("Required");
                    hasError = true;
                } else {
                    carColourAllUpper = capitalizeFirst(carColour);
                }

                if(hasError) {
                    Toast.makeText(requireContext(), "Invalid data exists.", Toast.LENGTH_SHORT).show();

                } else {
                    driverData = new driverModelClass(IcNum, studID, formattedCarPlate, carModelAllUpper, carColourAllUpper);

                    DatabaseReference driverReference = dbReference.child("driverID").push();
                    driverReference.setValue(driverData);

                    Toast.makeText(requireContext(), "Sign up as driver successfully", Toast.LENGTH_SHORT).show();

                    AccountFragment accountFragment = new AccountFragment();
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout, accountFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                }
            }
        });

        return rootView;
    }

    public static String capitalizeFirst(String input) {

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

}