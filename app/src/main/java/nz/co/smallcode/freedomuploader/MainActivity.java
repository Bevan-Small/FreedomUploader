package nz.co.smallcode.freedomuploader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity  {

    private static final String BRS_LOG_TAG = "BRS Test";
    private static final String NO_ASSIGNED_COORDINATE = "200";
    private static final int IMAGE_GALLERY_REQUEST = 100;
    private static final int MAPS_LOCATION_REQUEST = 200;
    private static final double ZOOM_LEVEL = 11.0;
    private static final int PHOTO_MAX_DIMENSION = 960;

    Submission mSubmission = new Submission();
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

    }



    /////////////////////////////////// Location Handling //////////////////////////////////////////

    /**
     * Starts Google Maps activity when FIND LOCATION ON MAP button is pressed
     * Returns latitude and longitude strings with result code MAPS_LOCATION_REQUEST
     * @param view Find location on map button
     */
    public void findLocationOnMap(View view){
        // TODO launch map activity and get back location
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivityForResult(intent, MAPS_LOCATION_REQUEST);
    }


    /////////////////////////////////// Submission Handling ////////////////////////////////////////

    /**
     * Checks that all pertinent data has been entered and makes a submission to the firebase
     * database if data looks good
     * @param view
     */
    public void reviewData(View view) {

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

            Intent intent = new Intent(this, SubmissionActivity.class);
            intent.putExtra("photoFilename",createImageFromBitmap(mSubmission.getPhoto()));
            intent.putExtra("id",mSubmission.getId());
            intent.putExtra("countryCode",mSubmission.getCountryCode());
            intent.putExtra("index",mSubmission.getIndex());
            intent.putExtra("title",mSubmission.getTitle());
            intent.putExtra("description",mSubmission.getDescription());
            intent.putExtra("address",mSubmission.getAddress());
            intent.putExtra("rating",mSubmission.getRating());
            intent.putExtra("tag1",mSubmission.getTag1());
            intent.putExtra("tag2",mSubmission.getTag2());
            intent.putExtra("tag3",mSubmission.getTag3());
            intent.putExtra("longitude",mSubmission.getLongitude());
            intent.putExtra("latitude",mSubmission.getLatitude());

            startActivity(intent);

        }
    }


    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "submissionPhoto";
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
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

        // Calculating the "box"  number the submission fits into
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
     * @return true if data is good
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
        if (resultCode == RESULT_OK && data != null) {
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
            else if (requestCode == MAPS_LOCATION_REQUEST){
                // Set returned data to editText fields
                Bundle extras = data.getExtras();
                mEditTextLatitude.setText(extras.getString(MapsActivity.EXTRA_LATITUDE));
                mEditTextLongitude.setText(extras.getString(MapsActivity.EXTRA_LONGITUDE));

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
