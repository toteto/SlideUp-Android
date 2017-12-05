package com.mancj.slideup;

import android.view.MotionEvent;
import android.view.View;

/**
 * @author pa.gulko zTrap (12.07.2017)
 */
abstract class TouchConsumer {
    SlideUpBuilder mBuilder;
    AbstractSlideTranslator mTranslator;

    boolean mCanSlide = true;
    PercentageChangeCalculator mPercentageCalculator;
    
    float mViewHeight;
    float mViewWidth;
    
    float mStartPositionY;
    float mStartPositionX;
    volatile float mPrevPositionY;
    volatile float mPrevPositionX;
    float mViewStartPositionY;
    float mViewStartPositionX;
    
    TouchConsumer(SlideUpBuilder builder, PercentageChangeCalculator notifier,
                  AbstractSlideTranslator translator){
        mBuilder = builder;
        mTranslator = translator;
        mPercentageCalculator = notifier;
    }

    protected abstract boolean consumeTouchEvent(View touchedView, MotionEvent event);
    
    int getEnd(){
        if (mBuilder.mIsRTL){
            return mBuilder.mSliderView.getLeft();
        }else {
            return mBuilder.mSliderView.getRight();
        }
    }
    
    int getStart(){
        if (mBuilder.mIsRTL){
            return mBuilder.mSliderView.getRight();
        }else {
            return mBuilder.mSliderView.getLeft();
        }
    }
    
    int getTop(){
        return mBuilder.mSliderView.getTop();
    }
    
    int getBottom(){
        return mBuilder.mSliderView.getBottom();
    }

    boolean touchFromAlsoSlide(View touchedView, MotionEvent event) {
        return touchedView == mBuilder.mAlsoScrollView;
    }
}
