package com.skydoves.githubfollows.view.ui.detail

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.skydoves.githubfollows.R
import com.skydoves.githubfollows.databinding.ActivityDetailBinding
import com.skydoves.githubfollows.extension.gone
import com.skydoves.githubfollows.factory.AppViewModelFactory
import com.skydoves.githubfollows.models.GithubUser
import com.skydoves.githubfollows.models.ItemDetail
import com.skydoves.githubfollows.utils.GlideApp
import com.skydoves.githubfollows.utils.SvgSoftwareLayerSetter
import com.skydoves.githubfollows.view.adapter.DetailAdapter
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.layout_detail_body.*
import kotlinx.android.synthetic.main.layout_detail_header.*
import kotlinx.android.synthetic.main.toolbar_default.view.*
import org.jetbrains.anko.toast
import javax.inject.Inject

/**
 * Developed by skydoves on 2018-01-27.
 * Copyright (c) 2018 skydoves rights reserved.
 */

class DetailActivity : AppCompatActivity() {

    @Inject lateinit var viewModelFactory: AppViewModelFactory

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory).get(DetailActivityViewModel::class.java) }
    private val binding by lazy { DataBindingUtil.setContentView<ActivityDetailBinding>(this, R.layout.activity_detail) }
    private val adapter by lazy { DetailAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        supportPostponeEnterTransition()

        initializeListeners()
        initializeUI()
    }

    private fun initializeListeners() {
        binding.detailToolbar.toolbar_home.setOnClickListener { onBackPressed() }
        detail_header_cardView.setOnClickListener {
            setResult(1000, Intent().putExtra(viewModel.getPreferenceUserKeyName(), getLoginFromIntent()))
            onBackPressed()
        }
    }

    private fun initializeUI() {
        binding.detailToolbar.toolbar_title.text = getLoginFromIntent()
        Glide.with(this)
                .load(getAvatarFromIntent())
               .apply(RequestOptions().circleCrop().dontAnimate())
                .listener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        supportStartPostponedEnterTransition()
                        observeViewModel()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        supportStartPostponedEnterTransition()
                        observeViewModel()
                        return false
                    }
                })
                .into(detail_header_avatar)

        detail_body_recyclerView.layoutManager = LinearLayoutManager(this)
        detail_body_recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.githubUserLiveData.observe(this, Observer { updateUI(it) })
        viewModel.toastMessage.observe(this, Observer { toast(it.toString()) })
        viewModel.fetchGithubUser(getLoginFromIntent())
    }

    private fun updateUI(githubUser: GithubUser?) {
        githubUser?.let {
            binding.detailHeader.githubUser = githubUser
            binding.executePendingBindings()

            adapter.addItemDetail(ItemDetail(R.drawable.ic_person_pin, it.html_url))
            it.company?.let { adapter.addItemDetail(ItemDetail(R.drawable.ic_people, it)) }
            it.location?.let { adapter.addItemDetail(ItemDetail(R.drawable.ic_location, it)) }
            it.blog?.let { if(it.isNotEmpty()) { adapter.addItemDetail(ItemDetail(R.drawable.ic_insert_link, it)) } }

            val requestBuilder = GlideApp.with(this)
                    .`as`(PictureDrawable::class.java)
                    .transition(withCrossFade())
                    .listener(SvgSoftwareLayerSetter())

            detail_body_shimmer.startShimmerAnimation()
            requestBuilder
                    .load("${getString(R.string.ghchart)}${it.login}")
                    .listener(object: RequestListener<PictureDrawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<PictureDrawable>?, isFirstResource: Boolean): Boolean {
                            detail_body_shimmer.stopShimmerAnimation()
                            detail_body_preview.gone()
                            return false
                        }

                        override fun onResourceReady(resource: PictureDrawable?, model: Any?, target: Target<PictureDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            detail_body_shimmer.stopShimmerAnimation()
                            detail_body_preview.gone()
                            return false
                        }
                    })
                    .into(detail_body_contributes)
        }
    }

    private fun getLoginFromIntent(): String {
        return intent.getStringExtra("login")
    }

    private fun getAvatarFromIntent(): String {
        return intent.getStringExtra("avatar_url")
    }
}
