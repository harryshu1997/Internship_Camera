package com.smewise.camera2.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.os.Handler;
import android.support.v7.widget.AppCompatSeekBar;
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

import com.smewise.camera2.CameraActivity;
import com.smewise.camera2.R;
import com.smewise.camera2.callback.CameraUiEvent;
import com.smewise.camera2.manager.CameraSession;
import com.smewise.camera2.module.CameraFragment;
import com.smewise.camera2.module.ProfessionalModule;


public class ProfessionalUI extends CameraBaseUI implements TextureView.SurfaceTextureListener {

    private final String TAG = this.getClass().getSimpleName();

    private RelativeLayout mRootView;

    private GestureTextureView mPreviewTexture;
    private AppCompatSeekBar mFocusLensBar;
    private TextView mSensitivity;
    private ImageButton mchangeB;


    public ProfessionalUI(Context context, Handler handler, CameraUiEvent event) {
        super(event);
        mRootView = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.module_professional_layout, null);
        mPreviewTexture = mRootView.findViewById(R.id.texture_preview);
        mPreviewTexture.setSurfaceTextureListener(this);
        mPreviewTexture.setGestureListener(this);

        mFocusLensBar = mRootView.findViewById(R.id.sb_focus_length);
        mFocusLensBar.setOnSeekBarChangeListener(mFocusLensChangerListener);

        mSensitivity = mRootView.findViewById(R.id.et_sensitivity);
        mSensitivity.setOnEditorActionListener(mSensitivityListener);

        mchangeB = mRootView.findViewById(R.id.changeB);
        mchangeB.setOnClickListener(mchnageBListener);


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
        }
    }

    private ImageButton.OnClickListener mchnageBListener = new ImageButton.OnClickListener(){
        @Override
        public void onClick(View v) {//update view when press the button
            //must set CONTROL_AE_MDE to off before any changes
            uiEvent.onSettingChange(CaptureRequest.CONTROL_AE_MODE, CameraCharacteristics.CONTROL_AE_MODE_ON);
//            uiEvent.onSettingChange(CaptureRequest.SENSOR_EXPOSURE_TIME);
//            uiEvent.onSettingChange(CaptureRequest.CONTROL_SCENE_MODE);
//            uiEvent.onSettingChange(CaptureRequest.SENSOR_FRAME_DURATION);
            uiEvent.onSettingChange(CaptureRequest.CONTROL_MODE,CameraCharacteristics.CONTROL_MODE_AUTO);
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


    private EditText.OnEditorActionListener mSensitivityListener = new EditText.OnEditorActionListener(){

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            String ss = v.getText().toString();
            if(ss.length() >= 5 || ss.length() == 0){
                Toast.makeText(getRootView().getContext(),"长度不对！[0,3200]",Toast.LENGTH_SHORT).show();
                return false;
            }
            int senseVal = Integer.parseInt(ss);
            if(senseVal < 0 || senseVal > 3200){
                Toast.makeText(getRootView().getContext(),"Sensitivity Range should be [0,3200]",Toast.LENGTH_SHORT).show();
                return false;
            }
            uiEvent.onSettingChange(CaptureRequest.CONTROL_AE_MODE, CameraCharacteristics.CONTROL_AE_MODE_OFF);
            uiEvent.onSettingChange(CaptureRequest.SENSOR_SENSITIVITY,senseVal);
            return false;
        }
    };

}
