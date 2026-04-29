package com.familytv.app.common.widget

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import androidx.recyclerview.widget.RecyclerView

class TvFocusHelper {
    companion object {
        private const val SCALE_FACTOR = 1.05f
        private const val ANIMATION_DURATION = 100L

        fun applyFocusEffect(view: View) {
            view.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    animateScaleUp(v)
                } else {
                    animateScaleDown(v)
                }
            }
        }

        private fun animateScaleUp(view: View) {
            val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, SCALE_FACTOR)
            val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, SCALE_FACTOR)
            AnimatorSet().apply {
                duration = ANIMATION_DURATION
                playTogether(scaleX, scaleY)
                start()
            }
        }

        private fun animateScaleDown(view: View) {
            val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, SCALE_FACTOR, 1f)
            val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, SCALE_FACTOR, 1f)
            AnimatorSet().apply {
                duration = ANIMATION_DURATION
                playTogether(scaleX, scaleY)
                start()
            }
        }

        fun applyFocusBorder(view: View) {
            view.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    v.bringToFront()
                    (v.parent as? ViewGroup)?.requestLayout()
                    (v.parent as? ViewGroup)?.invalidate()
                }
            }
        }

        fun setupRecyclerViewFocus(recyclerView: RecyclerView) {
            recyclerView.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && recyclerView.childCount > 0) {
                    val firstVisibleChild = recyclerView.getChildAt(0)
                    firstVisibleChild?.requestFocus()
                }
            }
        }

        fun addSafeAreaPadding(view: View, left: Int, top: Int, right: Int, bottom: Int) {
            view.updatePadding(
                left = left,
                top = top,
                right = right,
                bottom = bottom
            )
        }
    }
}
