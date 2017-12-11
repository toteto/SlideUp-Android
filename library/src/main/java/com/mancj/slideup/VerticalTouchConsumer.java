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
        if (event.getAction() != MotionEvent.ACTION_DOWN && !mCanSlide) {
            return false;
        }
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
        float moveDifference = event.getRawY() - mStartPositionY;
        float moveDistance = Math.abs(moveDifference);
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mCandidateForClick = true;
                mViewHeight = mBuilder.mSliderView.getHeight();
                mStartPositionY = event.getRawY();
                mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
                mCanSlide = touchFromAlsoSlide(touchedView, event);
                mCanSlide |= mBuilder.mTouchableArea >= touchedArea;
                mOngoingTouch = mCanSlide;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveTo = mViewStartPositionY + moveDifference;
                calculateDirection(event);
                
                if (moveTo > 0 && mCanSlide){
                    mBuilder.mSliderView.setTranslationY(moveTo);
                    mPercentageCalculator.recalculatePercentage();
                }

                if (mCandidateForClick && moveDistance > mTouchSlop) {
                    mCandidateForClick = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCandidateForClick) {
                    mTranslator.slideToCurrentState(false);
                    touchedView.performClick();
                } else {
                    if (mGoingDown && moveDistance > mBuilder.mSliderView.getHeight() / 5.0) {
                        mTranslator.hideSlideView(false);
                    } else if (mGoingUp && moveDistance > mBuilder.mSliderView.getHeight() / 5.0) {
                        mTranslator.showSlideView(false);
                    } else {
                        mTranslator.slideToCurrentState(false);
                    }
                }
                mCanSlide = true;
                mGoingUp = false;
                mGoingDown = false;
                mOngoingTouch = false;
                mCandidateForClick = false;
                break;
        }
        mPrevPositionY = event.getRawY();
        mPrevPositionX = event.getRawX();
        return mCanSlide;
    }
    
    boolean consumeTopToBottom(View touchedView, MotionEvent event){
        float touchedArea = event.getY();
        float moveDifference = event.getRawY() - mStartPositionY;
        float moveDistance = Math.abs(moveDifference);
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mCandidateForClick = true;
                mViewHeight = mBuilder.mSliderView.getHeight();
                mStartPositionY = event.getRawY();
                mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
                mCanSlide = touchFromAlsoSlide(touchedView, event);
                mCanSlide |= getBottom() - mBuilder.mTouchableArea <= touchedArea;
                mOngoingTouch = mCanSlide;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveTo = mViewStartPositionY + moveDifference;
                calculateDirection(event);
            
                if (moveTo < 0 && mCanSlide){
                    mBuilder.mSliderView.setTranslationY(moveTo);
                    mPercentageCalculator.recalculatePercentage();
                }
                if (mCandidateForClick && moveDistance > mTouchSlop) {
                    mCandidateForClick = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCandidateForClick) {
                    mTranslator.slideToCurrentState(false);
                    touchedView.performClick();
                } else {
                    if (mGoingUp && moveDistance > mBuilder.mSliderView.getHeight() / 5.0) {
                        mTranslator.hideSlideView(false);
                    } else if (mGoingDown && moveDistance > mBuilder.mSliderView.getHeight() / 5.0) {
                        mTranslator.showSlideView(false);
                    } else {
                        mTranslator.slideToCurrentState(false);
                    }
                }
                mCanSlide = true;
                mGoingUp = false;
                mGoingDown = false;
                mOngoingTouch = false;
                mCandidateForClick = false;
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
