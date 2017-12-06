package com.mancj.slideup;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.END;
import static android.view.Gravity.START;
import static android.view.Gravity.TOP;
import static com.mancj.slideup.SlideUp.State.HIDDEN;
import static com.mancj.slideup.SlideUp.State.SHOWED;

public class SlideUp implements View.OnTouchListener, ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener, LoggerNotifier, PercentageChangeCalculator {
    private final static String TAG = SlideUp.class.getSimpleName();
    
    final static String KEY_START_GRAVITY = TAG + "_start_gravity";
    final static String KEY_DEBUG = TAG + "_debug";
    final static String KEY_TOUCHABLE_AREA = TAG + "_touchable_area";
    final static String KEY_STATE = TAG + "_state";
    final static String KEY_AUTO_SLIDE_DURATION = TAG + "_auto_slide_duration";
    final static String KEY_HIDE_SOFT_INPUT = TAG + "_hide_soft_input";
    final static String KEY_STATE_SAVED = TAG + "_state_saved";
    static final String KEY_FILTER_FAKE_POSITIVES = TAG + "_filter_fake_positives";
    static final String KEY_STICKY = TAG + "_sticky";

  /**
     * <p>Available start states</p>
     */
    public enum State {
        
        /**
         * State when the view is hidden.
         */
        HIDDEN,
        
        /**
         * State when the view is showed.
         */
        SHOWED
    }
    
