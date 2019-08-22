package com.example.android.camera2basic;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class MobileVisionDecoder extends Activity implements OnItemClickListener {
    boolean ShowAll = false;
    private String BarcodeImage_Path = "";
    private BarcodeDetector barcodeDetector;
    String decodeResult;
    ListView listView;
    List<RowItem> rowItems;
    String[] descriptions;
    Object[] images;
    int num =0;
    String[] titles;
    String[] CutImagePath;
    ArrayList<String> num_Success = new ArrayList<>();
    ArrayList<Rect> DrawRect = new  ArrayList<>();
    int mposition;
    int tapposition;

    Mat CapturedBarcodeImage = Imgcodecs.imread(Environment.getExternalStorageDirectory() + "/BarcodeImages/pic.jpg");

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decoder_layout);


        BarcodeImage_Path = Environment.getExternalStorageDirectory()+"/BarcodeImages/" ;
//        Bundle args = this.getIntent().getBundleExtra("BUNDLE");
//        DrawRect = (ArrayList<Rect>) args.getSerializable("RETURNRECT");
        DrawRect = (ArrayList<Rect>) this.getIntent().getExtras().getSerializable("arrayList");
        //restart if no rect
        if (DrawRect.size()==0){
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }
//        Log.e("colin","DrawRect x = "+DrawRect.get(0).x);
//        Log.e("colin","DrawRect y = "+DrawRect.get(0).y);
//        Log.e("colin","DrawRect width = "+DrawRect.get(0).width);
//        Log.e("colin","DrawRect height = "+DrawRect.get(0).height);
        num = DrawRect.size();
        Log.e("colin"," DrawRect.size() = "+DrawRect.size());


        titles = new String[num+2];
        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).build();
        images = new Object[num+2];
        descriptions = new String[num+2];
        CutImagePath = new String[num+2];

        // use original roi image to decode, if fail : decode the rotated image.
        Log.e("colin","time5");

        for(int n = 0; n < num; n++) {
            if (decode_result(n, "Barcode_roi_img_") == false) {
                Log.e("colin","use rotated");
                decode_result(n, "Barcode_roi_rotated_");
            }
        }

        Log.e("colin","time6");

        rowItems = new ArrayList<>();
        if(ShowAll == false)
        {
            num = num_Success.size();
//            Log.e("colin", "num_Success.size() = "+num_Success.size());
        }
        int item_index;
        for (int i = 0; i < num; i++) {
            if(ShowAll == false) {
                item_index = Integer.parseInt(num_Success.get(i));
//                Log.e("colin", "item_index = "+item_index);
            } else{
                item_index = i;
            }
            RowItem item = new RowItem((Bitmap)images[item_index], titles[item_index], descriptions[item_index]);
            rowItems.add(item);
        }
        //show all detected barcode
        images[num+1] = null;
        titles[num+1] = "Please Tap to Show";
        descriptions[num+1] = "Total "+num_Success.size()+" Barcode Detected ";
        RowItem item = new RowItem((Bitmap)images[num+1], titles[num+1], descriptions[num+1]);
        rowItems.add(0,item);

        listView = (ListView) findViewById(R.id.list);
        CustomListViewAdapter adapter = new CustomListViewAdapter(this,
                R.layout.list_item, rowItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        if (rowItems.isEmpty()){
            Log.e("colin","r1");
            final Activity activity = this;
            if (null != activity) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("No Barcode Found.");
                builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        //return to main
                        Intent intent = new Intent(activity, CameraActivity.class);
                        startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        tapposition = position;
        if (position==0){

            if(!new File(BarcodeImage_Path +"pic_drawRectALL.jpg").exists()) {
                Toast toast = Toast.makeText(getApplicationContext(), "Opening Location Image...", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 30);
                toast.show();
            }
            //do in background
            Runnable OpenImage = new OpenImageThread();
            new Thread(OpenImage).start();

        }else {

            if(ShowAll == false){
                position = Integer.parseInt(num_Success.get(position-1));
            }
            mposition = position;//for background thread to know the position
            // only show loading when first tapping
            if(!new File(BarcodeImage_Path +"pic_drawRect"+String.valueOf(mposition)+".jpg").exists()) {
                Toast toast = Toast.makeText(getApplicationContext(), "Opening Location Image...", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 30);
                toast.show();
            }
            //do in background
            Runnable OpenImage = new OpenImageThread();
            new Thread(OpenImage).start();
        }
    }

    public boolean decode_result(int n, String type){
        String file = new File(BarcodeImage_Path, type + n +".jpg").toString();
        Bitmap bm = BitmapFactory.decodeFile(file);
        images[n] = bm;

        Frame outputFrame = new Frame.Builder().setBitmap(bm).build();
        SparseArray<Barcode> barcode_decode_result = barcodeDetector.detect(outputFrame);
        String value = "";
        if(barcode_decode_result.size()>0) {
            Barcode barcode = barcode_decode_result.valueAt(0);
            value = barcode.rawValue;
        }
        if(!value.equals(""))
        {
            if (ShowAll == false){
                num_Success.add(String.valueOf(n));
                Log.e("colin", "String.valueOf(n) = "+String.valueOf(n));
            }

            decodeResult = value;
            CutImagePath[n] = BarcodeImage_Path + type + n +".jpg";
            descriptions[n] = "Content : "+decodeResult;
            titles[n] = ShowAll == false? String.valueOf(num_Success.size())+"th Barcode"+
                    ", Location(x,y) = ("+String.valueOf(DrawRect.get(n).x+DrawRect.get(n).width/2)
                    +","+String.valueOf(CapturedBarcodeImage.height()-(DrawRect.get(n).y+DrawRect.get(n).height/2))
                    +")":" Barcode : ";

                Log.e("colin", " barcode.rawValue = "+value);


            return true;
        }
        else
        {
            decodeResult = "Can not Decode";
            CutImagePath[n] = BarcodeImage_Path + type + n + ".jpg";
            descriptions[n] = decodeResult;
            titles[n] = "Unknown : ";
            return false;
        }
    }
    public class OpenImageThread implements Runnable {
        public void run() {
            Mat DrawImg = Imgcodecs.imread(Environment.getExternalStorageDirectory() + "/BarcodeImages/pic.jpg");
            if (tapposition==0){

                if(!new File(BarcodeImage_Path +"pic_drawRectALL.jpg").exists()) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Log.e("colin","run numsuccess "+num_Success);
                    for(int i=0;i<num_Success.size();i++){
                        int loopposition = Integer.parseInt(num_Success.get(i));
                        Point p1 = new Point();
                        Point p2 = new Point();
                        p1.x = DrawRect.get(loopposition).x;
                        p1.y = DrawRect.get(loopposition).y;
                        p2.x = DrawRect.get(loopposition).x + DrawRect.get(loopposition).width;
                        p2.y = DrawRect.get(loopposition).y + DrawRect.get(loopposition).height;
                        Imgproc.rectangle(DrawImg, p1, p2, new Scalar(200, 0, 0), 5);
                        Imgproc.putText(DrawImg,String.valueOf(i+1),p1,3,5,new Scalar(200,0,0),4);
                        Log.e("colin","ok");
                    }
                    Imgcodecs.imwrite(BarcodeImage_Path + "pic_drawRectALL.jpg", DrawImg);
                    intent.setDataAndType(Uri.parse("file://" + BarcodeImage_Path + "pic_drawRectALL.jpg"), "image/*");
                    startActivity(intent);
                }else{
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + BarcodeImage_Path + "pic_drawRectALL.jpg"), "image/*");
                    startActivity(intent);
                }

            }else{
                if(!new File(BarcodeImage_Path +"pic_drawRect"+String.valueOf(mposition)+".jpg").exists()) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Point p1 = new Point();
                    Point p2 = new Point();
                    p1.x = DrawRect.get(mposition).x;
                    p1.y = DrawRect.get(mposition).y;
                    p2.x = DrawRect.get(mposition).x + DrawRect.get(mposition).width;
                    p2.y = DrawRect.get(mposition).y + DrawRect.get(mposition).height;
                    Imgproc.rectangle(DrawImg, p1, p2, new Scalar(200, 0, 0), 5);
                    Imgproc.putText(DrawImg,String.valueOf(tapposition),p1,3,5,new Scalar(200,0,0),4);
                    Imgcodecs.imwrite(BarcodeImage_Path + "pic_drawRect" + String.valueOf(mposition) + ".jpg", DrawImg);
                    intent.setDataAndType(Uri.parse("file://" + BarcodeImage_Path + "pic_drawRect"+String.valueOf(mposition)+".jpg"), "image/*");
                    startActivity(intent);
                }else{
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + BarcodeImage_Path + "pic_drawRect"+String.valueOf(mposition)+".jpg"), "image/*");
                    startActivity(intent);
                }
            }

        }
    }
}
