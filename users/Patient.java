package users;

public class Patient extends Person{
    String doctor;
    String nurse;
            
    public Patient(String name, String division, String doctor, String nurse) {
        super(name, division);
        this.doctor = doctor;
    }
}
