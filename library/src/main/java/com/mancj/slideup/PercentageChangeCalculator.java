package com.mancj.slideup;

public interface PercentageChangeCalculator {

  /**
   * Calculate what percentage of how much the SlideUp is opened.
   *
   * @return 100 = fully closed, 0 = fully open
   */
  float recalculatePercentage();
}
