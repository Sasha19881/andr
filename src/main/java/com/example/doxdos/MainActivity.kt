package com.example.doxdos

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doxdos.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var slotAnimators: List<SlotAnimator>
    private var mediaPlayer: MediaPlayer? = null

    private val slotSymbols = intArrayOf(
        R.drawable.cherry,
        R.drawable.vino,
        R.drawable.pas,
        R.drawable.lemon,
        R.drawable.seven,
        R.drawable.arbuz
    )
    private val symbolWeights = intArrayOf(15, 10, 15, 20, 20, 20)

    private var balance = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        slotAnimators = listOf(
            SlotAnimator(binding.slot1ImageView),
            SlotAnimator(binding.slot2ImageView),
            SlotAnimator(binding.slot3ImageView),
            SlotAnimator(binding.slot4ImageView),
            SlotAnimator(binding.slot5ImageView),
            SlotAnimator(binding.slot6ImageView),
            SlotAnimator(binding.slot7ImageView),
            SlotAnimator(binding.slot8ImageView),
            SlotAnimator(binding.slot9ImageView)
        )

        updateBalanceText()

        binding.spinButton.setOnClickListener {
            spinSlots()
        }

        // Обработчики для кнопок увеличения/уменьшения ставки
        binding.increaseBetButton.setOnClickListener {
            increaseBet()
        }
        binding.decreaseBetButton.setOnClickListener {
            decreaseBet()
        }
    }

    private fun increaseBet() {
        try {
            var betAmount = binding.betEditText.text.toString().toInt()
            betAmount += 10 // Шаг увеличения
            binding.betEditText.setText(betAmount.toString())
        } catch (e: NumberFormatException) {
            // Обработка ошибки, если в EditText не число
            binding.betEditText.setText("10")
        }
    }

    private fun decreaseBet() {
        try {
            var betAmount = binding.betEditText.text.toString().toInt()
            if (betAmount > 10) {
                betAmount -= 10 // Шаг уменьшения
                binding.betEditText.setText(betAmount.toString())
            }
        } catch (e: NumberFormatException) {
            // Обработка ошибки, если в EditText не число
            binding.betEditText.setText("10")
        }
    }

    private fun spinSlots() {
        try {
            val betAmount = binding.betEditText.text.toString().toInt()

            if (betAmount <= 0) {
                Toast.makeText(this, "Ставка должна быть больше нуля!", Toast.LENGTH_SHORT).show()
                return
            }

            if (balance >= betAmount) {
                balance -= betAmount
                updateBalanceText()

                val results = IntArray(9) { getRandomIndexWithWeights() }

                slotAnimators.forEachIndexed { index, slotAnimator ->
                    slotAnimator.startSpinning {
                        //  binding.apply { //Оптимизируем убираем это
                        binding.slot1ImageView.setImageResource(slotSymbols[results[0]])
                        binding.slot2ImageView.setImageResource(slotSymbols[results[1]])
                        binding.slot3ImageView.setImageResource(slotSymbols[results[2]])
                        binding.slot4ImageView.setImageResource(slotSymbols[results[3]])
                        binding.slot5ImageView.setImageResource(slotSymbols[results[4]])
                        binding.slot6ImageView.setImageResource(slotSymbols[results[5]])
                        binding.slot7ImageView.setImageResource(slotSymbols[results[6]])
                        binding.slot8ImageView.setImageResource(slotSymbols[results[7]])
                        binding.slot9ImageView.setImageResource(slotSymbols[results[8]])
                        //   }

                        val winInfo = calculateTotalWin(results, betAmount)
                        val winAmount = winInfo.first
                        val winningLines = winInfo.second

                        balance += winAmount
                        updateBalanceText()

                        binding.winTextView.text = "Выигрыш: $winAmount"

                        if (winAmount > 0) {
                            playWinSound()
                            blinkWinningSymbols(winningLines)
                        }

                        showWinningLines(winningLines, results)
                    }
                }


            } else {
                Toast.makeText(this, "Недостаточно средств!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Введите корректную сумму ставки!", Toast.LENGTH_SHORT).show()
            binding.betEditText.setText("10")
        }
    }


    private fun getRandomIndexWithWeights(): Int {
        val totalWeight = symbolWeights.sum()
        val randomNumber = Random.nextInt(totalWeight)

        var currentWeight = 0
        for (i in symbolWeights.indices) {
            currentWeight += symbolWeights[i]
            if (randomNumber < currentWeight) {
                return i
            }
        }
        return 0
    }

    private fun calculateTotalWin(results: IntArray, betAmount: Int): Pair<Int, List<IntArray>> {
        var totalWin = 0
        val winningLines = mutableListOf<IntArray>()

        val lines = arrayOf(
            intArrayOf(0, 1, 2), // Top row
            intArrayOf(3, 4, 5), // Middle row
            intArrayOf(6, 7, 8), // Bottom row
            intArrayOf(0, 3, 6), // Left column
            intArrayOf(1, 4, 7), // Middle column
            intArrayOf(2, 5, 8)  // Right column
        )

        for ((index, line) in lines.withIndex()) {
            val win = calculateWinForLine(
                slotSymbols[results[line[0]]],
                slotSymbols[results[line[1]]],
                slotSymbols[results[line[2]]],
                betAmount
            )
            if (win > 0) {
                totalWin += win
                winningLines.add(line)  // Add winning line
            }
        }

        return Pair(totalWin, winningLines)
    }

    private fun calculateWinForLine(slot1: Int, slot2: Int, slot3: Int, betAmount: Int): Int {
        val sevenSlotNumber = R.drawable.seven
        return when {
            slot1 == sevenSlotNumber && slot2 == sevenSlotNumber && slot3 == sevenSlotNumber -> betAmount * 20
            slot1 == slot2 && slot2 == slot3 -> betAmount * 5
            else -> 0
        }
    }

    private fun updateBalanceText() {
        binding.balanceTextView.text = "Баланс: $balance"
    }


    private fun showWinningLines(winningLines: List<IntArray>, results: IntArray) {
        //First Hide
        hideAllLines()

        for (line in winningLines) {
            when {
                line.contentEquals(intArrayOf(0, 1, 2)) -> { // Top row
                    binding.line1Horizontal.visibility = View.VISIBLE
                    binding.line2Horizontal.visibility = View.VISIBLE
                    binding.line3Horizontal.visibility = View.VISIBLE
                }

                line.contentEquals(intArrayOf(3, 4, 5)) -> { // Middle row
                    binding.line4Horizontal.visibility = View.VISIBLE
                    binding.line5Horizontal.visibility = View.VISIBLE
                    binding.line6Horizontal.visibility = View.VISIBLE
                }

                line.contentEquals(intArrayOf(6, 7, 8)) -> { // Bottom row
                    binding.line7Horizontal.visibility = View.VISIBLE
                    binding.line8Horizontal.visibility = View.VISIBLE
                    binding.line9Horizontal.visibility = View.VISIBLE
                }

                line.contentEquals(intArrayOf(0, 3, 6)) -> { // Left column
                    binding.line1Vertical.visibility = View.VISIBLE
                    binding.line4Vertical.visibility = View.VISIBLE
                    binding.line7Vertical.visibility = View.VISIBLE
                }

                line.contentEquals(intArrayOf(1, 4, 7)) -> { // Middle column
                    binding.line2Vertical.visibility = View.VISIBLE
                    binding.line5Vertical.visibility = View.VISIBLE
                    binding.line8Vertical.visibility = View.VISIBLE
                }

                line.contentEquals(intArrayOf(2, 5, 8)) -> { // Right column
                    binding.line3Vertical.visibility = View.VISIBLE
                    binding.line6Vertical.visibility = View.VISIBLE
                    binding.line9Vertical.visibility = View.VISIBLE
                }


            }
        }
    }

    private fun hideAllLines() {
        //Horizontal
        binding.line1Horizontal.visibility = View.GONE
        binding.line2Horizontal.visibility = View.GONE
        binding.line3Horizontal.visibility = View.GONE
        binding.line4Horizontal.visibility = View.GONE
        binding.line5Horizontal.visibility = View.GONE
        binding.line6Horizontal.visibility = View.GONE
        binding.line7Horizontal.visibility = View.GONE
        binding.line8Horizontal.visibility = View.GONE
        binding.line9Horizontal.visibility = View.GONE

        //Vertical
        binding.line1Vertical.visibility = View.GONE
        binding.line2Vertical.visibility = View.GONE
        binding.line3Vertical.visibility = View.GONE
        binding.line4Vertical.visibility = View.GONE
        binding.line5Vertical.visibility = View.GONE
        binding.line6Vertical.visibility = View.GONE
        binding.line7Vertical.visibility = View.GONE
        binding.line8Vertical.visibility = View.GONE
        binding.line9Vertical.visibility = View.GONE

    }

    private fun playWinSound() {
        mediaPlayer?.release() // Освобождаем ресурсы предыдущего MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.win)
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // Освобождаем ресурсы при уничтожении Activity
    }


    private fun blinkWinningSymbols(winningLines: List<IntArray>) {
        val slotImageViews = arrayOf(
            binding.slot1ImageView,
            binding.slot2ImageView,
            binding.slot3ImageView,
            binding.slot4ImageView,
            binding.slot5ImageView,
            binding.slot6ImageView,
            binding.slot7ImageView,
            binding.slot8ImageView,
            binding.slot9ImageView
        )

        val originalAlphas = slotImageViews.map { it.alpha } // Запоминаем исходные значения прозрачности

        val blinkAnimator = ValueAnimator.ofFloat(1f, 0.2f, 1f) // Мигание от 100% до 20% прозрачности и обратно
        blinkAnimator.duration = 500 // Длительность одного цикла мигания
        blinkAnimator.repeatCount = 3 // Количество повторений (мигнет 3 раза)
        blinkAnimator.interpolator = AccelerateDecelerateInterpolator()

        blinkAnimator.addUpdateListener { animator ->
            val alphaValue = animator.animatedValue as Float
            for (line in winningLines) {
                for (index in line) {
                    slotImageViews[index].alpha = alphaValue
                }
            }
        }

        blinkAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Возвращаем исходные значения прозрачности после завершения анимации
                for (i in slotImageViews.indices) {
                    slotImageViews[i].alpha = originalAlphas[i]
                }
            }
        })

        blinkAnimator.start()
    }

    // ------------------------------------------------------------------------------------------------------
    inner class SlotAnimator(private val imageView: ImageView) {
        private var currentAnimator: ValueAnimator? = null
        private var currentSymbolIndex = 0
        private var isSpinning = false // Флаг для отслеживания вращения

        fun startSpinning(onSpinEnd: () -> Unit) {
            if (isSpinning) {
                return  // Если уже вращается, ничего не делаем
            }

            isSpinning = true
            currentAnimator?.cancel()

            val startTranslationY = 0f
            val endTranslationY = imageView.height.toFloat()

            currentSymbolIndex = 0

            currentAnimator = ValueAnimator.ofFloat(startTranslationY, endTranslationY)
            currentAnimator?.duration = 700 // Adjust duration as needed
            currentAnimator?.interpolator = AccelerateDecelerateInterpolator()
            currentAnimator?.repeatCount = 2 // Reduce repeats

            currentAnimator?.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                imageView.translationY = value

                // Change image more frequently
                if (value % (imageView.height / 2) == 0f) { //Check after half the distance
                    currentSymbolIndex = Random.nextInt(slotSymbols.size)
                    imageView.setImageResource(slotSymbols[currentSymbolIndex])
                }
            }

            currentAnimator?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    hideAllLines()
                }

                override fun onAnimationEnd(animation: Animator) {
                    imageView.translationY = 0f
                    isSpinning = false // Сбрасываем флаг после завершения
                }
            })

            currentAnimator?.start()

            val delay = (1000..2000).random().toLong() //Reduce Delay

            Handler(Looper.getMainLooper()).postDelayed({
                currentAnimator?.cancel()
                currentAnimator?.removeAllListeners()
                imageView.translationY = 0f
                isSpinning = false // Также сбрасываем флаг здесь
                onSpinEnd()
            }, delay)
        }
    }
}
