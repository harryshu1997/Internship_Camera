package com.smewise.camera2.module;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.smewise.camera2.CDBHelper;
import com.smewise.camera2.CameraActivity;
import com.smewise.camera2.CmodeActivity;
import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.callback.CameraUiEvent;
import com.smewise.camera2.callback.MenuInfo;
import com.smewise.camera2.callback.RequestCallback;
import com.smewise.camera2.data.CamListPreference;
import com.smewise.camera2.data.PrefListAdapter;
import com.smewise.camera2.manager.CameraSession;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.manager.DeviceManager;
import com.smewise.camera2.manager.FocusOverlayManager;
import com.smewise.camera2.manager.Session;
import com.smewise.camera2.manager.SingleDeviceManager;
import com.smewise.camera2.ui.CameraBaseMenu;
import com.smewise.camera2.ui.CameraMenu;
import com.smewise.camera2.ui.CameraSubMenu;
import com.smewise.camera2.ui.ProfessionalUI;
import com.smewise.camera2.utils.FileSaver;
import com.smewise.camera2.utils.MediaFunc;


public class ProfessionalModule extends CameraModule implements FileSaver.FileListener, CameraBaseMenu.OnMenuClickListener {

    private SurfaceTexture mSurfaceTexture;
    private ProfessionalUI mUI;
    private CameraSession mSession;
    private SingleDeviceManager mDeviceMgr;
    private FocusOverlayManager mFocusManager;
    private CameraMenu mCameraMenu;
    private static long ExposureTime;
    private static boolean isHDR;
    private static long duration;


    private static final String TAG = Config.getTag(ProfessionalModule.class);

    public static void setExp(long val){
        ExposureTime = val;
    }
    public static long getExposureTime(){
        return ExposureTime;
    }

    public static void setDuration(long val){
        duration = val;
    }
    public static long getDuration(){
        return duration;
    }
    public static void setIsHDR(boolean val){
        isHDR = val;
    }
    public static boolean getIsHDR(){
        return isHDR;
    }



    @Override
    protected void init() {

        mUI = new ProfessionalUI(appContext, mainHandler, mCameraUiEvent);
        mUI.setCoverView(getCoverView());
        mFocusManager = new FocusOverlayManager(getBaseUI().getFocusView(), mainHandler.getLooper());
        mFocusManager.setListener(mCameraUiEvent);
        mCameraMenu = new CameraMenu(appContext, R.xml.menu_preference2, mMenuInfo);
        mCameraMenu.setOnMenuClickListener(this);
        mDeviceMgr = new SingleDeviceManager(appContext, getExecutor(), mCameraEvent);
        mSession = new CameraSession(appContext, mainHandler, getSettings());
    }

    private void updateUI(CameraUiEvent event){
        //must set CONTROL_AE_MDE to off before any changes
        event.onSettingChange(CaptureRequest.CONTROL_AE_MODE, CameraCharacteristics.CONTROL_AE_MODE_OFF);
        event.onSettingChange(CaptureRequest.SENSOR_EXPOSURE_TIME);
        event.onSettingChange(CaptureRequest.CONTROL_SCENE_MODE);
        event.onSettingChange(CaptureRequest.SENSOR_FRAME_DURATION);
    }

    @Override
    public void start() {
        String cameraId = getSettings().getGlobalPref(
                CameraSettings.KEY_CAMERA_ID, mDeviceMgr.getCameraIdList()[0]);
        mDeviceMgr.setCameraId(cameraId);
        mDeviceMgr.openCamera(mainHandler);
        // when module changed , need update listener
        fileSaver.setFileListener(this);
        getBaseUI().setCameraUiEvent(mCameraUiEvent);
        getBaseUI().setMenuView(mCameraMenu.getView());
        addModuleView(mUI.getRootView());
        Log.d(TAG, "start module");
    }