    @IntDef(value = {START, END, TOP, BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    @interface StartVector {
    }
    
    private State mCurrentState;
    
    private SlideUpBuilder mBuilder;
    
    private TouchConsumer mTouchConsumer;

    private AnimationProcessor mAnimationProcessor;
    private AbstractSlideTranslator mTranslationDelegate;
    
    /**
     * <p>Interface to listen to all handled events taking place in the slider</p>
     */
    public interface Listener {
        
        interface Slide extends Listener {
            
            /**
             * @param percent percents of complete slide <b color="#EF6C00">(100 = HIDDEN, 0 = SHOWED)</b>
             */
            void onSlide(SlideUp slideUp, float percent);
        }
        
        interface Visibility extends Listener {

            void onShown(SlideUp slideUp);

            void onHidden(SlideUp slideUp);
        }
        
        interface Events extends Visibility, Slide {
        }
    }
    
    SlideUp(SlideUpBuilder builder) {
        mBuilder = builder;
        init();
    }
    
    private void init() {
        mBuilder.mSliderView.setOnTouchListener(this);
        if(mBuilder.mAlsoScrollView != null) {
            mBuilder.mAlsoScrollView.setOnTouchListener(this);
        }
        mCurrentState = mBuilder.mStartState;
        createAnimation();
        switch (mBuilder.mStartGravity) {
            case TOP:
                mTranslationDelegate = new TopToBottomSlideTranslator(mBuilder, mAnimationProcessor);
                break;
            case BOTTOM:
                mTranslationDelegate = new BottomToTopSlideTranslator(mBuilder, mAnimationProcessor);
                break;
            case START:
                mTranslationDelegate = new StartToEndTranslator(mBuilder, mAnimationProcessor);
                break;
            case END:
                mTranslationDelegate = new EndToStartSlideTranslator(mBuilder, mAnimationProcessor);
                break;
        }
        mBuilder.mSliderView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutSingleListener(mBuilder.mSliderView, new Runnable() {
                    @Override
                    public void run() {

                        switch (mBuilder.mStartGravity) {
                            case TOP:
                                mBuilder.mSliderView.setPivotY(mBuilder.mSliderView.getHeight());
                                setTouchableAreaVertical();
                                break;
                            case BOTTOM:
                                mBuilder.mSliderView.setPivotY(0);
                                setTouchableAreaVertical();
                                break;
                            case START:
                                mBuilder.mSliderView.setPivotX(0);
                                setTouchableAreaHorizontal();
                                break;
                            case END:
                                mBuilder.mSliderView.setPivotX(mBuilder.mSliderView.getWidth());
                                setTouchableAreaHorizontal();
                                break;
                        }
                        mTouchConsumer = createConsumers();
                        updateToCurrentState();
                    }
                }));
        updateToCurrentState();
    }

    private void setTouchableAreaHorizontal(){
        if (mBuilder.mTouchableArea == 0) {
            mBuilder.mTouchableArea = (float) Math.ceil(mBuilder.mSliderView.getWidth() / 10);
        }
    }

    private void setTouchableAreaVertical(){
        if (mBuilder.mTouchableArea == 0) {
            mBuilder.mTouchableArea = (float) Math.ceil(mBuilder.mSliderView.getHeight() / 10);
        }
    }

    private void createAnimation() {
        mAnimationProcessor = new AnimationProcessor(mBuilder, this, this);
    }

    @NonNull
    private TouchConsumer createConsumers() {
        switch (mBuilder.mStartGravity) {
            case TOP:
            case BOTTOM:
                return new VerticalTouchConsumer(mBuilder, this, mTranslationDelegate);
            case START:
            case END:
                return new HorizontalTouchConsumer(mBuilder, this, mTranslationDelegate);
        }
        throw new RuntimeException("Invalid hidden gravity of the slide view.");
    }

    private void updateToCurrentState() {
        switch (mCurrentState) {
            case HIDDEN:
                hideImmediately();
                break;
            case SHOWED:
                showImmediately();
                break;
        }
    }

    //region public interface
    /**
     * <p>Trying hide soft input from window</p>
     *
     * @see InputMethodManager#hideSoftInputFromWindow(IBinder, int)
     */
    public void hideSoftInput() {
        ((InputMethodManager) mBuilder.mSliderView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mBuilder.mSliderView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * <p>Trying show soft input to window</p>
     *
     * @see InputMethodManager#showSoftInput(View, int)
     */
    public void showSoftInput() {
        ((InputMethodManager) mBuilder.mSliderView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(mBuilder.mSliderView, 0);
    }

    /**
     * <p>Returns the visibility status for this view.</p>
     *
     * @return true if the SlideUp has status {@link State#SHOWED}
     */
    public boolean isVisible() {
        return mCurrentState == State.SHOWED;
    }

    /**
     * <p>Add Listener which will be used in combination with this SlideUp</p>
     */
    public void addSlideListener(@NonNull Listener listener) {
        mBuilder.mListeners.add(listener);
    }

    /**
     * <p>Remove Listener which was used in combination with this SlideUp</p>
     */
    public void removeSlideListener(@NonNull Listener listener) {
        mBuilder.mListeners.remove(listener);
    }

    /**
     * <p>Returns typed view which was used as slider</p>
     */
    public <T extends View> T getSliderView() {
        return (T) mBuilder.mSliderView;
    }

    /**
     * <p>Set duration of animation (whenever you use {@link #hide()} or {@link #show()} methods)</p>
     *
     * @param autoSlideDuration <b>(default - <b color="#EF6C00">300</b>)</b>
     */
    public void setAutoSlideDuration(int autoSlideDuration) {
        mBuilder.withAutoSlideDuration(autoSlideDuration);
        mAnimationProcessor.paramsChanged();
    }

    /**
     * <p>Returns duration of animation (whenever you use {@link #hide()} or {@link #show()} methods)</p>
     */
    public float getAutoSlideDuration() {
        return mBuilder.mAutoSlideDuration;
    }

    /**
     * <p>Set touchable area <b>(in dp)</b> for interaction</p>
     *
     * @param touchableArea <b>(default - <b color="#EF6C00">300dp</b>)</b>
     */
    public void setTouchableAreaDp(float touchableArea) {
        mBuilder.withTouchableAreaDp(touchableArea);
    }

    /**
     * <p>Set touchable area <b>(in px)</b> for interaction</p>
     *
     * @param touchableArea <b>(default - <b color="#EF6C00">300dp</b>)</b>
     */
    public void setTouchableAreaPx(float touchableArea) {
        mBuilder.withTouchableAreaPx(touchableArea);
    }

    /**
     * <p>Returns touchable area <b>(in dp)</b> for interaction</p>
     */
    public float getTouchableAreaDp() {
        return mBuilder.mTouchableArea / mBuilder.mDensity;
    }

    /**
     * <p>Returns touchable area <b>(in px)</b> for interaction</p>
     */
    public float getTouchableAreaPx() {
        return mBuilder.mTouchableArea;
    }

    /**
     * <p>Returns running status of animation</p>
     *
     * @return true if animation is running
     */
    public boolean isAnimationRunning() {
        return mAnimationProcessor.isAnimationRunning();
    }

    /**
     * <p>Show view with animation</p>
     */
    public void show() {
        show(false);
    }

    /**
     * <p>Hide view with animation</p>
     */
    public void hide() {
        hide(false);
    }

    /**
     * <p>Hide view without animation</p>
     */
    public void hideImmediately() {
        hide(true);
    }

    /**
     * <p>Show view without animation</p>
     */
    public void showImmediately() {
        show(true);
    }

    /**
     * <p>Turning on/off debug logging</p>
     *
     * @param enabled <b>(default - <b color="#EF6C00">false</b>)</b>
     */
    public void setLoggingEnabled(boolean enabled) {
        mBuilder.withLoggingEnabled(enabled);
    }

    /**
     * <p>Returns current status of debug logging</p>
     */
    public boolean isLoggingEnabled() {
        return mBuilder.mDebug;
    }

    /**
     * <p>Turning on/off gestures</p>
     *
     * @param enabled <b>(default - <b color="#EF6C00">true</b>)</b>
     */
    public void setGesturesEnabled(boolean enabled) {
        mBuilder.withGesturesEnabled(enabled);
    }

    /**
     * <p>Returns current status of gestures</p>
     */
    public boolean isGesturesEnabled() {
        return mBuilder.mGesturesEnabled;
    }

    /**
     * <p>Returns current interpolator</p>
     */
    public TimeInterpolator getInterpolator() {
        return mBuilder.mInterpolator;
    }

    /**
     * <p>Returns gravity which used in combination with this SlideUp</p>
     */
    @StartVector
    public int getStartGravity() {
        return mBuilder.mStartGravity;
    }

    /**
     * <p>Sets interpolator for animation (whenever you use {@link #hide()} or {@link #show()} methods)</p>
     *
     * @param interpolator <b>(default - <b color="#EF6C00">Decelerate interpolator</b>)</b>
     */
    public void setInterpolator(TimeInterpolator interpolator) {
        mBuilder.withInterpolator(interpolator);
        mAnimationProcessor.paramsChanged();
    }

    /**
     * <p>Returns current behavior of soft input</p>
     */
    public boolean isHideKeyboardWhenDisplayed() {
        return mBuilder.mHideKeyboard;
    }

    /**
     * <p>Sets behavior of soft input</p>
     *
     * @param hide <b>(default - <b color="#EF6C00">false</b>)</b>
     */
    public void setHideKeyboardWhenDisplayed(boolean hide) {
        mBuilder.withHideSoftInputWhenDisplayed(hide);
    }

    /**
     * <p>Toggle current state with animation</p>
     */
    public void toggle() {
        if (isVisible()) {
            hide();
        } else {
            show();
        }
    }

    /**
     * <p>Toggle current state without animation</p>
     */
    public void toggleImmediately() {
        if (isVisible()) {
            hideImmediately();
        } else {
            showImmediately();
        }
    }

    /**
     * <p>Saving current parameters of SlideUp</p>
     */
    public void onSaveInstanceState(@NonNull Bundle savedState) {
        savedState.putBoolean(KEY_STATE_SAVED, true);
        savedState.putInt(KEY_START_GRAVITY, mBuilder.mStartGravity);
        savedState.putBoolean(KEY_DEBUG, mBuilder.mDebug);
        savedState.putFloat(KEY_TOUCHABLE_AREA, mBuilder.mTouchableArea / mBuilder.mDensity);
        savedState.putSerializable(KEY_STATE, mCurrentState);
        savedState.putInt(KEY_AUTO_SLIDE_DURATION, mBuilder.mAutoSlideDuration);
        savedState.putBoolean(KEY_HIDE_SOFT_INPUT, mBuilder.mHideKeyboard);
        savedState.putBoolean(KEY_FILTER_FAKE_POSITIVES, mBuilder.mFilterFakePositives);
        savedState.putBoolean(KEY_STICKY, mBuilder.mSticky);
    }
    //endregion

    private void hide(boolean immediately) {
        mTranslationDelegate.hideSlideView(immediately);
        recalculatePercentage();
    }

    private void show(boolean immediately) {
        mAnimationProcessor.endAnimation();
        mTranslationDelegate.showSlideView(immediately);
        recalculatePercentage();
    }

    @Override
    public final boolean onTouch(View v, MotionEvent event) {
        if (mAnimationProcessor.isAnimationRunning()) return false;
        if (mBuilder.mSticky && isVisible()) return false;
        if (!mBuilder.mGesturesEnabled){
            mBuilder.mSliderView.performClick();
            return true;
        }
        boolean consumed = mTouchConsumer.consumeTouchEvent(v, event);
        if (!consumed){
            mBuilder.mSliderView.performClick();
        }
        return true;
    }

    @Override
    public final void onAnimationUpdate(ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        switch (mBuilder.mStartGravity) {
            case TOP:
                onAnimationUpdateTopToBottom(value);
                break;
            case BOTTOM:
                onAnimationUpdateBottomToTop(value);
                break;
            case START:
                onAnimationUpdateStartToEnd(value);
                break;
            case END:
                onAnimationUpdateEndToStart(value);
                break;
        }
        recalculatePercentage();
    }

    private void onAnimationUpdateTopToBottom(float value) {
        mTranslationDelegate.setTranslationY(-value);
    }

    private void onAnimationUpdateBottomToTop(float value) {
        mTranslationDelegate.setTranslationY(value);
    }

    private void onAnimationUpdateStartToEnd(float value) {
        mTranslationDelegate.setTranslationX(-value);
    }

    private void onAnimationUpdateEndToStart(float value) {
        mTranslationDelegate.setTranslationX(value);
    }

    @Override
    public float recalculatePercentage() {
        final float tabHeight = mBuilder.mPullTabView != null ? mBuilder.mPullTabView.getHeight() : 0;
        final float tabWidth = mBuilder.mPullTabView != null ? mBuilder.mPullTabView.getWidth() : 0;

        final float percents;
        final float visibleDistance;
        switch (mBuilder.mStartGravity) {
            case TOP:
                visibleDistance = mBuilder.mSliderView.getTop() - mBuilder.mSliderView.getY();
                percents = (visibleDistance) * 100 / (mBuilder.mSliderView.getHeight() - tabHeight);
                break;
            case BOTTOM:
                visibleDistance = mBuilder.mSliderView.getY() - mBuilder.mSliderView.getTop();
                percents = (visibleDistance) * 100 / (mBuilder.mSliderView.getHeight() - tabHeight);
                break;
            case START:
                visibleDistance = mBuilder.mSliderView.getX() - tabWidth - getStart();
                percents = (visibleDistance) * 100 / -(mBuilder.mSliderView.getWidth() - tabWidth);
                break;

            case END:
                visibleDistance = mBuilder.mSliderView.getX() + tabWidth - getStart();
                percents = (visibleDistance) * 100 / (mBuilder.mSliderView.getWidth() - tabWidth);
                break;
            default: throw new RuntimeException("Invalid hidden gravity of the slide view.");
        }
        // return range of 0-100
        float result = percents > 100 ? 100 : percents < 0 ? 0 : percents;
        notifyPercentChanged(result);
        return result;
    }

    private int getStart() {
        if (mBuilder.mIsRTL) {
            return mBuilder.mSliderView.getRight();
        } else {
            return mBuilder.mSliderView.getLeft();
        }
    }

    private void setVisibility(State newVisibility) {
        if (mCurrentState != newVisibility && (!mBuilder.mFilterFakePositives || !mTouchConsumer.isOngoingTouch())) {
            // visibility has changed and if applied filter fake positives
            mCurrentState = newVisibility;
            notifyVisibilityChanged(newVisibility);
        }
    }
    
    @Override
    public void notifyPercentChanged(float percent) {
        if (percent == 100) {
            setVisibility(HIDDEN);
        } else if (percent == 0) {
            setVisibility(SHOWED);
        }
        if (mAnimationProcessor.getSlideAnimationTo() == 0 && mBuilder.mHideKeyboard) {
            hideSoftInput();
        }
        if (!mBuilder.mListeners.isEmpty()) {
            for (int i = 0; i < mBuilder.mListeners.size(); i++) {
                Listener l = mBuilder.mListeners.get(i);
                if (l != null) {
                    if (l instanceof Listener.Slide) {
                        Listener.Slide slide = (Listener.Slide) l;
                        slide.onSlide(this, percent);
                        logValue(i, "onSlide", percent);
                    }
                } else {
                    logError(i, "onSlide");
                }
            }
        }
    }
    
    @Override
    public void notifyVisibilityChanged(State visibility) {
        if (!mBuilder.mListeners.isEmpty()) {
            for (int i = 0; i < mBuilder.mListeners.size(); i++) {
                Listener l = mBuilder.mListeners.get(i);
                if (l != null) {
                    if (l instanceof Listener.Visibility) {
                        Listener.Visibility vis = (Listener.Visibility) l;
                        if (visibility == SHOWED) {
                            vis.onShown(this);
                        } else if (visibility == HIDDEN) {
                            vis.onHidden(this);
                        }
                        logValue(i, "onVisibilityChanged", visibility.name());
                    }
                } else {
                    logError(i, "onVisibilityChanged");
                }
            }
        }
    }
    
    @Override
    public final void onAnimationStart(Animator animator) {
    }
    
    @Override
    public final void onAnimationEnd(Animator animator) {
    }
    
    @Override
    public final void onAnimationCancel(Animator animator) {
    }
    
    @Override
    public final void onAnimationRepeat(Animator animator) {
    }
    
    private void logValue(int listener, String method, Object message) {
        if (mBuilder.mDebug) {
            Log.e(TAG, String.format("Listener(%1s) (%2$-23s) value = %3$s", listener, method, message));
        }
    }
    
    private void logError(int listener, String method) {
        if (mBuilder.mDebug) {
            Log.d(TAG, String.format("Listener(%1s) (%2$-23s) Listener is null, skip notification...", listener, method));
        }
    }
}
