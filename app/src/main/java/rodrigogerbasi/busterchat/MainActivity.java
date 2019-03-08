package rodrigogerbasi.busterchat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.content.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.graphics.Color;
import android.widget.Toast;

//Citation: a lot of this code is taken from stackoverflow or android api because
//we were never taught any of this
public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int MY_PERMISSION_REQUEST_CAMERA = 1;
    private static final int RESULT_LOAD_IMAGE = 2;
    private static final int RGB_MASK = 0x00FFFFFF;
    Button push;

    //Context context = getApplicationContext();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // setting up things
        Button takePic = (Button) findViewById(R.id.picButton);
        Button select = (Button) findViewById(R.id.selectButton);
        Button share = (Button) findViewById(R.id.shareButton);
        final Button brighten = (Button) findViewById(R.id.brightenButton);
        final Button inverse = (Button) findViewById(R.id.inverseButton);
        final Button darken = (Button) findViewById(R.id.darkenButton);
        push = (Button) findViewById(R.id.pushButton);
        //PERMISSIONS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_REQUEST_CAMERA);
        }
        //CAMERA BUTTON
        takePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dispatchTakePictureIntent();
            }
        });
        //SELECT BUTTON
        select.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dispatchImageSelectionIntent();
            }
        });
        //SHARE BUTTON
        share.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dispatchShareIntent();
            }
        });
        //BRIGHTEN BUTTON
        brighten.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                brighten();
            }
        });
        //DARKEN BUTTON
        darken.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                darken();
            }
        });
        //INVERSE BUTTON
        inverse.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                inverse();
            }
        });
        //PUSH BUTTON
        push.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(), "Thanks", Toast.LENGTH_LONG).show();

            }
        });


    }
    //OUTSIDE OF CREATE
    //Requesting permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                } else {
                    //if not granted
                    finish();
                    System.exit(0);
                }
                return;
            }
        }
    }
    //END of request permissinos for camera

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    //Start activities call this
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        ImageView view = (ImageView)findViewById(R.id.imageView);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            view.setImageBitmap(imageBitmap);
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmapImage = null;
            try {
                bitmapImage = decodeBitmap(selectedImage );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // Show the Selected Image on ImageView
            view.setImageBitmap(bitmapImage);

        }
        }

    //selecting mage

    private void dispatchImageSelectionIntent() {
        Intent imageSelectionIntent =
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(imageSelectionIntent, RESULT_LOAD_IMAGE);
    }

    public  Bitmap decodeBitmap(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        final int REQUIRED_SIZE = 100;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }
    //end of select image methods

    //SHARE IMAGE
    public void dispatchShareIntent(){
        ImageView view = (ImageView)findViewById(R.id.imageView);
        view.setDrawingCacheEnabled(true);
        Bitmap b = view.getDrawingCache();
        MediaStore.Images.Media.insertImage(getContentResolver(), b, "title", "description");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        BitmapDrawable bitmapDrawable = (BitmapDrawable) view.getDrawable();
        Bitmap image =  bitmapDrawable.getBitmap();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory().getPath()));
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    //bounding pixels so they dont do weird stuff
    private int bound(double color) {
        if(color > 255)
            return 255;

        if(color < 0)
            return 0;
        return (int) color;
    }

    //FILTERS
    public void brighten() {
        ImageView view = (ImageView)findViewById(R.id.imageView);
        Bitmap bit = ((BitmapDrawable) view.getDrawable()).getBitmap();
        bit = bit.copy(Bitmap.Config.ARGB_8888,true);
        for (int i = 0; i < bit.getWidth() ; i++) {
            for (int j = 0; j < bit.getHeight() ; j++) {
                int temp = bit.getPixel(i,j);
                double blue = (Color.blue(temp) + 25.5);
                double red = (Color.red(temp) + 25.5);
                double green = (Color.green(temp) + 25.5);
                bit.setPixel(i,j,Color.rgb(bound(red),bound(green),bound(blue)) );
            }
        }
        view.setImageBitmap(bit);
    } //end of bright

    public void darken(){
        ImageView view = (ImageView)findViewById(R.id.imageView);
        Bitmap bit = ((BitmapDrawable) view.getDrawable()).getBitmap();
        bit = bit.copy(Bitmap.Config.ARGB_8888,true);
        for (int i = 0; i < bit.getWidth() ; i++) {
            for (int j = 0; j < bit.getHeight() ; j++) {
                int temp = bit.getPixel(i,j);

                double blue = (Color.blue(temp) - 25.5);
                double red = (Color.red(temp) - 25.5);
                double green = (Color.green(temp) - 25.5);
                bit.setPixel(i,j,Color.rgb(bound(red),bound(green),bound(blue)) );
            }
        }
        view.setImageBitmap(bit);
    } //end of darken

    public void inverse(){
        ImageView view = (ImageView)findViewById(R.id.imageView);
        Bitmap bit = ((BitmapDrawable) view.getDrawable()).getBitmap();
        bit = bit.copy(Bitmap.Config.ARGB_8888,true);
        for (int i = 0; i < bit.getWidth() ; i++) {
            for (int j = 0; j <bit.getHeight() ; j++) {
                int temp = bit.getPixel(i,j);

                double blue = Math.abs(Color.blue(temp) - 255);
                double red = Math.abs(Color.red(temp) - 255);
                double green = Math.abs(Color.green(temp) - 255);
                bit.setPixel(i,j,Color.rgb(bound(red),bound(green),bound(blue)) );
            }
        }
        view.setImageBitmap(bit);
    }

}
