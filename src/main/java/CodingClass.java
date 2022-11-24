import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class CodingClass {
    private final ArrayList<Character> symbols = new ArrayList<>();
    CodingClass(){
        FileInputStream fIn;
        int i;
        try {
            fIn = new FileInputStream("src/main/resources/expression.txt");
        } catch (FileNotFoundException e){
            System.out.println("Невозможно прочитать файл");
            return;
        }

        try {
            do {
                i = fIn.read();
                if (i != -1) symbols.add((char) i);
            } while (i!=-1); //391
        } catch (IOException e){
            System.out.println("Ошибка чтения из файла");
        }

        ParsingClass parsingClass = new ParsingClass(symbols);
        parsingClass.showText();
        parsingClass.showCode();
        parsingClass.showCodeTable();
        parsingClass.showErrors();


        try {
            fIn.close();
        } catch (IOException e){
            System.out.println("Ошибка закрытия файла");
        }

    }
    public static void main(String[] args){
        CodingClass codingClass = new CodingClass();
    }
}

