package com.riesenbeck.myblescanner;

/**
 * Created by Michael Riesenbeck on 22.09.2016.
 */
public class Camera {
    private static Camera cameraRef = null;
    private Camera(){

    }
    public Camera getInstance(){
        if (cameraRef == null){
            cameraRef = new Camera();
        }
        return cameraRef;
    }

}
