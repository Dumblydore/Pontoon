package me.mauricee.pontoon.ui.main.user

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ConcatAdapter
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.SimpleBindingAdapter
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.databinding.FragmentUserBinding
import me.mauricee.pontoon.databinding.ItemActivityCommentBinding
import me.mauricee.pontoon.ext.changingStatusBarColor
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.model.user.activity.ActivityEntity
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserFragment : BaseFragment(R.layout.fragment_user) {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var presenterFactory: UserPresenter.Factory

    @Inject
    lateinit var viewModelFactory: UserViewModel.Factory

    private val args by navArgs<UserFragmentArgs>()

    private val viewModel: UserViewModel by assistedViewModel {
        viewModelFactory.create(presenterFactory.create(UserArgs(args.userId)))
    }
    private val binding: FragmentUserBinding by viewBinding(FragmentUserBinding::bind)

    private val headerAdapter by lazy { UserHeaderAdapter(themeManager, binding.userToolbar, this) }
    private val adapter = SimpleBindingAdapter(ItemActivityCommentBinding::inflate, ::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changingStatusBarColor()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NavigationUI.setupWithNavController(binding.userToolbar, findNavController())

        binding.userContainerActivity.adapter = ConcatAdapter(headerAdapter, adapter)

        subscriptions += adapter.clicks.map { UserAction.ActivityClicked(it.model) }
                .subscribe(viewModel::sendAction)
        subscriptions += binding.userContainer.refreshes().map { UserAction.Refresh }
                .subscribe(viewModel::sendAction)

        viewModel.state.mapDistinct { it.uiState.lazyState() }.observe(viewLifecycleOwner) {
            binding.userContainerLazy.state = it
        }
        viewModel.state.map { it.uiState.isRefreshing() }.observe(viewLifecycleOwner) {
            binding.userContainer.isRefreshing = it
        }
        viewModel.state.mapDistinct(UserState::activity).observe(viewLifecycleOwner, adapter::submitList)

        viewModel.state.mapDistinct(UserState::user).notNull().observe(viewLifecycleOwner) {
            headerAdapter.user = it
        }
    }

    private fun bind(view: ItemActivityCommentBinding, activity: ActivityEntity) {
        view.itemTitle.text = getString(R.string.activity_comment_context)
        view.itemComment.text = activity.comment
    }
}