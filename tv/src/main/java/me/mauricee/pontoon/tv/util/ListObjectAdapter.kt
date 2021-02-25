package me.mauricee.pontoon.tv.util

import androidx.leanback.widget.ArrayObjectAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil

class ListObjectAdapter<T : Any> : ArrayObjectAdapter(), ObjectAdapterListCallback.ListProvider<T> {
    override val currentList: List<T>?
        get() = mDiffer?.currentList
    var mDiffer: AsyncListDiffer<T>? = null
    private val callback = ObjectAdapterListCallback<T>(this, this)
    private val mListener: AsyncListDiffer.ListListener<T> = object : AsyncListDiffer.ListListener<T> {
        override fun onCurrentListChanged(previousList: MutableList<T>, currentList: MutableList<T>) {
            onCurrentListChanged(previousList, currentList)
        }
    }

    protected fun ListAdapter(diffCallback: DiffUtil.ItemCallback<T>) {
        mDiffer = AsyncListDiffer(callback, AsyncDifferConfig.Builder(diffCallback).build())
        mDiffer!!.addListListener(mListener)
    }

    protected fun ListAdapter(config: AsyncDifferConfig<T?>) {
        mDiffer = AsyncListDiffer<T>(callback, config)
        mDiffer!!.addListListener(mListener)
    }

    /**
     * Submits a new list to be diffed, and displayed.
     *
     *
     * If a list is already being displayed, a diff will be computed on a background thread, which
     * will dispatch Adapter.notifyItem events on the main thread.
     *
     * @param list The new list to be displayed.
     */
    fun submitList(list: List<T>?) {
        mDiffer!!.submitList(list)
    }

    /**
     * Set the new list to be displayed.
     *
     *
     * If a List is already being displayed, a diff will be computed on a background thread, which
     * will dispatch Adapter.notifyItem events on the main thread.
     *
     *
     * The commit callback can be used to know when the List is committed, but note that it
     * may not be executed. If List B is submitted immediately after List A, and is
     * committed directly, the callback associated with List A will not be run.
     *
     * @param list The new list to be displayed.
     * @param commitCallback Optional runnable that is executed when the List is committed, if
     * it is committed.
     */
    fun submitList(list: List<T>?, commitCallback: Runnable?) {
        mDiffer!!.submitList(list, commitCallback)
    }

    protected fun getItem(position: Int): T? {
        return mDiffer!!.getCurrentList()[position]
    }

    fun getItemCount(): Int {
        return mDiffer!!.getCurrentList().size
    }

    /**
     * Called when the current List is updated.
     *
     *
     * If a `null` List is passed to [.submitList], or no List has been
     * submitted, the current List is represented as an empty List.
     *
     * @param previousList List that was displayed previously.
     * @param currentList new List being displayed, will be empty if `null` was passed to
     * [.submitList].
     *
     * @see .getCurrentList
     */
    fun onCurrentListChanged(previousList: List<T?>, currentList: List<T?>) {}
}