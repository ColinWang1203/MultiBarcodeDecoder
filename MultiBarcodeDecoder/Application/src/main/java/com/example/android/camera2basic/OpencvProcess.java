package com.example.android.camera2basic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.core.Rect;

import java.util.ArrayList;

import static android.R.id.message;
import static com.example.android.camera2basic.R.attr.title;

/**
 * Created by user on 2017/6/27.
 */
public class OpencvProcess extends Activity implements MyAsyncTask.AsyncTaskResult<ArrayList<Rect>>{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opencvprocess);
        MyAsyncTask myAsyncTask = new MyAsyncTask(OpencvProcess.this);
        myAsyncTask.RectResult = this;
        myAsyncTask.execute();
    }

    @Override
    public void taskFinish( ArrayList<Rect> result ) {

        Intent intent = new Intent(this, MobileVisionDecoder.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("arrayList", result);
        intent.putExtras(bundle);
        this.startActivity(intent);
        finish();//prevent user from returning back to this activity
    }
}
