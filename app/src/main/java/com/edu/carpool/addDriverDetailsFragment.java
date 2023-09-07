package com.edu.carpool;

import static android.Manifest.permission.CAMERA;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class addDriverDetailsFragment extends Fragment {

    private TextView ID, show, errorMsg, errorMsg2;
    private ImageButton loadImageBtn, loadImageBtn2, snapImageBtn, snapImageBtn2;
    private Button confirmBtn;
    private ImageView displayImage, displayImage2;
    private DatabaseReference userRef, imgRef;
    private Drawable drawable, drawable2;
    private Uri targetUri, targetUri2;
    private byte[] byte1, byte2;

    static final int REQUEST_IMAGE_CAPTURE = 3;
    static final int REQUEST_IMAGE_CAPTURE2 = 4;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int orientation = getResources().getConfiguration().orientation;
        int layoutResId = (orientation == Configuration.ORIENTATION_LANDSCAPE) ?
                R.layout.fragment_add_driver_details_land : R.layout.fragment_add_driver_details;

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(layoutResId, container, false);

        ID = rootView.findViewById(R.id.driverID);
        show = rootView.findViewById(R.id.show);
        errorMsg = rootView.findViewById(R.id.message);
        loadImageBtn = rootView.findViewById(R.id.choose);
        snapImageBtn = rootView.findViewById(R.id.camera);
        displayImage = rootView.findViewById(R.id.select_image);
        confirmBtn = rootView.findViewById(R.id.confirm);
        errorMsg2 = rootView.findViewById(R.id.message2);
        loadImageBtn2 = rootView.findViewById(R.id.choose2);
        snapImageBtn2 = rootView.findViewById(R.id.camera2);
        displayImage2 = rootView.findViewById(R.id.select_image2);

        pref = requireContext().getSharedPreferences("DriverPref", Context.MODE_PRIVATE);
        editor = pref.edit();

        //display driverID and retrieve image, continue from last fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            String driver = bundle.getString("driverID");
            ID.setText("ID: " + driver);
            String userId = bundle.getString("users");

            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("driverID");
            imgRef = userRef.child('-' + driver).child("imagesDetails");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    imgRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot imageSnapshot : snapshot.getChildren()) {

                                    String drvLicenseURLFromDB = imageSnapshot.child("licenseUrl").getValue(String.class);
                                    String roadTaxURLFromDB = imageSnapshot.child("roadtaxUrl").getValue(String.class);
                                    String drvLicenseSnapURLFromDB = imageSnapshot.child("licenseByte").getValue(String.class);
                                    String roadTaxSnapURLFromDB = imageSnapshot.child("roadtaxByte").getValue(String.class);

                                    if (drvLicenseURLFromDB == null) {
                                        Picasso.get().load(drvLicenseSnapURLFromDB).into(displayImage);
                                        if (roadTaxURLFromDB == null) {
                                            Picasso.get().load(roadTaxSnapURLFromDB).into(displayImage2);
                                        } else if (roadTaxSnapURLFromDB == null) {
                                            Picasso.get().load(roadTaxURLFromDB).into(displayImage2);
                                        }
                                    } else if (drvLicenseSnapURLFromDB == null) {
                                        Picasso.get().load(drvLicenseURLFromDB).into(displayImage);
                                        if (roadTaxURLFromDB == null) {
                                            Picasso.get().load(roadTaxSnapURLFromDB).into(displayImage2);
                                        } else if (roadTaxSnapURLFromDB == null) {
                                            Picasso.get().load(roadTaxURLFromDB).into(displayImage2);
                                        }
                                    }
                                }

                            } else {
                                DatabaseReference dummy = imgRef.push();
                                dummy.child("uriLicense").setValue("");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
        }

        loadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

        loadImageBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

        snapImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    snapImage();
                } else {
                    reqPermission();
                }
            }
        });

        snapImageBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    snapImage2();
                } else {
                    reqPermission();
                }
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();

                    userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("driverID");

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                DataSnapshot shot = snapshot.getChildren().iterator().next();
                                String driverId = shot.getKey();

                                imgRef.child(driverId).child("imagesDetails");

                                String uri = pref.getString("targetUri", "");
                                String uri2 = pref.getString("targetUri2", "");
                                String img = pref.getString("imgByte", "");
                                String img2 = pref.getString("imgByte2", "");

                                drawable = displayImage.getDrawable();
                                drawable2 = displayImage2.getDrawable();

                                imgRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            for (DataSnapshot imageSnapshot : snapshot.getChildren()) {
                                                String imgDid = imageSnapshot.getKey();

                                                String drLUriFromDB = imageSnapshot.child("uriLicense").getValue(String.class);
                                                String roTUriFromDB = imageSnapshot.child("uriRoadtax").getValue(String.class);
                                                String drLByteFromDB = imageSnapshot.child("byteLicense").getValue(String.class);
                                                String roTByteFromDB = imageSnapshot.child("byteRoadtax").getValue(String.class);

                                                Toast msg = Toast.makeText(requireContext(), "Driver License Successfully Updated", Toast.LENGTH_SHORT);

                                                if (drawable == null && drawable2 == null) {
                                                    errorMsg.setVisibility(View.VISIBLE);
                                                    errorMsg2.setVisibility(View.VISIBLE);
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            errorMsg.setVisibility(View.GONE);
                                                            errorMsg2.setVisibility(View.GONE);
                                                        }
                                                    }, 2000);
                                                } else {
                                                    if ((uri.equals("") || uri2.equals("")) && !uri.equals(drLUriFromDB) && !uri2.equals(roTUriFromDB)) {
                                                        detectText(targetUri, null);
                                                        saveImageToDatabase(targetUri, null, imgDid, "uriLicense");
                                                        saveImageToDatabase(targetUri2, null, imgDid, "uriRoadtax");
                                                        ID.setText("1");
                                                        msg.show();

                                                    } else if ((img.equals("") || img2.equals("")) && !img.equals(drLByteFromDB) && !img2.equals(roTByteFromDB)) {
                                                        detectText(null, byte1);
                                                        saveImageToDatabase(null, byte1, imgDid, "byteLicense");
                                                        saveImageToDatabase(null, byte2, imgDid, "byteRoadtax");
                                                        ID.setText("2");
                                                        msg.show();

                                                    } else if ((uri.equals("") || img2.equals("")) && !uri.equals(drLUriFromDB) && !img2.equals(roTByteFromDB)) {
                                                        detectText(targetUri, null);
                                                        saveImageToDatabase(targetUri, null, imgDid, "uriLicense");
                                                        saveImageToDatabase(null, byte2, imgDid, "byteRoadtax");
                                                        ID.setText("3");
                                                        msg.show();

                                                    } else if ((img.equals("") || uri2.equals("")) && !img.equals(drLByteFromDB) && !uri2.equals(roTUriFromDB)) {
                                                        detectText(null, byte1);
                                                        saveImageToDatabase(null, byte1, imgDid, "byteLicense");
                                                        saveImageToDatabase(targetUri2, null, imgDid, "uriRoadtax");
                                                        ID.setText("4");
                                                        msg.show();

                                                    } else if (uri.equals("") && !uri.equals(drLUriFromDB)) {
                                                        detectText(targetUri, null);
                                                        saveImageToDatabase(targetUri, null, imgDid, "uriLicense");
                                                        ID.setText("5");
                                                        msg.show();
                                                    } else if (img.equals("") && !img.equals(drLByteFromDB)) {
                                                        detectText(null, byte1);
                                                        saveImageToDatabase(null, byte1, imgDid, "byteLicense");
                                                        ID.setText("6");
                                                        msg.show();

                                                    } else if (uri2.equals("") && !uri2.equals(roTUriFromDB)) {
                                                        saveImageToDatabase(targetUri2, null, imgDid, "uriRoadtax");
                                                        ID.setText("7");
                                                        msg.show();

                                                    } else if (img2.equals("") && !img2.equals(roTByteFromDB)) {
                                                        saveImageToDatabase(null, byte2, imgDid, "byteRoadtax");
                                                        ID.setText("8");
                                                        msg.show();

                                                    } else {
                                                        Toast.makeText(requireContext(), "No changes found", Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {}
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NotNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0) {
                targetUri = data.getData();
                Bitmap bitmap;
                try {
                    bitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(targetUri));
                    displayImage.setImageBitmap(bitmap);

                    editor.putString("targetUri", targetUri.toString());
                    editor.remove("imgByte");
                    editor.apply();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 1) {
                targetUri2 = data.getData();
                Bitmap bitmap;
                try {
                    bitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(targetUri2));
                    displayImage2.setImageBitmap(bitmap);

                    editor.putString("targetUri2", targetUri2.toString());
                    editor.remove("imgByte2");
                    editor.apply();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (data != null && data.getExtras() != null) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    if (bitmap != null) {
                        displayImage.setImageBitmap(bitmap);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte1 = stream.toByteArray();
                        String imgByte = Base64.getEncoder().encodeToString(byte1);

                        editor.putString("imgByte", imgByte);
                        editor.remove("targetUri");
                        editor.apply();
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE2) {
                if (data != null && data.getExtras() != null) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    if (bitmap != null) {
                        displayImage2.setImageBitmap(bitmap);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte2 = stream.toByteArray();
                        String imgByte2 = Base64.getEncoder().encodeToString(byte2);

                        editor.putString("imgByte2", imgByte2);
                        editor.remove("targetUri2");
                        editor.apply();
                    }
                }
            }
        }
    }

    //camera functions
    private boolean checkPermission() {
        int camPermission = ContextCompat.checkSelfPermission(requireContext(), CAMERA);
        return camPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void reqPermission() {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(requireActivity(), new String[]{CAMERA}, PERMISSION_CODE);
    }

    private void snapImage() {
        Intent snapPic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (snapPic.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivityForResult(snapPic, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void snapImage2() {
        Intent snapPic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (snapPic.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivityForResult(snapPic, REQUEST_IMAGE_CAPTURE2);
        }
    }

    //check validity of license
    private void detectText(Uri imageUri, byte[] bytes) {
        InputImage image = null;
        try {
            if (imageUri != null) {
                image = InputImage.fromFilePath(requireContext(), imageUri);
            } else if (bytes != null) {
                Bitmap bitM = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                image = InputImage.fromBitmap(bitM, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (image != null) {
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            Task<Text> result = recognizer.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(@NotNull Text text) {
                            StringBuilder result = new StringBuilder();
                            for (Text.TextBlock block : text.getTextBlocks()) {
                                String blockText = block.getText();
                                result.append(blockText).append(" "); //append block text with a space

                                Pattern datePattern = Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b");
                                Matcher matcher = datePattern.matcher(result.toString());

                                Date currentDate = new Date(); //get current date
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                while (matcher.find()) {
                                    String date = matcher.group();
                                    String dateform = dateFormat.format(currentDate);

                                    try {
                                        Date date1 = dateFormat.parse(dateform);
                                        Date date2 = dateFormat.parse(date);

                                        if (date1.before(date2)) {
                                            show.setVisibility(View.VISIBLE);
                                            show.setText("Valid license!");
                                        } else if (date1.after(date2)) {
                                            show.setVisibility(View.VISIBLE);
                                            show.setText("Invalid license! Exceed: " + date);
                                        } else {
                                            show.setVisibility(View.INVISIBLE);
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            show.setVisibility(View.GONE);
                                        }
                                    }, 4000);
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NotNull Exception e) {
                            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveImageToDatabase(Uri imageUri, byte[] imageByte, String imgDid, String
            uriKey) {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String imageName;

        // Determine the image name based on the uriKey
        if (uriKey.equals("uriLicense") || uriKey.equals("byteLicense")) {
            imageName = "license";
        } else if (uriKey.equals("uriRoadtax") || uriKey.equals("byteRoadtax")) {
            imageName = "road_tax";
        } else {
            imageName = "";
        }

        String imagePath = "users:" + userId + "/" + imageName + ".jpg";

        StorageReference storeRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storeRef.child(imagePath);

        UploadTask uploadTask;

        if (imageUri != null) {
            uploadTask = imageRef.putFile(imageUri);
        } else if (imageByte != null) {
            uploadTask = imageRef.putBytes(imageByte);
        } else {
            uploadTask = null;
        }

        if (uploadTask != null) {
            //download images URL, keep it and uri in DB
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        public void onSuccess(Uri downloadUrl) {

                            DatabaseReference childRef = imgRef.child(imgDid);

                            if (uriKey.equals("uriLicense") && imageByte == null) {
                                childRef.child("licenseUrl").setValue(downloadUrl.toString());
                                childRef.child(uriKey).setValue(imageUri.toString());

                                childRef.child("licenseByte").removeValue();
                                childRef.child("byteLicense").removeValue();

                            } else if (uriKey.equals("uriRoadtax") && imageByte == null) {
                                childRef.child("roadtaxUrl").setValue(downloadUrl.toString());
                                childRef.child(uriKey).setValue(imageUri.toString());

                                childRef.child("roadtaxByte").removeValue();
                                childRef.child("byteRoadtax").removeValue();

                            } else if (uriKey.equals("byteLicense") && imageUri == null) {
                                childRef.child("licenseByte").setValue(downloadUrl.toString());
                                String imgByte = Base64.getEncoder().encodeToString(imageByte);
                                childRef.child(uriKey).setValue(imgByte);

                                childRef.child("licenseUrl").removeValue();
                                childRef.child("uriLicense").removeValue();

                            } else if (uriKey.equals("byteRoadtax") && imageUri == null) {
                                childRef.child("roadtaxByte").setValue(downloadUrl.toString());
                                String imgByte2 = Base64.getEncoder().encodeToString(imageByte);
                                childRef.child(uriKey).setValue(imgByte2);

                                childRef.child("roadtaxUrl").removeValue();
                                childRef.child("uriRoadtax").removeValue();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NotNull Exception e) {
                            Toast.makeText(requireContext(), "Failed to download URL " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NotNull Exception e) {
                            Toast.makeText(requireContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}