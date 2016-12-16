package nz.co.smallcode.freedomuploader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String BRS_LOG_TAG = "BRS Test";
    private static final String NO_ASSIGNED_COORDINATE = "200";
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 100;
    private static final int IMAGE_GALLERY_REQUEST = 100;
    private static final double ZOOM_LEVEL = 11.0;

    Submission mSubmission = new Submission();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }


    /**
     * Gets the phones location and sets the longitude and latitude views to match
     * @param view
     */
    public void setLocationAsCurrent(View view) {

        Context context = this;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // TODO make compatible with devices running API 22 and below
        //Check that the permission to access fine location has been granted. If not, asks for permission
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);

        }


        // If permission granted, fetch location
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // TODO change PASSIVE_PROVIDER to something that forces an update on GPS/network coords
            Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            long deviceTime = SystemClock.elapsedRealtimeNanos();
            long timestamp = location.getElapsedRealtimeNanos();

            if ((deviceTime - timestamp) < 60000000000L) {
                // Set long and lat views as device location
                EditText latitiude = (EditText) findViewById(R.id.editTextLatitude);
                latitiude.setText(Double.toString(location.getLatitude()));

                EditText longitude = (EditText) findViewById(R.id.editTextLongitude);
                longitude.setText(Double.toString(location.getLongitude()));


            } else {
                Toast.makeText(this, "Please update your location", Toast.LENGTH_LONG).show();
            }

        }
    }

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


    /*
     * Allows users to pull an image up from the SD card. It will present the image and
     * save the image uri in the Submission object
     */

    /**
     * Allows users to pull an image up from the SD card. The image is received in onActivityResult()
     *
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

                try {
                    // Reading the image
                    inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);

                    // Setting the submission object's photo as the picked image
                    mSubmission.setPhoto(imageBitmap);

                    // Setting the found picture to the imageview
                    ImageView photoView = (ImageView) findViewById(R.id.imageViewPhoto);
                    photoView.setImageBitmap(imageBitmap);


                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }

            }
        }
    }


}
