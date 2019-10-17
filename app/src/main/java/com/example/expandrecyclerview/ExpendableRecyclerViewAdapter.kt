package com.example.expandrecyclerview

import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.expandrecyclerview.databinding.ItemPersonBinding
import com.example.expandrecyclerview.extension.safeClick
import com.example.expandrecyclerview.model.Person
import com.example.expandrecyclerview.utils.Animations

class ExpendableRecyclerViewAdapter(private val items: List<Person>) :
    RecyclerView.Adapter<ExpendableRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemPersonBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_person,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items.get(position))

    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemPersonBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.constraintLayoutMain.safeClick(View.OnClickListener {
                binding.person?.reverseExpanded()
                binding.imageViewExpand.animate().setDuration(200).rotationBy(180.0f)
                if(binding.person!!.isExpanded) {
                    Animations.expandAction(binding.constraintLayoutDescription)
                } else {
                    Animations.collapseAction(binding.constraintLayoutDescription)
                }
                notifyItemChanged(layoutPosition)
            })

            //disable scroll of recycler view when touch on expand layout
            binding.textViewDescription.setOnTouchListener { view: View, motionEvent: MotionEvent ->
                view.parent.requestDisallowInterceptTouchEvent(true)
                false
            }

            binding.textViewDescription.movementMethod = ScrollingMovementMethod()

        }

        fun bind(item: Person) {
            binding.person = item
            binding.executePendingBindings()
        }
    }
}
