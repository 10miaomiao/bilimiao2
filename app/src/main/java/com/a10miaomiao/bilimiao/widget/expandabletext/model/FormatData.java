package com.a10miaomiao.bilimiao.widget.expandabletext.model;



import com.a10miaomiao.bilimiao.widget.expandabletext.app.LinkType;

import java.util.List;

/**
 * 记录可以点击的内容 和 位置
 */
public class FormatData {
    private String formatedContent;
    private List<PositionData> positionDatas;

    public String getFormatedContent() {
        return formatedContent;
    }

    public void setFormatedContent(String formatedContent) {
        this.formatedContent = formatedContent;
    }

    public List<PositionData> getPositionDatas() {
        return positionDatas;
    }

    public void setPositionDatas(List<PositionData> positionDatas) {
        this.positionDatas = positionDatas;
    }

    public static class PositionData {
        private int start;
        private int end;
        private String url;
        private LinkType type;
        //自定义法规则的aim
        private String selfAim;
        //自定义规则的内容
        private String selfContent;

        public String getSelfAim() {
            return selfAim;
        }

        public void setSelfAim(String selfAim) {
            this.selfAim = selfAim;
        }

        public String getSelfContent() {
            return selfContent;
        }

        public void setSelfContent(String selfContent) {
            this.selfContent = selfContent;
        }

        public LinkType getType() {
            return type;
        }

        public void setType(LinkType type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public PositionData(int start, int end, String url, LinkType type) {
            this.start = start;
            this.end = end;
            this.url = url;
            this.type = type;
        }

        public PositionData(int start, int end, String selfAim,String selfContent, LinkType type) {
            this.start = start;
            this.end = end;
            this.selfAim = selfAim;
            this.selfContent = selfContent;
            this.type = type;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
    }
}
