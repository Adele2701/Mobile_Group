package com.edu.carpool;

import static android.Manifest.permission.CAMERA;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
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

import androidx.cardview.widget.CardView;
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

public class addDriverDetailsFragment extends Fragment {

    private TextView ID, errorMsg, errorMsg2, resultTV;
    private ImageButton loadImageBtn, loadImageBtn2, snapImageBtn, snapImageBtn2;
    private Button confirmBtn;
    private ImageView displayImage, displayImage2;
    private DatabaseReference userRef;
    private Drawable drawable, drawable2;
    private Uri targetUri, targetUri2;
    private byte[] imgByte, imgByte2;

    static final int REQUEST_IMAGE_CAPTURE = 3;
    static final int REQUEST_IMAGE_CAPTURE2 = 4;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_driver_details, container, false);

        CardView view = rootView.findViewById(R.id.cardView);
        ID = rootView.findViewById(R.id.driverID);
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
            ID.setText("ID: " + bundle.getString("driverID"));
            String userId = bundle.getString("users");

            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("driverID");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        for (DataSnapshot driverSnapshot : snapshot.getChildren()) {
                            String drvLicenseURLFromDB = driverSnapshot.child("licenseUrl").getValue(String.class);
                            String roadTaxURLFromDB = driverSnapshot.child("roadtaxUrl").getValue(String.class);
                            String drvLicenseSnapURLFromDB = driverSnapshot.child("licenseByte").getValue(String.class);
                            String roadTaxSnapURLFromDB = driverSnapshot.child("roadtaxByte").getValue(String.class);

                            Picasso.get().load(drvLicenseURLFromDB).into(displayImage);
                            Picasso.get().load(roadTaxURLFromDB).into(displayImage2);
                            Picasso.get().load(drvLicenseSnapURLFromDB).into(displayImage);
                            Picasso.get().load(roadTaxSnapURLFromDB).into(displayImage2);
                        }
                    }
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
                                for (DataSnapshot driverSnapshot : snapshot.getChildren()) {
                                    String driverId = driverSnapshot.getKey();

                                    drawable = displayImage.getDrawable();
                                    drawable2 = displayImage2.getDrawable();

                                    String drvLicenseUriFromDB = driverSnapshot.child("uriLicense").getValue(String.class);
                                    String roadTaxUriFromDB = driverSnapshot.child("uriRoadtax").getValue(String.class);
                                    String drvLicenseByteFromDB = driverSnapshot.child("byteLicense").getValue(String.class);
                                    String roadTaxByteFromDB = driverSnapshot.child("byteRoadtax").getValue(String.class);

                                    String uri = pref.getString("targetUri", "");
                                    String uri2 = pref.getString("targetUri2", "");
                                    String img = pref.getString("imgByte", "");
                                    String img2 = pref.getString("imgByte", "");

                                    ID.setText(imgByte + " OMG " + imgByte2);

                                    if (drawable == null) {
                                        errorMsg.setVisibility(View.VISIBLE);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                errorMsg.setVisibility(View.GONE);
                                            }
                                        }, 2000);
                                    } else if (drawable2 == null) {
                                        errorMsg2.setVisibility(View.VISIBLE);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                errorMsg2.setVisibility(View.GONE);
                                            }
                                        }, 2000);
                                    } else if (!uri.equals(drvLicenseUriFromDB) && !uri2.equals(roadTaxUriFromDB)
                                            || !img.equals(drvLicenseByteFromDB) && !img2.equals(roadTaxByteFromDB)) {

                                        saveImageToDatabase(targetUri, imgByte, driverId, "uriLicense");
                                        saveImageToDatabase(targetUri2, imgByte2, driverId, "uriRoadtax");
                                        saveImageToDatabase(targetUri, imgByte, driverId, "byteLicense");
                                        saveImageToDatabase(targetUri2, imgByte2, driverId, "byteRoadtax");
                                        Toast.makeText(requireContext(), "Driver License Successfully Updated", Toast.LENGTH_SHORT).show();

                                    } else if (!uri.equals(drvLicenseUriFromDB) || !img.equals(drvLicenseByteFromDB)) {

                                        saveImageToDatabase(targetUri, imgByte, driverId, "uriLicense");
                                        saveImageToDatabase(targetUri, imgByte, driverId, "byteLicense");
                                        Toast.makeText(requireContext(),"Driver License Successfully Updated", Toast.LENGTH_SHORT).show();

                                    } else if (!uri2.equals(roadTaxUriFromDB) || !img2.equals(roadTaxByteFromDB)) {

                                        saveImageToDatabase(targetUri2, imgByte2, driverId, "uriRoadtax");
                                        saveImageToDatabase(targetUri2, imgByte2, driverId, "byteRoadtax");
                                        Toast.makeText(requireContext(), "Driver License Successfully Updated", Toast.LENGTH_SHORT).show();

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
                        byte[] imgByte = stream.toByteArray();

                        editor.putString("imgByte", imgByte.toString());
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
                        byte[] imgByte2 = stream.toByteArray();

                        editor.putString("imgByte2", imgByte2.toString());
                        editor.apply();
                    }
                }
            }
        }
    }

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

    private void detectText(Uri imageUri) {
        InputImage image = null;
        try {
            image = InputImage.fromFilePath(requireContext(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(@NotNull Text text) {
                        StringBuilder result = new StringBuilder();
                        for (Text.TextBlock block : text.getTextBlocks()) {
                            String blockText = block.getText();
                            Point[] blockCornerPoint = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            for (Text.Line line : block.getLines()) {
                                String lineText = line.getText();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                for (Text.Element element : line.getElements()) {
                                    String elementText = element.getText();
                                    result.append(elementText);
                                }

                                resultTV.setText(blockText);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NotNull Exception e) {
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveImageToDatabase(Uri imageUri, byte[] imageByte, String driverId, String uriKey) {

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

        if (imageUri != null && imageByte == null) {
            uploadTask = imageRef.putFile(imageUri);
        } else if (imageByte != null && imageUri == null) {
            uploadTask = imageRef.putBytes(imageByte);
        } else {
            Toast.makeText(requireContext(), "Nothing to update!", Toast.LENGTH_SHORT).show();
            uploadTask = null;
        }

        //download images URL and uri to keep in database
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    public void onSuccess(Uri downloadUrl) {

                        DatabaseReference childRef = userRef.child(driverId);

                        if (uriKey.equals("uriLicense")) {
                            childRef.child("licenseUrl").setValue(downloadUrl.toString());
                            childRef.child(uriKey).setValue(imageUri.toString());

                            childRef.child("licenseByte").removeValue();

                        } else if (uriKey.equals("uriRoadtax")) {
                            childRef.child("roadtaxUrl").setValue(downloadUrl.toString());
                            childRef.child(uriKey).setValue(imageUri.toString());

                            childRef.child("roadtaxByte").removeValue();

                        } else if (uriKey.equals("byteLicense")) {
                            childRef.child("licenseByte").setValue(downloadUrl.toString());
                            childRef.child(uriKey).setValue(imageByte.toString());

                            childRef.child("licenseUrl").removeValue();

                        } else if (uriKey.equals("byteRoadtax")) {
                            childRef.child("roadtaxByte").setValue(downloadUrl.toString());
                            childRef.child(uriKey).setValue(imageByte.toString());

                            childRef.child("roadtaxUrl").removeValue();

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