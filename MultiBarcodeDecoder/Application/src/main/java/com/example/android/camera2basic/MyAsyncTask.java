package com.example.android.camera2basic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.angle;
import static android.R.attr.process;
import static android.R.attr.src;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.getRotationMatrix2D;

/**
 * Created by user on 2017/6/27.
 */


public class MyAsyncTask extends AsyncTask<Void,Void,ArrayList<Rect>> {

    public AsyncTaskResult<ArrayList<Rect>> RectResult = null;

    int num = 0;

    public interface AsyncTaskResult<T extends Object> {
        void taskFinish(T result);
    }

    //get parent activity to show ProgressDialog in parent activity
    ProgressDialog pd;
    private Activity mParentActivity;
    public MyAsyncTask(Activity parentActivity) {
        // TODO Auto-generated constructor stub
        super();
        mParentActivity = parentActivity;
    }

    //use ArrayList to initialize unknown size array
    ArrayList<Rect> Return_Rect = new ArrayList<>();


    @Override
    protected ArrayList<Rect> doInBackground(Void... voids) {

        Log.e("colin","time1");
        String BarcodeImage_Path = Environment.getExternalStorageDirectory() + "/BarcodeImages/";
        Mat CapturedBarcodeImage = Imgcodecs.imread(Environment.getExternalStorageDirectory() + "/BarcodeImages/pic.jpg");
        Mat CapturedBarcodeImage_grey = new Mat();
        Imgproc.cvtColor(CapturedBarcodeImage, CapturedBarcodeImage_grey, Imgproc.COLOR_RGB2GRAY);
        Log.e("colin","time1");
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Mat dst1 = new Mat();
        Mat dst2 = new Mat();
        Mat dst3 = new Mat();
        Mat dst4 = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat lines1 = new Mat();
        Log.e("colin","time1");
//        Imgproc.Sobel(CapturedBarcodeImage_grey, gradX, -1, 1, 0, -1, 1, 0);
//        Imgproc.Sobel(CapturedBarcodeImage_grey, gradY, -1, 0, 1, -1, 1, 0);
//        Log.e("colin","time1");
//        Core.addWeighted(gradX, 0.5, gradY, 0.5, 0, dst1);
        Imgproc.Canny(CapturedBarcodeImage_grey,dst1,50,100);
        Log.e("colin","time1");
//        Imgproc.threshold(dst1, dst2, 50, 255, Imgproc.THRESH_BINARY);
//        Log.e("colin","time1");
        dst2=dst1;

//        //Hough remove line will take a long time
//        Imgproc.HoughLinesP(dst2, lines1, 1, Math.PI /180,10,100,10);
//        for (int y=0;y<lines1.cols();y++) {
//            double[] vec = lines1.get(0, y);
//
//            double  x1 = vec[0],
//                    y1 = vec[1],
//                    x2 = vec[2],
//                    y2 = vec[3];
//
//            Point start = new Point(x1, y1);
//            Point end = new Point(x2, y2);
//            Imgproc.line(dst2, start, end, new Scalar(0, 0, 0), 10);
//        }

//        Imgproc.erode(dst2, dst3, new Mat(), new Point(-0.01, -0.01), 1);//eliminate white salt
//        Log.e("colin","time1");
        dst3 = dst2;

//        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(21,10));
//        Imgproc.morphologyEx(dst3, dst4, Imgproc.MORPH_CLOSE, element);

//        if (isTight = false){
//            Imgproc.dilate(dst3, dst4, new Mat(), new Point(-1, -1), 6);
//            Log.e("colin","(asy)isTight = false");
//        } else{
//            Imgproc.dilate(dst3, dst4, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 2)));
//            Log.e("colin","(asy)isTight = true");
//        }

        Imgproc.dilate(dst3, dst4, new Mat(), new Point(-1, -1), 6);

        Log.e("colin","time2");

//        Imgcodecs.imwrite(BarcodeImage_Path + "1.jpg", dst1);
//        Imgcodecs.imwrite(BarcodeImage_Path + "2.jpg", dst2);
//        Imgcodecs.imwrite(BarcodeImage_Path + "3.jpg", dst3);
        Imgcodecs.imwrite(BarcodeImage_Path + "4.jpg", dst4);
//        Log.e("colin","time3");

        Imgproc.findContours(dst4, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
//        dst3=dst4;
//        Imgproc.drawContours(dst3, contours, -1,new Scalar(255,0,0),3);
//        Imgcodecs.imwrite(BarcodeImage_Path + "3.jpg", dst3);
        double area_low = CapturedBarcodeImage.cols() * CapturedBarcodeImage.rows() / 345600 * 200;
        double area_high = CapturedBarcodeImage.cols() * CapturedBarcodeImage.rows() / 345600 * 8000;

//        Log.e("colin","hierarchy.size()"+hierarchy.size());
//        Log.e("colin","hierarchy.toString()"+hierarchy.toString());
//        Log.e("colin","hierarchy.toString()"+hierarchy.get(0,0).length);


        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint wrapper = contours.get(i);
            double area = Imgproc.contourArea(wrapper);

            if (area > area_low && area < area_high) {
                RotatedRect r = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));

                Rect r_rect = Imgproc.boundingRect(new MatOfPoint(contours.get(i).toArray()));

                //eliminate broken contour
                if (r.size.area()/area > 0.5 && r.size.area()/area < 1.5) {
                    //eliminate outer block by choosing contour with no parent and no previous
                    if (hierarchy.get(0,i)[3] == -1){
                        Log.e("colin","hierarchy.get(0,"+i+")[0] == "+hierarchy.get(0,i)[0]);//same
                        Log.e("colin","hierarchy.get(0,"+i+")[1] == "+hierarchy.get(0,i)[1]);//pre
                        Log.e("colin","hierarchy.get(0,"+i+")[2] == "+hierarchy.get(0,i)[2]);//child
                        Log.e("colin","hierarchy.get(0,"+i+")[3] == "+hierarchy.get(0,i)[3]);//parent
                        Mat boxMat = new Mat();
                        Imgproc.boxPoints(r, boxMat);

                        MatOfPoint2f allPointMatOfPoint = new MatOfPoint2f(boxMat);
                        Point[] allPoint = allPointMatOfPoint.toArray();

                        boolean touch_boarder = false;

                        //eliminate rectangles that touch boarders
                        if (allPoint[0].x <= 5 || allPoint[0].y <= 5 || allPoint[0].x >= CapturedBarcodeImage.width() - 5 || allPoint[0].y >= CapturedBarcodeImage.height() - 5
                                || allPoint[1].x <= 5 || allPoint[1].y <= 5 || allPoint[1].x >= CapturedBarcodeImage.width() - 5 || allPoint[1].y >= CapturedBarcodeImage.height() - 5
                                || allPoint[2].x <= 5 || allPoint[2].y <= 5 || allPoint[2].x >= CapturedBarcodeImage.width() - 5 || allPoint[2].y >= CapturedBarcodeImage.height() - 5
                                || allPoint[3].x <= 5 || allPoint[3].y <= 5 || allPoint[3].x >= CapturedBarcodeImage.width() - 5 || allPoint[3].y >= CapturedBarcodeImage.height() - 5) {
                            touch_boarder = true;
                        }

                        if (touch_boarder == false) {
                            Mat roi_img = new Mat(CapturedBarcodeImage, r_rect);
                            Mat roi_rotated = new Mat();
//                            int rad;
//                            if (roi_img.height() > roi_img.width()) {
//                                rad = roi_img.height();
//                            } else {
//                                rad = roi_img.width();
//                            }
//                            Mat roi_resize_image = new Mat();
//                            Size sz = new Size(rad, rad);
//                            Imgproc.resize(roi_img, roi_resize_image, sz);
//
//                            r.center.x = rad / 2;
//                            r.center.y = rad / 2;
//
//                            if (r.angle < -45.) {
//                                r.angle += 90.0;
//                                double temp = r.size.width;
//                                r.size.width = r.size.height;
//                                r.size.height = temp;
//                            }
//                             //rotate image
//                            Mat M = Imgproc.getRotationMatrix2D(r.center, r.angle, 1.0);
//                            Imgproc.warpAffine(roi_resize_image, roi_rotated, M, roi_resize_image.size(), Imgproc.INTER_LINEAR, 0, new Scalar(255, 255, 255));

                            Point roi_center = new Point();
                            roi_center.x = roi_img.width()/2;
                            roi_center.y = roi_img.height()/2;
                            Mat M = Imgproc.getRotationMatrix2D(roi_center, r.angle, 1.0);
                            Imgproc.warpAffine(roi_img, roi_rotated, M, roi_img.size(), Imgproc.INTER_LINEAR, 0, new Scalar(255, 255, 255));
//                            Rect CutRotatedRect = new Rect();
//                            CutRotatedRect.x = roi_rotated.width()/2-(int)r.size.width/2;
//                            CutRotatedRect.y = roi_rotated.height()/2-(int)r.size.height/2;
//                            CutRotatedRect.width = (int)r.size.width;
//                            CutRotatedRect.height = (int)r.size.height;
//                            Log.e("colin","roi_rotated.width() = "+roi_rotated.width());
//                            Log.e("colin","roi_rotated.height() = "+roi_rotated.height());
//                            Log.e("colin","r.width() = "+r.size.width);
//                            Log.e("colin","r.height() = "+r.size.height);
//                            Mat final_roi_rotated = new Mat(roi_rotated, CutRotatedRect);
//                            Imgcodecs.imwrite(BarcodeImage_Path + "Barcode_roi_img_test_" + num + ".jpg", final_roi_rotated);

                            Mat roi_img_bin = new Mat(dst4, r_rect);
                            Imgproc.threshold(roi_img_bin, roi_img_bin, 0.999, 1, Imgproc.THRESH_BINARY);
                            //need to fill at least 50% to eliminate rect boarder
                            if (IsFill(roi_img_bin, 50)) {

                                Imgcodecs.imwrite(BarcodeImage_Path + "Barcode_roi_img_" + num + ".jpg", roi_img);

                                Imgcodecs.imwrite(BarcodeImage_Path + "Barcode_roi_rotated_" + num + ".jpg", roi_rotated);

                                Return_Rect.add(num, r_rect);

                                num = num + 1;
                                Log.e("colin", "area " + area);
                                Log.e("colin", "loop num = " + num);
                            }
                        }
                    }
                }
            }
        }
        Log.e("colin","time3");
        Log.e("colin","num = "+num);
        return Return_Rect;
    }

    protected void onPostExecute(ArrayList<Rect> result) {
        this.RectResult.taskFinish(result);
        pd.dismiss();
    }
    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(mParentActivity);
        pd.setTitle("OpenCV");
        pd.setMessage("OpenCV Processing...");
        pd.show();
        super.onPreExecute();
    }

    //check if the cut rect full of contour
        public boolean IsFill(Mat roi_img_bin, double fill_threshold) {
            Mat roi_img_bin_white = roi_img_bin.clone();
            roi_img_bin_white.setTo(new Scalar(1));
            Mat compare_roi_rotated_abs = new Mat();
            Core.absdiff(roi_img_bin_white, roi_img_bin, compare_roi_rotated_abs);
            MatOfDouble mu = new MatOfDouble();
            MatOfDouble sigma = new MatOfDouble();
            Core.meanStdDev(compare_roi_rotated_abs,mu,sigma);
            Log.e("colin","mu = "+mu.get(0,0)[0]);
            //for 80% fill, (20*255+80*0)/100 = 51.1, thus avg value should lower than 51.1
            double trans_threshold = (100-fill_threshold)*1/100;
            return mu.get(0,0)[0] < trans_threshold ? true : false;
        }
}
