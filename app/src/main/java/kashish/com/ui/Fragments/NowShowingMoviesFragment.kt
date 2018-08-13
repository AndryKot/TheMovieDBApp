package kashish.com.ui.Fragments


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.TextView
import android.widget.Toast
import kashish.com.Injection

import kashish.com.R
import kashish.com.adapters.MovieAdapter
import kashish.com.adapters.NowShowingAdapter
import kashish.com.database.AppDatabase
import kashish.com.database.AppExecutors
import kashish.com.database.Entities.NowShowingEntry
import kashish.com.interfaces.OnMovieClickListener
import kashish.com.models.Movie
import kashish.com.requestmodels.MovieRequest
import kashish.com.network.NetworkService
import kashish.com.ui.Activities.DetailActivity
import kashish.com.utils.Constants
import kashish.com.utils.Constants.Companion.CONTENT_MOVIE
import kashish.com.utils.Constants.Companion.CONTENT_PROGRESS
import kashish.com.utils.Urls.Companion.TMDB_API_KEY
import kashish.com.viewmodels.NowShowingViewModel
import retrofit2.Call
import retrofit2.Callback


/**
 * A simple [Fragment] subclass.
 */
class NowShowingMoviesFragment : Fragment(), OnMovieClickListener {

    private val TAG:String = "NowShowinMoviesFragment"
    private val GRID_COLUMNS_PORTRAIT = 1
    private val GRID_COLUMNS_LANDSCAPE = 2
    private lateinit var mMainView : View
    private lateinit var mRecyclerView : RecyclerView
    private lateinit var mSwipeRefreshLayout : SwipeRefreshLayout
    private lateinit var mGridLayoutManager : GridLayoutManager
    private lateinit var emptyList: TextView

    private lateinit var viewModel: NowShowingViewModel
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var networkService: NetworkService
    private lateinit var mDatabase: AppDatabase



    lateinit var mMovieAdapter: NowShowingAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_now_showing, container, false)

        initViews()
        initRecyclerView()
        setSwipeRefreshLayoutListener()
        setupScrollListener()
        getNowShowingData()

        return mMainView
    }

    private fun initViews(){
        mRecyclerView = mMainView.findViewById(R.id.fragment_now_showing_movies_recycler_view)
        mSwipeRefreshLayout = mMainView.findViewById(R.id.fragment_now_showing_movies_swipe_refresh)
        emptyList = mMainView.findViewById(R.id.emptyNowShowingList)

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        networkService = NetworkService.instance
        mDatabase = AppDatabase.getInstance(context!!.applicationContext)
    }

    private fun initRecyclerView() {
        configureRecyclerAdapter(resources.configuration.orientation)
        viewModel = ViewModelProviders.of(this, Injection.provideNowShowingViewModelFactory(context!!))
                .get(NowShowingViewModel::class.java)

        mMovieAdapter = NowShowingAdapter(this,mSharedPreferences)
        mRecyclerView.adapter = mMovieAdapter

        Toast.makeText(context,"Started",Toast.LENGTH_SHORT).show()

        viewModel.nowshowing.observe(this, Observer<List<NowShowingEntry>> {
            Log.i("asdfghjkjhgfdfghj", "list: ${it?.size}")
            showEmptyList(it?.size == 0)
            mMovieAdapter.submitList(it)
        })
        viewModel.networkErrors.observe(this, Observer<String> {
            Toast.makeText(context, "\uD83D\uDE28 Wooops ${it}", Toast.LENGTH_LONG).show()
        })
    }
    private fun setSwipeRefreshLayoutListener() {
        mSwipeRefreshLayout.setOnRefreshListener {
            AppExecutors.getInstance().diskIO().execute(Runnable {
                mDatabase.nowShowingDao().deleteAll()
            })
            mRecyclerView.scrollToPosition(0)
            viewModel.getNowShowing()
            mMovieAdapter.submitList(null)
            mSwipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getNowShowingData(){
        viewModel.getNowShowing()
        mMovieAdapter.submitList(null)
        mSwipeRefreshLayout.isRefreshing = false
    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
            emptyList.visibility = View.VISIBLE
            mRecyclerView.visibility = View.GONE
        } else {
            emptyList.visibility = View.GONE
            mRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupScrollListener() {
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = mGridLayoutManager.itemCount
                val visibleItemCount = mGridLayoutManager.childCount
                val lastVisibleItem = mGridLayoutManager.findLastVisibleItemPosition()

                viewModel.listScrolled(visibleItemCount, lastVisibleItem, totalItemCount)
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerAdapter(newConfig!!.orientation)
    }

    private fun configureRecyclerAdapter(orientation: Int) {
        val isPortrait = orientation == Configuration.ORIENTATION_PORTRAIT
        mGridLayoutManager = GridLayoutManager(context, if (isPortrait) GRID_COLUMNS_PORTRAIT else GRID_COLUMNS_LANDSCAPE)
        mRecyclerView.setLayoutManager(mGridLayoutManager)
    }

    override fun onMovieClickListener(movie: Movie) {
        val detailIntent = Intent(context, DetailActivity::class.java)
        detailIntent.putExtra("movie",movie)
        context!!.startActivity(detailIntent)
    }



}// Required empty public constructor
