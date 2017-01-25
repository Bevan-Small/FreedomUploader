package nz.co.smallcode.freedomuploader;

/**
 * Created by Bevan on 11-Jan-17.
 * Object used by firebase to write adventures to the database
 */

public class DatabaseAdventure {


    private String title;
    private String description;
    private String address;
    private double rating;
    private String tag1;
    private String tag2;
    private String tag3;
    private double longitude;
    private double latitude;
    private String countryCode;

    public DatabaseAdventure(){
        // Default constructor used by firebase
    }

    public DatabaseAdventure(Submission submission){
        title = submission.getTitle();
        description = submission.getDescription();
        address = submission.getAddress();
        rating= submission.getRating();
        tag1 = submission.getTag1();
        tag2 = submission.getTag2();
        tag3 = submission.getTag3();
        longitude = submission.getLongitude();
        latitude = submission.getLatitude();
        countryCode = submission.getCountryCode();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getTag1() {
        return tag1;
    }

    public void setTag1(String tag1) {
        this.tag1 = tag1;
    }

    public String getTag2() {
        return tag2;
    }

    public void setTag2(String tag2) {
        this.tag2 = tag2;
    }

    public String getTag3() {
        return tag3;
    }

    public void setTag3(String tag3) {
        this.tag3 = tag3;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
