package me.mauricee.pontoon.tv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import me.mauricee.pontoon.tv.databinding.FragmentHostBinding
import me.mauricee.pontoon.ui.UiState
import me.mauricee.pontoon.ui.util.viewBinding.viewBinding

abstract class HostFragment<F : Fragment> : Fragment(R.layout.fragment_host) {
    protected abstract val content: F
    protected open val loading: Fragment = LoadingFragment()
    protected open val errorFragment: Fragment = ErrorFragment()

    private val binding by viewBinding(FragmentHostBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiState().distinctUntilChanged().observe(viewLifecycleOwner) { state ->
            val newFragment = when (state) {
                UiState.Empty -> null
                is UiState.Failed -> errorFragment
                UiState.Loading,
                UiState.Refreshing -> loading
                UiState.Success -> content
            }
            childFragmentManager.commit(allowStateLoss = true) {
                newFragment?.let { replace(binding.host.id, it) }
            }
        }
    }

    protected abstract fun uiState(): LiveData<UiState>
}