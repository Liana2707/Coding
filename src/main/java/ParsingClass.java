import java.util.*;

import static java.lang.Character.*;

public class ParsingClass {
    final private int SUP_NOT_TERM_CODE = 51;
    final private int SUP_TERM_CODE = 101;
    final private int SUP_SEMANTIC_CODE = 151;
    final private int TAB_CODE = -100;

    private enum Category {Term, NotTerm, Semantic, Errors, Other}

    private final ArrayList<Character> text;
    private final ArrayList<StringBuilder> errors = new ArrayList<>();
    private final Map<Integer, StringBuilder> codeTable = new HashMap<>() {
        {
            put(0, new StringBuilder(" "));
            put(1, new StringBuilder(":"));
            put(2, new StringBuilder("("));
            put(3, new StringBuilder(")"));
            put(4, new StringBuilder("."));
            put(5, new StringBuilder("*"));
            put(6, new StringBuilder(";"));
            put(7, new StringBuilder(","));
            put(8, new StringBuilder("#"));
            put(9, new StringBuilder("["));
            put(10, new StringBuilder("]"));
            put(1000, new StringBuilder("Eofgram"));
        }
    };
    private int numberNotTerm = 10;
    private int numberTerm = 50;
    private int numberSemantic = 100;

    private class PreviousWord {
        StringBuilder stringBuilder = new StringBuilder();
        Category category = Category.Other;
        int code = 0;
    }

    private class CodeList {
        private int currentIndexCodeList = 0;
        public final int[] codes;

        public int length() {
            return currentIndexCodeList;
        }

        private CodeList() {
            this.codes = new int[text.size()];
        }

        public void add(int elem) {
            codes[currentIndexCodeList] = elem;
            currentIndexCodeList++;
        }

        public void removeLast() {
            currentIndexCodeList--;
            codes[currentIndexCodeList] = 0;
        }
    }

    private final CodeList codeList;
    private final PreviousWord previousWord = new PreviousWord();

    ParsingClass(ArrayList<Character> symbols) {
        symbols.add(' ');
        text = symbols;
        codeList = new CodeList();
        Parse();
    }

    private void Parse() {
        StringBuilder currentWord = new StringBuilder();
        boolean isQuotes = false;
        for (char symbol : text) {
            final String PUNCTUATION = ":().*;,#[]";

            if (symbol == '\'' && !isQuotes) {
                currentWord.append(symbol);
                isQuotes = true;
            } else if (symbol != '\'' && isQuotes) {
                currentWord.append(symbol);
            } else if (symbol == '\'') {
                isQuotes = false;
                currentWord.append(symbol);
                if (!isOldWord(currentWord)) checkWord(currentWord);
                currentWord = new StringBuilder();
            } else if (symbol == ' ' && currentWord.length() != 0) {
                if (!isOldWord(currentWord)) checkWord(currentWord);
                currentWord = new StringBuilder();
            } else if (PUNCTUATION.indexOf(symbol) == -1 && symbol != ' ' && symbol != '\r' && symbol != '\n') {
                currentWord.append(symbol);
            } else if (PUNCTUATION.indexOf(symbol) != -1) {
                if (symbol == ':') fixCategory();
                codeList.add(PUNCTUATION.indexOf(symbol) + 1);
            } else if (symbol == '\r') {
                codeList.add(TAB_CODE);
            }
        }
    }

    private boolean isOldWord(StringBuilder word) {
        boolean isOld = false;
        for (int key : codeTable.keySet()) {
            if (Objects.equals(word.substring(0), codeTable.get(key).substring(0))) {
                isOld = true;
                codeList.add(key);
                if (key >= 11 && key < SUP_NOT_TERM_CODE)
                    previousWord.category = Category.NotTerm;
                else if (key >= SUP_NOT_TERM_CODE && key < SUP_TERM_CODE)
                    previousWord.category = Category.Term;
                else if (key >= SUP_TERM_CODE && key < SUP_SEMANTIC_CODE)
                    previousWord.category = Category.Semantic;
                else if (key == 1000 || (key >= 0 && key < 11) || key == -100)
                    previousWord.category = Category.Other;
                else previousWord.category = Category.Errors;
                previousWord.stringBuilder = codeTable.get(key);
                previousWord.code = key;
                break;
            }
        }
        return isOld;
    }

