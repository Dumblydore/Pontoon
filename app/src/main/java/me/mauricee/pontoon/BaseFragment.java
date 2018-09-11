package me.mauricee.pontoon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseFragment<P extends BaseContract.Presenter> extends DaggerFragment {

    @Inject
    protected P presenter;

    protected final CompositeDisposable subscriptions = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    @SuppressWarnings("UNCHECKED_CALL")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
        subscriptions.dispose();
    }

    /**
     * Method to reset the state of the fragment. e.g: scroll back to top of a list.
     */
    public void reset() {

    }

    @LayoutRes
    abstract protected int getLayoutId();
}
