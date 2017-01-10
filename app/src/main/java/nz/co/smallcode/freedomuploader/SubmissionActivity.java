package nz.co.smallcode.freedomuploader;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class SubmissionActivity extends AppCompatActivity {

    private Submission mSubmission;
    private ImageView mPhotoView;
    private TextView mDescriptionView;
    private TextView mAddressView;
    private TextView mCoordinateView;
    private TextView mTagView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        // Set data
        Intent extras = getIntent();
        mSubmission = new Submission();
        setSubmissionData(extras);

        // Display data
        findViews();
        displayData();

    }


    /**
     * Provides data from mSubmission to views
     */
    private void displayData() {
        mPhotoView.setImageBitmap(mSubmission.getPhoto());
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
        mPhotoView = (ImageView) findViewById(R.id.imageViewSubmissionPhoto);
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
                extras.getLongExtra("rating", 0L),
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
