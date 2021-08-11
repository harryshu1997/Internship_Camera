package com.smewise.camera2.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.os.Handler;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.smewise.camera2.CDBHelper;
import com.smewise.camera2.CameraActivity;
import com.smewise.camera2.CmodeActivity;
import com.smewise.camera2.Global;
import com.smewise.camera2.R;
import com.smewise.camera2.callback.CameraUiEvent;
import com.smewise.camera2.manager.CameraSession;
import com.smewise.camera2.module.CameraFragment;
import com.smewise.camera2.module.ProfessionalModule;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Thread.sleep;


public class ProfessionalUI extends CameraBaseUI implements TextureView.SurfaceTextureListener {

    private final String TAG = this.getClass().getSimpleName();

    private RelativeLayout mRootView;

    private GestureTextureView mPreviewTexture;
    private AppCompatSeekBar mFocusLensBar;
    private AppCompatSeekBar mSensitivity;
    private SeekBar mExp;
    private SeekBar mZoom;
    private SeekBar mOptic_Zoom;


    private TextView zoomLevel;
    private TextView vSensitivity;
    private EditText vExp;

   // private EditText vExp;

   // private EditText vFrame;

    private ImageButton mNone, mAuto;

    private boolean isAuto = false;

    private boolean expChange = false;

    //cmode set global variables
    Global g = Global.getInstance();



    public ProfessionalUI(final Context context, Handler handler, CameraUiEvent event) {
        super(event);
        mRootView = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.module_professional_layout, null);
        mPreviewTexture = mRootView.findViewById(R.id.texture_preview);
        mPreviewTexture.setSurfaceTextureListener(this);
        mPreviewTexture.setGestureListener(this);

        mFocusLensBar = mRootView.findViewById(R.id.sb_focus_length);
        mFocusLensBar.setOnSeekBarChangeListener(mFocusLensChangerListener);

        mSensitivity = mRootView.findViewById(R.id.sb_sensitivity);
        mSensitivity.setOnSeekBarChangeListener(mSensitivityListener);
        vSensitivity = mRootView.findViewById(R.id.v_sensitivity);
        vSensitivity.setText("" + mSensitivity.getProgress());

        mZoom = mRootView.findViewById(R.id.zoom);
        mZoom.setOnSeekBarChangeListener(mZoomListener);
        zoomLevel = mRootView.findViewById(R.id.zoom_level);

        mOptic_Zoom = mRootView.findViewById(R.id.optic_zoom);
        mOptic_Zoom.setOnSeekBarChangeListener(mOpticZoomListener);

        mExp = mRootView.findViewById(R.id.ExpTime);
        mExp.setOnSeekBarChangeListener(ExpListener);


        vExp = mRootView.findViewById(R.id.ExpTime_v);
        vExp.setOnKeyListener(mExpListener);

        //initialize UI exp value 2021/6/18
        long min = 10783L;
        long max = 100000000L;
        long res = (mExp.getProgress() * (max-min))/10000;
        vExp.setText(Long.toString(res));

        //for c-mode
        g.setExpTime(res);

        mNone = mRootView.findViewById(R.id.none);
        mNone.setOnClickListener(mNoneListener);


        mAuto = mRootView.findViewById(R.id.auto);
        mAuto.setOnClickListener(mAutoListener);


    }

    @Override
    public void setUIClickable(boolean clickable) {
        super.setUIClickable(clickable);
        mPreviewTexture.setClickable(clickable);
    }

    @Override
    public RelativeLayout getRootView() {
        return mRootView;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        uiEvent.onPreviewUiReady(surface, null);

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        uiEvent.onPreviewUiDestroy();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // preview frame is ready when receive second frame
        if (frameCount == 2) {return;}
        frameCount++;
        if (frameCount == 2) {
            uiEvent.onAction(CameraUiEvent.ACTION_PREVIEW_READY, null);
            uiEvent.onSettingChange(CaptureRequest.CONTROL_AE_MODE, CameraCharacteristics.CONTROL_AE_MODE_OFF);
            uiEvent.onSettingChange(CaptureRequest.SENSOR_SENSITIVITY,mSensitivity.getProgress());
            try{
                //try to init preview
                uiEvent.onSettingChange(CaptureRequest.SENSOR_EXPOSURE_TIME,Long.parseLong(vExp.getText().toString()));
                //uiEvent.onSettingChange(CaptureRequest.SENSOR_FRAME_DURATION,Long.parseLong(vFrame.getText().toString())); //delete frame duration 2021/6/18
            }catch (Exception e){
                Log.e(TAG,"cannot initialize preview !!");
                e.printStackTrace();
            }
        }
    }

