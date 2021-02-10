package me.mauricee.pontoon.ui.preferences.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commitNow
import com.mikepenz.aboutlibraries.LibsBuilder
import dagger.hilt.android.AndroidEntryPoint
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentAboutBinding
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.ui.BaseFragment

@AndroidEntryPoint
class AboutFragment : BaseFragment(R.layout.fragment_about) {
    private val binding by viewBinding(FragmentAboutBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.commitNow {
            replace(binding.root.id, LibsBuilder().supportFragment())
        }
    }
}