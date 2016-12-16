package nz.co.smallcode.freedomuploader;

import android.graphics.Bitmap;

/**
 * Created by Bevan on 15-Dec-16.
 */

public class Submission {

    private static int ID_COORDINATE_LENGTH = 11;
    private String id;
    private Bitmap photo;
    private String countryCode;
    private long index;
    private String title;
    private String description;
    private String address;
    private long rating;
    private String tag1;
    private String tag2;
    private String tag3;
    private double longitude;
    private double latitude;


    public Submission() {
        // default constructor required for uploading to Firebase
    }

    /**
     * Sets all data fields except the photo Bitmap
     * @param countryCode
     * @param index
     * @param title
     * @param description
     * @param address
     * @param rating
     * @param tag1
     * @param tag2
     * @param tag3
     * @param longitude
     * @param latitude
     */
    public void setDatabaseData(String countryCode, long index, String title, String description,
                                String address, long rating, String tag1, String tag2, String tag3,
                                double longitude, double latitude){
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

        this.id = generateId(this.countryCode, this.latitude, this.longitude);
    }

    /**
     * Creates the id of the submission in format CCCSYYY.YYYYYYSZZZ.ZZZZZZ
     * XXX - country code
     * YYY.YYYYYY - latitude
     * ZZZ.ZZZZZZ - longitude
     * S - sign (either "-" or "0")
     * @param countryCode
     * @param longitude
     * @param latitude
     * @return
     */
    private String generateId(String countryCode, double longitude, double latitude){

        StringBuilder idBuilder = new StringBuilder(countryCode);
        idBuilder.append(coordinateToParsedString(latitude));
        idBuilder.append(coordinateToParsedString(longitude));

        return idBuilder.toString();

    }


    /**
     * Takes a coordinate and parses it into a String of shape SAAA.AAAAAA
     * Does NOT check if coordinates are too large, so "." may migrate right
     * @param coordinate
     * @return
     */
    private String coordinateToParsedString(double coordinate){
        StringBuilder coordinateStringBuilder = new StringBuilder(Double.toString(coordinate));

        // Pad zeroes on front
        if (coordinateStringBuilder.substring(0, 1).equals("-")) {
            // Treats negative numbers
            if (coordinateStringBuilder.substring(2, 3).equals(".")) {
                coordinateStringBuilder.insert(1, "00");
            } else if (coordinateStringBuilder.substring(3, 4).equals(".")) {
                coordinateStringBuilder.insert(1, "0");
            }

        } else {
            // Treats positive numbers
            if (coordinateStringBuilder.substring(1, 2).equals(".")) {
                coordinateStringBuilder.insert(0, "000");
            } else if (coordinateStringBuilder.substring(2, 3).equals(".")) {
                coordinateStringBuilder.insert(0, "00");

            } else if (coordinateStringBuilder.substring(3, 4).equals(".")){
                coordinateStringBuilder.insert(0, "0");
            }
        }

        // Remove digits beyond sixth decimal place
        while (coordinateStringBuilder.length() > ID_COORDINATE_LENGTH) {
            coordinateStringBuilder.deleteCharAt(coordinateStringBuilder.length() - 1);
        }

        // Pad zeros on tail
        while (coordinateStringBuilder.length() < ID_COORDINATE_LENGTH) {
            coordinateStringBuilder.append("0");
        }

        return coordinateStringBuilder.toString();
    }



    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoto(Bitmap photo){
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public long getIndex() {
        return index;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public long getRating() {
        return rating;
    }

    public String getTag1() {
        return tag1;
    }

    public String getTag2() {
        return tag2;
    }

    public String getTag3() {
        return tag3;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
