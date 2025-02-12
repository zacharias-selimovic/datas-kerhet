package users;

public class Person {

    String name;
    String id;
    String division;

    public Person(String name, String id, String division) {
       this.name = name;
       this.division = division;
    }

    public String getName() {
       return name;
    }

    public String getId() {
        return id;
    }

    public String getDivision() {
       return division;
    }

    public String toString() {
        return "Name: " + name + ", ID: " + id + ", Division: " + division;
    }
    
}
