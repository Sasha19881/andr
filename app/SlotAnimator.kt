import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.view.isVisible
import kotlin.random.Random

inner class SlotAnimator(private val imageView: ImageView) {
    private var currentAnimator: ValueAnimator? = null
    private var currentSymbolIndex = 0
    private var isSpinning = false
    private val numberOfSymbols = slotSymbols.size
    private val animationDuration = 1500L // Увеличим продолжительность
    private val symbolDisplayDuration = 200L // Время отображения одного символа

    fun startSpinning(onSpinEnd: () -> Unit) {
        if (isSpinning) return

        isSpinning = true
        currentAnimator?.cancel()
        imageView.translationY = 0f // Reset translation

        currentSymbolIndex = Random.nextInt(numberOfSymbols)
        imageView.setImageResource(slotSymbols[currentSymbolIndex])

        currentAnimator = ValueAnimator.ofFloat(0f, 1f)
        currentAnimator?.duration = animationDuration
        currentAnimator?.interpolator = LinearInterpolator() // Линейная интерполяция для равномерного движения
        currentAnimator?.repeatCount = ValueAnimator.INFINITE // Бесконечное повторение

        currentAnimator?.addUpdateListener { animation ->
            val progress = animation.animatedValue as Float
            val cycleProgress = (progress * animationDuration) % symbolDisplayDuration
            val nextSymbolIndex = (currentSymbolIndex + 1) % numberOfSymbols

            // Рассчитываем видимость текущего и следующего символов
            val currentVisibility = 1f - (cycleProgress / symbolDisplayDuration)
            val nextVisibility = cycleProgress / symbolDisplayDuration

            // Применяем значения к ImageView
            imageView.alpha = currentVisibility

            if (nextVisibility > 0) {
                // Если требуется отображение следующего символа, создаем новый ImageView
                val nextImageView = ImageView(imageView.context)
                nextImageView.setImageResource(slotSymbols[nextSymbolIndex])
                nextImageView.layoutParams = imageView.layoutParams
                nextImageView.alpha = nextVisibility
                nextImageView.translationY = imageView.height.toFloat() // Сдвигаем вниз

                (imageView.parent as? View)?.let { parentView ->
                    (parentView as? androidx.constraintlayout.widget.ConstraintLayout)?.addView(nextImageView)

                    // Удаляем предыдущий следующий символ
                    parentView.findViewWithTag<ImageView>("nextSymbol")?.let { oldNextSymbol ->
                        (parentView as? androidx.constraintlayout.widget.ConstraintLayout)?.removeView(oldNextSymbol)
                    }

                    nextImageView.tag = "nextSymbol" // Добавляем тэг для поиска
                }
            }
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

        val delay = (2000 until 3000).random().toLong() // Задержка для остановки
        Handler(Looper.getMainLooper()).postDelayed({
            currentAnimator?.cancel()
            currentAnimator?.removeAllListeners()
            imageView.translationY = 0f
            isSpinning = false

            // Удаляем следующий символ при остановке
            (imageView.parent as? View)?.findViewWithTag<ImageView>("nextSymbol")?.let { nextSymbol ->
                (imageView.parent as? androidx.constraintlayout.widget.ConstraintLayout)?.removeView(nextSymbol)
            }

            onSpinEnd()
        }, delay)
    }
}
as? androidx.constraintlayout.widget.ConstraintLayout)?.removeView(nextSymbol)
}

onSpinEnd()
}, delay)
}
}