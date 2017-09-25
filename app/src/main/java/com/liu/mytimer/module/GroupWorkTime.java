package com.liu.mytimer.module;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by kunming.liu on 2017/9/22.
 */
@Entity(indexes = {
        @Index(value = "id DESC", unique = true)
})
public class GroupWorkTime {
    @Id
    private Long id;
    private String date;
    private int childCount;
    @Transient
    private boolean isExpand;// @Transient 不會創建在table裡面
    private String startTime;
    private String endTime;
    private String workContent;
    private String totalTime;
    private int type = 0;

    @Generated(hash = 573529413)
    public GroupWorkTime(Long id, String date, int childCount, String startTime,
            String endTime, String workContent, String totalTime, int type) {
        this.id = id;
        this.date = date;
        this.childCount = childCount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workContent = workContent;
        this.totalTime = totalTime;
        this.type = type;
    }

    @Generated(hash = 456902085)
    public GroupWorkTime() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getChildCount() {
        return this.childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getWorkContent() {
        return workContent;
    }

    public void setWorkContent(String workContent) {
        this.workContent = workContent;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
