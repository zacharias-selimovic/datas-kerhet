
public class Record {
    private String content;
    public String doctorCN; //The CN of the doctor that created the record
    public String nurseCN; //The CN of the nurse
    public String patientCN; // The CN of the patient

    public Record(String doctorCN, String nurseCN, String patientCN){
        this.doctorCN = doctorCN;
        this.nurseCN = nurseCN;
        this.patientCN = patientCN;
        this.content = RandomSentenceGenerator.generateRandomSentence();
    }

    public String read(String readerOU, String readerCN){
        String OU = readerOU;
        switch (OU){
            case "Doctor" :
                if(readerCN.equals(this.doctorCN)){
                    return this.content;
                } 
                else {
                    return null;
                }
            case "Nurse" :
                if(readerCN.equals(this.nurseCN)){
                    return this.content;
                }
                else {
                    return null;
                }
            case "Patient" :
                if(readerCN.equals(this.patientCN)){
                    return this.content;
                }
                else {
                    return null;
                }
            case "Authority" :
                return this.content;
            default :
                return "Unknown organization! get out";

        }
    }
    
    public String write(String OU, String CN, String edit){
        if(OU.equals("Doctor")){
            if(CN.equals(this.doctorCN)){
                this.content += edit;
                return "Successfully written";
            } else {
                return "You dont have permission to do this";
            }
        } else {
            return "You dont have permission to do this";
        }
    }

}