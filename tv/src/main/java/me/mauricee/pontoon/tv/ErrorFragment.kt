package me.mauricee.pontoon.tv

import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View
import androidx.leanback.app.ErrorSupportFragment
import androidx.navigation.fragment.findNavController

/**
 * This class demonstrates how to extend [androidx.leanback.app.ErrorFragment].
 */
class ErrorFragment : ErrorSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = resources.getString(R.string.app_name)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.lb_ic_sad_cloud)
        message = resources.getString(R.string.error_fragment_message)
        setDefaultBackground(TRANSLUCENT)

        buttonText = resources.getString(R.string.dismiss_error)
        buttonClickListener = View.OnClickListener { findNavController().navigateUp() }
    }

    companion object {
        private const val TRANSLUCENT = true
    }
}