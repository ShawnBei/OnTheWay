package uk.ac.uel.ontheway;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AlbumListActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri filePath;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    int travelIndex = -1;
    String newName = null;
    int imageSize = -1;
    String thisTravelName = "";
    ArrayList<String> imageNames = new ArrayList<>();
    Button backToTravelDetailButton2;
    Button addNewImageButton;
    Button chooseImageButton;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser user = mAuth.getCurrentUser();

    GridView gv;
    GridAdapter newGrid;
    ArrayList<File> list;
    String folderName = "OnTheWayPhotos";
    File defaultDir = Environment.getExternalStorageDirectory();

    ImageView albumImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);



        // get passed index
        if (getIntent().hasExtra("uk.ac.uel.TRAVEL_INDEX")){
            travelIndex = getIntent().getExtras().getInt("uk.ac.uel.TRAVEL_INDEX");
            thisTravelName = getIntent().getExtras().getString("uk.ac.uel.TRAVEL_NAME");
            imageSize = getIntent().getExtras().getInt("uk.ac.uel.IMAGE_COUNT");
        }
        else
            Toast.makeText(AlbumListActivity.this, "Error!", Toast.LENGTH_SHORT).show();

        // get images names
        getImageNames();


        // define components
        backToTravelDetailButton2 = (Button) findViewById(R.id.backToTravelDetailButton2);
        addNewImageButton = (Button) findViewById(R.id.addNewImageButton);
        chooseImageButton = (Button) findViewById(R.id.chooseImageButton);
        albumImageView = findViewById(R.id.albumImageView);
        albumImageView.setContentDescription("Seleted Image");
        gv = (GridView) findViewById(R.id.gridView);






        // load adapter
        //gv.setAdapter(newGrid);




        // when click on single image
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getApplicationContext(),ViewImage.class).putExtra("img",list.get(position).toString()));

            }
        });


        // upload image (click upload button)
        addNewImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });

        // click on choose button
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileFinder();
            }
        });

        // click on back button
        backToTravelDetailButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showTravelList = new Intent(AlbumListActivity.this, TravelDetailActivity.class);
                showTravelList.putExtra("uk.ac.uel.TRAVEL_INDEX", travelIndex);
                showTravelList.putExtra("uk.ac.uel.TRAVEL_NAME", thisTravelName);
                startActivity(showTravelList);
            }
        });
    }







    private void getImageNames(){
        mDatabase.child(user.getUid()).child("ImageNames").child(String.valueOf(travelIndex))
                .addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String newItem = dataSnapshot.getValue(String.class);
                        Log.d("New Item", newItem);
                        imageNames.add(newItem);

                        // get images from storage
                        getImagesFromDatabase();
                    }
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

//                        String stringIndex = dataSnapshot.getKey();
//                        Log.d("delete index", stringIndex);
//
//                        String stringValue = (String) dataSnapshot.getValue();
//                        Log.d("delete value", stringValue);
//
//                        int index = Integer.parseInt(stringIndex);
//
//                        if(index == imageNames.size()-1){
//                            imageNames.remove(index);
//                            // set adapter
//                        }
//                        else{
//                            if(index < imageNames.size()-1) {
//                                imageNames.remove(index);
//                                for (int i = 0; i < imageNames.size(); i++) {
//                                    mDatabase.child(user.getUid()).child("ImageNames").child(String.valueOf(i)).setValue(imageNames.get(i));
//                                }
//                                mDatabase.child(user.getUid()).child("ImageNames").child(String.valueOf(imageNames.size())).setValue(null);
//                            }
//                            else{
//                            }
//                        }
                    }
                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private void getImagesFromDatabase(){
        for (int i = imageNames.size() - 1; i > -1; i --){

            final ProgressDialog progressDialog = new ProgressDialog(AlbumListActivity.this); // test
            progressDialog.setTitle("Downloading...");
            progressDialog.show();

            StorageReference downloadRef = storageReference.child("images/" + thisTravelName + "/" + imageNames.get(i) + ".jpg");

            File createRootFile = new File(defaultDir + "/OnTheWayPhotos/" + thisTravelName);
            if (createRootFile.exists() || createRootFile.isDirectory()){
            }else{
                createRootFile.mkdir();
                Log.d("new folder", "created");
            }

            File localFile = new File(defaultDir + "/OnTheWayPhotos/" + thisTravelName + "/" + imageNames.get(i) + ".jpg");
            if(!localFile.exists()) {
                downloadRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        File comPath =  new File(defaultDir + "/" + folderName + "/" + thisTravelName);
                        list = imageReader(comPath);
                        newGrid = new GridAdapter();
                        gv.setAdapter(newGrid);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage()+ "!!!!!!", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage(((int) progress) + "% Downloaded...");
                    }
                });

            }else{
                progressDialog.dismiss();
                continue;
            }
        }
        File comPath =  new File(defaultDir + "/" + folderName + "/" + thisTravelName);
        list = imageReader(comPath);
        newGrid = new GridAdapter();
        gv.setAdapter(newGrid);
    }



    private void showFileFinder(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an Image"), PICK_IMAGE_REQUEST); // this will run onActivityResult()
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ){
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                albumImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadFile(){

        if (filePath != null) {

            // input image name
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(AlbumListActivity.this);
            final View mView = getLayoutInflater().inflate(R.layout.dialog_set_image_name, null);
            final EditText mNewImageName = mView.findViewById(R.id.newImageEditText);
            Button confirmButton = mView.findViewById(R.id.newImageConfirmButton);
            Button cancelButton = mView.findViewById(R.id.newImageCancelButton);
            final DatePickerDialog.OnDateSetListener mDateSetListener;

            mBuilder.setView(mView);
            final AlertDialog nameDialog = mBuilder.create();
            nameDialog.show();

            // when click confirm button
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!(mNewImageName.getText().toString().isEmpty())){
                        newName = mNewImageName.getText().toString();

                        nameDialog.hide(); // test

                        final ProgressDialog progressDialog = new ProgressDialog(AlbumListActivity.this); // test
                        progressDialog.setTitle("Uploading...");
                        progressDialog.show();

                        StorageReference riversRef = storageReference.child("images/"+ thisTravelName + "/" + newName + ".jpg");

                        riversRef.putFile(filePath)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        progressDialog.dismiss();
                                        mDatabase.child(user.getUid()).child("ImageNames").child(String.valueOf(travelIndex)).child(String.valueOf(imageNames.size())).setValue(newName);
                                        Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                                        progressDialog.setMessage(((int) progress) + "% Uploaded...");
                                    }
                                });
                        filePath = null;
                        albumImageView.setImageBitmap(null);
                        nameDialog.dismiss(); // test
                    }
                    else{
                        filePath = null;
                        albumImageView.setImageBitmap(null);
                        Toast.makeText(AlbumListActivity.this, "Please fill any empty fields!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // when click cancel button
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    filePath = null;
                    albumImageView.setImageBitmap(null);
                    nameDialog.dismiss();
                }
            });


        }else{
            // display a error toast
            Toast.makeText(getApplicationContext(), "Please select an image!", Toast.LENGTH_LONG).show();
        }
    }

    // adapter for grid view
    class GridAdapter extends BaseAdapter {

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
            Toast.makeText(AlbumListActivity.this, "No Photos", Toast.LENGTH_SHORT).show();
        }
        return a;
    }
}
