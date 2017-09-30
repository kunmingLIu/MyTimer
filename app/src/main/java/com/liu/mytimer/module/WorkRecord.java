package com.liu.mytimer.module;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kunming.liu on 2017/9/27.
 */
@Entity(indexes = {
        @Index(value = "id DESC", unique = true)
})
public class WorkRecord {
    @Id
    private Long id;
    private String date; //此筆紀錄的日期 xxxx(年)/xx(月)/xx(日)
    private int type = 0; // 0: group 1 : child
    @Transient // @Transient 不會創建在table裡面
    private boolean isExpand = false;// true : 已經展開 false : 尚未展開
    private String startTime;//開始的時間 xx(點):xx(分):xx(秒) 15:20:22
    private String endTime;//結束的時間 xx(點):xx(分):xx(秒)
    private String workContent;//工作內容
    private long totalWorkTime;
    @Transient
    private List<WorkRecord> childList;





    @Generated(hash = 123399184)
    public WorkRecord(Long id, String date, int type, String startTime,
            String endTime, String workContent, long totalWorkTime) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workContent = workContent;
        this.totalWorkTime = totalWorkTime;
    }

    @Generated(hash = 467260428)
    public WorkRecord() {
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

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return this.endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getWorkContent() {
        return this.workContent;
    }

    public void setWorkContent(String workContent) {
        this.workContent = workContent;
    }

    public long getTotalWorkTime() {
        return this.totalWorkTime;
    }

    public void setTotalWorkTime(long totalWorkTime) {
        this.totalWorkTime = totalWorkTime;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public List<WorkRecord> getChildList() {
        return childList;
    }

    public void setChildList(List<WorkRecord> childList) {
        this.childList = childList;
    }

    public int getChildCount(){
        if(childList == null){
            return 0;
        }else{
            return childList.size();
        }
    }
}
