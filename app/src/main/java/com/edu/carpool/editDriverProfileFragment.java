package com.edu.carpool;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class editDriverProfileFragment extends Fragment {

    private EditText identityNum, studentID, plateNum, model, colour;
    private TextView ID, linkAddEdit;
    private Button saveBtn;
    private DatabaseReference userRef;
    private String identityNumFromDB, studentIDFromDB, plateNumFromDB, modelFromDB, colourFromDB;
    private boolean hasError, changesDetected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_driver_profile, container, false);

        ID = rootView.findViewById(R.id.driverID);
        identityNum = rootView.findViewById(R.id.edit_icNum);
        studentID = rootView.findViewById(R.id.edit_studentID);
        plateNum = rootView.findViewById(R.id.edit_carPlateNum);
        model = rootView.findViewById(R.id.edit_carModel);
        colour = rootView.findViewById(R.id.edit_carColour);
        saveBtn = rootView.findViewById(R.id.save_button);
        linkAddEdit = rootView.findViewById(R.id.addEdit);

        // Retrieve and display data from database
        FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();
        if (User != null) {
            String userId = User.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("driverID");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    if(snapshot.exists()) {
                        for (DataSnapshot driverSnapshot : snapshot.getChildren()) {
                            String driverId = driverSnapshot.getKey();
                            ID.setText("ID: " + driverId.substring(1));

                            identityNumFromDB = driverSnapshot.child("ic").getValue(String.class);
                            studentIDFromDB = driverSnapshot.child("studentID").getValue(String.class);
                            plateNumFromDB = driverSnapshot.child("carPlateNum").getValue(String.class);
                            modelFromDB = driverSnapshot.child("carModel").getValue(String.class);
                            colourFromDB = driverSnapshot.child("carColour").getValue(String.class);

                            identityNum.setText(identityNumFromDB);
                            studentID.setText(studentIDFromDB);
                            plateNum.setText(plateNumFromDB);
                            model.setText(modelFromDB);
                            colour.setText(colourFromDB);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasError = false;
                changesDetected = false;

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("driverID");

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {

                            if(snapshot.exists()) {
                                for (DataSnapshot driverSnapshot : snapshot.getChildren()) {
                                    String driverId = driverSnapshot.getKey();
                                    DatabaseReference childRef = userRef.child(driverId);

                                    String carPlate = plateNum.getText().toString();
                                    String carPlateFormatted = carPlate.toUpperCase();
                                    if (carPlate.isEmpty()) {
                                        plateNum.setError("Required");
                                        hasError = true;
                                    } else if (!carPlateFormatted.equals(plateNumFromDB)) {
                                        childRef.child("carPlateNum").setValue(carPlateFormatted);
                                        changesDetected = true;
                                    }

                                    String carModel = model.getText().toString();
                                    String carModelFormatted = capitalizeFirst(carModel);
                                    if (carModel.isEmpty()) {
                                        model.setError("Required");
                                        hasError = true;
                                    } else if (!carModelFormatted.equals(modelFromDB)) {
                                        childRef.child("carModel").setValue(carModelFormatted);
                                        changesDetected = true;
                                    }

                                    String carColour = colour.getText().toString();
                                    String carColourFormatted = capitalizeFirst(carColour);
                                    if (carColour.isEmpty()) {
                                        colour.setError("Required");
                                        hasError = true;
                                    } else if (!carColourFormatted.equals(colourFromDB)) {
                                        childRef.child("carColour").setValue(carColourFormatted);
                                        changesDetected = true;
                                    }

                                    if(hasError){
                                        Toast.makeText(requireContext(), "Invalid data exists", Toast.LENGTH_SHORT).show();
                                    } else if (changesDetected){
                                        Toast.makeText(requireContext(), "Driver Profile Successfully Updated", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(requireContext(), "No changes found", Toast.LENGTH_SHORT).show();
                                    }
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

        linkAddEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("users", userRef.getParent().getKey());
                bundle.putString("driverID", ID.getText().toString().substring(4));

                addDriverDetailsFragment addDriverDetailsFragment = new addDriverDetailsFragment();
                addDriverDetailsFragment.setArguments(bundle);
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, addDriverDetailsFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
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