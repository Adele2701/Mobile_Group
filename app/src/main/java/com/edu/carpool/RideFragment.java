package com.edu.carpool;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RideFragment extends Fragment implements DriverRecyclerViewInterface{

    private CardView btn_time_date, btn_cus_req, btn_phone;
    private TextView tv_welcome, tv_get_driver, tv_driver_selected_name, tv_car_selected_plate, tv_car_selected_colour, tv_car_selected_model;
    private ConstraintLayout selected_driver_layout;
    private EditText input_from, input_to;
    private ConstraintLayout get_driver_layout, cl_from, cl_to;
    private FirebaseAuth mAuth;
    private Button book_button;
    private AlertDialog globalBuilder;
    private String scheduled_date, custom_req;
    private FirebaseDatabase db;
    private DatabaseReference dbReference;
    private int driver_pos;

    // Store instances of the driver model class
    ArrayList<driverModelClass> driverModels = new ArrayList<>();
    ArrayList<UserModelClass> userModels = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_ride, container, false);

        tv_get_driver = view.findViewById(R.id.tv_get_driver);
        tv_welcome = view.findViewById(R.id.tv_welcome);
        get_driver_layout = view.findViewById(R.id.get_driver_layout);
        input_from = view.findViewById(R.id.input_from);
        input_to = view.findViewById(R.id.input_to);
        cl_from = view.findViewById(R.id.cl_from);
        cl_to = view.findViewById(R.id.cl_to);
        book_button = view.findViewById(R.id.book_button);
        selected_driver_layout = view.findViewById(R.id.selected_driver_layout);
        tv_driver_selected_name = view.findViewById(R.id.tv_driver_selected_name);
        tv_car_selected_plate = view.findViewById(R.id.tv_car_selected_plate);
        tv_car_selected_colour = view.findViewById(R.id.tv_car_selected_colour);
        tv_car_selected_model = view.findViewById(R.id.tv_car_selected_model);
        btn_time_date = view.findViewById(R.id.btn_time_date);
        btn_cus_req = view.findViewById(R.id.btn_cus_req);
        btn_phone = view.findViewById(R.id.btn_phone);


        mAuth = FirebaseAuth.getInstance();

        cl_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
                //cl_from.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
                //cl_from.getLayoutTransition().setDuration(1000);
                //cl_from.getLayoutParams().height = height;
                //cl_from.requestLayout();

                // Show dialog box to prompt user to enter current address
                DialogBox("from");
            }
        });

        input_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show dialog box to prompt user to enter current address
                DialogBox("from");
            }
        });

        cl_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show dialog box to prompt user to enter destination address
                DialogBox("to");
            }
        });

        input_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show dialog box to prompt user to enter destination address
                DialogBox("to");
            }
        });

        // Let user to key in time and date for the appointment
        btn_time_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SingleDateAndTimePickerDialog.Builder(getContext())
                        .bottomSheet()
                        .curved()
                        //.stepSizeMinutes(15)
                        //.displayHours(false)
                        //.displayMinutes(false)
                        //.todayText("aujourd'hui")
                        .displayListener(new SingleDateAndTimePickerDialog.DisplayListener() {
                            @Override
                            public void onDisplayed(SingleDateAndTimePicker picker) {
                                // Retrieve the SingleDateAndTimePicker
                            }


                            public void onClosed(SingleDateAndTimePicker picker) {
                                // On dialog closed
                            }
                        })
                        .title("Choose your date and time")
                        .listener(new SingleDateAndTimePickerDialog.Listener() {
                            @Override
                            public void onDateSelected(Date date) {
                                ZonedDateTime zdt = date.toInstant().atZone(ZoneId.systemDefault());
                                DateTimeFormatter dtf =  new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("EEE MMM dd, yyyy h:mma")
                                        .toFormatter(Locale.getDefault());
                                String formatted_date = zdt.format(dtf);
                                Toast.makeText(requireContext(), "Appointment Schedule: " + formatted_date, Toast.LENGTH_LONG).show();
                                scheduled_date = formatted_date;
                            }
                        }).display();
            }
        });

        // Let user to key in custom request
        btn_cus_req.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_rounded1);

                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(60, 0, 100, 0);

                final EditText input = new EditText(getContext());
                layout.addView(input, params);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(layout);
                builder.setTitle("Custom request");
                input.setText(custom_req);

                builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Clear custom request input field
                        custom_req = "";
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        custom_req = input.getText().toString();
                    }
                });

                // Cancel the dialog box
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        // Let user to phone the driver
        btn_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validation - Check if the driver has the phone field or not
                try{
                    if (userModels.get(driver_pos).getPhoneNum() == null || userModels.get(driver_pos).getPhoneNum().equals("")){
                        Toast.makeText(requireContext(), "The driver does not have a phone number", Toast.LENGTH_SHORT).show();
                    } else {
                        String phone = userModels.get(driver_pos).getPhoneNum().toString();
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                        startActivity(intent);
                    }
                } catch (IndexOutOfBoundsException ex){
                    Toast.makeText(requireContext(), "Please select your driver first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Check whether user is logged in or not
        if (mAuth.getCurrentUser() == null) {
            // User is not signed in, show guest layout for user
            get_driver_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // When the layout is clicked, when redirect user to login page
                    clickAnim(view);
                    Intent intent = new Intent(getActivity(), Login.class);
                    startActivity(intent);
                }
            });

            book_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Redirect user to login page
                    Toast.makeText(requireContext(), "Log in now to book", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), Login.class);
                    startActivity(intent);
                }
            });
            return view;
        }


        // This section executes only when the user is logged in
        GetUserDataFromDB();

        // Show list of driver when clicked
        tv_get_driver.setText("Choose your driver");
        get_driver_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When the layout is clicked, when redirect user to driver page
                // Check whether the driver DB is empty or not
                clickAnim(view);
                getAllDriver();

            }
        });

        book_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do not allow user to book when the input fields are emtpy
                if (driverModels.size() == 0){
                    // when user is not selected
                    Toast.makeText(requireContext(), "Please select your driver", Toast.LENGTH_SHORT).show();
                } else if (input_from.getText().toString().isEmpty()){
                    // if from destination is empty, tell user to fill out
                    Toast.makeText(requireContext(), "Please enter your current location", Toast.LENGTH_SHORT).show();
                } else if (input_to.getText().toString().isEmpty()){
                    // if destination is empty, tell user to fill out
                    Toast.makeText(requireContext(), "Please enter your destination address", Toast.LENGTH_SHORT).show();
                } else if (scheduled_date == null || "".equals(scheduled_date)) {
                    // if date is empty, tell user to fill out
                    Toast.makeText(requireContext(), "Please enter your date and time", Toast.LENGTH_SHORT).show();
                } else {
                    // Show summary of the schedule before the user can book
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_rounded2);
                    View viewCustomTitle = getActivity().getLayoutInflater().inflate(R.layout.title_bar, null);
                    View viewCustomSummary = getActivity().getLayoutInflater().inflate(R.layout.booking_summary, null);
                    TextView tv_title = viewCustomTitle.findViewById(R.id.textView);
                    String title_text = "Your appointment summary";
                    tv_title.setText(title_text);
                    builder.setCustomTitle(viewCustomTitle);

                    TextView summary_driver_input = viewCustomSummary.findViewById(R.id.summary_driver_input);
                    TextView summary_to_input = viewCustomSummary.findViewById(R.id.summary_to_input);
                    TextView summary_from_input = viewCustomSummary.findViewById(R.id.summary_from_input);
                    TextView summary_custom_input = viewCustomSummary.findViewById(R.id.summary_custom_input);
                    TextView summary_schedule_input = viewCustomSummary.findViewById(R.id.summary_schedule_input);

                    summary_custom_input.setText(custom_req);
                    summary_driver_input.setText(userModels.get(driver_pos).getName().toString());
                    summary_to_input.setText(input_to.getText().toString());
                    summary_from_input.setText(input_from.getText().toString());
                    summary_schedule_input.setText(scheduled_date);

                    builder.setView(viewCustomSummary);

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    builder.setPositiveButton("Book", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Write to firebase
                            FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();

                            String from_address = input_from.getText().toString();
                            String to_address = input_to.getText().toString();
                            String driver_name = userModels.get(driver_pos).getName();
                            String driver_id = userModels.get(driver_pos).getID();
                            String date_time = scheduled_date;
                            String custom_request = custom_req;

                            // Status of the appointment is set to pending as default, it is
                            // up to the driver to accept or decline then the status
                            // will be set to either accepted or declined by there
                            String status = "Pending";
                            String user_id = User.getUid();
                            writeBookingToDB(from_address, to_address, driver_name, driver_id, date_time, custom_request, user_id, status);
                            Toast.makeText(requireContext(), "Your appointment has been booked", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();
                }
            }
        });

        return view;
    }

    // Retrieve and display user data
    public void GetUserDataFromDB(){
        FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();
        if (User != null) {
            String userId = User.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        //Only get first part of the name "Jason Lee" = "Jason"
                        String[] name = (snapshot.child("name").getValue(String.class)).split("\\s+");
                        String firstName = name[0];
                        tv_welcome.setText("Welcome, " + firstName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }
    }

    public void getAllDriver(){
        // Clear the array list after showing the alert dialog box
        // To prevent the array list from having the same value
        driverModels = new ArrayList<>();
        userModels = new ArrayList<>();
        RideFragment rideFragment = this;
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("users");
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot driverSnapshot : snapshot.getChildren()){
                    if (driverSnapshot.exists() && driverSnapshot.hasChild("driverID")){
                        String name = driverSnapshot.child("name").getValue(String.class);
                        String phoneNum = driverSnapshot.child("phoneNum").getValue(String.class);
                        String id = driverSnapshot.child("id").getValue(String.class);
                        for (DataSnapshot driverInfo : driverSnapshot.getChildren()){
                            for (DataSnapshot driverInnerInfo : driverInfo.getChildren()){
                                String carModel = driverInnerInfo.child("carModel").getValue(String.class);
                                String carColour = driverInnerInfo.child("carColour").getValue(String.class);
                                String carPlate = driverInnerInfo.child("carPlateNum").getValue(String.class);

                                userModels.add(new UserModelClass(id, name, null, phoneNum, null, null));
                                driverModels.add(new driverModelClass(null, null, carPlate, carModel, carColour));
                            }
                            break;
                        }
                    }
                }
                // Create the custom alert dialog box with the recycler view
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_rounded2);
                View viewCustomTitle = getActivity().getLayoutInflater().inflate(R.layout.title_bar, null);
                View viewRecycler = getActivity().getLayoutInflater().inflate(R.layout.recycler_parent, null);
                builder.setCustomTitle(viewCustomTitle);


                // Attach the adapter to the recycler view
                DriverAdapter adapter = new DriverAdapter(getContext(), driverModels, userModels, rideFragment);
                RecyclerView recyclerView = viewRecycler.findViewById(R.id.mRecyclerView);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                builder.setView(viewRecycler);

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                // Assign dialog box to global builder so that it can be closed programmatically in other method
                globalBuilder = alertDialog;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // error
            }
        });
    }

    // Dialog Box
    public void DialogBox(String choice){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_rounded1);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(60, 0, 100, 0);

        final EditText input = new EditText(getContext());
        layout.addView(input, params);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(layout);

        // Place already entered address into the edittext
        if (choice.equals("from")){
            builder.setTitle("Current Address");
            input.setText(input_from.getText().toString());
        } else if (choice.equals("to")){
            builder.setTitle("Destination Address");
            input.setText(input_to.getText().toString());
        }

        builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (choice.equals("from")){
                    input_from.getText().clear();
                } else if (choice.equals("to")){
                    input_to.getText().clear();
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String address = input.getText().toString();
                if (address.isEmpty()){
                    Toast.makeText(requireContext(), "Please enter your address", Toast.LENGTH_SHORT).show();
                } else {
                    if (choice.equals("from")){
                        input_from.setText(address);
                    } else if (choice.equals("to")){
                        input_to.setText(address);
                    }
                }
            }
        });

        // Cancel the dialog box
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void clickAnim(View view){
        Animator scale = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 1.1f, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 1.1f, 1)
        );
        scale.setDuration(200);
        scale.start();
    }

    private void writeBookingToDB(String from_address, String to_address, String driver_name, String driver_id, String date_time, String custom_request, String user_id, String status){

        // get sequence from firebase
        DatabaseReference appointmentRef = FirebaseDatabase.getInstance().getReference("appointment");
        appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String seq = Long.toString(snapshot.child("seq").getValue(Long.class));
                    AppointmentModelClass appointmentModelClass = new AppointmentModelClass(from_address, to_address, driver_name, driver_id, date_time, custom_request, user_id, status);
                    appointmentRef.child(seq).setValue(appointmentModelClass);

                    // increment seq value
                    appointmentRef.child("seq").setValue(snapshot.child("seq").getValue(Long.class) + 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    @Override
    public void onItemClick(int position) {
        // After the driver is selected, hide tv_get_driver then show the constraint layout
        // Does not allow the driver to select himself as a driver
        FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();
        String uid = User.getUid();
        if (userModels.get(position).getID().toString().equals(uid)) {
            Toast.makeText(requireContext(), "You are not allowed to select yourself as a driver", Toast.LENGTH_SHORT).show();
        } else {
            driver_pos = position;
            tv_get_driver.setVisibility(View.GONE);

            selected_driver_layout.setVisibility(View.VISIBLE);

            tv_driver_selected_name.setText(userModels.get(position).getName().toString());
            tv_car_selected_plate.setText(driverModels.get(position).getCarPlateNum().toString());
            tv_car_selected_colour.setText(driverModels.get(position).getCarColour().toString());
            tv_car_selected_model.setText(driverModels.get(position).getCarModel().toString());
            Toast.makeText(requireContext(), "Driver is selected", Toast.LENGTH_SHORT).show();
            globalBuilder.dismiss();
        }
    }
}