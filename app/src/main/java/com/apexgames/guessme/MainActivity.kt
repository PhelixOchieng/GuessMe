package com.apexgames.guessme

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.LinearLayout.LayoutParams
import androidx.core.view.setPadding
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    // variables for the inputs, For entering the word
    private var updateInput = true
    private var clicked = 0

    // coinage variables and constants
    private val maxCoinsPerLevel = 10
    private val minCoinsPerLevel = maxCoinsPerLevel - 3
    private val coinDeduction = 1
    private val minHintThreshold = 1
    private var coinsThisLevel = maxCoinsPerLevel
    private var coinsAmount = 0  // Database retrieved the current coins number

    // the hint backgrounds
    private val hintBackgrounds = listOf(
        R.drawable.hint_container_1,
        R.drawable.hint_container_2
    )
    private var hintContainerIndex = 0

    // layout params for the hint text layout
    private val hint_layout_params: LayoutParams = LayoutParams(
        LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT
    )


    // Database fetch has been done to retrieve all words with their corresponding descriptions
    private val wordsList = mapOf(
        "car" to listOf(
            "I have seats and a roof but no fireplace",
            "I have a trunk though I'm not an elephant",
            "Sometimes I'm metal, sometimes I'm plastic and other times I'm both"
        ),
        "moon" to listOf(
            "I might look small but I'm quite huge",
            "Sometimes I'm light, sometimes I'm dark"
        ),
        "boat" to listOf(
            "I start out on land but then move to water full time",
            "Sometimes I'm metal sometimes I'm wood other times I'm both"
        )
    )
    // Keep track of used words
    private val words = wordsList.keys.toList()

    // These will only be loaded from the db and not always set to 0
    private var currentWordIndex = 0
    var hintIndex = 0

    var word = ""
    var wordDescriptions: List<String> = emptyList()
    var wordDescriptionSize = 0
    var wordSize = 0

    private fun showHint() {

        if (hintIndex < wordDescriptionSize) {
            var wordDescription = wordDescriptions[hintIndex]

            val t = TextView(this)
            hint_layout_params.setMargins(0, 0, 0, 10)
            t.layoutParams = hint_layout_params
            t.textSize = 14f
            t.gravity = Gravity.CENTER
            t.setPadding(7)
            t.setTextColor(getColor(R.color.hintColor))

            if (hintContainerIndex < hintBackgrounds.size) {
                t.setBackgroundResource(hintBackgrounds[hintContainerIndex])
                hintContainerIndex++
            } else {
                hintContainerIndex = 0
                t.setBackgroundResource(hintBackgrounds[hintContainerIndex])
            }

            if (coinsThisLevel > minCoinsPerLevel && hintIndex > minHintThreshold) {
                coinsThisLevel -= coinDeduction
            }

            t.text = wordDescription
            hint_layout.addView(t)

            var startValue = -300f
            val startVelocity = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics)
            if (hintIndex % 2 == 0) {
                startValue = -startValue
            }

            var springAnimationSlideLeft = t.let { hint ->
                SpringAnimation(hint, DynamicAnimation.TRANSLATION_X, 0f).apply {
                    setStartVelocity(startVelocity)
                    setStartValue(-startValue)

                    spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                    spring.stiffness = SpringForce.STIFFNESS_LOW

                    start()
                }
            }


                hintIndex++
        } else {
            Toast.makeText(this, "You have no more hints", Toast.LENGTH_SHORT).show()
        }
    }

    private fun LLog(str: Any) {
        Toast.makeText(this, "$str", Toast.LENGTH_SHORT).show()
    }

    private fun pickWord(): Boolean {
        if (currentWordIndex < words.size) {
            word = words[currentWordIndex]
            wordDescriptions = wordsList.getValue(word)
            wordDescriptionSize = wordDescriptions.size
            wordSize = word.length

            currentWordIndex++
            return true
        }

        return false
    }

    private fun clearHintsScreen() {
        hint_layout.removeAllViews()
    }

    private fun showInputs() {
        val input_layout_params: LayoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        val maxInputButtons = 12
        val distribution = (maxInputButtons / wordSize)

        var wordCharIndex = 0
        clicked = 0
        val wordChars = "abcdefghijklmnopqrstuvwxyz"



        for(i in 0 until wordSize) {
            val t = TextView(this)
            input_layout_params.setMargins(5, 0, 5, 0)
            t.layoutParams = input_layout_params
            t.textSize = 14f
            t.gravity = Gravity.CENTER
            t.setTextColor(Color.WHITE)
            t.setBackgroundResource(R.drawable.input_space)

            input_space_lay.addView(t)

            t.setOnClickListener {
                t.text = ""
                clicked--
            }
        }

        val dissolveAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.dissolve_input_tab)

        for(i in 0 until maxInputButtons) {
            val t = TextView(this)
            input_layout_params.setMargins(2, 0, 2, 0)
            t.layoutParams = input_layout_params
            t.textSize = 24f
            t.gravity = Gravity.CENTER
            t.setTextColor(Color.BLACK)
            t.setBackgroundResource(R.drawable.tile)
            t.id = View.generateViewId()

            var wordChar = wordChars[Random.nextInt(0, 26)].toString().toUpperCase()

            if ((i + 1) % distribution == 0) {
                wordChar = word[wordCharIndex].toString().toUpperCase()

                wordCharIndex++
            }
            t.text = wordChar


            t.setOnClickListener {
                if (updateInput) {
                    var view = input_space_lay.getChildAt(clicked) as TextView
                    view.text = t.text
                    clicked++

                    //                t.startAnimation(dissolveAnimation)

                    if (clicked == wordSize) {
                        if (!submitWord()) {
                            clicked = 0
                        }
                    }
                }
            }

            if (i % 2 == 0) {
                tiles_bottom_row.addView(t)
                continue
            }
            tiles_top_row.addView(t)

//            t.let {tile ->
//                SpringAnimation(tile, DynamicAnimation.TRANSLATION_Z, 0f).apply {
//                    setStartValue(1000f)
//
//                    spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
//                    spring.stiffness = SpringForce.STIFFNESS_LOW
//
//                    start()
//                }
//            }
        }
    }

    private fun clearUserInput() {
        input_space_lay.removeAllViews()
        tiles_top_row.removeAllViews()
        tiles_bottom_row.removeAllViews()
    }

    private fun nextLevel() {
        // pick the next word
        if (pickWord()) {
            // not end of the game yet
            clearHintsScreen()
            clearUserInput()

            var levelIndicatorText = level_indicator.text
            var levelIndicatorSplit = levelIndicatorText.split(" ")

            var levelNumber = (levelIndicatorSplit.last().toInt() + 1).toString()
            var newLevelText = (levelIndicatorSplit.first() + " " + levelNumber)

            level_indicator.text = newLevelText

            // reset hint counter and the text edit
            hintIndex = 0
            showInputs()
            showHint()
        } else {
            // its the end of the game i.e no more words
            Toast.makeText(this, "This is the end of the game", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCoins() {
        coinsAmount += coinsThisLevel
        coinsThisLevel = maxCoinsPerLevel

        coins_total.text = coinsAmount.toString()
    }

    private fun submitWord() : Boolean {
        updateInput = false

        val count = input_space_lay.childCount
        var submittedWord = ""
        for(i in 0 until count) {
            var e = input_space_lay.getChildAt(i) as TextView
            submittedWord += e.text.toString().toLowerCase()
        }

        if (submittedWord == word) {
            // user found the word. Go to next level
            for(i in 0 until count) {
                var e = input_space_lay.getChildAt(i) as TextView

                e.setTextColor(getColor(R.color.successColor))
            }

            Handler().postDelayed({
                updateCoins()
                nextLevel()

                updateInput = true
            }, 1000)

            return true
        }
        // false word submitted
//        val errorBlinkAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.error_blink)
        for(i in 0 until count) {
            var e = input_space_lay.getChildAt(i) as TextView
//            e.startAnimation(errorBlinkAnim)

            e.setTextColor(getColor(R.color.errorColor))
        }
        Handler().postDelayed( {
            for(i in 0 until count) {
                var e = input_space_lay.getChildAt(i) as TextView

                e.setTextColor(getColor(R.color.textInputColor))
                updateInput = true

            }
        }, 1005)

        Handler().postDelayed( {
            for(i in 0 until count) {
                var e = input_space_lay.getChildAt(i) as TextView

                e.text = ""
            }
        }, 1000 )

        return false
    }

    private fun showSettings() {
        val backgrounds = listOf(
            R.drawable.bg_1,
            R.drawable.bg_2,
            R.drawable.bg_3,
            R.drawable.bg_4,
            R.drawable.bg_5
        )

        background_lay.setBackgroundResource(backgrounds[3])
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // assumed these are gotten from db
        val currentLevelNumber = 0 // Starting game. The number is always 1 less than the actual level
        currentWordIndex = currentLevelNumber

        nextLevel()

        // Event listeners
        // Game play
        var expandForce = SpringForce(1.15f)
        var shrinkForce = SpringForce(1f)

        expandForce.setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY)
        shrinkForce.setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
            .setStiffness(SpringForce.STIFFNESS_LOW)


        next_hint_btn.setOnClickListener {

            val springAnimExpand = next_hint_btn.let { btn ->
                SpringAnimation(btn, DynamicAnimation.SCALE_X, 1.15f).apply {
                    spring = expandForce

                    start()
                }


            }
            val springAnimContract = next_hint_btn.let { btn ->
                SpringAnimation(btn, DynamicAnimation.SCALE_X, 1f).apply {
                    spring = shrinkForce

                }
            }


            springAnimExpand.addEndListener {_, _, _, _ ->
                springAnimContract.start()
            }
            showHint()
        }


        // Navigational
        home.setOnClickListener {
            Toast.makeText(this, "You Clicked Me", Toast.LENGTH_SHORT).show()
        }

        clue_btn.setOnClickListener {

        }

        settings_btn.setOnClickListener {
            showSettings()
        }
    }
}
