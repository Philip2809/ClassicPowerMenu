package com.kieronquinn.app.classicpowermenu.ui.base

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import autoCleared
import com.kieronquinn.app.classicpowermenu.components.blur.BlurProvider
import com.kieronquinn.monetcompat.core.MonetCompat
import org.koin.android.ext.android.inject

abstract class BaseDialogFragment<T: ViewBinding>(private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> T): DialogFragment() {

    internal val monet by lazy {
        MonetCompat.getInstance()
    }

    internal var binding by autoCleared<T>()

    private val blurProvider by inject<BlurProvider>()

    private var isBlurShowing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate.invoke(layoutInflater, container, false)
        return binding.root
    }

    private var showBlurAnimation: ValueAnimator? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            showBlurAnimation = ValueAnimator.ofFloat(0f, 1.25f).apply {
                duration = 250L
                addUpdateListener {
                    val progress = it.animatedValue as Float
                    dialog?.window?.decorView?.alpha = progress
                    applyBlur(progress)
                }
                addListener(onEnd = {
                    isBlurShowing = true
                })
                start()
            }
        }
    }

    fun dismissWithAnimation(){
        showBlurAnimation?.cancel()
        ValueAnimator.ofFloat(1.25f, 0f).apply {
            duration = 250L
            addUpdateListener {
                val progress = it.animatedValue as Float
                dialog?.window?.decorView?.alpha = progress
                applyBlur(progress)
            }
            addListener(onEnd = {
                dismiss()
            })
        }.start()
    }

    private fun applyBlur(ratio: Float){
        val dialogWindow = dialog?.window ?: return
        blurProvider.applyBlurToWindow(dialogWindow, ratio)
    }

    override fun onResume() {
        super.onResume()
        if(isBlurShowing){
            view?.post {
                applyBlur(1.25f)
            }
        }
    }

}