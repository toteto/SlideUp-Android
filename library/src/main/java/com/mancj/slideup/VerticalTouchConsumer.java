package com.mancj.slideup;

import android.view.MotionEvent;
import android.view.View;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.TOP;

/**
 * @author pa.gulko zTrap (05.07.2017)
 */
class VerticalTouchConsumer extends TouchConsumer {
    private boolean mGoingUp = false;
    private boolean mGoingDown = false;
    
    VerticalTouchConsumer(SlideUpBuilder builder, PercentageChangeCalculator percentageChangeCalculator, AbstractSlideTranslator translator) {
        super(builder, percentageChangeCalculator, translator);
    }

    @Override
    protected boolean consumeTouchEvent(View touchedView, MotionEvent event) {
        switch (mBuilder.mStartGravity) {
            case BOTTOM:
                return consumeBottomToTop(touchedView, event);
            case TOP:
                return consumeTopToBottom(touchedView, event);
        }
        return false;
    }

    boolean consumeBottomToTop(View touchedView, MotionEvent event){
        float touchedArea = event.getY();
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mViewHeight = mBuilder.mSliderView.getHeight();
                mStartPositionY = event.getRawY();
                mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
                mCanSlide = touchFromAlsoSlide(touchedView, event);
                mCanSlide |= mBuilder.mTouchableArea >= touchedArea;
                break;
            case MotionEvent.ACTION_MOVE:
                float difference = event.getRawY() - mStartPositionY;
                float moveTo = mViewStartPositionY + difference;
                float percents = moveTo * 100 / mBuilder.mSliderView.getHeight();
                calculateDirection(event);
                
                if (moveTo > 0 && mCanSlide){
                    mBuilder.mSliderView.setTranslationY(moveTo);
                    mPercentageCalculator.recalculatePercentage();
                }
                break;
            case MotionEvent.ACTION_UP:
                float slideAnimationFrom = mBuilder.mSliderView.getTranslationY();
                if (slideAnimationFrom == mViewStartPositionY) {
                    return !Internal.isUpEventInView(mBuilder.mSliderView, event);
                }
                boolean scrollableAreaConsumed = mBuilder.mSliderView.getTranslationY() > mBuilder.mSliderView.getHeight() / 5;
                
                if (scrollableAreaConsumed && mGoingDown){
                    mTranslator.hideSlideView(false);
                } else {
                    mTranslator.showSlideView(false);
                }
                mCanSlide = true;
                mGoingUp = false;
                mGoingDown = false;
                break;
        }
        mPrevPositionY = event.getRawY();
        mPrevPositionX = event.getRawX();
        return true;
    }
    
    boolean consumeTopToBottom(View touchedView, MotionEvent event){
        float touchedArea = event.getY();
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mViewHeight = mBuilder.mSliderView.getHeight();
                mStartPositionY = event.getRawY();
                mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
                mCanSlide = touchFromAlsoSlide(touchedView, event);
                mCanSlide |= getBottom() - mBuilder.mTouchableArea <= touchedArea;
                break;
            case MotionEvent.ACTION_MOVE:
                float difference = event.getRawY() - mStartPositionY;
                float moveTo = mViewStartPositionY + difference;
                calculateDirection(event);
            
                if (moveTo < 0 && mCanSlide){
                    mBuilder.mSliderView.setTranslationY(moveTo);
                    mPercentageCalculator.recalculatePercentage();
                }
                break;
            case MotionEvent.ACTION_UP:
                float slideAnimationFrom = -mBuilder.mSliderView.getTranslationY();
                if (slideAnimationFrom == mViewStartPositionY){
                    return !Internal.isUpEventInView(mBuilder.mSliderView, event);
                }
                boolean scrollableAreaConsumed = mBuilder.mSliderView.getTranslationY() < -mBuilder.mSliderView.getHeight() / 5;
            
                if (scrollableAreaConsumed && mGoingUp){
                    mTranslator.hideSlideView(false);
                }else {
                    mTranslator.showSlideView(true);
                }
                mCanSlide = true;
                break;
        }
        mPrevPositionY = event.getRawY();
        mPrevPositionX = event.getRawX();
        return true;
    }

    private void calculateDirection(MotionEvent event) {
        mGoingUp = mPrevPositionY - event.getRawY() > 0;
        mGoingDown = mPrevPositionY - event.getRawY() < 0;
    }
}
