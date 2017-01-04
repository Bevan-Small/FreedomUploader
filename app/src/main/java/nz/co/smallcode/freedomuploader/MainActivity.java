package nz.co.smallcode.freedomuploader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private static final String BRS_LOG_TAG = "BRS Test";
    private static final String NO_ASSIGNED_COORDINATE = "200";
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 100;
    private static final int IMAGE_GALLERY_REQUEST = 100;
    private static final double ZOOM_LEVEL = 11.0;
    private static final int PHOTO_MAX_DIMENSION = 960;

    private GoogleApiClient mGoogleApiClient;

    Submission mSubmission = new Submission();
    Location mLastLocation;
    Bitmap mPhotoBitmap;
    int mBitmapWindowOffset;

    ImageView mImageViewPhoto;
    EditText mEditTextLatitude;
    EditText mEditTextLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageViewPhoto = (ImageView)findViewById(R.id.imageViewPhoto);
        mEditTextLatitude = (EditText)findViewById(R.id.editTextLatitude);
        mEditTextLongitude = (EditText)findViewById(R.id.editTextLongitude);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    /////////////////////////////////// Activity Lifecycle /////////////////////////////////////////

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /////////////////////////////////// Location Handling //////////////////////////////////////////

    @Override
    public void onConnectionFailed(ConnectionResult result){
        // TODO handle connection failures once work on finding location is done
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        if (checkAndRequestLocation()) {
            try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);}
            catch (SecurityException e){
                Log.e(BRS_LOG_TAG,"Somehow permissions are both granted and not granted");}
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }



    /**
     * Checks for fine location permission. Requests permission if not granted
     * @return true if permission granted or API 22 or below
     */
    private boolean checkAndRequestLocation(){

       // Permission is granted automatically if sdk version is less than 23
        if (Build.VERSION.SDK_INT < 23){
            return true;
        }
        // Check that the permission to access fine location has been granted. If not, asks for permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);

        }

        // Returns true if permission granted
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Gets the phones location and sets the longitude and latitude views to match
     * @param view
     */
    public void setLocationAsCurrent(View view) {

        if (mLastLocation != null){

            // Display last location on screen
            mEditTextLatitude.setText(Double.toString(mLastLocation.getLatitude()));
            mEditTextLongitude.setText(Double.toString(mLastLocation.getLongitude()));

            // Check time elapsed since fix and display
            long timeFromBootToLocationFix = mLastLocation.getElapsedRealtimeNanos();
            long timeFromBoot = SystemClock.elapsedRealtimeNanos();
            long minimumFixAge = timeFromBoot - timeFromBootToLocationFix;

            long maxTimeInMinutes = minimumFixAge / (long)(6 * Math.pow(10,10)) +1;

            TextView locationAgeTextView = (TextView)findViewById(R.id.textViewLocationAge);
            locationAgeTextView.setText("Location is at least " + Long.toString(maxTimeInMinutes)
                    + " minutes old");

        }
        else {
            Toast.makeText(this, "No location found", Toast.LENGTH_LONG).show();
        }
    }


    /////////////////////////////////// Submission Handling ////////////////////////////////////////

    /**
     * Checks that all pertinent data has been entered and makes a submission to the firebase
     * database if data looks good
     * @param view
     */
    public void submitDataToDatabase(View view) {

        // Sets variables of mSubmission to their corresponding view values
        setSubmissionData();

        // Checks that mSubmission variables are good quality
        boolean dataGood = checkData();

        // TODO remove this code and the associated textview when satisfied with the id generation
        TextView idView = (TextView)findViewById(R.id.textViewGeneratedID);
        idView.setText(mSubmission.getId());

        // makes submission to database
        if (dataGood){
            Toast.makeText(this,"Successful!",Toast.LENGTH_LONG).show();
            // TODO submit data to data base
            // TODO check data isn't in database already
        }
    }


    /**
     *  Writes text field data to mSubmission variable
     */
    public void setSubmissionData() {

        EditText titleView = (EditText) findViewById(R.id.editTextTitle);
        String title = titleView.getText().toString();

        EditText descriptionView = (EditText) findViewById(R.id.editTextDescription);
        String description = descriptionView.getText().toString();

        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        int stars = ratingBar.getNumStars();

        EditText addressView = (EditText) findViewById(R.id.editTextAddress);
        String address = addressView.getText().toString();

        EditText latitudeView = (EditText) findViewById(R.id.editTextLatitude);
        String latText = latitudeView.getText().toString();
        // If no latitude has been entered, the latitude is assigned an illegal value
        if (latText.equals("")) {
            latText = NO_ASSIGNED_COORDINATE;
        }
        double latitude = Double.parseDouble(latText);

        EditText longitudeView = (EditText) findViewById(R.id.editTextLongitude);
        String longText = longitudeView.getText().toString();
        // If no longitude has been entered, the latitude is assigned an illegal value
        if (longText.equals("")) {
            longText = NO_ASSIGNED_COORDINATE;
        }
        double longitude = Double.parseDouble(longText);

        Spinner tag1Spinner = (Spinner) findViewById(R.id.spinnerTag1);
        String tag1 = tag1Spinner.getSelectedItem().toString();

        Spinner tag2Spinner = (Spinner) findViewById(R.id.spinnerTag2);
        String tag2 = tag2Spinner.getSelectedItem().toString();

        Spinner tag3Spinner = (Spinner) findViewById(R.id.spinnerTag3);
        String tag3 = tag3Spinner.getSelectedItem().toString();

        // Calculating the "box" the submission fits into
        long index = indexCalculator(latitude, longitude);

        // TODO make a country code table when another country is added
        String countryCode = "NZL";

        mSubmission.setDatabaseData(countryCode, index, title, description,
                address, stars, tag1, tag2, tag3, longitude, latitude);
    }

    /**
     * Checks data in mSubmission variable is of good enough quality for submission to database
     * Checks all necessary fields are filled
     * Subs noAddress for address if field blank
     * Checks coordinates are in legal bounds
     * @return
     */
    public boolean checkData(){
        // TODO abstract this away to Submission class?

        boolean dataGood = true;
        boolean fieldsFilled = true;
        boolean outOfBounds = false;
        double latitude = mSubmission.getLatitude();
        double longitude = mSubmission.getLongitude();


        if (mSubmission.getTitle().equals("")) {
            fieldsFilled = false;
        }

        if (mSubmission.getDescription().equals("")) {
            fieldsFilled = false;
        }

        if (mSubmission.getRating() == 0) {
            fieldsFilled = false;
        }

        // If no address has been entered, substitute noAddress
        if (mSubmission.getAddress().equals("")) {
            mSubmission.setAddress("noAddress");
        }

        if (latitude == 200 || longitude == 200) {
            fieldsFilled = false;
        } else if (latitude > 85.0 || latitude < -85.0 || longitude >= 180 || longitude <= -180) {
            outOfBounds = true;
        }

        if (!fieldsFilled) {
            Toast.makeText(this,"Please fill in all fields",Toast.LENGTH_LONG).show();
            dataGood = false;
        } else if (outOfBounds) {
            Toast.makeText(this, "Longitude must be between -180 and 180 degrees. Latitude must be " +
                    "between -85 and 85 degrees. Check Google Maps!", Toast.LENGTH_LONG ).show();
            dataGood = false;
        }

        return dataGood;
    }

    /**
     * Calculates the index of the box that the new submission falls into on the web mercator
     * projection
     * @param latitude
     * @param longitude
     * @return
     */
    public long indexCalculator(double latitude, double longitude) {
        // TODO verify this works 16 Dec 2016
        long boxesPerRow = 1024;
        double maxIJ = 256.0 * Math.pow(2.0, ZOOM_LEVEL);

        // Convert coords to radians
        double latRadians = latitude * Math.PI / 180.0;
        double longRadians = longitude * Math.PI / 180.0;

        // Calculate x/y coordinates for web mercator projection
        double x = 128.0 / Math.PI * Math.pow(2.0, ZOOM_LEVEL) * (longRadians + Math.PI);
        double y = 128.0 / Math.PI * Math.pow(2.0, ZOOM_LEVEL) * (Math.PI - Math.log(Math.tan((Math.PI / 4.0 + latRadians / 2.0))));

        // Calculate the column (i) and row (j) for the box the coordinates belong in
        long i = (long) (x * boxesPerRow / maxIJ);
        long j = (long) (y * boxesPerRow / maxIJ);

        // Calculate the index of the box
        long index = i + j * boxesPerRow;

        return index;
    }


    /////////////////////////////////// Image Handling /////////////////////////////////////////////

    /**
     * Allows users to pull an image up from the SD card. The image is received in onActivityResult()
     * @param view
     */
    public void selectAnImage(View view) {

        // Send user off to gallery/app that stores images to pick their picture
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);

        // Set data and type allowed(all image types)
        photoPickerIntent.setDataAndType(data, "image/*");

        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                // Working with image gallery return
                Uri imageUri = data.getData();
                InputStream inputStream;

                // TODO deal with photos that are too small
                try {
                    // Reading the image
                    inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);

                    // Scaling bitmap down if required
                    mPhotoBitmap = scaleBitmap(imageBitmap);

                    // Zeroing offset and setting image
                    mBitmapWindowOffset =0;
                    setCroppedBitmap();

                    inputStream.close();

                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                } catch (IOException e){
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    /**
     * Scales bitmap down so greatest dimension is PHOTO_MAX_DIMENSION
     * @param bitmap
     * @return
     */
    public static Bitmap scaleBitmap(Bitmap bitmap){
        // TODO prevent from scaling up
        double scaleFactor;
        double bitmapHeight = bitmap.getHeight();
        double bitmapWidth = bitmap.getWidth();


        if (bitmap.getHeight() > bitmap.getWidth()){
            scaleFactor = bitmapHeight/(double)PHOTO_MAX_DIMENSION;
        } else {
            scaleFactor = bitmapWidth/(double)PHOTO_MAX_DIMENSION;
        }

        if (scaleFactor>1) {
            return Bitmap.createScaledBitmap(bitmap, (int) (bitmapWidth / scaleFactor), (int) (bitmapHeight / scaleFactor), false);
        }

        else {return bitmap;}
    }


    /**
     * pushes image window along by offset and set to mSubmission and ImageView
     */
    public void setCroppedBitmap(){
        int startX;
        int startY;
        int width = mPhotoBitmap.getWidth();
        int height = mPhotoBitmap.getHeight();
        int dimension;

        // Establishing if photo is portrait or landscape
        if (width>height){
            startY = 0;
            startX = mBitmapWindowOffset;
            dimension = height;
        } else {
            startY = mBitmapWindowOffset;
            startX = 0;
            dimension = width;
        }

        // Setting cropped image in submission object and on screen
        mSubmission.setPhoto(Bitmap.createBitmap(mPhotoBitmap, startX, startY, dimension, dimension, null, false));
        mImageViewPhoto.setImageBitmap(mSubmission.getPhoto());
    }

    /**
     * Increment offset and set photo if legal
     * @param view
     */
    public void incrementOffset(View view){
        int width = mPhotoBitmap.getWidth();
        int height = mPhotoBitmap.getHeight();

        // If increment doesn't push the image window beyond bounds, then increment offset of crop
        // and set image
        int difference = Math.abs(width-height);
        if (mBitmapWindowOffset +10 < difference){
            mBitmapWindowOffset+=10;
            setCroppedBitmap();
        }
    }

    /**
     * Decrement offset and set photo if legal
     * @param view
     */
    public void decrementOffset(View view){
        // If increment doesn't push the image window beyond bounds, then increment offset of crop
        // and set image
        if (mBitmapWindowOffset -10 >= 0){
            mBitmapWindowOffset-=10;
            setCroppedBitmap();
        }
    }


}
