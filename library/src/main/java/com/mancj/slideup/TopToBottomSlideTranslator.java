package com.mancj.slideup;


import static com.mancj.slideup.Internal.calculateHiddenTranslation;
import static com.mancj.slideup.SlideUp.State.HIDDEN;
import static com.mancj.slideup.SlideUp.State.SHOWED;

public class TopToBottomSlideTranslator extends AbstractSlideTranslator {
  public TopToBottomSlideTranslator(SlideUpBuilder builder, AnimationProcessor animationProcessor) {
    super(builder, animationProcessor);
  }

  @Override
  protected void immediatelyShowSlideView() {
    if (mBuilder.mSliderView.getHeight() > 0) {
      mBuilder.mSliderView.setTranslationY(0);
    } else {
      mBuilder.mStartState = SHOWED;
    }
  }

  @Override
  public void setTranslationY(float translationY) {
    super.setTranslationY(-translationY);
  }

  @Override
  protected void animateShowSlideView() {
    mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.getTranslationY(), 0);
  }

  @Override
  protected void immediatelyHideSlideView() {
    if (mBuilder.mSliderView.getHeight() > 0) {
      mBuilder.mSliderView.setTranslationY(calculateHiddenTranslation(mBuilder));
    } else {
      mBuilder.mStartState = HIDDEN;
    }
  }

  @Override
  protected void animateHideSlideView() {
    mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.getTranslationY(), calculateHiddenTranslation(mBuilder));
  }
}
