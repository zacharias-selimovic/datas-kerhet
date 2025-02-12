package users;

public class Patient extends Person{
    String doctor;
    String nurse;
            
    public Patient(String name, String id, String division, String doctor, String nurse) {
        super(name, id, division);
        this.doctor = doctor;
        this.nurse = nurse;
    }

    public String getDoctor() {
        return doctor;
    }

    public String getNurse() {
        return nurse;
    }
}
