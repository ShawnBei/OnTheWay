package uk.ac.uel.ontheway;
import android.Manifest;

/**
 * Created by Major on 27/03/2018.
 */

public class Permissions {
    public static final String[] PERMISSIONS ={
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
    };

    public static final String[] CAMERA_PERMISSION ={
            Manifest.permission.CAMERA
    };

    public static final String[] WRITE_PERMISSION ={
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] READ_PERMISSION ={
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String[] LOCATION_PERMISSION ={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static final String[] INTERNET_PERMISSION ={
            Manifest.permission.INTERNET
    };

}