    private DeviceManager.CameraEvent mCameraEvent = new DeviceManager.CameraEvent() {
        @Override
        public void onDeviceOpened(CameraDevice device) {
            super.onDeviceOpened(device);
            Log.d(TAG, "camera opened");
            mSession.applyRequest(Session.RQ_SET_DEVICE, device);
            enableState(Controller.CAMERA_STATE_OPENED);
            if (stateEnabled(Controller.CAMERA_STATE_UI_READY)) {
                mSession.applyRequest(Session.RQ_START_PREVIEW, mSurfaceTexture, mRequestCallback);
            }
        }

        @Override
        public void onDeviceClosed() {
            super.onDeviceClosed();
            disableState(Controller.CAMERA_STATE_OPENED);
            if (mUI != null) {
                mUI.resetFrameCount();
            }
            Log.d(TAG, "camera closed");
        }
    };

    private RequestCallback mRequestCallback = new RequestCallback() {
        @Override
        public void onDataBack(byte[] data, int width, int height) {
            super.onDataBack(data, width, height);
            saveFile(data, width, height, mDeviceMgr.getCameraId(),
                    CameraSettings.KEY_PICTURE_FORMAT, "CAMERA");
            mSession.applyRequest(Session.RQ_RESTART_PREVIEW);
        }

        @Override
        public void onViewChange(int width, int height) {
            super.onViewChange(width, height);
            getBaseUI().updateUiSize(width, height);
            mFocusManager.onPreviewChanged(width, height, mDeviceMgr.getCharacteristics());

        }

        @Override
        public void onAFStateChanged(int state) {
            super.onAFStateChanged(state);
            updateAFState(state, mFocusManager);
        }

        @Override
        public void onAEStateChanged(int state) {
            super.onAFStateChanged(state);

        }
    };

    @Override
    public void stop() {
        getBaseUI().setCameraUiEvent(null);
        getCoverView().showCover();
        mCameraMenu.close();
        mFocusManager.removeDelayMessage();
        mFocusManager.hideFocusUI();
        mSession.release();
        mDeviceMgr.releaseCamera();
        Log.d(TAG, "stop module");
    }

    private void takePicture() {
        mUI.setUIClickable(false);
        getBaseUI().setUIClickable(false);
        mSession.applyRequest(Session.RQ_TAKE_PICTURE, getToolKit().getOrientation());
    }

    /**
     * FileSaver.FileListener
     * @param uri image file uri
     * @param path image file path
     * @param thumbnail image thumbnail
     */
    @Override
    public void onFileSaved(Uri uri, String path, Bitmap thumbnail) {
        mUI.setUIClickable(true);
        getBaseUI().setUIClickable(true);
        getBaseUI().setThumbnail(thumbnail);
        MediaFunc.setCurrentUri(uri);
    }

    /**
     * callback for file save error
     * @param msg error msg
     */
    @Override
    public void onFileSaveError(String msg) {
        Toast.makeText(appContext,msg, Toast.LENGTH_LONG).show();
        mUI.setUIClickable(true);
        getBaseUI().setUIClickable(true);
    }


    private CameraUiEvent mCameraUiEvent = new CameraUiEvent() {
        @Override
        public void onPreviewUiReady(SurfaceTexture mainSurface, SurfaceTexture auxSurface) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            mSurfaceTexture = mainSurface;
            enableState(Controller.CAMERA_STATE_UI_READY);
            if (stateEnabled(Controller.CAMERA_STATE_OPENED)) {
                mSession.applyRequest(Session.RQ_START_PREVIEW, mSurfaceTexture, mRequestCallback);

            }

        }

        @Override
        public void onPreviewUiDestroy() {
            disableState(Controller.CAMERA_STATE_UI_READY);
            Log.d(TAG, "onSurfaceTextureDestroyed");
        }

        @Override
        public void onTouchToFocus(float x, float y) {
            mFocusManager.startFocus(x, y);
            mCameraMenu.close();
            CameraCharacteristics c = mDeviceMgr.getCharacteristics();
            MeteringRectangle focusRect = mFocusManager.getFocusArea(x, y, true);
            MeteringRectangle meterRect = mFocusManager.getFocusArea(x, y, false);
            mSession.applyRequest(Session.RQ_AF_AE_REGIONS, focusRect, meterRect);
        }

