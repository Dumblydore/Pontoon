package me.mauricee.pontoon.main.user

import android.os.Bundle
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_user.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R

class UserFragment : UserContract.View, BaseFragment<UserPresenter>() {
    override fun getLayoutId(): Int = R.layout.fragment_user
    override val actions: Observable<UserContract.Action>
        get() = Observable.just(UserContract.Action.Refresh(arguments!!.getString(UserKey)))

    override fun updateState(state: UserContract.State) = when (state) {
        is UserContract.State.User -> user_toolbar.title = state.user.username
        is UserContract.State.Comments -> {
        }
    }

    companion object {
        private const val UserKey = "UserKey"
        fun newInstance(userId: String) = UserFragment().apply { arguments = Bundle().apply { putString(UserKey, userId) } }
    }
}