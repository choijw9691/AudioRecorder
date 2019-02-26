//package com.ebook.epub.viewer;
//
//import android.os.Handler;
//import android.os.SystemClock;
//import android.util.Log;
//import android.view.GestureDetector;
//import android.view.GestureDetector.OnGestureListener;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.VelocityTracker;
//import android.view.View;
//import android.view.View.OnKeyListener;
//import android.view.View.OnTouchListener;
//import android.view.ViewConfiguration;
//
//import java.util.ArrayList;
//
///**
// * @author  syw
// */
//public class EPubViewerInputListener implements OnGestureListener, OnTouchListener, OnKeyListener {
//
//    String TAG = "ViewerInputListener";
//
//    EPubViewer mViewer;
//    GestureDetector mGestureDetector;
//    int mMaximumVelocity;
//
//    public static ArrayList<MotionEvent> touchEventList = new ArrayList<>();
//    public static MotionEvent touchEvent;
//    View touchView;
//    VelocityTracker vt;
//
//    float pressDownX;
//    float pressDownY;
//    long  pressTime;
//    boolean pressed=false;
//    boolean dragging=false;
//    boolean doubleTap=false;
//    boolean moveStart = false;
//
//    boolean isDoubleTapSupported=true;
//
//    boolean multiPressed=false;
//    float[] multiX = new float[] { 0, 0, 0 };
//    float[] multiY = new float[] { 0, 0, 0 };
//
//    private static final int SWIPE_MIN_DISTANCE = 120;
//    private static final int SWIPE_MAX_OFF_PATH = 250;
//    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
//
//    public static final int FLING_LEFT = 1;
//    public static final int FLING_RIGHT = 2;
//    public static final int FLING_UP = 3;
//    public static final int FLING_DOWN = 4;
//
//    private float twoFingerDistantX=0;
//    private float twoFingerDistantY=0;
//
//    public EPubViewerInputListener(ViewerContainer viewBase) {
//        mGestureDetector = new GestureDetector(this);
//        mViewer = viewBase.mEPubViewer;
//
//        if( mViewer == null )
//            return;
//
//        vt = VelocityTracker.obtain();
//        mMaximumVelocity = ViewConfiguration.get(mViewer.mContext).getScaledMaximumFlingVelocity();
//    }
//
//    @Override
//    public boolean onKey(View v, int keyCode, KeyEvent event) {
//        if( mViewer.__forceChapterChanging ) {
//            return true;
//        }
//
//        return false;
//    }
//
//    Handler mHandler = new Handler();
//
//    private class LongClickRunnable implements Runnable {
//        public void run() {
//            if (performLongClick()) {
//                myLongClickPerformed = true;
//            }
//        }
//    }
//    private volatile LongClickRunnable myPendingLongClickRunnable;
//    private volatile boolean myLongClickPerformed;
//
//    private void postLongClickRunnable() {
//        myLongClickPerformed = false;
//        pressed = false;
//        if (myPendingLongClickRunnable == null) {
//            myPendingLongClickRunnable = new LongClickRunnable();
//        }
//        mHandler.postDelayed(myPendingLongClickRunnable, /*2 **/ ViewConfiguration.getLongPressTimeout());
//    }
//    private void removeLongClickCallback() {
//        if (myPendingLongClickRunnable != null) {
//            mHandler.removeCallbacks(myPendingLongClickRunnable);
//        }
//    }
//
//    private class ShortClickRunnable implements Runnable {
//        public void run() {
//            DebugSet.d("TAG", "ShortClickRunnable run");
//            mViewer.onSingleTap((int)pressDownX, (int)pressDownY);
//            pressed = false;
//            myPendingShortClickRunnable = null;
//        }
//    }
//    private void removeShortClickCallback() {
//        if (myPendingShortClickRunnable != null) {
//            mHandler.removeCallbacks(myPendingShortClickRunnable);
//        }
//    }
//
//    private volatile ShortClickRunnable myPendingShortClickRunnable;
//
//    public boolean performLongClick() {
//
//        if( mViewer != null ) {
//            mViewer.onLongPress((int)pressDownX, (int)pressDownY);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//
//        if( mViewer.__previewMode )
//            return true;
//
//        DebugSet.d("event", "onTouch + " + event.getAction());
//
//        if( event.getAction() == MotionEvent.ACTION_DOWN ){
//            moveStart = false;
//            dragging = false;
//            touchEventList.clear();
//        }
//
//        touchEventList.add(MotionEvent.obtain(event));
//
//        float x = event.getX();
//        float y = event.getY();
//
//        int action = event.getAction();
//        int touchCount = event.getPointerCount();
//
//        switch(action & MotionEvent.ACTION_MASK) {
//
//            case MotionEvent.ACTION_POINTER_DOWN: {
//                DebugSet.d(TAG, "Viewer::ACTION_POINTER_DOWN >>>>>>>>> ");
//                if( myPendingLongClickRunnable != null ) {
//                    removeLongClickCallback();
//                    myPendingLongClickRunnable = null;
//                }
//
//                if (touchCount == 2){
//                    for(int i=0; i<touchCount; i++) {
//                        multiX[i] = event.getX(i);
//                        multiY[i] = event.getY(i);
//                    }
//                    multiPressed = true;
//                }
//
//                break;
//            }
//
//            case MotionEvent.ACTION_POINTER_UP: {
//                DebugSet.d(TAG, "Viewer::ACTION_POINTER_UP >>>>>>>>> ");
//                if( touchCount == 2 ) {
//                    mViewer.onTwoFingerMove(0);
//                }
//                break;
//            }
//
//            case MotionEvent.ACTION_DOWN: {
//                DebugSet.d(TAG, "Viewer::ACTION_DOWN >>>>>>>>> ");
//                if (myPendingShortClickRunnable != null) {
//                    removeShortClickCallback();
//                    myPendingShortClickRunnable = null;
//                    doubleTap = true;
//                } else {
//                    postLongClickRunnable();
//                    pressed = true;
//                }
//
//                touchView  = v;
//
//                touchEvent = MotionEvent.obtain(event);
//
//                pressTime = SystemClock.elapsedRealtime();
//                pressDownX = x;
//                pressDownY = y;
//
//                pressed = true;
//                mViewer.onTouchDown((int)x, (int)y);
//
//                break;
//            }
//
//            case MotionEvent.ACTION_UP: {
//                DebugSet.d(TAG, "Viewer::ACTION_UP >>>>>>>>> ");
//                if( multiPressed ) {
//                    multiPressed = false;
//                    return true;
//                }
//
//                if( doubleTap ) {
//                    mViewer.onDoubleTap((int)x, (int)y);
//                    pressed = false;
//                }
//
//                if( myLongClickPerformed ) {
//                    mViewer.onTouchUp((int)x, (int)y);
//                }
//                else {
//                    if( myPendingLongClickRunnable != null ) {
//                        removeLongClickCallback();
//                        myPendingLongClickRunnable = null;
//                    }
//
//                    if (pressed) {
//                        if( isDoubleTapSupported ) {
//                            if( myPendingShortClickRunnable == null ) {
//                                myPendingShortClickRunnable = new ShortClickRunnable();
//                            }
//                            mHandler.postDelayed(myPendingShortClickRunnable, ViewConfiguration.getDoubleTapTimeout());
//                        }
//                        else {
//                            mViewer.onSingleTap((int)x, (int)y);
//                        }
//
//                    } else {
//                        mViewer.onTouchUp((int)x, (int)y);
//                    }
//                }
//
//                pressed = false;
//                doubleTap = false;
//
//                break;
//            }
//
//            case MotionEvent.ACTION_MOVE: {
//
//                if(touchCount == 2) {
//
//                    if(multiX[0] == event.getX(0)
//                            && multiX[1] == event.getX(1)
//                            && multiY[0] == event.getY(0)
//                            && multiY[1] == event.getY(1)){
//                        // 처음 two finger touch in 들어가는 경우 콜백 안보내기 위한 예외처리
//                        break;
//                    }
//
//                    float moveDistx = event.getX(0) - event.getX(1);
//                    float moveDisty = event.getY(0) - event.getY(1);
//
//                    if(Math.abs(moveDistx) > Math.abs(twoFingerDistantX) || Math.abs(moveDisty) > Math.abs(twoFingerDistantY)){
//
//                        if( Math.abs(moveDistx) - Math.abs(twoFingerDistantX) > 100 || Math.abs(moveDisty) - Math.abs(twoFingerDistantY) > 100){
//
//                            twoFingerDistantX = moveDistx;
//                            twoFingerDistantY = moveDisty;
//
//                            mViewer.onTwoFingerMove(1);
//                        }
//                    } else if( Math.abs(moveDistx) < Math.abs(twoFingerDistantX) || Math.abs(moveDisty) < Math.abs(twoFingerDistantY)){
//
//                        if( Math.abs(twoFingerDistantX) - Math.abs(moveDistx) > 100 || Math.abs(twoFingerDistantY) - Math.abs(moveDisty) > 100){
//
//                            twoFingerDistantX = moveDistx;
//                            twoFingerDistantY = moveDisty;
//
//                            //smaller
//
//                            mViewer.onTwoFingerMove(-1);
//                        }
//                    }
//                } else {
//
//                    int slop = ViewConfiguration.get(mViewer.getContext()).getScaledTouchSlop();
//                    final boolean isAMove = Math.abs(pressDownX - x) > slop || Math.abs(pressDownY - y) > slop;
//                    Log.d("SSIN","SSIN isAMove : "+isAMove);
//                    if( myLongClickPerformed ) {
//                        if(isAMove) {
//                            mViewer.onTouchMoveAfterLongPress((int) x, (int) y);
//                        }
//                    } else {
//                        if( pressed ) {
//                            if(isAMove) {
//                                if( myPendingShortClickRunnable != null ) {
//                                    removeShortClickCallback();
//                                    myPendingShortClickRunnable = null;
//                                }
//                                if( myPendingLongClickRunnable != null ) {
//                                    removeLongClickCallback();
//                                    myPendingLongClickRunnable = null;
//                                }
//                                pressed = false;
//                                moveStart = true;
//                            }
//                        }
//                        if(!pressed) {
////                            if(isAMove){
////                                mViewer.onFingerMove((int)x, (int)y);
////                                pressDownX = x;
////                                pressDownY = y;
////                            }
//                            if(BookHelper.animationType==3){
//                                mViewer.onTouchMove((int)x, (int)y);
//                            } else {
//                                if(isAMove){
//                                    mViewer.onTouchMove((int)x, (int)y);
//                                    pressDownX = x;
//                                    pressDownY = y;
//                                }
//                            }
//                        }
//                    }
//
////                    if( moveStart ) {
////                        long currentTime = SystemClock.elapsedRealtime();
////                        if( dragging == false && (currentTime - pressTime) > 300 ){
////                            mViewer.onFingerPress((int)pressDownX, (int)pressDownY);
////                            dragging = true;
////                        }
////                    }
//                }
//
//                break;
//            }
//        }
//        return mGestureDetector.onTouchEvent(event);
//    }
//
//    @Override
//    public boolean onDown(MotionEvent e) {
//        return false;
//    }
//
//    @Override
//    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//
//        if( mViewer.isPreventPageMove ) return false;
//
//        if( mViewer.mOnTouchEventListener != null && !mViewer.mTextSelectionMode  ) {
//
//            float x = e1.getX() - e2.getX();
//            float y = e1.getY() - e2.getY();
//
//            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
//                return false;
//
//            // right to left swipe
//            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
//                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//
//                if( dragging == false )
//                    mViewer.onFling(e1, x, y, FLING_RIGHT);
//            }
//            // left to right swipe
//            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
//                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//
//                if( dragging == false )
//                    mViewer.onFling(e1, x, y, FLING_LEFT);
//            }
//            // down to up swipe
//            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
//                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                mViewer.onFling(e1, x, y, FLING_UP);
//            }
//            // up to down swipe
//            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
//                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                mViewer.onFling(e1, x, y, FLING_DOWN);
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public void onLongPress(MotionEvent e) {
//        DebugSet.d("TAG", "onLongPress");
//    }
//
//    @Override
//    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        return false;
//    }
//
//    @Override
//    public void onShowPress(MotionEvent e) {
//    }
//
//    @Override
//    public boolean onSingleTapUp(MotionEvent e) {
//        return false;
//    }
//
//    // calculate 2 points distance
//    private int distinceTo(float x1, float y1, float x2, float y2) {
//        float p1 = x1 - x2;
//        float z1 = y1 - y2;
//        return (int) Math.sqrt(Math.pow(p1, 2) + Math.pow(z1, 2));
//    }
//}
