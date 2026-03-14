package com.blb.shpbot;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.graphics.Rect;
import android.content.SharedPreferences;

import java.util.List;

public class MyAccessibilityService extends AccessibilityService {

    public static MyAccessibilityService instance;


    String TAG="SHPBOT";

    int STEP=0;

    long lastAction=0;

    public boolean botPaused = false;
    boolean lastEnabledState = true;


    Handler handler=new Handler();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        SharedPreferences prefs =
                getSharedPreferences("bot",MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("enabled",true);
        botPaused = !enabled;

        instance = this;   // TAMBAHKAN BARIS INI

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        info.feedbackType =
                AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.notificationTimeout = 100;

        info.flags =
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                        AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;

        setServiceInfo(info);

        Log.d(TAG,"Accessibility Connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event){

        SharedPreferences prefs =
                getSharedPreferences("bot",MODE_PRIVATE);

        boolean enabled = prefs.getBoolean("enabled",true);

        if(enabled != lastEnabledState){

            if(enabled){
                Log.d(TAG,"BOT RESUMED");
            }else{
                Log.d(TAG,"BOT PAUSED");
            }

            lastEnabledState = enabled;
        }

        if(!enabled) return;

        if(event==null) return;

        if(event.getPackageName()==null) return;

        if(!event.getPackageName().toString().equals("com.shopee.id")) return;

        if(!waitUI()) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if(root==null) return;

        Log.d(TAG,"ROOT HASH : "+root.hashCode());



        int page = detectPage(root);

        Log.d(TAG,"PAGE : "+page);



        // =========================
        // HALAMAN HOME
        // =========================
        if(page == 0){

            Log.d(TAG,"PAGE HOME");
            clickText(root,"Live & Video");

        }



        // =========================
        // HALAMAN LIVE VIDEO
        // =========================
        else if(page == 1){

            Log.d(TAG,"PAGE LIVE VIDEO");
            AccessibilityNodeInfo root2 = getFreshRoot();
            if(root2 != null){

                dumpText(root2);

                if(clickCreateIcon(root2)){

                    Log.d(TAG,"Klik icon Create");

                }else{

                    Log.d(TAG,"Create belum ditemukan → retry");

                    handler.postDelayed(() -> {

                        AccessibilityNodeInfo rootRetry = getFreshRoot();

                        if(rootRetry != null){

                            dumpText(rootRetry);

                            clickCreateIcon(rootRetry);

                        }

                    },400);

                }

            }

        }



        // =========================
        // HALAMAN CAMERA / GALERI
        // =========================
        else if(page == 2){

            Log.d(TAG,"PAGE CAMERA");

            swipeUp();
            Log.d(TAG,"swipe");
            handler.postDelayed(() -> {

                AccessibilityNodeInfo root2 = getRootInActiveWindow();

                if(root2 != null){

                    if(clickText(root2,"Draf")){

                        Log.d(TAG,"Klik:Draf");

                    }

                }

            },500);

        }



        // =========================
        // HALAMAN DRAFT
        // =========================
        else if(page == 4){

            Log.d(TAG,"PAGE DRAFT");
            AccessibilityNodeInfo root2 = getRootInActiveWindow();

            if(root2 != null){

                if(clickFirstVideo(root2)){

                    Log.d(TAG,"Video pertama diklik");

                }

            }

        }



        // =========================
        // HALAMAN EDITOR
        // =========================
        else if(page == 5){

            Log.d(TAG,"PAGE EDITOR");
            if(clickText(root,"Next") || clickText(root,"Lanjut")){

                Log.d(TAG,"Klik Next / Lanjut");

            }

        }



        // =========================
        // HALAMAN PUBLISH
        // =========================
        else if(page == 600){

            Log.d(TAG,"PAGE PUBLISH sementara berhasil");
            /*
            if(clickText(root,"Posting") || clickText(root,"Publish")){

                Log.d(TAG,"Klik Posting");

            }

            */


        }


        /*
        // =========================
        // CEK HASIL PUBLISH
        // =========================
        if(
                findText(root,"berhasil") ||
                        findText(root,"diposting") ||
                        findText(root,"posted") ||
                        findText(root,"success")
        ){

            Log.d(TAG,"Publish BERHASIL");

        }
        */



        // =========================
        // RETRY DETECT PAGE
        // =========================
        if(page == -1){

            handler.postDelayed(() -> {

                AccessibilityNodeInfo root2 = getRootInActiveWindow();

                if(root2 != null){

                    int pageRetry = detectPage(root2);

                    if(pageRetry != -1){

                        Log.d(TAG,"Retry detect halaman → "+pageRetry);

                    }

                }

            },800);

        }

    }

    private boolean findText(AccessibilityNodeInfo node,String text){

        if(node==null) return false;

        if(node.getText()!=null){

            if(node.getText().toString().toLowerCase().contains(text.toLowerCase()))
                return true;

        }

        if(node.getContentDescription()!=null){

            if(node.getContentDescription().toString().toLowerCase().contains(text.toLowerCase()))
                return true;

        }

        for(int i=0;i<node.getChildCount();i++){

            if(findText(node.getChild(i),text))
                return true;

        }

        return false;
    }

    private boolean clickText(AccessibilityNodeInfo root,String text){

        List<AccessibilityNodeInfo> nodes=
                root.findAccessibilityNodeInfosByText(text);

        if(nodes==null) return false;

        for(AccessibilityNodeInfo node:nodes){

            AccessibilityNodeInfo parent=node;

            while(parent!=null){

                if(parent.isClickable()){

                    parent.performAction(
                            AccessibilityNodeInfo.ACTION_CLICK
                    );

                    Log.d(TAG,"Klik : "+text);

                    return true;

                }

                parent=parent.getParent();

            }

        }

        return false;

    }

    private boolean clickDraftDate(AccessibilityNodeInfo root){

        if(root==null) return false;

        if(root.getText()!=null){

            String text=root.getText().toString();

            if(text.contains("20")){

                Log.d(TAG,"Tanggal ditemukan : "+text);

                AccessibilityNodeInfo parent=root;

                while(parent!=null){

                    if(parent.isClickable()){

                        parent.performAction(
                                AccessibilityNodeInfo.ACTION_CLICK
                        );

                        Log.d(TAG,"Klik video draft");

                        return true;

                    }

                    parent=parent.getParent();

                }

            }

        }

        for(int i=0;i<root.getChildCount();i++){

            if(clickDraftDate(root.getChild(i)))
                return true;

        }

        return false;

    }

    private void dumpText(AccessibilityNodeInfo node){

        if(node==null) return;

        if(node.getText()!=null){

            Log.d(TAG,"TEXT : "+node.getText().toString());

        }

        if(node.getContentDescription()!=null){

            Log.d(TAG,"DESC : "+node.getContentDescription().toString());

        }

        for(int i=0;i<node.getChildCount();i++){

            dumpText(node.getChild(i));

        }

    }

    private boolean clickCreateIcon(AccessibilityNodeInfo node){

        if(node==null) return false;

        if(node.getContentDescription()!=null){

            String desc=node.getContentDescription()
                    .toString()
                    .toLowerCase();

            if(desc.contains("create")){

                AccessibilityNodeInfo parent=node;

                while(parent!=null){

                    if(parent.isClickable()){

                        parent.performAction(
                                AccessibilityNodeInfo.ACTION_CLICK
                        );

                        Log.d(TAG,"Klik icon Create");

                        return true;

                    }

                    parent=parent.getParent();

                }

            }

        }

        for(int i=0;i<node.getChildCount();i++){

            if(clickCreateIcon(node.getChild(i))) return true;

        }

        return false;

    }

    private void swipeUp(){

        android.util.DisplayMetrics metrics =
                getResources().getDisplayMetrics();

        int x = metrics.widthPixels / 2;

        int startY = (int)(metrics.heightPixels * 0.60);
        int endY   = (int)(metrics.heightPixels * 0.20);

        Path path=new Path();

        path.moveTo(x,startY);
        path.lineTo(x,endY);

        GestureDescription.Builder builder=
                new GestureDescription.Builder();

        builder.addStroke(
                new GestureDescription.StrokeDescription(
                        path,0,500
                )
        );

        dispatchGesture(builder.build(),null,null);

        Log.d(TAG,"Swipe up");

    }

    private void tapPercent(double px,double py){

        android.util.DisplayMetrics metrics =
                getResources().getDisplayMetrics();

        int x = (int)(metrics.widthPixels * px);

        int y = (int)(metrics.heightPixels * py);

        tap(x,y);

    }

    private void tap(int x,int y){

        android.graphics.Path path = new android.graphics.Path();

        path.moveTo(x,y);

        android.accessibilityservice.GestureDescription.Builder builder =
                new android.accessibilityservice.GestureDescription.Builder();

        builder.addStroke(
                new android.accessibilityservice.GestureDescription.StrokeDescription(
                        path,
                        0,
                        250
                )
        );

        dispatchGesture(builder.build(),null,null);

        Log.d(TAG,"Tap : "+x+" , "+y);

    }

    private boolean clickFirstVideo(AccessibilityNodeInfo node){


        if(node == null) return false;


        if(node.isClickable() && node.getChildCount() > 0){


            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);


            int x = bounds.centerX();
            int y = bounds.centerY();


            Log.d(TAG,"Tap video di : "+x+" , "+y);


            tap(x,y);


            return true;
        }


        for(int i=0;i<node.getChildCount();i++){


            if(clickFirstVideo(node.getChild(i)))
                return true;


        }


        return false;
    }




