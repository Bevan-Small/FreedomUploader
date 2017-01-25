package nz.co.smallcode.freedomuploader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SubmissionActivity extends AppCompatActivity {

    private static final String BRS_LOG_TAG = "BRS Test";

    private DatabaseReference mDatabaseRootReference = FirebaseDatabase.getInstance().getReference("Testing");
    private DatabaseReference mBoxReference;
    private DatabaseReference mIdReference;

    private StorageReference mStorageRootReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://freedom-b2cc8.appspot.com");
    private StorageReference mStorageGeneralDirectory = mStorageRootReference.child("Testing");

    // Full size image
    private StorageReference mStorageDirectoryFullsize = mStorageGeneralDirectory.child("Fullsize");
    private StorageReference mStorageBoxReferenceFullsize;
    private StorageReference mImageReferenceFullsize;

    // Scaled image
    private StorageReference mStorageDirectoryIcon = mStorageGeneralDirectory.child("Icons");
    private StorageReference mStorageBoxReferenceIcon;
    private StorageReference mImageReferenceIcon;


    private Submission mSubmission;
    private DatabaseAdventure mDatabaseAdventure;
    private String mId;
    private String mBox;

    private TextView mTitleView;
    private ImageView mPhotoView;
    private RatingBar mRatingBar;
    private TextView mDescriptionView;
    private TextView mAddressView;
    private TextView mCoordinateView;
    private TextView mTagView;
    private Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        // Set database persistence
        // FirebaseDatabase.getInstance().setPersistenceEnabled(true);


        // Set data
        Intent extras = getIntent();
        mSubmission = new Submission();
        setSubmissionData(extras);
        mDatabaseAdventure = new DatabaseAdventure(mSubmission);
        mId = mSubmission.getId();
        mBox = String.valueOf(mSubmission.getIndex());


        // Display data
        findViews();
        displayData();

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startChecksAndUpload();
            }
        });

    }


    /////////////////////////////////// Uploading and checks ///////////////////////////////////////

    /**
     * Handles checking the database for duplicate adventures and uploads if no conflicts
     */
    private void startChecksAndUpload(){
        // TODO check nearby activities before references are made to new boxes and ids to prevent empty nodes being created

        if (firebaseConnected()) {
            checkNearbyActivities();
            checkIdClash();
            uploadSubmission();
            checkUploadSuccessful();
        }
    }

    /**
     * Checks there isn't already an adventure at the submissions location
     * i.e. matching ids
     * Initiates upload if no id clash
     */
    private void checkIdClash(){
        // TODO inspect node at index for keys matching mSubmission id

        // Creating reference to specific location
        mBoxReference = mDatabaseRootReference.child(mBox);
        mIdReference = mBoxReference.child(mId);

        mIdReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get object at location of users submission (returns null if there isn't one)
                if (dataSnapshot.getValue(DatabaseAdventure.class) == null){
                    // Call upload submission
                    uploadSubmission();
                } else {
                    Log.e(BRS_LOG_TAG, "A post with this id already exists");
                    Toast.makeText(SubmissionActivity.this, "A post with this id already exists", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(BRS_LOG_TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    /**
     * Asks user to check their submission isn't a duplicate of nearby adventures
     */
    private void checkNearbyActivities(){
        // TODO display nearby activities to avoid doubleups
    }


    /**
     * Uploads users submission to database
     */
    private void uploadSubmission(){
        // TODO convert references to member variables
        // Uploading data to database
        DatabaseReference boxReference = mDatabaseRootReference.child(mBox);
        DatabaseReference idReference = boxReference.child(mId);
        idReference.setValue(mDatabaseAdventure);

        // Uploading photo to storage
        String index = String.valueOf(mSubmission.getIndex());
        String id = mSubmission.getId();

        // Full scale image
        mStorageBoxReferenceFullsize = mStorageDirectoryFullsize.child(index);
        mImageReferenceFullsize = mStorageBoxReferenceFullsize.child(id + "FS.png");
        uploadPhoto(mSubmission.getPhoto(), mImageReferenceFullsize);

        // Icon size image
        mStorageBoxReferenceIcon = mStorageDirectoryIcon.child(index);
        mImageReferenceIcon = mStorageBoxReferenceIcon.child(id + "IC.png");
        uploadPhoto(Bitmap.createScaledBitmap(mSubmission.getPhoto(), 48, 48, false), mImageReferenceIcon);

    }

    private void uploadPhoto(Bitmap bitmap, StorageReference imageReference){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
    }


    /**
     * Alerts user when submission is on database
     */
    private void checkUploadSuccessful(){
        // TODO check data is on database
        Toast.makeText(this,"Uploaded to database!", Toast.LENGTH_LONG).show();
    }


    private boolean firebaseConnected() {


        return true;
    }
    /////////////////////////////////// Retrieving and displaying data /////////////////////////////

    /**
     * Provides data from mSubmission to views
     */
    private void displayData() {
        mTitleView.setText(mSubmission.getTitle());
        mPhotoView.setImageBitmap(mSubmission.getPhoto());
        mRatingBar.setRating(mSubmission.getRating());
        mDescriptionView.setText(mSubmission.getDescription());
        mAddressView.setText(mSubmission.getAddress());
        mCoordinateView.setText(String.valueOf(mSubmission.getLatitude()
                + ", "
                + String.valueOf(mSubmission.getLongitude())));
        mTagView.setText(mSubmission.getTag1()
                + ", " + mSubmission.getTag2()
                + ", " + mSubmission.getTag3());
    }

    /**
     * Assign views to associated member variables
     */
    private void findViews() {
        mTitleView = (TextView)findViewById(R.id.textViewSubmissionTitle);
        mPhotoView = (ImageView) findViewById(R.id.imageViewSubmissionPhoto);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBarSubmission);
        mDescriptionView = (TextView) findViewById(R.id.textViewSubmissionDescription);
        mAddressView = (TextView) findViewById(R.id.textViewSubmissionAddress);
        mCoordinateView = (TextView) findViewById(R.id.textViewSubmissionCoordinates);
        mTagView = (TextView) findViewById(R.id.textViewSubmissionTags);
        uploadButton = (Button) findViewById(R.id.buttonSubmissionUpload);
    }

    /**
     * Gets data from intent extras and saves them into a Submission object
     *
     * @param extras intent received by SubmissionActivity
     */
    private void setSubmissionData(Intent extras) {

        mSubmission.setDatabaseData(extras.getStringExtra("countryCode"),
                extras.getLongExtra("index", 0L),
                extras.getStringExtra("title"),
                extras.getStringExtra("description"),
                extras.getStringExtra("address"),
                extras.getFloatExtra("rating", 0.0f),
                extras.getStringExtra("tag1"),
                extras.getStringExtra("tag2"),
                extras.getStringExtra("tag3"),
                extras.getDoubleExtra("longitude", 0.0),
                extras.getDoubleExtra("latitude", 0.0)
        );

        // Set photo
        try {
            mSubmission.setPhoto(BitmapFactory.decodeStream(this.openFileInput(extras.getStringExtra("photoFilename"))));
        } catch (IOException e) {
            Log.e("IOException ", "File not found: " + "myImage");
        }



    }

}
