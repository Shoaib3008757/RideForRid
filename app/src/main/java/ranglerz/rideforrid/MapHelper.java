package ranglerz.rideforrid;

/**
 * Created by Shoaib-anwar on 24-Nov-17.
 */
import com.google.android.gms.maps.model.LatLng;


public class MapHelper {


    public double latitude;
    public double longitude;

    public LatLng scr;
    public LatLng des;

    public String userName;
    public String email;
    public String carRegistratinNumber;
    public String password;
    public String confirmPassword;
    public String mobileNumber;


    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getCarRegistratinNumber() {
        return carRegistratinNumber;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCarRegistratinNumber(String carRegistratinNumber) {
        this.carRegistratinNumber = carRegistratinNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }





    public LatLng getScr() {
        return scr;
    }

    public LatLng getDes() {
        return des;
    }

    public void setScr(LatLng scr) {
        this.scr = scr;
    }

    public void setDes(LatLng des) {
        this.des = des;
    }



    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


}
