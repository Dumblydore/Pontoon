package me.mauricee.pontoon.ui;

import android.animation.Animator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;

public abstract class NewBaseFragment extends DaggerFragment {

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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscriptions.clear();
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

