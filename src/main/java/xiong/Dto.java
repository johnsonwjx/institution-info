package xiong;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johnson on 25/05/2017.
 */
public class Dto {
    private String company;

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    private String companyCode;
    private String username;
    private String idnum;
    private List<WorkRecord> workRecordList = new ArrayList<>();

    public Dto() {
    }

    public void addWorkRecord(WorkRecord workRecord) {
        workRecordList.add(workRecord);
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdnum() {
        return idnum;
    }

    public void setIdnum(String idnum) {
        this.idnum = idnum;
    }

    public List<WorkRecord> getWorkRecordList() {
        return workRecordList;
    }

    public void setWorkRecordList(List<WorkRecord> workRecordList) {
        this.workRecordList = workRecordList;
    }

    @Override
    public String toString() {
        return "Dto{" +
                "username='" + username + '\'' +
                ", idnum='" + idnum + '\'' +
                '}';
    }
}
