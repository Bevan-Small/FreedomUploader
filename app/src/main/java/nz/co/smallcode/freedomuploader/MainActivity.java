package nz.co.smallcode.freedomuploader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void setLocationAsCurrent(View view){
        // TODO fetch coordinates and display in textviews
    }

    public void submitDataToDatabase(View view){
        // TODO check all data fields filled
        // TODO enforce lat/long as numbers
        boolean fieldsFilled = true;
        boolean outOfBounds = false;

        EditText titleView = (EditText)findViewById(R.id.editTextTitle);
        String title = titleView.getText().toString();
        if (title.equals("")){
            fieldsFilled =false;
        }
        EditText descriptionView = (EditText)findViewById(R.id.editTextDescription);
        String description = descriptionView.getText().toString();
        if (description.equals("")){
            fieldsFilled =false;
        }
        RatingBar ratingBar = (RatingBar)findViewById(R.id.ratingBar);
        int stars = ratingBar.getNumStars();
        if (stars == 0){
            fieldsFilled = false;
        }
        EditText addressView = (EditText) findViewById(R.id.editTextAddress);
        String address = addressView.getText().toString();
        if (address.equals("")){
            address = "noAddress";
        }

        EditText latitudeView = (EditText) findViewById(R.id.editTextLatitude);
        String latText = latitudeView.getText().toString();
        if (latText.equals("")){
            latText =latText+"200";
        }
        double latitude = Double.parseDouble(latText);

        EditText longitudeView = (EditText) findViewById(R.id.editTextLongitude);
        String longText = longitudeView.getText().toString();
        if (longText.equals("")){
            longText = "200";
        }
        double longitude = Double.parseDouble(longText);

        if (latitude ==200 || longitude ==200){
            fieldsFilled =false;
        } else if (latitude > 85.0 || latitude < -85.0 || longitude >=180 || longitude <=-180){
            outOfBounds = true;
        }

        Spinner tag1Spinner = (Spinner) findViewById(R.id.spinnerTag1);
        String tag1 = tag1Spinner.getSelectedItem().toString();

        Spinner tag2Spinner = (Spinner) findViewById(R.id.spinnerTag2);
        String tag2 = tag2Spinner.getSelectedItem().toString();

        Spinner tag3Spinner = (Spinner) findViewById(R.id.spinnerTag3);
        String tag3 = tag3Spinner.getSelectedItem().toString();

        if (!fieldsFilled){
            CharSequence text = "Please fill in all fields";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        } else if (outOfBounds){
            CharSequence text = "Longitude must be between -180 and 180 degrees. Latitude must be " +
                    "between -85 and 85 degrees. Check Google Maps!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        }
        else {
            // TODO present data for review
            // TODO make a country code lookup table when other countries are included
            long index = indexCalculator(latitude, longitude);
            String countryCode = "NZL";

            Submission newSub = new Submission( countryCode, index,  title,  description,
                     address, stars, tag1, tag2, tag3, longitude, latitude);

            CharSequence text = "Success";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();

        }
    }

    public long indexCalculator(double latitude, double longitude) {
        // TODO verify this works 16 Dec 2016
        double zoom = 11.0;
        long boxesPerRow = 1024;
        double maxIJ = 256.0 * Math.pow(2.0,zoom);

        double latRadians = latitude*Math.PI/180.0;
        double longRadians = longitude*Math.PI/180.0;

        double x = 128.0/Math.PI * Math.pow(2.0,zoom)*(longRadians +Math.PI);
        double y = 128.0/Math.PI * Math.pow(2.0,zoom)*(Math.PI - Math.log(Math.tan((Math.PI/4.0 + latRadians/2.0))));

        long i = (long)(x*boxesPerRow / maxIJ);
        long j = (long)(y*boxesPerRow / maxIJ);

        long index = i + j*boxesPerRow;

        return index;
    }

}
