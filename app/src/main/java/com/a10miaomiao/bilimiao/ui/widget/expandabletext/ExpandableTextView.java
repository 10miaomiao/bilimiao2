package com.a10miaomiao.bilimiao.ui.widget.expandabletext;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.a10miaomiao.bilimiao.R;
import com.a10miaomiao.bilimiao.ui.widget.expandabletext.app.LinkType;
import com.a10miaomiao.bilimiao.ui.widget.expandabletext.app.StatusType;
import com.a10miaomiao.bilimiao.ui.widget.expandabletext.model.ExpandableStatusFix;
import com.a10miaomiao.bilimiao.ui.widget.expandabletext.model.FormatData;
import com.a10miaomiao.bilimiao.ui.widget.expandabletext.model.UUIDUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.support.v4.util.PatternsCompat.AUTOLINK_WEB_URL;

/**
 * @date: on 2018-08-24
 * @author: cretin
 * @email: mxnzp_life@163.com
 * @desc: 一个支持展开 收起 网页链接 和 @用户 点击识别 的TextView
 */
public class ExpandableTextView extends AppCompatTextView {
    private static final int DEF_MAX_LINE = 4;
    public static String TEXT_CONTRACT = "收起";
    public static String TEXT_EXPEND = "展开";
    public static final String Space = " ";
    public static String TEXT_TARGET = "网页链接";
    public static final String IMAGE_TARGET = "图";
    public static final String TARGET = IMAGE_TARGET + TEXT_TARGET;
    public static final String DEFAULT_CONTENT = "                                                                                                                                                                                                                                                                                                                           ";

    private static int retryTime = 0;

    /**
     * http?://([-\\w\\.]+)+(:\\d+)?(/([\\w/_\\.]*(\\?\\S+)?)?)?
     */

//    public static final String regexp = "((http[s]{0,1}|ftp)://[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)|((www.)|[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)";
//    public static final String regexp = "http?://([-\\w\\.]+)+(:\\d+)?(/([\\w/_\\.]*(\\?\\S+)?)?)?";

    public static final String regexp_mention = "@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}";
    //匹配自定义链接的正则表达式
//    public static final String self_regex = "\\[([\\w\\p{InCJKUnifiedIdeographs}-]*)]\\([\\w\\p{InCJKUnifiedIdeographs}-]*\\)";
    public static final String self_regex = "\\[([^\\[]*)\\]\\(([^\\(]*)\\)";

    private TextPaint mPaint;

    boolean linkHit;

    private Context mContext;

    /**
     * 记录当前的model
     */
    private ExpandableStatusFix mModel;

    /**
     * 计算的layout
     */
    private DynamicLayout mDynamicLayout;

    //hide状态下，展示多少行开始省略
    private int mLimitLines;

    private int currentLines;

    private int mWidth;

    private Drawable mLinkDrawable = null;

    /**
     * 链接和@用户的事件点击
     */
    private OnLinkClickListener linkClickListener;

    /**
     * 继续处理Content，添加表情
     */
    private OnNextContentListener nextContentListener;

    /**
     * 点击展开或者收回按钮的时候 是否真的执行操作
     */
    private boolean needRealExpandOrContract = true;

    /**
     * 展开或者收回事件监听
     */
    private OnExpandOrContractClickListener expandOrContractClickListener;

    /**
     * 是否需要收起
     */
    private boolean mNeedContract = true;

    private FormatData mFormatData;

    /**
     * 是否需要展开功能
     */
    private boolean mNeedExpend = true;

    /**
     * 是否需要转换url成网页链接四个字
     */
    private boolean mNeedConvertUrl = true;

    /**
     * 是否需要@用户的功能
     */
    private boolean mNeedMention = true;

    /**
     * 是否需要对链接进行处理
     */
    private boolean mNeedLink = true;

    /**
     * 是否需要对自定义情况进行处理
     */
    private boolean mNeedSelf = false;

    /**
     * 是否需要永远将展开或收回显示在最右边
     */
    private boolean mNeedAlwaysShowRight = false;

    /**
     * 是否需要动画 默认开启动画
     */
    private boolean mNeedAnimation = true;

    private int mLineCount;

    private CharSequence mContent;