        @Override
        public void resetTouchToFocus() {
            if (stateEnabled(Controller.CAMERA_MODULE_RUNNING)) {
                mSession.applyRequest(Session.RQ_FOCUS_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            }
        }





        @Override
        public <T> void onSettingChange(CaptureRequest.Key<T> key, T value) {
            if (key == CaptureRequest.LENS_FOCUS_DISTANCE) {
                mSession.applyRequest(Session.RQ_FOCUS_DISTANCE, value);
            }else if(key == CaptureRequest.SENSOR_SENSITIVITY){
                mSession.applyRequest(Session.RQ_SENSOR_SENSITIVITY,value);
               // System.out.println("value: " +value+  "sensitivityyyyyy: " + mDeviceMgr.getCharacteristics());
            }else if(key == CaptureRequest.CONTROL_AE_MODE){
                mSession.applyRequest(Session.RQ_AE_MODE,value);
               // System.out.println("ae mode to!!!! " + value );
            }else if(key == CaptureRequest.CONTROL_MODE){
                mSession.applyRequest(Session.RQ_MODE,value);
            }else if(key == CaptureRequest.SENSOR_EXPOSURE_TIME){
                mSession.applyRequest(Session.RQ_SENSOR_EXPOSURE_TIME,value);
            }else if(key == CaptureRequest.SENSOR_FRAME_DURATION){
                mSession.applyRequest(Session.RQ_FRAME_DURATION,value);
            }else if(key == CaptureRequest.LENS_FOCAL_LENGTH){
                mSession.applyRequest(Session.RQ_FOCUS_LENGTH,value);
            }
        }

        @Override
        public <T> void onSettingChange(CaptureRequest.Key<T> key) {
            super.onSettingChange(key);
            if(key == CaptureRequest.SENSOR_EXPOSURE_TIME){
                //set exposure time
               // mSession.applyRequest(Session.RQ_SENSOR_EXPOSURE_TIME,ExposureTime); 2021/6/7
                Log.i(TAG,"view changed exposure time: " + ExposureTime);

            }
            else if(key == CaptureRequest.CONTROL_SCENE_MODE){
                Log.i(TAG,"Set HDR to: " + isHDR);
                mSession.applyRequest(Session.RQ_HDR,isHDR);
            }
            else if(key == CaptureRequest.SENSOR_FRAME_DURATION){
              //  mSession.applyRequest(Session.RQ_FRAME_DURATION,duration); 2021/6/7
                Log.i(TAG,"Set Frame Duration to: " + duration);

            }
            else{
                // adding more here
            }
        }

        @Override
        public <T> void onAction(String type, T value) {
            mCameraMenu.close();
            switch (type) {
                case CameraUiEvent.ACTION_CLICK:
                    handleClick((View) value);
                    break;
                case CameraUiEvent.ACTION_CHANGE_MODULE:
                    setNewModule((Integer) value);
                    break;
                case CameraUiEvent.ACTION_SWITCH_CAMERA:
                    break;
                case CameraUiEvent.ACTION_PREVIEW_READY:
                    getCoverView().hideCoverWithAnimation();
                    break;
                case "zoom":
                    mSession.applyRequest(Session.RQ_ZOOM,value);
                default:
                    break;
            }
        }
    };

    private MenuInfo mMenuInfo = new MenuInfo() {
        @Override
        public String[] getCameraIdList() {
            return mDeviceMgr.getCameraIdList();
        }

        @Override
        public String getCurrentCameraId() {
            return getSettings().getGlobalPref(CameraSettings.KEY_CAMERA_ID);
        }

        @Override
        public String getCurrentValue(String key) {
            return getSettings().getGlobalPref(key);
        }
    };

    /**
     * CameraBaseMenu.OnMenuClickListener
     * @param key clicked menu key
     * @param value clicked menu value
     */
    @Override
    public void onMenuClick(String key, String value) {
        switch (key) {
//            case CameraSettings.KEY_SWITCH_CAMERA:
//                switchCamera();
//                break;
            case CameraSettings.KEY_FLASH_MODE:
                getSettings().setPrefValueById(mDeviceMgr.getCameraId(), key, value);
                mSession.applyRequest(Session.RQ_FLASH_MODE, value);
                break;
            default:
                break;
        }
    }
    private void handleClick(View view) {
        switch (view.getId()) {
            case R.id.info:
                showDialog1();
                break;
            case R.id.btn_shutter:
                takePicture();
                break;
            case R.id.btn_setting:
                showSetting();
                break;
            case R.id.thumbnail:
                MediaFunc.goToGallery(appContext);
                break;
        }
    }

}
