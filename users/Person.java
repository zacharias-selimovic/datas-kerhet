package users;

public class Person {

    String name;
    String division;

    public Person(String name, String division) {
       this.name = name;
       this.division = division;
    }

    public String getName() {
       return name;
    }

    public String getDivision() {
       return division;
    }
    
}
