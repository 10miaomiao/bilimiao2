package com.a10miaomiao.bilimiao.widget.rangedate.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.widget.rangedate.listener.OnClickDayListener
import com.a10miaomiao.bilimiao.widget.rangedate.model.DayInfo

class DayAdapter(
    val data: List<DayInfo>,
    private val listener: OnClickDayListener?,
    var isHourMode: Boolean = false //是否是钟点房

) : RecyclerView.Adapter<DayAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val view = when (viewType) {

            DayInfo.TYPE_DAY_TITLE       -> {
                LayoutInflater.from(viewGroup.context).inflate(R.layout.rangedate_item_select_day_title, viewGroup, false)
            }

            DayInfo.TYPE_DAY_NORMAL      -> {
                LayoutInflater.from(viewGroup.context).inflate(R.layout.rangedate_item_select_day, viewGroup, false)
            }

            DayInfo.TYPE_DAY_PLACEHOLDER -> {
                LayoutInflater.from(viewGroup.context).inflate(R.layout.rangedate_item_select_placeholder, viewGroup, false)
            }

            else                         -> {
                LayoutInflater.from(viewGroup.context).inflate(R.layout.rangedate_item_select_placeholder, viewGroup, false)
            }
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = data[position]

        when (getItemViewType(position)) {
            DayInfo.TYPE_DAY_TITLE       -> {
                holder.tvTitle?.text = item.groupName
            }

            DayInfo.TYPE_DAY_PLACEHOLDER -> {
                //占位符
            }

            DayInfo.TYPE_DAY_NORMAL      -> {

                holder.tvJr?.text = item.jr
                if (item.isToday) {
                    holder.tvDay?.text = "今天"

                } else {
                    if (item.day <= 0) {
                        holder.tvDay?.text = ""

                    } else {
                        holder.tvDay?.text = item.day.toString()
                    }
                }

                if (item.isEnableDay) {
                    if (item.isSelect && !item.isMiddle && !item.isSelectEnd) {
                        //入店时间
                        holder.itemView.setBackgroundResource(R.drawable.rangedate_item_day_select)

                        holder.tvDay?.setTextColor(Color.WHITE)
                        holder.tvJr?.setTextColor(Color.WHITE)
                        holder.tvState?.setTextColor(Color.WHITE)

                        holder.tvState?.text = if (isHourMode) "入/离" else "入住"

                    } else if (item.isSelect && !item.isMiddle && item.isSelectEnd) {
                        //离店时间
                        holder.itemView.setBackgroundResource(R.drawable.rangedate_item_day_select)

                        holder.tvDay?.setTextColor(Color.WHITE)
                        holder.tvJr?.setTextColor(Color.WHITE)
                        holder.tvState?.setTextColor(Color.WHITE)

                        holder.tvState?.text = "离店"

                    } else if (item.isSelect && item.isMiddle && !item.isSelectEnd) {
                        //中间选中
                        holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                        holder.viewDay?.setBackgroundResource(R.drawable.rangedate_item_day_select_middle)

                        holder.tvDay?.setTextColor(Color.WHITE)
                        holder.tvJr?.setTextColor(Color.WHITE)
                        holder.tvState?.setTextColor(Color.WHITE)

                        holder.tvState?.text = ""

                    } else {
                        //未选中
                        holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                        holder.viewDay?.setBackgroundColor(Color.TRANSPARENT)

                        if (item.isWeekend) {
                            //周六周末
                            holder.tvDay?.setTextColor(Color.parseColor("#F8C300"))
                            holder.tvJr?.setTextColor(Color.parseColor("#F8C300"))
                            holder.tvState?.setTextColor(Color.parseColor("#F8C300"))

                        } else {
                            holder.tvDay?.setTextColor(Color.parseColor("#333333"))
                            holder.tvJr?.setTextColor(Color.parseColor("#333333"))
                            holder.tvState?.setTextColor(Color.parseColor("#333333"))
                        }

                        holder.tvState?.text = ""
                    }

                } else {
                    //设置不可点击
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                    holder.viewDay?.setBackgroundColor(Color.TRANSPARENT)

                    holder.tvDay?.setTextColor(Color.parseColor("#ADADAD"))
                    holder.tvJr?.setTextColor(Color.parseColor("#ADADAD"))
                    holder.tvState?.setTextColor(Color.parseColor("#ADADAD"))

                    holder.tvState?.text = ""
                }

                holder.itemView.setOnClickListener {
                    if (item.isEnableDay) {
                        listener?.onClickDay(holder.itemView, item)
                    }
                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvTitle: TextView? = null
        var viewDay: View? = null
        var tvJr: TextView? = null
        var tvDay: TextView? = null
        var tvState: TextView? = null

        init {
            tvTitle = view.findViewById(R.id.tv_title)
            viewDay = view.findViewById(R.id.view_content)
            tvJr = view.findViewById(R.id.tv_jr)
            tvDay = view.findViewById(R.id.tv_day)
            tvState = view.findViewById(R.id.tv_state)
        }
    }

}