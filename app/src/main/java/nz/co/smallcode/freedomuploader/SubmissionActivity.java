package nz.co.smallcode.freedomuploader;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class SubmissionActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRootReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseAdventure mDatabaseAdventure;

    private Submission mSubmission;
    private TextView mTitleView;
    private ImageView mPhotoView;
    private RatingBar mRatingBar;
    private TextView mDescriptionView;
    private TextView mAddressView;
    private TextView mCoordinateView;
    private TextView mTagView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        // Initialise database and set persistence
        // FirebaseDatabase.getInstance().setPersistenceEnabled(true);


        // Set data
        Intent extras = getIntent();
        mSubmission = new Submission();
        setSubmissionData(extras);
        mDatabaseAdventure = new DatabaseAdventure(mSubmission);

        // Display data
        findViews();
        displayData();

    }


    /////////////////////////////////// Uploading and checks ///////////////////////////////////////

    private void startChecksAndUpload(){
        checkIdClash();
        checkNearbyActivities();
        try {
            uploadSubmission();
        } catch (Exception e){
            Log.e("Failed"," dont know why");
        }
    }

    private void checkIdClash(){
        // TODO inspect node at index for keys matching mSubmission id
    }

    private void checkNearbyActivities(){
        // TODO display nearby activities to avoid doubleups
    }

    private void uploadSubmission(){
        String box = String.valueOf(mSubmission.getIndex());
        String id = mSubmission.getId();

        // mDatabaseReference.child(box).child(key).setValue(mDatabaseAdventure);
        // mDatabaseReference.push().setValue(mDatabaseAdventure);
        DatabaseReference boxReference = mDatabaseRootReference.child(box);
        DatabaseReference idReference = boxReference.child(id);
        boxReference.setValue(mDatabaseAdventure);
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
