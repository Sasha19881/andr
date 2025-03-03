import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import kotlin.random.Random

inner class SlotAnimator(private val imageView: ImageView) {
    private var currentAnimator: ValueAnimator? = null
    private var currentSymbolIndex = 0
    private var isSpinning = false
    private val numberOfSymbols = slotSymbols.size
    private val animationDuration = 1200L // Немного увеличена длительность
    private val numberOfCycles = 4       // Увеличено количество циклов
    private val fadeDuration = 150L       // Длительность анимации затухания

    fun startSpinning(onSpinEnd: () -> Unit) {
        if (isSpinning) return

        isSpinning = true
        currentAnimator?.cancel()
        imageView.translationY = 0f // Reset translation

        currentAnimator = ValueAnimator.ofInt(0, numberOfSymbols * numberOfCycles)
        currentAnimator?.duration = animationDuration
        currentAnimator?.interpolator = AccelerateDecelerateInterpolator()

        currentAnimator?.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val symbolIndex = value % numberOfSymbols

            // Анимация затухания
            val fadeOut = AlphaAnimation(1f, 0f)
            fadeOut.duration = fadeDuration
            fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}

                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    imageView.setImageResource(slotSymbols[symbolIndex])

                    val fadeIn = AlphaAnimation(0f, 1f)
                    fadeIn.duration = fadeDuration
                    imageView.startAnimation(fadeIn)
                }

                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })

            imageView.startAnimation(fadeOut)
        }

        currentAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                hideAllLines()
            }

            override fun onAnimationEnd(animation: Animator) {
                isSpinning = false
                onSpinEnd()
            }
        })

        currentAnimator?.start()

        val delay = (500 until 1500).random().toLong()
        Handler(Looper.getMainLooper()).postDelayed({
            currentAnimator?.cancel()
            currentAnimator?.removeAllListeners()
            isSpinning = false
            onSpinEnd()
        }, delay)
    }
}