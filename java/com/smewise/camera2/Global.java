package com.smewise.camera2;

public class Global {
    boolean CMode ;
    int sensitivity;
    long ExpTime; //min = 10783
    long FrameDuration;
    String result = "";

    private static Global mInstance = null;

    protected Global(){
        CMode = false;
        sensitivity = 0;
        ExpTime = 10783;
        FrameDuration = 0;
    };

    public static synchronized Global getInstance(){
        if(null == mInstance){
            mInstance = new Global();
        }
        return mInstance;
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    public long getExpTime() {
        return ExpTime;
    }

    public void setExpTime(long expTime) {
        ExpTime = expTime;
    }

    public long getFrameDuration() {
        return FrameDuration;
    }

    public void setFrameDuration(long frameDuration) {
        FrameDuration = frameDuration;
    }

    public String getResult() {
        this.result = "Sensitivity: " + this.sensitivity + "\n" + "Exposure Time: " + this.ExpTime +  "\n" + "FrameDuration: " + this.FrameDuration;
        return result;
    }

    public boolean getCMode(){
        return this.CMode;
    }

    public void setCMode(boolean set){
        this.CMode = set;
    }
}
