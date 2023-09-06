package com.edu.carpool;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    private TextView accName, accGender, accPhoneNum, badge;
    private FloatingActionButton manageFab, logoutFab, driverSignupFab, addEmergencyFab, editProfileFab, editDriverFab;
    private TextView logoutActionText, driverSignupActionText, addEmergencyActionText, editProfileActionText, editDriverActionText;
    private Boolean isAllFabsVisible;
    private Bundle bundle;
    private FirebaseAuth mAuth;
    private ProgressBar loadingSpinner;
    private DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);

        // To validate user is logged-in or not
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            // User is not signed in, prompt a page that allows user redirect to login
            PromptUserToLoginFragment promptUserToLoginFragment = new PromptUserToLoginFragment();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, promptUserToLoginFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            return rootView;
        }

        accName = rootView.findViewById(R.id.accountName);
        accGender = rootView.findViewById(R.id.accountGender);
        accPhoneNum = rootView.findViewById(R.id.accountPhoneNum);
        manageFab = rootView.findViewById(R.id.manage_fab);
        loadingSpinner = rootView.findViewById(R.id.loading_spinner);
        loadingSpinner.setVisibility(View.GONE);
        badge = rootView.findViewById(R.id.driverBadge);

        GetUserDataFromDB();
        checkDriverStatus();

        logoutFab = rootView.findViewById(R.id.logout_fab);
        driverSignupFab = rootView.findViewById(R.id.signupDriver_fab);
        addEmergencyFab = rootView.findViewById(R.id.addEmergency_fab);
        editProfileFab = rootView.findViewById(R.id.editProfile_fab);
        editDriverFab = rootView.findViewById(R.id.editDriver_fab);

        logoutActionText = rootView.findViewById(R.id.logout_action_text);
        driverSignupActionText = rootView.findViewById(R.id.signupDriver_action_text);
        addEmergencyActionText = rootView.findViewById(R.id.addEmergency_action_text);
        editProfileActionText = rootView.findViewById(R.id.editProfile_action_text);
        editDriverActionText = rootView.findViewById(R.id.editDriver_action_text);

        logoutFab.setVisibility(View.GONE);
        driverSignupFab.setVisibility(View.GONE);
        addEmergencyFab.setVisibility(View.GONE);
        editProfileFab.setVisibility(View.GONE);
        editDriverFab.setVisibility(View.GONE);

        logoutActionText.setVisibility(View.GONE);
        driverSignupActionText.setVisibility(View.GONE);
        addEmergencyActionText.setVisibility(View.GONE);
        editProfileActionText.setVisibility(View.GONE);
        editDriverActionText.setVisibility(View.GONE);

        isAllFabsVisible = false;

            // Floating button
            manageFab.setOnClickListener(view -> {
                if (!isAllFabsVisible) {
                    // when isAllFabsVisible becomes true
                    // make all the action name texts and FABs VISIBLE
                    logoutFab.show();
                    addEmergencyFab.show();
                    editProfileFab.show();

                    if(badge.getVisibility() == View.VISIBLE){
                        editDriverFab.show();
                        editDriverActionText.setVisibility(View.VISIBLE);
                    } else {
                        driverSignupFab.show();
                        driverSignupActionText.setVisibility(View.VISIBLE);
                    }

                    logoutActionText.setVisibility(View.VISIBLE);
                    addEmergencyActionText.setVisibility(View.VISIBLE);
                    editProfileActionText.setVisibility(View.VISIBLE);

                    isAllFabsVisible = true;
                } else {
                    logoutFab.hide();
                    driverSignupFab.hide();
                    addEmergencyFab.hide();
                    editProfileFab.hide();
                    editDriverFab.hide();

                    logoutActionText.setVisibility(View.GONE);
                    driverSignupActionText.setVisibility(View.GONE);
                    addEmergencyActionText.setVisibility(View.GONE);
                    editProfileActionText.setVisibility(View.GONE);
                    editDriverActionText.setVisibility(View.GONE);

                    isAllFabsVisible = false;
                }
            });

            logoutFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });

            driverSignupFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    driverSignupFragment driverSignupFragment = new driverSignupFragment();
                    driverSignupFragment.setArguments(bundle);
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout, driverSignupFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                }
            });

            editProfileFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditProfileFragment editProfileFragment = new EditProfileFragment();
                    editProfileFragment.setArguments(bundle);
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout, editProfileFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });

            editDriverFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editDriverProfileFragment editDriverProfileFragment = new editDriverProfileFragment();
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout, editDriverProfileFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });

            return rootView;
    }

    // Retrieve and display user data
    public void GetUserDataFromDB() {
        loadingSpinner.setVisibility(View.VISIBLE);

        FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();
        if (User != null) {
            String userId = User.getUid();
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    loadingSpinner.setVisibility(View.GONE);

                    if (snapshot.exists()) {
                        String nameFromDB = snapshot.child("name").getValue(String.class);
                        String genderFromDB = snapshot.child("gender").getValue(String.class);
                        String phoneNumFromDB = snapshot.child("phoneNum").getValue(String.class);

                        accName.setText(nameFromDB);

                        if(genderFromDB.isEmpty()) {
                            accGender.setText("Not set");
                            accGender.setTextColor(Color.rgb(191, 191, 191));
                        } else {
                            accGender.setText(genderFromDB);
                        }

                        if(phoneNumFromDB.isEmpty()) {
                            accPhoneNum.setText("Not set");
                            accPhoneNum.setTextColor(Color.rgb(191, 191, 191));
                        } else {
                            accPhoneNum.setText(phoneNumFromDB);
                        }

                        bundle = new Bundle();
                        bundle.putString("name", nameFromDB);
                        bundle.putString("gender", genderFromDB);
                        bundle.putString("phoneNum", phoneNumFromDB);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    loadingSpinner.setVisibility(View.GONE);
                }
            });
        }
    }

    // If user is a driver, assign a badge
    public void checkDriverStatus() {
        FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();
        if (User != null) {
            String userId = User.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("driverID");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        badge.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
        }
    }
}
