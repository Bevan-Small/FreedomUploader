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
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final String BRS_LOG_TAG = "BRS Test";
    public static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 100;
    public static final int IMAGE_GALLERY_REQUEST = 100;
    Submission mSubmission = new Submission();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    /*
     * Gets the phones location and sets the longitude and latitude views to match
     *
     */

    public void setLocationAsCurrent(View view) {
        // TODO fetch coordinates and display in textviews
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

            Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            long deviceTime = SystemClock.elapsedRealtimeNanos();
            long timestamp = location.getElapsedRealtimeNanos();




            if ((deviceTime-timestamp) < 60000000000L){
                // Set long and lat as device location

                EditText latitiude = (EditText)findViewById(R.id.editTextLatitude);
                latitiude.setText(Double.toString(location.getLatitude()));


                EditText longitude = (EditText)findViewById(R.id.editTextLongitude);
                longitude.setText(Double.toString(location.getLongitude()));



            } else {
                Toast.makeText(this, "Please update your location",Toast.LENGTH_LONG).show();
            }


        }

    }

    /*
     * Checks that all pertinent data has been entered and makes a submission to the firebase
     * database if data looks good
     */

    public void submitDataToDatabase(View view) {
        // TODO check all data fields filled
        // TODO enforce lat/long as numbers
        boolean fieldsFilled = true;
        boolean outOfBounds = false;

        EditText titleView = (EditText) findViewById(R.id.editTextTitle);
        String title = titleView.getText().toString();
        if (title.equals("")) {
            fieldsFilled = false;
        }
        EditText descriptionView = (EditText) findViewById(R.id.editTextDescription);
        String description = descriptionView.getText().toString();
        if (description.equals("")) {
            fieldsFilled = false;
        }
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        int stars = ratingBar.getNumStars();
        if (stars == 0) {
            fieldsFilled = false;
        }
        EditText addressView = (EditText) findViewById(R.id.editTextAddress);
        String address = addressView.getText().toString();
        if (address.equals("")) {
            address = "noAddress";
        }

        EditText latitudeView = (EditText) findViewById(R.id.editTextLatitude);
        String latText = latitudeView.getText().toString();
        if (latText.equals("")) {
            latText = latText + "200";
        }
        double latitude = Double.parseDouble(latText);

        EditText longitudeView = (EditText) findViewById(R.id.editTextLongitude);
        String longText = longitudeView.getText().toString();
        if (longText.equals("")) {
            longText = "200";
        }
        double longitude = Double.parseDouble(longText);

        if (latitude == 200 || longitude == 200) {
            fieldsFilled = false;
        } else if (latitude > 85.0 || latitude < -85.0 || longitude >= 180 || longitude <= -180) {
            outOfBounds = true;
        }

        Spinner tag1Spinner = (Spinner) findViewById(R.id.spinnerTag1);
        String tag1 = tag1Spinner.getSelectedItem().toString();

        Spinner tag2Spinner = (Spinner) findViewById(R.id.spinnerTag2);
        String tag2 = tag2Spinner.getSelectedItem().toString();

        Spinner tag3Spinner = (Spinner) findViewById(R.id.spinnerTag3);
        String tag3 = tag3Spinner.getSelectedItem().toString();

        if (!fieldsFilled) {
            CharSequence text = "Please fill in all fields";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        } else if (outOfBounds) {
            CharSequence text = "Longitude must be between -180 and 180 degrees. Latitude must be " +
                    "between -85 and 85 degrees. Check Google Maps!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        } else {
            // TODO present data for review
            // TODO make a country code lookup table when other countries are included
            long index = indexCalculator(latitude, longitude);
            String countryCode = "NZL";

            mSubmission.setDatabaseData(countryCode, index, title, description,
                    address, stars, tag1, tag2, tag3, longitude, latitude);

            CharSequence text = "Success";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();

        }
    }

    /*
     * Calculates the index of the box that the new submission falls into on the web mercator
     * projection
     */

    public long indexCalculator(double latitude, double longitude) {
        // TODO verify this works 16 Dec 2016
        double zoom = 11.0;
        long boxesPerRow = 1024;
        double maxIJ = 256.0 * Math.pow(2.0, zoom);

        double latRadians = latitude * Math.PI / 180.0;
        double longRadians = longitude * Math.PI / 180.0;

        double x = 128.0 / Math.PI * Math.pow(2.0, zoom) * (longRadians + Math.PI);
        double y = 128.0 / Math.PI * Math.pow(2.0, zoom) * (Math.PI - Math.log(Math.tan((Math.PI / 4.0 + latRadians / 2.0))));

        long i = (long) (x * boxesPerRow / maxIJ);
        long j = (long) (y * boxesPerRow / maxIJ);

        long index = i + j * boxesPerRow;

        return index;
    }


    /*
     * Allows users to pull an image up from the SD card. It will present the image and
     * save the image uri in the Submission object
     */

    public void selectAnImage(View view) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);

        // Set data and type (all image types)
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
