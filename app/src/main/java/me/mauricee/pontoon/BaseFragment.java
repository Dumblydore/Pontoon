package me.mauricee.pontoon;

import android.animation.Animator;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;
import me.mauricee.pontoon.common.theme.StyleKt;
import me.mauricee.pontoon.common.theme.ThemeManager;
import me.mauricee.pontoon.ext.ViewExtKt;

public abstract class BaseFragment<P extends BaseContract.Presenter> extends DaggerFragment {

    @Inject
    protected P presenter;
    @Inject
    protected ThemeManager manager;

    protected final CompositeDisposable subscriptions = new CompositeDisposable();
    protected final List<Animator> animations = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    @SuppressWarnings("UNCHECKED_CALL")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(getToolbar());
        presenter.attachView(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscriptions.clear();
        presenter.detachView();
        for (Animator animator : animations) {
            animator.cancel();
        }
    }

    /**
     * Method to reset the state of the fragment. e.g: scroll back to top of a list.
     */
    public void reset() {

    }

    @Nullable
    protected Toolbar getToolbar() {
        return null;
    }

    @LayoutRes
    abstract protected int getLayoutId();
}
