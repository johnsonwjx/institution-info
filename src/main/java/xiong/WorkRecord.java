package xiong;

/**
 * Created by johnson on 25/05/2017.
 */
public class WorkRecord {
    private String startTime;
    private String endTime;

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

    private String company;

    @Override
    public String toString() {
        return "WorkRecord{" +
                "company='" + company + '\'' +
                '}';
    }
}
