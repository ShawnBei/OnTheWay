package uk.ac.uel.ontheway;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

public class ViewImage extends AppCompatActivity {
    ImageView iv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

//        Intent i =getIntent();
//        File f=i.getExtras().getParcelable("img");

        String f = getIntent().getStringExtra("img");

        iv2=(ImageView) findViewById(R.id.imageView3);
        iv2.setImageURI(Uri.parse(f));


    }
}