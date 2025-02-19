
import java.util.Random;

public class RandomSentenceGenerator {
    private static final String[] subjects = {"The cat", "A dog", "The programmer", "A scientist", "The robot", "An alien"};
    private static final String[] verbs = {"jumps over", "eats", "analyzes", "discovers", "builds", "destroys"};
    private static final String[] objects = {"a sandwich", "the moon", "a strange code", "a secret", "a new planet", "a magical potion"};
    private static final String[] endings = {"happily.", "without thinking.", "like a genius.", "with great enthusiasm.", "in a mysterious way.", "unexpectedly."};

    public static String generateRandomSentence() {
        Random random = new Random();
        String subject = subjects[random.nextInt(subjects.length)];
        String verb = verbs[random.nextInt(verbs.length)];
        String object = objects[random.nextInt(objects.length)];
        String ending = endings[random.nextInt(endings.length)];

        return subject + " " + verb + " " + object + " " + ending;
    }
}
