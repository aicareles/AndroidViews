package com.example.androidviews

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.androidviews.databinding.ActivityMainBinding
import com.example.androidviews.ledview.LedView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.ledView.run {
            val layoutParams = layoutParams
            val width = resources.displayMetrics.widthPixels
            layoutParams.width = width
            layoutParams.height = width
            setLayoutParams(layoutParams)
            init(64, 64, 0.2f)
        }

        binding.colorBar.setOnColorChangerListener {
            binding.ledView.setSelectedColor(it)
        }
        binding.btnHorizontalMirror.setOnClickListener {
            binding.ledView.setMirrorMode(LedView.MIRROR_HORIZONTAL)
        }
        binding.btnVerticalMirror.setOnClickListener {
            binding.ledView.setMirrorMode(LedView.MIRROR_VERTICAL)
        }
        binding.btnMode.setOnClickListener {
            binding.ledView.run {
                when(mode){
                    LedView.MODE_PAINT-> {
                        mode = LedView.MODE_ERASER
                        binding.btnMode.text = "橡皮擦"
                    }
                    LedView.MODE_ERASER-> {
                        mode = LedView.MODE_PAINT
                        binding.btnMode.text = "画笔"
                    }
                }
            }
        }

    }
}