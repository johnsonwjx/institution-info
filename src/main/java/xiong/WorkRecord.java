package xiong;

/**
 * Created by johnson on 25/05/2017.
 */
public class WorkRecord {
    //起始时间
    private String startTime;
    //终止时间
    private String endTime;

    //工作单位及职务
    private String company;
    //该时段缴费是否清退
    private boolean replay;

    //该时段个人缴费本金
    private int principal;

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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public boolean isReplay() {
        return replay;
    }

    public void setReplay(boolean replay) {
        this.replay = replay;
    }

    public int getPrincipal() {
        return principal;
    }

    public void setPrincipal(int principal) {
        this.principal = principal;
    }

}
