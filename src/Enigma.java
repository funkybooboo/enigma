import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Enigma {

    private static final int SHIFT_CONSTANT = 13; // 13 is a random number chosen to mix things up. It could be any number
    private final List<Map<String, String>> rotors;

    public Enigma(String code) {
        rotors = generateRotors(code);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String code = getEncryptionCodeFromUser(scanner);
        Enigma enigma = new Enigma(code);
        while (true) {
            System.out.println("1. (E)ncode");
            System.out.println("2. (D)ecode");
            System.out.println("3. (C)hange encryption code");
            System.out.println("4. (Q)uit");
            System.out.println();
            System.out.print("What would you like to do? (1-4): ");
            String action = scanner.nextLine();
            if ("1".equals(action) || action.equalsIgnoreCase("e")) {
                enigma.encode(scanner);
            } else if ("2".equals(action) || action.equalsIgnoreCase("d")) {
                enigma.decode(scanner);
            } else if ("3".equals(action) || action.equalsIgnoreCase("c")) {
                code = getEncryptionCodeFromUser(scanner);
                enigma = new Enigma(code);
            } else if ("4".equals(action) || action.equalsIgnoreCase("q")) {
                System.exit(0);
                return;
            } else {
                System.out.println("Invalid option");
            }
            System.out.println();
        }
    }

    /**
     * Gets the encryption code from the user
     *
     * @param scanner The scanner that provides user input
     * @return The encryption code
     */
    private static String getEncryptionCodeFromUser(Scanner scanner) {
        String code;
        do {
            System.out.print("Enter the encryption code: ");
            code = scanner.nextLine().trim();
            if (code.length() == 0) {
                System.out.println("Invalid code, try again");
            }
        } while (code.length() == 0);
        return code;
    }

    /**
     * Prompts the user for a phrase to encode and then encodes the phrase
     *
     * @param scanner The scanner that provides user input
     */
    private void encode(Scanner scanner) {
        System.out.print("Enter a phrase to encode: ");
        String phrase = scanner.nextLine();
        String encodedMessage = encodeMessage(phrase);
        System.out.println();
        System.out.print("Encoded message: ");
        System.out.println(encodedMessage);
    }

    /**
     * Encodes the phrase
     *
     * @param phrase The user's phrase
     * @return The decoded phrase
     */
    private String encodeMessage(String phrase) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < phrase.length() ; i++) {
            String c = String.valueOf(phrase.charAt(i));
            for (Map<String, String> rotor : rotors) {
                c = rotor.get(c);
                // rotate the rotor so the same letter doesn't produce the same encoded letter
                int v = c.charAt(0) + i;
                if (v >= 127) {
                    v = v - 127 + 32;
                }
                c = String.valueOf((char)v);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Decodes the phrase provided by the user
     *
     * @param scanner The scanner that provides user input
     */
    private void decode(Scanner scanner) {
        System.out.print("Enter a phrase to decode: ");
        String phrase = scanner.nextLine();
        String decodedMessage = decodeMessage(phrase);
        System.out.println();
        System.out.print("Decoded message: ");
        System.out.println(decodedMessage);
    }

    /**
     * Decodes the phrase
     *
     * @param phrase The user's phrase
     * @return The decoded phrase
     */
    private String decodeMessage(String phrase) {
        StringBuilder sb = new StringBuilder();
        for (int i = phrase.length()-1; i >= 0 ; i--) {
            String c = String.valueOf(phrase.charAt(i));
            for (int r = rotors.size()-1 ; r >= 0 ; r--) {
                Map<String, String> rotor = rotors.get(r);
                // need to invert the map (make the value the key and the key the value)
                rotor = invertMap(rotor);
                c = rotor.get(c);
                // rotate the rotor
                int v = c.charAt(0) - i;
                if (v < 32) {
                    v = 127 - (32 - v);
                }
                c = String.valueOf((char)v);
            }
            sb.append(c);
        }
        return sb.reverse().toString();
    }

    /**
     * Generates a list of rotors the size of the length of the code.
     * For example, if the code is 'cats' then the list will contain 4 rotors.
     *
     * A Rotor consists of a Map of key/values.
     * For example, "a" -> "Y"
     *              "b" -> "5"
     *              "c" -> "+"
     * @param code The encryption code
     * @return A list of rotors
     */
    private List<Map<String, String>> generateRotors(String code) {
        List<Map<String, String>> rotors = new ArrayList<>();
        for (int i = 0 ; i < code.length() ; i++) {
            char c = code.charAt(i);
            rotors.add(generateRotor(c, i));
        }
        return rotors;
    }

    /**
     * Creates a rotor that contains a mapping for all characters between 32 and 126 inclusive
     *
     * @param c The character in the encryption code that is the seed for generating the rotor
     * @param pos The position of the character in the encryption code
     * @return The rotor
     */
    private Map<String, String> generateRotor(char c, int pos) {
        Map<String, String> rotor = new HashMap<>();
        for (int i = c; i < 127 ; i++) {
            generateCode(rotor, i, pos);
        }
        for (int i = 32; i <= c; i++) {
            generateCode(rotor, i, pos);
        }
        return rotor;
    }

    /**
     * Takes the decimal value of a character and shifts it to a different character and
     * maps it to the provided character
     * For example, if decimalChar is 97 which is character 'a', it may put into the 'rotor' map
     *   'a' -> 'o'
     *
     * @param rotor The rotor map to fill
     * @param decimalChar Decimal value of the character
     * @param pos The position of the character in the encryption code
     */
    private void generateCode(Map<String, String> rotor, int decimalChar, int pos) {
        int codedChar = getCodedChar(decimalChar, pos);
        rotor.put(String.valueOf((char) decimalChar), String.valueOf((char) codedChar));
    }

    /**
     * Takes the decimal value of a character and shifts it to a different character
     * For example, given decimal 97 which is character 'a', this would shift that character by pos + 13
     * So, in this example if pos was 1, then it would return 97 + 1 + 13 = 111 which is a character 'o'
     * If the decimal value computed >= 127, then it rolls around to characters starting at decimal 32
     * For example decimal 122 ('z') would be 122 + 1 + 13 = 136 which is > 127, so it would return
     *   136 - (127 + 32) = 41 which is character ')'
     *
     * @param decimalChar Decimal value of the character
     * @param pos The position of the character in the encryption code
     * @return The decimal value of the character shifted
     */
    private int getCodedChar(int decimalChar, int pos) {
        int shift = decimalChar + (pos + SHIFT_CONSTANT);
        while (shift >= 127) {
            shift = shift - 127 + 32;
        }
        return shift;
    }

    /**
     * Takes a map and inverts the key/values
     * For example, "a" -> "Y"
     *              "b" -> "5"
     *              "c" -> "+"
     *
     * would return "Y" -> "a"
     *              "5" -> "b"
     *              "+" -> "c"
     * @param map The map to invert
     * @return The inverted map
     */
    private Map<String, String> invertMap(Map<String, String> map) {
        Map<String, String> invertedMap = new HashMap<>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            invertedMap.put(entry.getValue(), entry.getKey());
        }

        return invertedMap;
    }
}