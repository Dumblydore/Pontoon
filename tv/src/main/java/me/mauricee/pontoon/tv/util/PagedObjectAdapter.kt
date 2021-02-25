package me.mauricee.pontoon.tv.util

import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.Presenter
import androidx.paging.AsyncPagedListDiffer
import androidx.paging.AsyncPagedListDiffer.PagedListListener
import androidx.paging.PagedList
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback

class PagedObjectAdapter<T : Any> constructor(presenter: Presenter, diffCallback: DiffUtil.ItemCallback<T>) : ArrayObjectAdapter(presenter), ListUpdateCallback {
    private val mDiffer: AsyncPagedListDiffer<T> = AsyncPagedListDiffer(this, AsyncDifferConfig.Builder(diffCallback).build())
    private val mListener = PagedListListener(::onCurrentListChanged)

    init {
        mDiffer.addPagedListListener(mListener)
    }

    /**
     * Set the new list to be displayed.
     *
     *
     * If a list is already being displayed, a diff will be computed on a background thread, which
     * will dispatch Adapter.notifyItem events on the main thread.
     *
     * @param pagedList The new list to be displayed.
     */
    fun submitList(pagedList: PagedList<T>?) {
        mDiffer.submitList(pagedList)
    }

    /**
     * Set the new list to be displayed.
     *
     *
     * If a list is already being displayed, a diff will be computed on a background thread, which
     * will dispatch Adapter.notifyItem events on the main thread.
     *
     *
     * The commit callback can be used to know when the PagedList is committed, but note that it
     * may not be executed. If PagedList B is submitted immediately after PagedList A, and is
     * committed directly, the callback associated with PagedList A will not be run.
     *
     * @param pagedList The new list to be displayed.
     * @param commitCallback Optional runnable that is executed when the PagedList is committed, if
     * it is committed.
     */
    fun submitList(pagedList: PagedList<T>?,
                   commitCallback: Runnable?) {
        mDiffer.submitList(pagedList, commitCallback)
    }

    protected fun getItem(position: Int): T? {
        return mDiffer.getItem(position)
    }

//    override fun getItemCount(): Int {
//        return mDiffer.itemCount
//    }

    /**
     * Returns the PagedList currently being displayed by the Adapter.
     *
     *
     * This is not necessarily the most recent list passed to [.submitList],
     * because a diff is computed asynchronously between the new list and the current list before
     * updating the currentList value. May be null if no PagedList is being presented.
     *
     * @return The list currently being displayed.
     *
     * @see .onCurrentListChanged
     */
    val currentList: PagedList<T>?
        get() = mDiffer.currentList

    /**
     * Called when the current PagedList is updated.
     *
     *
     * This may be dispatched as part of [.submitList] if a background diff isn't
     * needed (such as when the first list is passed, or the list is cleared). In either case,
     * PagedListAdapter will simply call
     * [notifyItemRangeInserted/Removed(0, mPreviousSize)][.notifyItemRangeInserted].
     *
     *
     * This method will *not*be called when the Adapter switches from presenting a PagedList
     * to a snapshot version of the PagedList during a diff. This means you cannot observe each
     * PagedList via this method.
     *
     * @param previousList PagedList that was previously displayed, may be null.
     * @param currentList new PagedList being displayed, may be null.
     *
     * @see .getCurrentList
     */
    fun onCurrentListChanged(previousList: PagedList<T>?, currentList: PagedList<T>?) {

    }

    override fun onInserted(position: Int, count: Int) {
        addAll(position, currentList?.subList(position, count))
    }

    override fun onRemoved(position: Int, count: Int) {
        removeItems(position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        move(fromPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        notifyArrayItemRangeChanged(position, count)
    }
}