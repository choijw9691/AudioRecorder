package com.ebook.epub.viewer;

import android.content.Context;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class ViewerActionListener implements View.OnTouchListener, View.OnKeyListener {

    public EPubViewer mViewer;

    private GestureDetector mGestureDetector;

    public static MotionEvent touchEvent;

    public static boolean isLongPressed = false;

    private boolean multiPressed=false;

    private float pressDownX;
    private float pressDownY;

    private float twoFingerDistantX=0;
    private float twoFingerDistantY=0;

    private float[] multiX = new float[] { 0, 0, 0 };
    private float[] multiY = new float[] { 0, 0, 0 };

    private int SWIPE_THRESHOLD_VELOCITY = 100;
    private int SWIPE_MAX_OFF_PATH = 250;
    private int SWIPE_MIN_DISTANCE = 120;

    public static final int FLING_LEFT = 1;
    public static final int FLING_RIGHT = 2;

    public ViewerActionListener(Context context, ViewerContainer viewBase) {
        mGestureDetector = new GestureDetector(context, new SimpleGestureListener());
        mViewer = viewBase.mEPubViewer;
        if( mViewer == null )
            return;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if( mViewer.__forceChapterChanging ) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if( mViewer.__previewMode )
            return true;

        mGestureDetector.onTouchEvent(motionEvent);

        int touchCount = motionEvent.getPointerCount();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                preventGestureEvent = false;
                isLongPressed = false;
                pressDownX = motionEvent.getX();
                pressDownY = motionEvent.getY();
                mViewer.onTouchDown((int)pressDownX, (int)pressDownY);
                break;

            case MotionEvent.ACTION_MOVE:
                if(touchCount == 2 && !mViewer.mTextSelectionMode) {
                    int slop = ViewConfiguration.get(mViewer.getContext()).getScaledTouchSlop();
                    boolean userMoveConfirmed = Math.abs(multiX[0] - motionEvent.getX(0)) > slop
                            ||  Math.abs(multiX[1] - motionEvent.getX(1)) > slop
                            ||  Math.abs(multiY[0] - motionEvent.getY(0)) > slop
                            ||  Math.abs(multiY[1] - motionEvent.getY(1)) > slop;

                    if(!userMoveConfirmed) {
                        multiPressed = false;
                        return true;
                    } else {
                        multiPressed = true;
                    }

                    float moveDistx = motionEvent.getX(0) - motionEvent.getX(1);
                    float moveDisty = motionEvent.getY(0) - motionEvent.getY(1);

                    if(Math.abs(moveDistx) > Math.abs(twoFingerDistantX) || Math.abs(moveDisty) > Math.abs(twoFingerDistantY)){

                        if( Math.abs(moveDistx) - Math.abs(twoFingerDistantX) > 100 || Math.abs(moveDisty) - Math.abs(twoFingerDistantY) > 100){

                            twoFingerDistantX = moveDistx;
                            twoFingerDistantY = moveDisty;

                            mViewer.onTwoFingerMove(1);
                        }
                    } else if( Math.abs(moveDistx) < Math.abs(twoFingerDistantX) || Math.abs(moveDisty) < Math.abs(twoFingerDistantY)){

                        if( Math.abs(twoFingerDistantX) - Math.abs(moveDistx) > 100 || Math.abs(twoFingerDistantY) - Math.abs(moveDisty) > 100){

                            twoFingerDistantX = moveDistx;
                            twoFingerDistantY = moveDisty;

                            mViewer.onTwoFingerMove(-1);
                        }
                    }
                } else if(touchCount == 1 && mViewer.mTextSelectionMode) {
                    int slop = ViewConfiguration.get(mViewer.getContext()).getScaledTouchSlop();
                    final boolean userMoveConfirmed = Math.abs(pressDownX - motionEvent.getX()) > slop || Math.abs(pressDownY - motionEvent.getY()) > slop;
                    float moveDistx = motionEvent.getX() - pressDownX;
                    float moveDisty = motionEvent.getY() - pressDownY;
                    if (isLongPressed){
                        mViewer.onTouchMoveAfterLongPress((int) motionEvent.getX(), (int) motionEvent.getY(), moveDistx, moveDisty);
                    } else {
                        mViewer.onTouchMove((int) motionEvent.getX(), (int) motionEvent.getY(), moveDistx, moveDisty);
                    }
                    return true;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:

                if(mViewer.mTextSelectionMode)
                    return true;

                if (touchCount == 2){
                    for(int i=0; i<touchCount; i++) {
                        multiX[i] = motionEvent.getX(i);
                        multiY[i] = motionEvent.getY(i);
                    }
                    multiPressed = true;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if( touchCount == 2 && multiPressed) {
                    mViewer.onTwoFingerMove(0);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:

                if( multiPressed ) {
                    multiPressed = false;
                    return true;
                }

                touchEvent = motionEvent;

                mViewer.onTouchUp((int)motionEvent.getX(), (int)motionEvent.getY());
                break;
        }
        return false;
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public void onLongPress(MotionEvent e) {
            if( mViewer != null ) {
                mViewer.onLongPress((int)e.getX(), (int)e.getY());
                isLongPressed = true;
            }
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if( mViewer.isPreventPageMove || mViewer.mTextSelectionMode ) return false;

            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;

            float x = e1.getX() - e2.getX();
            float y = e1.getY() - e2.getY();

            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                mViewer.onFling(e1, x, y, FLING_RIGHT);
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                mViewer.onFling(e1, x, y, FLING_LEFT);
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mViewer.onDoubleTap((int)e.getX(), (int)e.getY());
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(preventGestureEvent)
                return false;

            touchEvent = e;
            mViewer.onSingleTap((int)e.getX(), (int)e.getY());
            return false;
        }
    }

    public static boolean preventGestureEvent = false;
}