//    private ImageButton.OnClickListener mchnageBListener = new ImageButton.OnClickListener(){
//        @Override
//        public void onClick(View v) {//update view when press the button
//            //must set CONTROL_AE_MDE to off before any changes
//            uiEvent.onSettingChange(CaptureRequest.CONTROL_AE_MODE, CameraCharacteristics.CONTROL_AE_MODE_ON);
////            uiEvent.onSettingChange(CaptureRequest.SENSOR_EXPOSURE_TIME);
////            uiEvent.onSettingChange(CaptureRequest.CONTROL_SCENE_MODE);
////            uiEvent.onSettingChange(CaptureRequest.SENSOR_FRAME_DURATION);
//            uiEvent.onSettingChange(CaptureRequest.CONTROL_MODE,CameraCharacteristics.CONTROL_MODE_AUTO);
//        }
//    };

    private EditText.OnKeyListener mExpListener = new EditText.OnKeyListener(){
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                // uiEvent.onSettingChange(CaptureRequest.CONTROL_AE_MODE, CameraCharacteristics.CONTROL_AE_MODE_OFF);
                long val = 10783;
                String EXP = vExp.getText().toString();
                try{
                    val = Long.parseLong(EXP);
                    if(val < 10780 || val > 100000000){
                        throw new Exception();
                    }
                }catch (Exception e){
                    Toast.makeText(getRootView().getContext(),"Wrong EXP format! \n Range: (10783, 100000000)",Toast.LENGTH_SHORT).show();
                }
                uiEvent.onSettingChange(CaptureRequest.SENSOR_EXPOSURE_TIME,val);

                //change progress value
                int p;
                long min = 10783L;
                long max = 100000000L;
                long x = val*10000/(max-min);
                p = (int)x;
                mExp.setProgress(p);

                //for C mode
                g.setExpTime(val);

                System.out.println("set EXP to： " + val);
                return true;
            }
            return false;
        }
    };

    private SeekBar.OnSeekBarChangeListener mOpticZoomListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float res = 0.001f * progress;
            uiEvent.onSettingChange(CaptureRequest.LENS_FOCAL_LENGTH,res);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener ExpListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            long max = 100000000L;
            long min = 10783L;
            long res = (progress * (max-min))/10000;
            vExp.setText(Long.toString(res));
            uiEvent.onSettingChange(CaptureRequest.SENSOR_EXPOSURE_TIME,res);
            //for c mode
            g.setExpTime(res);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener mZoomListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float zoom_up = mZoom.getProgress();
            float zoom = zoom_up/100;
            zoomLevel.setText(Float.toString(zoom) + "x");
            uiEvent.onAction("zoom",zoom);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private ImageButton.OnClickListener mAutoListener = new ImageButton.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(isAuto){
                isAuto = false;
            }else{
                isAuto = true;
            }
            if(isAuto){

                uiEvent.onSettingChange(CaptureRequest.CONTROL_AE_MODE, CameraCharacteristics.CONTROL_AE_MODE_ON);
                uiEvent.onSettingChange(CaptureRequest.CONTROL_MODE,CameraCharacteristics.CONTROL_MODE_AUTO);
                mAuto.setBackgroundColor(Color.RED);
            }else{
                uiEvent.onSettingChange(CaptureRequest.CONTROL_AE_MODE, CameraCharacteristics.CONTROL_AE_MODE_OFF);
                mAuto.setBackgroundColor(Color.GRAY);
            }

        }
    };

    private ImageButton.OnClickListener mNoneListener = new ImageButton.OnClickListener(){
        @Override
        public void onClick(View v) {
            // set to auto mode
            mNone.setBackgroundColor(Color.BLUE);
            mAuto.setBackgroundColor(Color.GRAY);

        }
    };

    private SeekBar.OnSeekBarChangeListener mFocusLensChangerListener =
            new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float value = progress / (float) seekBar.getMax();
            uiEvent.onSettingChange(CaptureRequest.LENS_FOCUS_DISTANCE, value);
        }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };





    private SeekBar.OnSeekBarChangeListener mSensitivityListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            uiEvent.onSettingChange(CaptureRequest.SENSOR_SENSITIVITY, progress);
            vSensitivity.setText("" + progress);
            System.out.println("set sensitivity: " + seekBar.getProgress());
            //for C mode
            g.setSensitivity(progress);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mAuto.setBackgroundColor(Color.GRAY);
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
}