    public void restartShopee(){
        return;
    }

    public int detectPage(AccessibilityNodeInfo root){

        if(root == null){
            Log.d(TAG,"detectPage : root NULL");
            return -1;
        }

        // =====================
        // HALAMAN PUBLISH
        // =====================
        int publishScore = 0;

        if(findText(root,"Draf") || findText(root,"Draf")) publishScore++;
        if(findText(root,"Posting")) publishScore++;
        if(findText(root,"Izinkan")) publishScore++;

        if(publishScore >= 3){
            Log.d(TAG,"detectPage : HALAMAN PUBLISH (score="+publishScore+")");
            return 600;
        }

        // =====================
        // HALAMAN EDITOR
        // =====================
        int editorScore = 0;

        if(findText(root,"Next") || findText(root,"Lanjutkan")) editorScore++;
        if(findText(root,"Potong")) editorScore++;
        if(findText(root,"Teks")) editorScore++;

        if(editorScore >= 3){
            Log.d(TAG,"detectPage : HALAMAN EDITOR (score="+editorScore+")");
            return 5;
        }

        // =====================
        // HALAMAN DRAFT
        // =====================
        int draftScore = 0;

        if(findText(root,"Jika kamu") || findText(root,"menghapus aplikasi")) draftScore++;
        if(findText(root,"kamu akan")) draftScore++;
        if(findText(root,"kotak")) draftScore++;

        if(draftScore >= 3){
            Log.d(TAG,"detectPage : HALAMAN DRAFT (score="+draftScore+")");
            return 4;
        }

        // =====================
        // Buka Kamera
        // =====================
        int cameraScore = 0;

        if(findText(root,"foto") || findText(root,"Galeri")) cameraScore++;
        if(findText(root,"template")) cameraScore++;
        if(findText(root,"Video")) cameraScore++;

        if(cameraScore >= 2){
            Log.d(TAG,"detectPage : HALAMAN camera (score="+cameraScore+")");
            return 2;
        }

        // =====================
        // HALAMAN LIVE & VIDEO
        // =====================
        int liveScore = 0;

        if(findText(root,"Create")) liveScore++;
        if(findText(root,"Live")) liveScore++;
        if(findText(root,"Video")) liveScore++;

        if(liveScore >= 2){
            Log.d(TAG,"detectPage : HALAMAN LIVE & VIDEO (score="+liveScore+")");
            return 1;
        }

        // =====================
        // HALAMAN HOME
        // =====================
        int homeScore = 0;

        if(findText(root,"Shopee Live")) homeScore++;
        if(findText(root,"Shopee Video")) homeScore++;
        if(findText(root,"Spinjam")) homeScore++;

        if(homeScore >= 2){
            Log.d(TAG,"detectPage : HALAMAN HOME (score="+homeScore+")");
            return 0;
        }

        Log.d(TAG,"detectPage : HALAMAN TIDAK DIKENALI");

        return -1;
    }

    public int getCurrentPage(){

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if(root == null){
            Log.d(TAG,"getCurrentPage : root NULL");
            return -1;
        }

        return detectPage(root);
    }

    private boolean waitUI(){

        long now = System.currentTimeMillis();

        if(now - lastAction < 800) return false;

        lastAction = now;

        return true;

    }
    private AccessibilityNodeInfo getFreshRoot(){

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if(root == null) return null;

        root.refresh();

        Log.d(TAG,"ROOT HASH : "+root.hashCode());

        return root;
    }

    @Override
    public void onInterrupt(){}

}