    /**
     * 展开文字的颜色
     */
    private int mExpandTextColor;
    /**
     * 展开文字的颜色
     */
    private int mMentionTextColor;

    /**
     * 链接的字体颜色
     */
    private int mLinkTextColor;

    /**
     * 自定义规则的字体颜色
     */
    private int mSelfTextColor;

    /**
     * 收起的文字的颜色
     */
    private int mContractTextColor;

    /**
     * 展开的文案
     */
    private String mExpandString;
    /**
     * 收起的文案
     */
    private String mContractString;

    /**
     * 在收回和展开前面添加的内容
     */
    private String mEndExpandContent;

    /**
     * 在收回和展开前面添加的内容的字体颜色
     */
    private int mEndExpandTextColor;

    //是否AttachedToWindow
    private boolean isAttached;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ExpandableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        setMovementMethod(LocalLinkMovementMethod.getInstance());
        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                if (isAttached == false)
                    doSetContent();
                isAttached = true;
            }

            @Override
            public void onViewDetachedFromWindow(View v) {

            }
        });
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        //适配英文版
        TEXT_CONTRACT = context.getString(R.string.social_contract);
        TEXT_EXPEND = context.getString(R.string.social_expend);
        TEXT_TARGET = context.getString(R.string.social_text_target);

        if (attrs != null) {
            TypedArray a =
                    getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView,
                            defStyleAttr, 0);

            mLimitLines = a.getInt(R.styleable.ExpandableTextView_ep_max_line, DEF_MAX_LINE);
            mNeedExpend = a.getBoolean(R.styleable.ExpandableTextView_ep_need_expand, true);
            mNeedContract = a.getBoolean(R.styleable.ExpandableTextView_ep_need_contract, false);
            mNeedAnimation = a.getBoolean(R.styleable.ExpandableTextView_ep_need_animation, true);
            mNeedSelf = a.getBoolean(R.styleable.ExpandableTextView_ep_need_self, false);
            mNeedMention = a.getBoolean(R.styleable.ExpandableTextView_ep_need_mention, true);
            mNeedLink = a.getBoolean(R.styleable.ExpandableTextView_ep_need_link, true);
            mNeedAlwaysShowRight = a.getBoolean(R.styleable.ExpandableTextView_ep_need_always_showright, false);
            mNeedConvertUrl = a.getBoolean(R.styleable.ExpandableTextView_ep_need_convert_url, true);
            mContractString = a.getString(R.styleable.ExpandableTextView_ep_contract_text);
            mExpandString = a.getString(R.styleable.ExpandableTextView_ep_expand_text);
            if (TextUtils.isEmpty(mExpandString)) {
                mExpandString = TEXT_EXPEND;
            }
            if (TextUtils.isEmpty(mContractString)) {
                mContractString = TEXT_CONTRACT;
            }
            mExpandTextColor = a.getColor(R.styleable.ExpandableTextView_ep_expand_color,
                    Color.parseColor("#999999"));
            mEndExpandTextColor = a.getColor(R.styleable.ExpandableTextView_ep_expand_color,
                    Color.parseColor("#999999"));
            mContractTextColor = a.getColor(R.styleable.ExpandableTextView_ep_contract_color,
                    Color.parseColor("#999999"));
            mLinkTextColor = a.getColor(R.styleable.ExpandableTextView_ep_link_color,
                    Color.parseColor("#FF6200"));
            mSelfTextColor = a.getColor(R.styleable.ExpandableTextView_ep_self_color,
                    Color.parseColor("#FF6200"));
            mMentionTextColor = a.getColor(R.styleable.ExpandableTextView_ep_mention_color,
                    Color.parseColor("#FF6200"));
            int resId = a.getResourceId(R.styleable.ExpandableTextView_ep_link_res, R.drawable.ic_baseline_link_24);
            mLinkDrawable = getResources().getDrawable(resId);
            currentLines = mLimitLines;
            a.recycle();
        } else {
            mLinkDrawable = context.getResources().getDrawable(R.drawable.ic_baseline_link_24);
        }

        mContext = context;

        mPaint = getPaint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        //初始化link的图片
