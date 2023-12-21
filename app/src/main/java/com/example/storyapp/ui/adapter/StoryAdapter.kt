package com.example.storyapp.ui.adapter

import android.animation.ArgbEvaluator
import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.storyapp.R
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.databinding.ItemListStoryBinding
import com.example.storyapp.helper.DetailData
import com.example.storyapp.ui.activity.DetailStoryActivity

class StoryAdapter: PagingDataAdapter<ListStoryItem, StoryAdapter.MyViewHolder>(DIFF_CALLBACK){

    private lateinit var binding: ItemListStoryBinding
    private lateinit var onItemClickCallback: OnItemClickCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{
        binding = ItemListStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val from = ContextCompat.getColor(binding.root.context, R.color.pink_bg)
        val to = ContextCompat.getColor(binding.root.context, R.color.pink_200)

        binding.tvItemPhoto.setImageDrawable(
            GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    to, from
                ),
            ))
        val gradient = binding.tvItemPhoto.drawable as GradientDrawable
        val evaluator = ArgbEvaluator()
        val animator = TimeAnimator.ofFloat(0.0f, 1.0f)

        animator.duration = 1500
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener {
            val fraction = it.animatedFraction
            val newStart = evaluator.evaluate(fraction, from, to) as Int
            val newEnd = evaluator.evaluate(fraction, to, from) as Int

            gradient.colors = intArrayOf(newStart, newEnd)
        }
        animator.start()
        return MyViewHolder(binding, gradient, animator)
    }

    class MyViewHolder(private val binding: ItemListStoryBinding,private val  gradient: GradientDrawable, private val animator: ValueAnimator) : RecyclerView.ViewHolder(binding.root){
        fun bind(story: ListStoryItem){
            binding.tvItemName.text = story.name
            binding.deskCard.text = story.description

            Glide.with(binding.root)
                .load(story.photoUrl)
                .placeholder(gradient)
                .listener(
                    object : RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            animator.end()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            animator.end()
                            return false
                        }
                    }
                )
                .into(binding.tvItemPhoto)

            binding.cardItemStory.setOnClickListener{
                val intentDetail = Intent(itemView.context, DetailStoryActivity::class.java)
                intentDetail.putExtra(DetailStoryActivity.DETAIL_INTENT_KEY, DetailData(nama = story.name!!, image = story.photoUrl!!, description = story.description!!, time = story.createdAt!!))
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.tvItemPhoto,"imageStory"),
                        Pair(binding.tvItemName, "name"),
                        Pair(binding.deskCard, "description"),
                    )
                itemView.context.startActivity(intentDetail, optionsCompat.toBundle())
            }
        }
    }

    override fun onBindViewHolder (holder: MyViewHolder, position: Int){
        val story = getItem(position)

        if (story != null){
            holder.bind(story)
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ListStoryItem)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    companion object{
        val DIFF_CALLBACK = object :DiffUtil.ItemCallback<ListStoryItem>(){
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}