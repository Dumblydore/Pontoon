package me.mauricee.pontoon.tv.browse

import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.repository.video.Video
import me.mauricee.pontoon.tv.R
import me.mauricee.pontoon.tv.browse.BrowseFragmentDirections.actionBrowseFragmentToDetailHostFragment
import me.mauricee.pontoon.tv.util.PagedObjectAdapter
import me.mauricee.pontoon.ui.util.diff.DiffableItemCallback

@AndroidEntryPoint
class BrowseFragment : BrowseSupportFragment() {

    private val viewModel: BrowseViewModel by viewModels()
    private lateinit var mMetrics: DisplayMetrics
    private lateinit var mBackgroundManager: BackgroundManager
    private val adapters: MutableMap<String, PagedObjectAdapter<Video>> = mutableMapOf()

    private val subscriptions = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.events.observe(this) { event ->
            when(event) {
                is BrowseEvent.PlayVideo -> findNavController().navigate(actionBrowseFragmentToDetailHostFragment(event.videoId))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareBackgroundManager()
        setupEventListeners()
        setupUIElements()

        viewModel.state.map(BrowseState::rows).distinctUntilChanged().observe(viewLifecycleOwner, ::loadRows)
        viewModel.state.map(BrowseState::background).distinctUntilChanged().observe(viewLifecycleOwner, ::updateBackground)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscriptions.clear()
    }

    private fun prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager.attach(requireActivity().window)
        mMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)
    }


    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        // over title
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // set fastLane (or headers) background color
        brandColor = ContextCompat.getColor(requireContext(), R.color.fastlane_background)
        // set search icon color
        searchAffordanceColor = ContextCompat.getColor(requireContext(), R.color.search_opaque)
    }

    //
    private fun loadRows(rows: List<BrowseRow>) {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        rows.forEachIndexed { index, row ->
            val header = HeaderItem(index.toLong(), row.name)
            val adapter = adapters.getOrElse(row.id) {
                PagedObjectAdapter<Video>(VideoPresenterViewHolder(), DiffableItemCallback())
            }.also { it.submitList(row.videos) }
            rowsAdapter.add(ListRow(header, adapter))
        }
        val gridHeader = HeaderItem(rows.size.toLong(), "PREFERENCES")
        val mGridPresenter = GridItemPresenter()
        val gridRowAdapter = ArrayObjectAdapter(mGridPresenter)
        gridRowAdapter.add(resources.getString(R.string.grid_view))
        gridRowAdapter.add(getString(R.string.error_fragment))
        gridRowAdapter.add(resources.getString(R.string.personal_settings))
        rowsAdapter.add(ListRow(gridHeader, gridRowAdapter))
        adapter = rowsAdapter
    }

    //
    private fun setupEventListeners() {
        setOnSearchClickedListener {
            Toast.makeText(requireContext(), "Implement your own in-app search", Toast.LENGTH_LONG)
                    .show()
        }

        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(itemViewHolder: Presenter.ViewHolder?, item: Any?,
                                    rowViewHolder: RowPresenter.ViewHolder, row: Row) {
            if (item is Video) {
                viewModel.sendAction(BrowseAction.VideoSelected(item))
            } else {
                viewModel.sendAction(BrowseAction.ClearVideoSelected)
            }
        }
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder,
                item: Any,
                rowViewHolder: RowPresenter.ViewHolder,
                row: Row) {

            if (item is Video) {
                viewModel.sendAction(BrowseAction.VideoClicked(item))
//                Log.d(TAG, "Item: " + item.toString())
//                val intent = Intent(requireContext(), DetailsActivity::class.java)
//                intent.putExtra(DetailsActivity.MOVIE, item)
//
//                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        requireActivity(),
//                        (itemViewHolder.view as ImageCardView).mainImageView,
//                        DetailsActivity.SHARED_ELEMENT_NAME)
//                        .toBundle()
//                requireActivity().startActivity(intent, bundle)
            } else if (item is String) {
                if (item.contains(getString(R.string.error_fragment))) {
//                    val intent = Intent(requireContext(), BrowseErrorActivity::class.java)
//                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), item, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    //
    private fun updateBackground(uri: String?) {
        if (uri == null) {
            mBackgroundManager.drawable = ContextCompat.getDrawable(requireContext(), R.drawable.default_background)
        } else {
            val width = mMetrics.widthPixels
            val height = mMetrics.heightPixels
            subscriptions += Single.fromFuture(Glide.with(requireContext())
                    .load(uri)
                    .centerCrop()
                    .error(R.drawable.default_background)
                    .submit(width, height)).subscribeOn(Schedulers.io()).subscribe { resource ->
                mBackgroundManager.drawable = resource
            }
        }
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.default_background))
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return Presenter.ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}
    }

    companion object {
        private const val GRID_ITEM_WIDTH = 200
        private const val GRID_ITEM_HEIGHT = 200
    }
}