//        mLinkDrawable.setBounds(0, 0, 30, 30); //必须设置图片大小，否则不显示
    }

    private SpannableStringBuilder setRealContent(CharSequence content) {
        //处理给定的数据
        mFormatData = formatData(content);
        //用来计算内容的大小
        mDynamicLayout =
                new DynamicLayout(mFormatData.getFormatedContent(), mPaint, mWidth, Layout.Alignment.ALIGN_NORMAL, 1.2f, 0.0f,
                        true);
        //获取行数
        mLineCount = mDynamicLayout.getLineCount();

        if (onGetLineCountListener != null) {
            onGetLineCountListener.onGetLineCount(mLineCount, mLineCount > mLimitLines);
        }

        if (!mNeedExpend || mLineCount <= mLimitLines) {
            //不需要展开功能 直接处理链接模块
            return dealLink(mFormatData, false);
        } else {
            return dealLink(mFormatData, true);
        }
    }

    /**
     * 设置追加的内容
     *
     * @param endExpendContent
     */
    public void setEndExpendContent(String endExpendContent) {
        this.mEndExpandContent = endExpendContent;
    }

    /**
     * 设置内容
     *
     * @param content
     */
    public void setContent(final String content) {
        mContent = content;
        if (isAttached)
            doSetContent();
    }

    /**
     * 实际设置内容的
     */
    private void doSetContent() {
        if (mContent == null) {
            return;
        }
        currentLines = mLimitLines;

        if (mWidth <= 0) {
            if (getWidth() > 0)
                mWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        }

        if (mWidth <= 0) {
            if (retryTime > 10) {
                setText(DEFAULT_CONTENT);
            }
            this.post(new Runnable() {
                @Override
                public void run() {
                    retryTime++;
                    setContent(mContent.toString());
                }
            });
        } else {
            setRealContent(mContent.toString());
        }
    }

    /**
     * 设置最后的收起文案
     *
     * @return
     */
    private String getExpandEndContent() {
        if (TextUtils.isEmpty(mEndExpandContent)) {
            return String.format(Locale.getDefault(), "  %s",
                    mContractString);
        } else {
            return String.format(Locale.getDefault(), "  %s  %s",
                    mEndExpandContent, mContractString);
        }
    }

    /**
     * 设置展开的文案
     *
     * @return
     */
    private String getHideEndContent() {
        if (TextUtils.isEmpty(mEndExpandContent)) {
            return String.format(Locale.getDefault(), mNeedAlwaysShowRight ? "  %s" : "...  %s",
                    mExpandString);
        } else {
            return String.format(Locale.getDefault(), mNeedAlwaysShowRight ? "  %s  %s" : "...  %s  %s",
                    mEndExpandContent, mExpandString);
        }
    }

    /**
     * 处理文字中的链接问题
     *
     * @param formatData
     * @param ignoreMore
     */
    private SpannableStringBuilder dealLink(FormatData formatData, boolean ignoreMore) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        //获取存储的状态
        if (mModel != null && mModel.getStatus() != null) {
            boolean isHide = false;
            if (mModel.getStatus() != null) {
                if (mModel.getStatus().equals(StatusType.STATUS_CONTRACT)) {
                    //收起
                    isHide = true;
                } else {
                    //展开
                    isHide = false;
                }
            }
            if (isHide) {
                currentLines = mLimitLines + ((mLineCount - mLimitLines));
            } else {
                if (mNeedContract)
                    currentLines = mLimitLines;
            }
        }
        //处理折叠操作
        if (ignoreMore) {
            if (currentLines < mLineCount) {
                int index = currentLines - 1;
                int endPosition = mDynamicLayout.getLineEnd(index);
                int startPosition = mDynamicLayout.getLineStart(index);
                float lineWidth = mDynamicLayout.getLineWidth(index);

                String endString = getHideEndContent();

                //计算原内容被截取的位置下标
                int fitPosition =
                        getFitPosition(endString, endPosition, startPosition, lineWidth, mPaint.measureText(endString), 0);
                String substring = formatData.getFormatedContent().substring(0, fitPosition);
                if (substring.endsWith("\n")) {
                    substring = substring.substring(0, substring.length() - "\n".length());
                }
                ssb.append(substring);

                if (mNeedAlwaysShowRight) {
                    //计算一下最后一行有没有充满
                    float lastLineWidth = 0;
                    for (int i = 0; i < index; i++) {
                        lastLineWidth += mDynamicLayout.getLineWidth(i);
                    }
                    lastLineWidth = lastLineWidth / (index);
                    float emptyWidth = lastLineWidth - lineWidth - mPaint.measureText(endString);
                    if (emptyWidth > 0) {
                        float measureText = mPaint.measureText(Space);
                        int count = 0;
                        while (measureText * count < emptyWidth) {
                            count++;
                        }
                        count = count - 1;
                        for (int i = 0; i < count; i++) {
                            ssb.append(Space);
                        }
                    }
                }

                //在被截断的文字后面添加 展开 文字
                ssb.append(endString);

                int expendLength = TextUtils.isEmpty(mEndExpandContent) ? 0 : 2 + mEndExpandContent.length();
                ssb.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        if (needRealExpandOrContract) {
                            if (mModel != null) {
                                mModel.setStatus(StatusType.STATUS_CONTRACT);
                                action(mModel.getStatus());
                            } else {
                                action();
                            }
                        }
                        if (expandOrContractClickListener != null) {
                            expandOrContractClickListener.onClick(StatusType.STATUS_EXPAND);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(mExpandTextColor);
                        ds.setUnderlineText(false);
                    }
                }, ssb.length() - mExpandString.length() - expendLength, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            } else {
                ssb.append(formatData.getFormatedContent());
                if (mNeedContract) {
                    String endString = getExpandEndContent();

                    if (mNeedAlwaysShowRight) {
                        //计算一下最后一行有没有充满
                        int index = mDynamicLayout.getLineCount() - 1;
                        float lineWidth = mDynamicLayout.getLineWidth(index);
                        float lastLineWidth = 0;
                        for (int i = 0; i < index; i++) {
                            lastLineWidth += mDynamicLayout.getLineWidth(i);
                        }
                        lastLineWidth = lastLineWidth / (index);
                        float emptyWidth = lastLineWidth - lineWidth - mPaint.measureText(endString);
                        if (emptyWidth > 0) {
                            float measureText = mPaint.measureText(Space);
                            int count = 0;
                            while (measureText * count < emptyWidth) {
                                count++;
                            }
                            count = count - 1;
                            for (int i = 0; i < count; i++) {
                                ssb.append(Space);
                            }
                        }
                    }

                    ssb.append(endString);

                    int expendLength = TextUtils.isEmpty(mEndExpandContent) ? 0 : 2 + mEndExpandContent.length();
                    ssb.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            if (mModel != null) {
                                mModel.setStatus(StatusType.STATUS_EXPAND);
                                action(mModel.getStatus());
                            } else {
                                action();
                            }
                            if (expandOrContractClickListener != null) {
                                expandOrContractClickListener.onClick(StatusType.STATUS_CONTRACT);
                            }
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(mContractTextColor);
                            ds.setUnderlineText(false);
                        }
                    }, ssb.length() - mContractString.length() - expendLength, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                } else {
                    if (!TextUtils.isEmpty(mEndExpandContent)) {
                        ssb.append(mEndExpandContent);
                        ssb.setSpan(new ForegroundColorSpan(mEndExpandTextColor), ssb.length() - mEndExpandContent.length(), ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        } else {
            ssb.append(formatData.getFormatedContent());
            if (!TextUtils.isEmpty(mEndExpandContent)) {
                ssb.append(mEndExpandContent);
                ssb.setSpan(new ForegroundColorSpan(mEndExpandTextColor), ssb.length() - mEndExpandContent.length(), ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        //处理链接或者@用户
        List<FormatData.PositionData> positionDatas = formatData.getPositionDatas();
        HH:
        for (FormatData.PositionData data : positionDatas) {
            if (ssb.length() >= data.getEnd()) {
                if (data.getType().equals(LinkType.LINK_TYPE)) {
                    if (mNeedExpend && ignoreMore) {
                        int fitPosition = ssb.length() - getHideEndContent().length();
                        if (data.getStart() < fitPosition) {
                            SelfImageSpan imageSpan = new SelfImageSpan(mLinkDrawable, ImageSpan.ALIGN_BASELINE);
                            //设置链接图标
                            ssb.setSpan(imageSpan, data.getStart(), data.getStart() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            //设置链接文字样式
                            int endPosition = data.getEnd();
                            if (currentLines < mLineCount) {
                                if (fitPosition > data.getStart() + 1 && fitPosition < data.getEnd()) {
                                    endPosition = fitPosition;
                                }
                            }
                            if (data.getStart() + 1 < fitPosition) {
                                addUrl(ssb, data, endPosition);
                            }
                        }
                    } else {
                        SelfImageSpan imageSpan = new SelfImageSpan(mLinkDrawable, ImageSpan.ALIGN_BASELINE);
                        //设置链接图标
                        ssb.setSpan(imageSpan, data.getStart(), data.getStart() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        addUrl(ssb, data, data.getEnd());
                    }
                } else if (data.getType().equals(LinkType.MENTION_TYPE)) {
                    //如果需要展开
                    if (mNeedExpend && ignoreMore) {
                        int fitPosition = ssb.length() - getHideEndContent().length();
                        if (data.getStart() < fitPosition) {
                            int endPosition = data.getEnd();
                            if (currentLines < mLineCount) {
                                if (fitPosition < data.getEnd()) {
                                    endPosition = fitPosition;
                                }
                            }
                            addMention(ssb, data, endPosition);
                        }
                    } else {
                        addMention(ssb, data, data.getEnd());
                    }
                } else if (data.getType().equals(LinkType.SELF)) {
                    //自定义
                    //如果需要展开
                    if (mNeedExpend && ignoreMore) {
                        int fitPosition = ssb.length() - getHideEndContent().length();
                        if (data.getStart() < fitPosition) {
                            int endPosition = data.getEnd();
                            if (currentLines < mLineCount) {
                                if (fitPosition < data.getEnd()) {
                                    endPosition = fitPosition;
                                }
                            }
                            addSelf(ssb, data, endPosition);
                        }
                    } else {
                        addSelf(ssb, data, data.getEnd());
                    }
                }
            }
        }
        if (nextContentListener != null) {
            nextContentListener.onNextContent(ssb);
        }
        //清除链接点击时背景效果
        setHighlightColor(Color.TRANSPARENT);
        //将内容设置到控件中
        setText(ssb);
        return ssb;
    }

    /**
     * 获取需要插入的空格
     *
     * @param emptyWidth
     * @param endStringWidth
     * @return
     */
    private int getFitSpaceCount(float emptyWidth, float endStringWidth) {
        float measureText = mPaint.measureText(Space);
        int count = 0;
        while (endStringWidth + measureText * count < emptyWidth) {
            count++;
        }
        return --count;
    }


    /**
     * 添加自定义规则
     *
     * @param ssb
     * @param data
     * @param endPosition
     */
    private void addSelf(SpannableStringBuilder ssb, final FormatData.PositionData data, int endPosition) {
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (linkClickListener != null)
                    linkClickListener.onLinkClickListener(LinkType.SELF, data.getSelfAim(), data.getSelfContent());
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(mSelfTextColor);
                ds.setUnderlineText(false);
            }
        }, data.getStart(), endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }


    /**
     * 添加@用户的Span
     *
     * @param ssb
     * @param data
     * @param endPosition
     */
    private void addMention(SpannableStringBuilder ssb, final FormatData.PositionData data, int endPosition) {
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (linkClickListener != null)
                    linkClickListener.onLinkClickListener(LinkType.MENTION_TYPE, data.getUrl(), null);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(mMentionTextColor);
                ds.setUnderlineText(false);
            }
        }, data.getStart(), endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    /**
     * 添加链接的span
     *
     * @param ssb
     * @param data
     * @param endPosition
     */
    private void addUrl(SpannableStringBuilder ssb, final FormatData.PositionData data, int endPosition) {
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (linkClickListener != null) {
                    linkClickListener.onLinkClickListener(LinkType.LINK_TYPE, data.getUrl(), null);
                } else {
                    //如果没有设置监听 则调用默认的打开浏览器显示连接
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri url = Uri.parse(data.getUrl());
                    intent.setData(url);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(mLinkTextColor);
                ds.setUnderlineText(false);
            }
        }, data.getStart() + 1, endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    /**
     * 设置当前的状态
     *
     * @param type
     */
    public void setCurrStatus(StatusType type) {
        action(type);
    }

    private void action() {
        action(null);
    }

    /**
     * 执行展开和收回的动作
     */
    private void action(StatusType type) {
        boolean isHide = currentLines < mLineCount;
        if (type != null) {
            mNeedAnimation = false;
        }
        if (mNeedAnimation) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            final boolean finalIsHide = isHide;
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) animation.getAnimatedValue();
                    if (finalIsHide) {
                        currentLines = mLimitLines + (int) ((mLineCount - mLimitLines) * value);
                    } else {
                        if (mNeedContract)
                            currentLines = mLimitLines + (int) ((mLineCount - mLimitLines) * (1 - value));
                    }
                    setText(setRealContent(mContent));
                }
            });
            valueAnimator.setDuration(100);
            valueAnimator.start();
        } else {
            if (isHide) {
                currentLines = mLimitLines + ((mLineCount - mLimitLines));
            } else {
                if (mNeedContract)
                    currentLines = mLimitLines;
            }
            setText(setRealContent(mContent));
        }
    }

    /**
     * 计算原内容被裁剪的长度
     *
     * @param endString
     * @param endPosition   指定行最后文字的位置
     * @param startPosition 指定行文字开始的位置
     * @param lineWidth     指定行文字的宽度
     * @param endStringWith 最后添加的文字的宽度
     * @param offset        偏移量
     * @return
     */
    private int getFitPosition(String endString, int endPosition, int startPosition, float lineWidth,
                               float endStringWith, float offset) {
        //最后一行需要添加的文字的字数
        int position = (int) ((lineWidth - (endStringWith + offset)) * (endPosition - startPosition)
                / lineWidth);

        if (position <= endString.length()) return endPosition;

        //计算最后一行需要显示的正文的长度
        float measureText = mPaint.measureText(
                (mFormatData.getFormatedContent().substring(startPosition, startPosition + position)));

        //如果最后一行需要显示的正文的长度比最后一行的长减去“展开”文字的长度要短就可以了  否则加个空格继续算
        if (measureText <= lineWidth - endStringWith) {
            return startPosition + position;
        } else {
            return getFitPosition(endString, endPosition, startPosition, lineWidth, endStringWith, offset + mPaint.measureText(Space));
        }
    }

    /**
     * 对传入的数据进行正则匹配并处理
     *
     * @param content
     * @return
     */
    @SuppressLint("RestrictedApi")
    private FormatData formatData(CharSequence content) {
        FormatData formatData = new FormatData();
        List<FormatData.PositionData> datas = new ArrayList<>();
        //对链接进行正则匹配
//        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        Pattern pattern = Pattern.compile(self_regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        StringBuffer newResult = new StringBuffer();
        int start = 0;
        int end = 0;
        int temp = 0;
        Map<String, String> convert = new HashMap<>();
        //对自定义的进行正则匹配
        if (mNeedSelf) {
            List<FormatData.PositionData> datasMention = new ArrayList<>();
            while (matcher.find()) {
                start = matcher.start();
                end = matcher.end();
                newResult.append(content.toString().substring(temp, start));
                //将匹配到的内容进行统计处理
                String result = matcher.group();
                if (!TextUtils.isEmpty(result)) {
                    //解析数据
                    String aimSrt = result.substring(result.indexOf("[") + 1, result.indexOf("]"));
                    String contentSrt = result.substring(result.indexOf("(") + 1, result.indexOf(")"));
                    String key = UUIDUtils.getUuid(aimSrt.length());
                    datasMention.add(new FormatData.PositionData(newResult.length() + 1, newResult.length() + 2 + aimSrt.length(), aimSrt, contentSrt, LinkType.SELF));
                    convert.put(key, aimSrt);
                    newResult.append(" " + key + " ");
                    temp = end;
                }
            }
            datas.addAll(datasMention);
        }
        //重置状态
        newResult.append(content.toString().substring(end, content.toString().length()));
        content = newResult.toString();
        newResult = new StringBuffer();
        start = 0;
        end = 0;
        temp = 0;

        if (mNeedLink) {
            pattern = AUTOLINK_WEB_URL;
            matcher = pattern.matcher(content);
            while (matcher.find()) {
                start = matcher.start();
                end = matcher.end();
                newResult.append(content.toString().substring(temp, start));
                if (mNeedConvertUrl) {
                    //将匹配到的内容进行统计处理
                    datas.add(new FormatData.PositionData(newResult.length() + 1, newResult.length() + 2 + TARGET.length(), matcher.group(), LinkType.LINK_TYPE));
                    newResult.append(" " + TARGET + " ");
                } else {
                    String result = matcher.group();
                    String key = UUIDUtils.getUuid(result.length());
                    datas.add(new FormatData.PositionData(newResult.length(), newResult.length() + 2 + key.length(), result, LinkType.LINK_TYPE));
                    convert.put(key, result);
                    newResult.append(" " + key + " ");
                }
                temp = end;
            }
        }
        newResult.append(content.toString().substring(end, content.toString().length()));
        //对@用户 进行正则匹配
        if (mNeedMention) {
            pattern = Pattern.compile(regexp_mention, Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(newResult.toString());
            List<FormatData.PositionData> datasMention = new ArrayList<>();
            while (matcher.find()) {
                //将匹配到的内容进行统计处理
                datasMention.add(new FormatData.PositionData(matcher.start(), matcher.end(), matcher.group(), LinkType.MENTION_TYPE));
            }
            datas.addAll(0, datasMention);
        }
        if (!convert.isEmpty()) {
            String resultData = newResult.toString();
            for (Map.Entry<String, String> entry : convert.entrySet()) {
                resultData = resultData.replaceAll(entry.getKey(), entry.getValue());
            }
            newResult = new StringBuffer(resultData);
        }
        formatData.setFormatedContent(newResult.toString());
        formatData.setPositionDatas(datas);
        return formatData;
    }

    /**
     * 自定义ImageSpan 让Image 在行内居中显示
     */
    class SelfImageSpan extends ImageSpan {
        private Drawable drawable;

        public SelfImageSpan(Drawable d, int verticalAlignment) {
            super(d, verticalAlignment);
            this.drawable = d;
        }

        @Override
        public Drawable getDrawable() {
            return drawable;
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text,
                         int start, int end, float x,
                         int top, int y, int bottom, @NonNull Paint paint) {
            // image to draw
            Drawable b = getDrawable();
            // font metrics of text to be replaced
            Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            int transY = (y + fm.descent + y + fm.ascent) / 2
                    - b.getBounds().bottom / 2;
            canvas.save();
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }
    }

    /**
     * 绑定状态
     *
     * @param model
     */
    public void bind(ExpandableStatusFix model) {
        mModel = model;
    }

    public static class LocalLinkMovementMethod extends LinkMovementMethod {
        static LocalLinkMovementMethod sInstance;


        public static LocalLinkMovementMethod getInstance() {
            if (sInstance == null)
                sInstance = new LocalLinkMovementMethod();

            return sInstance;
        }

        @Override
        public boolean onTouchEvent(TextView widget,
                                    Spannable buffer, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(
                        off, off, ClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }

                    if (widget instanceof ExpandableTextView) {
                        ((ExpandableTextView) widget).linkHit = true;
                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                    Touch.onTouchEvent(widget, buffer, event);
                    return false;
                }
            }
            return Touch.onTouchEvent(widget, buffer, event);
        }
    }

    boolean dontConsumeNonUrlClicks = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        linkHit = false;
        boolean res = super.onTouchEvent(event);

        if (dontConsumeNonUrlClicks)
            return linkHit;

        //防止选择复制的状态不消失
        if (action == MotionEvent.ACTION_UP) {
            this.setTextIsSelectable(false);
        }

        return res;
    }

    public interface OnLinkClickListener {
        void onLinkClickListener(LinkType type, String content, String selfContent);
    }

    public interface OnGetLineCountListener {
        /**
         * lineCount 预估可能占有的行数
         * canExpand 是否达到可以展开的条件
         */
        void onGetLineCount(int lineCount, boolean canExpand);
    }

    private OnGetLineCountListener onGetLineCountListener;

    public OnGetLineCountListener getOnGetLineCountListener() {
        return onGetLineCountListener;
    }

    public void setOnGetLineCountListener(OnGetLineCountListener onGetLineCountListener) {
        this.onGetLineCountListener = onGetLineCountListener;
    }

    public interface OnExpandOrContractClickListener {
        void onClick(StatusType type);
    }

    public OnLinkClickListener getLinkClickListener() {
        return linkClickListener;
    }

    public void setLinkClickListener(OnLinkClickListener linkClickListener) {
        this.linkClickListener = linkClickListener;
    }

    public interface OnNextContentListener {
        void onNextContent(SpannableStringBuilder ssb);
    }

    public void setNextContentListener(OnNextContentListener nextContentListener) {
        this.nextContentListener = nextContentListener;
    }

    public boolean ismNeedMention() {
        return mNeedMention;
    }

    public void setNeedMention(boolean mNeedMention) {
        this.mNeedMention = mNeedMention;
    }

    public Drawable getLinkDrawable() {
        return mLinkDrawable;
    }

    public void setLinkDrawable(Drawable mLinkDrawable) {
        this.mLinkDrawable = mLinkDrawable;
    }

    public boolean isNeedContract() {
        return mNeedContract;
    }

    public void setNeedContract(boolean mNeedContract) {
        this.mNeedContract = mNeedContract;
    }

    public boolean isNeedExpend() {
        return mNeedExpend;
    }

    public void setNeedExpend(boolean mNeedExpend) {
        this.mNeedExpend = mNeedExpend;
    }

    public boolean isNeedAnimation() {
        return mNeedAnimation;
    }

    public void setNeedAnimation(boolean mNeedAnimation) {
        this.mNeedAnimation = mNeedAnimation;
    }

    public int getExpandableLineCount() {
        return mLineCount;
    }

    public void setExpandableLineCount(int mLineCount) {
        this.mLineCount = mLineCount;
    }

    public int getExpandTextColor() {
        return mExpandTextColor;
    }

    public void setExpandTextColor(int mExpandTextColor) {
        this.mExpandTextColor = mExpandTextColor;
    }

    public int getExpandableLinkTextColor() {
        return mLinkTextColor;
    }

    public void setExpandableLinkTextColor(int mLinkTextColor) {
        this.mLinkTextColor = mLinkTextColor;
    }

    public int getContractTextColor() {
        return mContractTextColor;
    }

    public void setContractTextColor(int mContractTextColor) {
        this.mContractTextColor = mContractTextColor;
    }

    public String getExpandString() {
        return mExpandString;
    }

    public void setExpandString(String mExpandString) {
        this.mExpandString = mExpandString;
    }

    public String getContractString() {
        return mContractString;
    }

    public void setContractString(String mContractString) {
        this.mContractString = mContractString;
    }

    public int getEndExpandTextColor() {
        return mEndExpandTextColor;
    }

    public void setEndExpandTextColor(int mEndExpandTextColor) {
        this.mEndExpandTextColor = mEndExpandTextColor;
    }

    public boolean isNeedLink() {
        return mNeedLink;
    }

    public void setNeedLink(boolean mNeedLink) {
        this.mNeedLink = mNeedLink;
    }

    public int getSelfTextColor() {
        return mSelfTextColor;
    }

    public void setSelfTextColor(int mSelfTextColor) {
        this.mSelfTextColor = mSelfTextColor;
    }

    public boolean isNeedSelf() {
        return mNeedSelf;
    }

    public void setNeedSelf(boolean mNeedSelf) {
        this.mNeedSelf = mNeedSelf;
    }

    public boolean isNeedAlwaysShowRight() {
        return mNeedAlwaysShowRight;
    }

    public void setNeedAlwaysShowRight(boolean mNeedAlwaysShowRight) {
        this.mNeedAlwaysShowRight = mNeedAlwaysShowRight;
    }

    public OnExpandOrContractClickListener getExpandOrContractClickListener() {
        return expandOrContractClickListener;
    }

    public void setExpandOrContractClickListener(OnExpandOrContractClickListener expandOrContractClickListener) {
        this.expandOrContractClickListener = expandOrContractClickListener;
    }

    public void setExpandOrContractClickListener(OnExpandOrContractClickListener expandOrContractClickListener, boolean needRealExpandOrContract) {
        this.expandOrContractClickListener = expandOrContractClickListener;
        this.needRealExpandOrContract = needRealExpandOrContract;
    }
}
