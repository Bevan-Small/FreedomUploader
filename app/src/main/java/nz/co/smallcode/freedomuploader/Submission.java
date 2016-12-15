package nz.co.smallcode.freedomuploader;

/**
 * Created by Bevan on 15-Dec-16.
 */

public class Submission {

    public String countryCode;
    public long index;
    public String title;
    public String description;
    public String address;
    public long rating;
    public String tag1;
    public String tag2;
    public String tag3;
    public double longitude;
    public double latitude;


    public Submission() {
        // default constructor required for uploading to Firebase
    }

    public Submission(String countryCode, long index, String title, String description,
                      String address, long rating, String tag1, String tag2, String tag3,
                      double longitude, double latitude) {
        this.countryCode = countryCode;
        this.index = index;
        this.title = title;
        this.description = description;
        this.address = address;
        this.rating = rating;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.longitude = longitude;
        this.latitude = latitude;

    }

}
