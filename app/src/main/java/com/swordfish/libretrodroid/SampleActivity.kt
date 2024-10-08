/*
 *     Copyright (C) 2022  Filippo Scognamiglio
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.libretrodroid

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.swordfish.radialgamepad.library.RadialGamePad
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.CrossConfig
import com.swordfish.radialgamepad.library.config.PrimaryDialConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.SecondaryDialConfig
import com.swordfish.radialgamepad.library.event.Event
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SampleActivity : AppCompatActivity() {
    private lateinit var retroGameView: GLRetroView

    private lateinit var leftPad: RadialGamePad
    private lateinit var rightPad: RadialGamePad

    private lateinit var mainContainerLayout: ConstraintLayout
    private lateinit var gameContainerLayout: FrameLayout
    private lateinit var leftGamePadContainer: FrameLayout
    private lateinit var rightGamePadContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.skyksit.retro.R.layout.sample_activity)

        mainContainerLayout = findViewById(com.skyksit.retro.R.id.maincontainer)
        gameContainerLayout = findViewById(com.skyksit.retro.R.id.gamecontainer)

        leftGamePadContainer = findViewById(com.skyksit.retro.R.id.leftgamepad)
        rightGamePadContainer = findViewById(com.skyksit.retro.R.id.rightgamepad)

        Timber.tag("SampleApp").d("on Created")
        /* Prepare config for GLRetroView */
        val data = GLRetroViewData(this).apply {
            /*
             * The name of the LibRetro core to load.
             * The typical location that libraries should be stored in is
             * app/src/main/jniLibs/<ABI>/
             *
             * ABI can be arm64-v8a, armeabi-v7a, x86, or x86_64
             */
            coreFilePath = "libmgba_libretro_android.so"

            /*
             * The path to the ROM to load.
             * Example: /data/data/<package-id>/files/example.gba
             */
            gameFilePath = filesDir.absolutePath + "/FFK.gba"

            /*
             * Direct ROM bytes to load.
             * This is mutually exclusive with gameFilePath.
             */
            gameFileBytes = null

            /* (Optional) System directory */
            systemDirectory = filesDir.absolutePath

            /* (Optional) Save file directory */
            savesDirectory = filesDir.absolutePath

            /* (Optional) Variables to give the LibRetro core */
            variables = arrayOf()

            /*
            * (Optional) SRAM state to deserialize upon init.
            * When games save their data, they store it in their SRAM.
            * It is necessary to preserve the SRAM upon closing the app
            * in order to load it again later.
            *
            * The SRAM can be serialized to a ByteArray via serializeSRAM().
            */
            saveRAMState = null

            /*
             * (Optional) Shader to apply to the view.
             *
             * SHADER_DEFAULT:      Bilinear filtering, can cause fuzziness in retro games.
             * SHADER_CRT:          Classic CRT scan lines.
             * SHADER_LCD:          Grid layout, similar to Nintendo DS bottom screens.
             * SHADER_SHARP:        Raw, unfiltered image.
             * SHADER_UPSCALING:    Improve the quality of retro graphics.
             */
            shader = ShaderConfig.Default

            /* Rumble events enabled */
            rumbleEventsEnabled = true

            /* Use low-latency audio on supported devices */
            preferLowLatencyAudio = true
        }

        Timber.tag("SampleApp").d("Game file path: ${filesDir.absolutePath + "/FFK.gba"}")

        /* Initialize the main emulator view */
        retroGameView = GLRetroView(this, data).apply {
            isFocusable = false
            isFocusableInTouchMode = false
        }

        retroGameView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }

        lifecycle.addObserver(retroGameView)
        gameContainerLayout.addView(retroGameView)

        initializeVirtualGamePad()

        lifecycleScope.launch {
            retroGameView.getRumbleEvents()
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collect {
                    handleRumbleEvent(it)
                }
        }
    }

    /* Pipe motion events to the GLRetroView */
    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            sendMotionEvent(
                event,
                GLRetroView.MOTION_SOURCE_DPAD,
                MotionEvent.AXIS_HAT_X,
                MotionEvent.AXIS_HAT_Y,
                0
            )
            sendMotionEvent(
                event,
                GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
                MotionEvent.AXIS_X,
                MotionEvent.AXIS_Y,
                0
            )
            sendMotionEvent(
                event,
                GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,
                MotionEvent.AXIS_Z,
                MotionEvent.AXIS_RZ,
                0
            )
        }
        return super.onGenericMotionEvent(event)
    }

    /*
     * Pipe hardware key events to the GLRetroView.
     *
     * WARNING: This method can override volume key events.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        retroGameView.sendKeyEvent(event.action, keyCode)
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        retroGameView.sendKeyEvent(event.action, keyCode)
        return super.onKeyUp(keyCode, event)
    }

    private fun sendMotionEvent(
        event: MotionEvent,
        source: Int,
        xAxis: Int,
        yAxis: Int,
        port: Int,
    ) {
        retroGameView.sendMotionEvent(
            source,
            event.getAxisValue(xAxis),
            event.getAxisValue(yAxis),
            port
        )
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is Event.Button -> {
                when (event.id) {
                    KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> retroGameView.frameSpeed = 5
                    KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> {
                        retroGameView.frameSpeed = 1
                        retroGameView.slowSpeed = 0.8f
                    }
                    else -> {
                        retroGameView.frameSpeed = 1
                        retroGameView.slowSpeed = 1.0f
                        retroGameView.sendKeyEvent(event.action, event.id)
                    }
                }
            }
            is Event.Direction -> retroGameView.sendMotionEvent(event.id, event.xAxis, event.yAxis)
        }
    }

    private fun handleRumbleEvent(rumbleEvent: RumbleEvent) {
        Timber.tag("SampleApp").i("Received rumble event: $rumbleEvent")
    }

    private fun initializeVirtualGamePad() {
        leftGamePadContainer.isVisible = true
        rightGamePadContainer.isVisible = true

        leftGamePadContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 레이아웃이 완료된 후 좌표 정보를 확인합니다.
                Timber.tag("SampleApp").d("leftGamePadContainer.left = ${leftGamePadContainer.left}")
                Timber.tag("SampleApp").d("leftGamePadContainer.top = ${leftGamePadContainer.top}")
                Timber.tag("SampleApp").d("leftGamePadContainer.right = ${leftGamePadContainer.right}")
                Timber.tag("SampleApp").d("leftGamePadContainer.bottom = ${leftGamePadContainer.bottom}")

                // 리스너를 제거합니다.
                leftGamePadContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })


        val theme = LemuroidTouchOverlayThemes.getGamePadTheme()
        leftPad = RadialGamePad(
            RadialGamePadConfig(
                theme = theme,
                sockets = 12,
                primaryDial = PrimaryDialConfig.Cross(CrossConfig(0)),
                secondaryDials = listOf(
                    SecondaryDialConfig.SingleButton(
                        2,
                        1f,
                        0f,
                        ButtonConfig(
                            id = KeyEvent.KEYCODE_BUTTON_SELECT,
                            label = "SELECT"
                        )
                    ),
                    SecondaryDialConfig.SingleButton(
                        3,
                        1f,
                        0f,
                        ButtonConfig(
                            id = KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
                            label = "FF"
                        )
                    ),
                    SecondaryDialConfig.SingleButton(
                        4,
                        1f,
                        0f,
                        ButtonConfig(
                            id = KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
                            label = "Slow"
                        )
                    ),
                    SecondaryDialConfig.Empty(
                        8,
                        1,
                        1f,
                        0f
                    ),
                    // When this stick is double tapped, it's going to fire a Button event
                    SecondaryDialConfig.Stick(
                        9,
                        2,
                        2.2f,
                        0.1f,
                        1,
                        KeyEvent.KEYCODE_BUTTON_THUMBL,
                        contentDescription = "Left Stick",
                        rotationProcessor = object : SecondaryDialConfig.RotationProcessor() {
                            override fun getRotation(rotation: Float): Float {
                                return rotation - 10f
                            }
                        }
                    )
                )
            )
            , 16f, this)
        rightPad = RadialGamePad(
            RadialGamePadConfig(
                theme = theme,
                sockets = 12,
                primaryDial = PrimaryDialConfig.PrimaryButtons(
                    listOf(
                        ButtonConfig(
                            id = KeyEvent.KEYCODE_BUTTON_A,
                            label = "A",
                            contentDescription = "Circle"
                        ),
                        ButtonConfig(
                            id = KeyEvent.KEYCODE_BUTTON_X,
                            label = "X",
                            contentDescription = "Triangle"
                        ),
                        ButtonConfig(
                            id = KeyEvent.KEYCODE_BUTTON_Y,
                            label = "Y",
                            contentDescription = "Square"
                        ),
                        ButtonConfig(
                            id = KeyEvent.KEYCODE_BUTTON_B,
                            label = "B",
                            contentDescription = "Cross"
                        )
                    )
                ),
                secondaryDials = listOf(
                    SecondaryDialConfig.DoubleButton(
                        2,
                        0f,
                        ButtonConfig(
                            id = KeyEvent.KEYCODE_BUTTON_R1,
                            label = "R"
                        )
                    ),
                    SecondaryDialConfig.SingleButton(
                        4,
                        1f,
                        0f,
                        ButtonConfig(
                            id = KeyEvent.KEYCODE_BUTTON_START,
                            label = "START"
                        )
                    ),
                    SecondaryDialConfig.SingleButton(
                        10,
                        1f,
                        -0.1f,
                        ButtonConfig(
                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                            label = "MENU"
                        )
                    ),
                    // When this stick is double tapped, it's going to fire a Button event
//                    SecondaryDialConfig.Cross(
//                        8,
//                        2,
//                        2.2f,
//                        0.1f,
//                        CrossConfig(0),
//                        rotationProcessor = object : SecondaryDialConfig.RotationProcessor() {
//                            override fun getRotation(rotation: Float): Float {
//                                return rotation + 8f
//                            }
//                        }
//                    )
                )
            ), 16f,this)

        // We want the pad anchored to the bottom of the screen
        leftPad.gravityX = -1f
        leftPad.gravityY = 1f

        rightPad.gravityX = 1f
        rightPad.gravityY = 1f

        leftGamePadContainer.addView(leftPad)
        rightGamePadContainer.addView(rightPad)

        lifecycleScope.launch {
            merge(leftPad.events(), rightPad.events())
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collect {
                    handleEvent(it)
                }
        }
    }
}
