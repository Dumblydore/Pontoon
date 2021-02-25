package me.mauricee.pontoon.tv.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import me.mauricee.pontoon.tv.HostFragment
import me.mauricee.pontoon.ui.UiState
import me.mauricee.pontoon.ui.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class DetailHostFragment : HostFragment<DetailsFragment>() {

    @Inject
    lateinit var presenterFactory: DetailPresenter.Factory

    @Inject
    lateinit var viewModelFactory: DetailViewModel.Factory

    private val args: DetailHostFragmentArgs by navArgs()
    private val viewModel: DetailViewModel by assistedViewModel {
        viewModelFactory.create(presenterFactory.create(args.videoId))
    }

    override val content: DetailsFragment
        get() = DetailsFragment()

    override fun uiState(): LiveData<UiState> = viewModel.state.map(DetailState::uiState)
}