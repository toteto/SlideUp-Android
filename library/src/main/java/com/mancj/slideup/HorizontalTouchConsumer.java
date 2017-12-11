package com.mancj.slideup;

import android.view.MotionEvent;
import android.view.View;

import static android.view.Gravity.END;
import static android.view.Gravity.START;

/**
 * @author pa.gulko zTrap (12.07.2017)
 */
class HorizontalTouchConsumer extends TouchConsumer {
    private boolean mGoingToStart = false;
    private boolean mGoingToEnd = false;
    
    HorizontalTouchConsumer(SlideUpBuilder builder, PercentageChangeCalculator percentageChangeCalculator, AbstractSlideTranslator translator) {
        super(builder, percentageChangeCalculator, translator);
    }

    @Override
    protected boolean consumeTouchEvent(View touchedView, MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN && !mCanSlide) {
            return false;
        }
        switch (mBuilder.mStartGravity) {
        case END:
            return consumeEndToStart(touchedView, event);
        case START:
            return consumeStartToEnd(touchedView, event);
        }
        return false;
    }

    boolean consumeEndToStart(View touchedView, MotionEvent event){
        float touchedArea = event.getX();
        float moveDifference = event.getRawX() - mStartPositionX;
        float moveDistance = Math.abs(moveDifference);
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mCandidateForClick = true;
                mViewWidth = mBuilder.mSliderView.getWidth();
                mStartPositionX = event.getRawX();
                mViewStartPositionX = mBuilder.mSliderView.getTranslationX();
                mCanSlide = touchFromAlsoSlide(touchedView, event);
                mCanSlide |= getStart() + mBuilder.mTouchableArea >= touchedArea;
                mOngoingTouch = mCanSlide;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveTo = mViewStartPositionX + moveDifference;
                calculateDirection(event);
                
                if (moveTo > 0 && mCanSlide){
                    mBuilder.mSliderView.setTranslationX(moveTo);
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
                    if (mGoingToEnd && moveDistance > mBuilder.mSliderView.getWidth() / 5.0) {
                        mTranslator.hideSlideView(false);
                    } else if (mGoingToStart && moveDistance > mBuilder.mSliderView.getWidth() / 5.0) {
                        mTranslator.showSlideView(false);
                    } else {
                        mTranslator.slideToCurrentState(false);
                    }
                }
                mCanSlide = true;
                mGoingToEnd = false;
                mGoingToStart = false;
                mOngoingTouch = false;
                mCandidateForClick = false;
                break;
        }
        mPrevPositionY = event.getRawY();
        mPrevPositionX = event.getRawX();
        return mCanSlide;
    }
    
    boolean consumeStartToEnd(View touchedView, MotionEvent event){
        float touchedArea = event.getX();
        float moveDifference = event.getRawX() - mStartPositionX;
        float moveDistance = Math.abs(moveDifference);
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mCandidateForClick = true;
                mViewWidth = mBuilder.mSliderView.getWidth();
                mStartPositionX = event.getRawX();
                mViewStartPositionX = mBuilder.mSliderView.getTranslationX();
                mCanSlide = touchFromAlsoSlide(touchedView, event);
                mCanSlide |= getEnd() - mBuilder.mTouchableArea <= touchedArea;
                mOngoingTouch = mCanSlide;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveTo = mViewStartPositionX + moveDifference;
                calculateDirection(event);
                
                if (moveTo < 0 && mCanSlide){
                    mBuilder.mSliderView.setTranslationX(moveTo);
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
                    if (mGoingToStart && moveDistance > mBuilder.mSliderView.getWidth() / 5.0) {
                        mTranslator.hideSlideView(false);
                    } else if (mGoingToEnd && moveDistance > mBuilder.mSliderView.getWidth() / 5.0) {
                        mTranslator.showSlideView(false);
                    } else {
                        mTranslator.slideToCurrentState(false);
                    }
                }
                mCanSlide = true;
                mGoingToEnd = false;
                mGoingToStart = false;
                mOngoingTouch = false;
                mCandidateForClick = false;
                break;
        }
        mPrevPositionY = event.getRawY();
        mPrevPositionX = event.getRawX();
        return mCanSlide;
    }

    private void calculateDirection(MotionEvent event) {
        mGoingToStart = mPrevPositionX - event.getRawX() > 0;
        mGoingToEnd = mPrevPositionX - event.getRawX() < 0;
    }
}
