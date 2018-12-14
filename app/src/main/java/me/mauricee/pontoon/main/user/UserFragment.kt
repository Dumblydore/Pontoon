package me.mauricee.pontoon.main.user

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_user.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.common.SpaceItemDecoration
import javax.inject.Inject

class UserFragment : UserContract.View, BaseFragment<UserPresenter>() {

    @Inject
    lateinit var adapter: UserActivityAdapter

    override val actions: Observable<UserContract.Action>
        get() = Observable.merge(adapter.actions,
                RxSwipeRefreshLayout.refreshes(user_container).map { refresh })
                .startWith(refresh)

    private val refresh by lazy { UserContract.Action.Refresh(arguments!!.getString(UserKey)) }
    override fun getLayoutId(): Int = R.layout.fragment_user

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user_container_lazy.setupWithSwipeRefreshLayout(user_container)
        user_container_activity.layoutManager = LinearLayoutManager(requireContext())
        user_container_activity.adapter = adapter
        user_container_activity.addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.grid_spacing)))
    }

    override fun updateState(state: UserContract.State) = when (state) {
        is UserContract.State.User -> user_toolbar.title = state.user.username
        UserContract.State.Loading -> user_container_lazy.state = LazyLayout.LOADING
        is UserContract.State.Activity -> {
            adapter.submitList(state.activity)
            user_container_lazy.state = LazyLayout.SUCCESS
        }
        is UserContract.State.Error -> user_container_lazy.state = LazyLayout.ERROR
    }

    companion object {
        private const val UserKey = "UserKey"
        fun newInstance(userId: String) = UserFragment().apply { arguments = Bundle().apply { putString(UserKey, userId) } }
    }
}