    private void checkWord(StringBuilder word) {
        if (!word.isEmpty()) {
            if (isNotTerm(word) && numberNotTerm < SUP_NOT_TERM_CODE - 1) {
                numberNotTerm++;
                codeList.add(numberNotTerm);
                codeTable.put(numberNotTerm, word);
                previousWord.category = Category.NotTerm;
                previousWord.code = numberNotTerm;
            } else if (isTerm(word) && numberTerm < SUP_TERM_CODE - 1) {
                numberTerm++;
                codeList.add(numberTerm);
                codeTable.put(numberTerm, word);
                previousWord.category = Category.Term;
                previousWord.code = numberTerm;
            } else if (isSemantics(word) && numberSemantic < SUP_SEMANTIC_CODE - 1) {
                numberSemantic++;
                codeList.add(numberSemantic);
                codeTable.put(numberSemantic, word);
                previousWord.code = numberSemantic;
                previousWord.category = Category.Semantic;
            } else if (Objects.equals(word.substring(0), "Eofgram")) {
                codeList.add(1000);
                previousWord.category = Category.Other;
                previousWord.code = 0;
            } else {
                errors.add(word);
                codeList.add(-1);
                previousWord.category = Category.Errors;
                previousWord.code = -1;
            }
            previousWord.stringBuilder = word;
        }
    }

    private boolean isNotTerm(StringBuilder word) {
        return word.charAt(0) != '\'' && Objects.equals(word.substring(0), word.substring(0).toUpperCase());
    }

    private boolean isSemantics(StringBuilder word) { // проверить то, что все буквы
        if (word.charAt(0) == '$') {
            return true;
        } else {
            for (int i = 0; i < word.length(); i++) {
                if (!isUpperCase((word.charAt(i))))
                    return false;
            }
            return true;
        }
    }

    private boolean isTerm(StringBuilder word) {
        if (word.charAt(0) == '\'' && word.charAt(word.length() - 1) == '\'')
            return true;
        else {
            for (int i = 0; i < word.length(); i++) {
                if (!isLowerCase((word.charAt(i))))
                    return false;
            }
            return true;
        }
    }

    private void fixCategory() {
        switch (previousWord.category) {
            case Term -> {
                codeTable.remove(previousWord.code);

                for (int i = 0; i <= codeList.length(); i++) {
                    if (codeList.codes[i] == previousWord.code)
                        codeList.codes[i] = -2;
                    if (codeList.codes[i] > previousWord.code && codeList.codes[i] < SUP_TERM_CODE)
                        codeList.codes[i]--;
                }
                numberTerm--;
                numberNotTerm++;
                for (int i = 0; i <= codeList.length(); i++) {
                    if (codeList.codes[i] == -2)
                        codeList.codes[i] = numberNotTerm;
                }
                codeTable.put(numberNotTerm, previousWord.stringBuilder);
                for (int i = numberTerm + 1; i < 101; i++) {
                    if (codeTable.containsKey(i)) {
                        StringBuilder temp = codeTable.get(i);
                        codeTable.remove(i);
                        codeTable.put(i - 1, temp);
                    }
                }
                previousWord.category = Category.NotTerm;
                previousWord.code = numberNotTerm;
            }
            case NotTerm -> {
                break;
            }
            case Semantic -> {
                codeTable.remove(previousWord.code);

                for (int i = 0; i <= codeList.length(); i++) {
                    if (codeList.codes[i] == previousWord.code)
                        codeList.codes[i] = -2;
                    if (codeList.codes[i] > previousWord.code && codeList.codes[i] < SUP_SEMANTIC_CODE)
                        codeList.codes[i]--;
                }
                numberSemantic--;
                numberNotTerm++;
                for (int i = 0; i <= codeList.length(); i++) {
                    if (codeList.codes[i] == -2)
                        codeList.codes[i] = numberNotTerm;
                }
                codeTable.put(numberNotTerm, previousWord.stringBuilder);
                for (int i = numberTerm + 2; i < 101; i++) {
                    if (codeTable.containsKey(i)) {
                        StringBuilder temp = codeTable.get(i);
                        codeTable.remove(i);
                        codeTable.put(i - 1, temp);
                    }
                }
                previousWord.category = Category.NotTerm;
                previousWord.code = numberNotTerm;
            }
            case Errors -> {
                codeList.removeLast();
                errors.remove(errors.size() - 1);
                numberNotTerm++;
                for (int i = 0; i < codeList.length(); i++) {
                    if (codeList.codes[i] == -1)
                        codeList.codes[i] = numberNotTerm;
                }
                codeList.add(numberNotTerm);
                codeTable.put(numberNotTerm, previousWord.stringBuilder);
            }
            case Other -> {
            }
        }
    }

    public void showCode() {
        for (int code : codeList.codes) {
            if (code != 0)
                if (code == TAB_CODE)
                    System.out.println("\n");
                else {
                    System.out.print(code + " ");
                }
        }
        System.out.println("\n");
    }

    public void showCodeTable() {
        for (int code : codeTable.keySet()) {
            if (code != 0)
                System.out.println(code + " " + codeTable.get(code));
        }
    }

    public void showText() {
        for (char symbol : text) {
            System.out.print(symbol);
        }
    }

    public void showErrors() {
        for (StringBuilder error : errors) {
            System.out.println(error);
        }
    }

}
