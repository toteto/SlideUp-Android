package com.mancj.slideup;

import android.support.annotation.NonNull;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.END;
import static android.view.Gravity.START;
import static android.view.Gravity.TOP;

/**
 * @author pa.gulko zTrap (12.07.2017)
 */
class Internal {
    private static Rect sRect = new Rect();

    static void checkNonNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }
    
    static boolean isUpEventInView(View view, MotionEvent event){
        view.getHitRect(sRect);
        return sRect.contains((int) event.getRawX(), (int) event.getRawY());
    }


    /**
     * Calculate at what translation should the {@link SlideUpBuilder#mSliderView} be in order to be hidden for the
     * start gravity specified in the builder.
     */
    public static float calculateHiddenTranslation(@NonNull SlideUpBuilder slideUpBuilder) {
        float pullTabWidth = slideUpBuilder.mPullTabView != null ? slideUpBuilder.mPullTabView.getWidth() : 0f;
        float pullTabHeight = slideUpBuilder.mPullTabView != null ? slideUpBuilder.mPullTabView.getHeight() : 0f;
        switch (slideUpBuilder.mStartGravity) {
            case TOP:
                return pullTabHeight - slideUpBuilder.mSliderView.getHeight();
            case BOTTOM:
                return slideUpBuilder.mSliderView.getHeight() - pullTabHeight;
            case START:
                return pullTabWidth - slideUpBuilder.mSliderView.getWidth();
            case END:
                return slideUpBuilder.mSliderView.getWidth() - pullTabWidth;
            default: throw new RuntimeException("Invalid hidden gravity of the slide view.");
        }
    }
}
