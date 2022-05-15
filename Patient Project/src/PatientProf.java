import com.med.MedCond;

public class PatientProf  {

    String adminID;
    String firstName;
    String lastName;
    String address;
    String phone;
    float coPay;
    String insuType;
    String patientType;
    MedCond medCondInfo;

    PatientProf(String adminID,String firstName,String lastName,String address, String phone,
                float coPay, String insuType,String patientType,MedCond medCondInfo){

        this.adminID=adminID;
        this.firstName=firstName;
        this.lastName=lastName;
        this.address=address;
        this.phone=phone;
        this.coPay=coPay;
        this.insuType=insuType;
        this.patientType=patientType;
        this.medCondInfo=medCondInfo;

    }

    public String getAdminID(){
        return this.adminID;
    }
    public String getFirstName(){
        return this.firstName;
    }
    public String getLastName(){
        return this.lastName;
    }
    public String getAddress(){
        return this.address;
    }
    public String getPhone(){
        return this.phone;
    }
    public float getCoPay(){
        return this.coPay;
    }
    public String getInsuType(){
        return this.insuType;
    }
    public String getPatientType(){
        return this.patientType;
    }
    public MedCond getMedCondInfo(){
        return this.medCondInfo;
    }

    public void updateFirstName(String firstName){
        this.firstName=firstName;
    }
    public void updateLastName(String lastName){
        this.lastName=lastName;
    }
    public void updateAddress(String address){
        this.address=address;
    }
    public void updatePhone(String phone){
        this.phone=phone;
    }
    public void updateCoPay(float coPay){
        this.coPay=coPay;
    }
    public void updatePatientType(String patientType){
        this.patientType=patientType;
    }

    public void updateMedCondInfo(MedCond medCondInfo){
        this.medCondInfo=medCondInfo;

    }
    public void updateInsuType(String insuType){
        this.insuType=insuType;
    }




}
