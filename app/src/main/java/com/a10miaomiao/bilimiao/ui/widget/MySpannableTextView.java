package com.a10miaomiao.bilimiao.ui.widget;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.a10miaomiao.bilimiao.R;
import com.a10miaomiao.bilimiao.utils.IntentHandlerUtil;

/**
 * 原作者:geminiyang on 2017/6/4.
 * 邮箱:15118871363@163.com
 * github地址：https://github.com/geminiyang/ShareTransilation
 * <p>
 * 修改:10喵喵 on 2018/6/9
 * 增加av号跳转
 */

public class MySpannableTextView extends AppCompatTextView {

    private OnAvTextClickListener onAvTextClickListener;

    public MySpannableTextView(Context context) {
        super(context, null);
    }

    public MySpannableTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        limitTextViewString(this.getText().toString(), 140, this, new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置监听函数
            }
        });
    }


    public void setLimitText(String text) {
        limitTextViewString(text, 48, this, new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置监听函数
            }
        });
    }


    /**
     * get the last char index for max limit row,if not exceed the limit,return -1
     *
     * @param textView
     * @param content
     * @param width
     * @param maxLine
     * @return
     */
    private int getLastCharIndexForLimitTextView(TextView textView, String content, int width, int maxLine) {
        TextPaint textPaint = textView.getPaint();
        StaticLayout staticLayout = new StaticLayout(content, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        if (staticLayout.getLineCount() > maxLine)
            return staticLayout.getLineStart(maxLine) - 1;//exceed
        else return -1;//not exceed the max line
    }

    /**
     * 限制TextView显示字符字符，并且添加showMore和show more的点击事件
     *
     * @param textString
     * @param textView
     * @param clickListener textView的点击监听器
     */
    private void limitTextViewString(String textString, int maxFirstShowCharCount, final TextView textView, final OnClickListener clickListener) {
        //计算处理花费时间
        final long startTime = System.currentTimeMillis();
        if (textView == null) return;
        int width = textView.getWidth();//在recyclerView和ListView中，由于复用的原因，这个TextView可能以前就画好了，能获得宽度
        if (width == 0) width = 1000;//获取textView的实际宽度，这里可以用各种方式（一般是dp转px写死）填入TextView的宽度
        int lastCharIndex = getLastCharIndexForLimitTextView(textView, textString, width, 10);
//返回-1表示没有达到行数限制
        if (lastCharIndex < 0 && textString.length() <= maxFirstShowCharCount) {
            //如果行数没超过限制
            textView.setText(textString);
            return;
        }
        //如果超出了行数限制
        textView.setMovementMethod(LinkMovementMethod.getInstance());//this will deprive the recyclerView's focus
        if (lastCharIndex > maxFirstShowCharCount || lastCharIndex < 0) {
            lastCharIndex = maxFirstShowCharCount;
        }
        //构造spannableString
        String explicitText = null;
        String explicitTextAll;
        if (textString.charAt(lastCharIndex) == '\n') {//manual enter
            explicitText = textString.substring(0, lastCharIndex);
        } else if (lastCharIndex > 12) {
            //如果最大行数限制的那一行到达12以后则直接显示 显示更多
            explicitText = textString.substring(0, lastCharIndex - 12);
        }
        int sourceLength = explicitText.length();
        String showMore = "...展开";
        explicitText = explicitText + showMore;
        final SpannableString mSpan = new SpannableString(explicitText);


        String dismissMore = " 收起";
        explicitTextAll = textString + dismissMore;
        final SpannableString mSpanALL = new SpannableString(explicitTextAll);

        //搜寻av号，然后高亮+跳转事件
        int m = explicitTextAll.indexOf("av");
        int n;
        while (m != -1) {
            n = m + 2;
            if (n >= explicitTextAll.length()) {
                break;
            }
            while (Character.isDigit(explicitTextAll.charAt(n))) { //判断是否为数字
                n++;
            }
            if (n != m) {
                final int finalM = m;
                final int finalN = n;
                final String finalExplicitTextAll = explicitTextAll;
                mSpanALL.setSpan(new ClickableSpan() {
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(getColorAccent());
                        ds.setAntiAlias(true);
                        ds.setUnderlineText(false);
                    }

                    @Override
                    public void onClick(View widget) {
                        if (onAvTextClickListener != null)
                            onAvTextClickListener.onAvTextClick(MySpannableTextView.this, finalExplicitTextAll.substring(finalM + 2, finalN));
                    }
                }, m, n, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            m = explicitTextAll.indexOf("av", n);
        }

        mSpanALL.setSpan(new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getColorAccent());
//                ds.setColor(textView.getResources().getColor(R.color.colorPrimary));
                ds.setAntiAlias(true);
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View widget) {
                textView.setText(mSpan);
                textView.setOnClickListener(null);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (clickListener != null)
                            textView.setOnClickListener(clickListener);//prevent the double click
                    }
                }, 20);
            }
        }, textString.length(), explicitTextAll.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mSpan.setSpan(new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
//                ds.setColor(textView.getResources().getColor(R.color.colorPrimary));
                ds.setColor(getColorAccent());
                ds.setAntiAlias(true);
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View widget) {//"...show more" click event
                textView.setText(mSpanALL);
                textView.setOnClickListener(null);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (clickListener != null)
                            textView.setOnClickListener(clickListener);//prevent the double click
                    }
                }, 20);
            }
        }, sourceLength, explicitText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置为“显示更多”状态下的TextVie

        textView.setText(mSpan);
    }

    public void setOnAvTextClickListener(OnAvTextClickListener onAvTextClickListener) {
        this.onAvTextClickListener = onAvTextClickListener;
    }

    public interface OnAvTextClickListener {
        void onAvTextClick(MySpannableTextView view, String avId);
    }

    private int getColorAccent() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme()
                .resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        return getResources().getColor(typedValue.resourceId);
    }
}