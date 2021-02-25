package me.mauricee.pontoon.tv.detail

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.*
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.bumptech.glide.Glide
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.repository.video.Video
import me.mauricee.pontoon.tv.R
import me.mauricee.pontoon.tv.browse.VideoPresenterViewHolder

/**
 * A wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its metadata plus related videos.
 */
class DetailsFragment : DetailsSupportFragment() {

    private lateinit var mDetailsBackground: DetailsSupportFragmentBackgroundController
    private lateinit var mPresenterSelector: ClassPresenterSelector
    private lateinit var mAdapter: ArrayObjectAdapter
    private val subscriptions = CompositeDisposable()

    private val viewModel: DetailViewModel by viewModels({ requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.map(DetailState::video).distinctUntilChanged().observe(viewLifecycleOwner, ::displayVideo)
        viewModel.state.map(DetailState::relatedVideos).distinctUntilChanged().observe(viewLifecycleOwner, ::setupRelatedMovieListRow)
    }

    fun displayVideo(video: Video?) {
        if (video != null) {
            mPresenterSelector = ClassPresenterSelector()
            mAdapter = ArrayObjectAdapter(mPresenterSelector)
            setupDetailsOverviewRow(video)
            setupDetailsOverviewRowPresenter()
            adapter = mAdapter
            initializeBackground(video)
//            onItemViewClickedListener = ItemViewClickedListener()
        }
    }

    //
//    override fun onCreate(savedInstanceState: Bundle?) {
//        Log.d(TAG, "onCreate DetailsFragment")
//        super.onCreate(savedInstanceState)
//
//        mDetailsBackground = DetailsFragmentBackgroundController(this)
//
//        mSelectedMovie = activity.intent.getSerializableExtra(DetailsActivity.MOVIE) as Movie
//        if (mSelectedMovie != null) {

    //        } else {
//            val intent = Intent(context, MainActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
    private fun initializeBackground(movie: Video) {
//        mDetailsBackground.enableParallax()
//        Glide.with(context)
//                .load(movie?.backgroundImageUrl)
//                .asBitmap()
//                .centerCrop()
//                .error(R.drawable.default_background)
//                .into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>() {
//                    override fun onResourceReady(bitmap: Bitmap,
//                                                 glideAnimation: GlideAnimation<in Bitmap>) {
//                        mDetailsBackground.coverBitmap = bitmap
//                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
//                    }
//                })
    }

    //
    private fun setupDetailsOverviewRow(movie: Video) {
        val row = DetailsOverviewRow(movie)
        row.imageDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.default_background)
        val width = convertDpToPixel(requireContext(), DETAIL_THUMB_WIDTH)
        val height = convertDpToPixel(requireContext(), DETAIL_THUMB_HEIGHT)
        subscriptions += Glide.with(this)
                .load(movie.thumbnail)
                .centerCrop()
                .error(R.drawable.default_background)
                .submit(width, height).let { Single.fromFuture(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it ->
                    row.imageDrawable = it
                    requireView().post { mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size()) }
                }

        row.actionsAdapter = ArrayObjectAdapter().apply {
            add(Action(0, resources.getString(R.string.play)))
        }
        mAdapter.add(row)
    }

    //
    private fun setupDetailsOverviewRowPresenter() {
        // Set detail background.
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        detailsPresenter.backgroundColor = ContextCompat.getColor(requireContext(), R.color.selected_background)

        // Hook up transition element.
//        val sharedElementHelper = FullWidthDetailsOverviewSharedElementHelper()
//        sharedElementHelper.setSharedElementEnterTransition(
//                activity, DetailsActivity.SHARED_ELEMENT_NAME)
//        detailsPresenter.setListener(sharedElementHelper)
//        detailsPresenter.isParticipatingEntranceTransition = true
//
//        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->
//            if (action.id == ACTION_WATCH_TRAILER) {
//                val intent = Intent(context, PlaybackActivity::class.java)
//                intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie)
//                startActivity(intent)
//            } else {
//                Toast.makeText(context, action.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }
        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    //
    private fun setupRelatedMovieListRow(list: List<Video>) {
        val subcategories = arrayOf(getString(R.string.related_movies))

        val listRowAdapter = ArrayObjectAdapter(VideoPresenterViewHolder())
        listRowAdapter.addAll(0, list)
        val header = HeaderItem(0, subcategories[0])
        mAdapter.add(ListRow(header, listRowAdapter))
        mPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    //    private inner class ItemViewClickedListener : OnItemViewClickedListener {
//        override fun onItemClicked(
//                itemViewHolder: Presenter.ViewHolder?,
//                item: Any?,
//                rowViewHolder: RowPresenter.ViewHolder,
//                row: Row) {
//            if (item is Movie) {
//                Log.d(TAG, "Item: " + item.toString())
//                val intent = Intent(context, DetailsActivity::class.java)
//                intent.putExtra(resources.getString(R.string.movie), mSelectedMovie)
//
//                val bundle =
//                        ActivityOptionsCompat.makeSceneTransitionAnimation(
//                                activity,
//                                (itemViewHolder?.view as ImageCardView).mainImageView,
//                                DetailsActivity.SHARED_ELEMENT_NAME)
//                                .toBundle()
//                activity.startActivity(intent, bundle)
//            }
//        }
//    }
//
    companion object {
        //        private val TAG = "VideoDetailsFragment"
//
//        private val ACTION_WATCH_TRAILER = 1L
//        private val ACTION_RENT = 2L
//        private val ACTION_BUY = 3L
//
        private const val DETAIL_THUMB_WIDTH = 274
        private const val DETAIL_THUMB_HEIGHT = 274
//
//        private val NUM_COLS = 10
    }
}