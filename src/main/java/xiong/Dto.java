package xiong;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johnson on 25/05/2017.
 */
public class Dto {
    //公司
    private String company;
    //组织机构代
    private String companyCode;
    //姓名
    private String username;
    //身份证
    private String idnum;
    //经历
    private List<WorkRecord> workRecordList = new ArrayList<>();

    public Dto() {
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
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

    public int getSum() {
        int sum=0;
        for(WorkRecord record:workRecordList){
            if(record.isReplay()){
                sum+=record.getPrincipal();
            }
        }
        return sum;
    }

    public void setWorkRecordList(List<WorkRecord> workRecordList) {
        this.workRecordList = workRecordList;
    }

}
