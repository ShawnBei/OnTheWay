package uk.ac.uel.ontheway;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

public class PhotoActivity extends AppCompatActivity {

    GridView gv;
    ArrayList<File> list;
    String folderName = "OnTheWayPhotos";
    Button backToMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        File sdCard = Environment.getExternalStorageDirectory();
        File comPath =  new File(sdCard, folderName);

        list=imageReader(comPath);
        gv=(GridView)findViewById(R.id.gridView);
        GridAdapter newGrid = new GridAdapter();
        backToMain = findViewById(R.id.photoBackToMainButton);

        // load adapter
        gv.setAdapter(newGrid);

        // when click on single image
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getApplicationContext(),ViewImage.class).putExtra("img",list.get(position).toString()));

            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showMain = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(showMain);
            }
        });
    }

    // adapter for grid view
    class GridAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = getLayoutInflater().inflate(R.layout.single_grid,parent,false);
            ImageView iv = convertView.findViewById(R.id.imageView2);
            iv.setImageURI(Uri.parse(getItem(position).toString()));
            return convertView;
        }
    }

    // read images from local file
    ArrayList<File> imageReader(File root){
        ArrayList<File> a = new ArrayList<>();

        File[] files = root.listFiles();
        if(files != null) {
            for (int i = 0; i < files.length; i ++) {
                if (files[i].isDirectory()) {
                    a.addAll(imageReader(files[i]));

                } else {
                    if (files[i].getName().endsWith(".jpg")) {
                        a.add(files[i]);
                    }
                }
            }
        }
        else{
            Toast.makeText(PhotoActivity.this, "No Photos", Toast.LENGTH_SHORT).show();
        }
        return a;
    }






}