package me.ddquin;

import java.util.HashMap;
import java.util.Map;

public class Test {

    public Map<Integer, String> unitToString = new HashMap<>();

    public static void main(String[] args) {
        Test t = new Test();
        System.out.println(t.numToWord(67));
        System.out.println(t.numToWord(99));
        System.out.println(t.numToWord(10));
        System.out.println(t.numToWord(5));
        System.out.println(t.numToWord(100));
        System.out.println(t.numToWord(101));
        System.out.println(t.numToWord(303));
        System.out.println(t.numToWord(999));
        System.out.println(t.numToWord(980));
        System.out.println(t.numToWord(80));
        String allWords = "";
        for (int i = 1; i <= 1000; i++) {
            allWords += t.numToWord(i);
        }
        System.out.println(allWords.length());
    }

    public Test() {
        unitToString.put(0, "");
        unitToString.put(1, "one");
        unitToString.put(2, "two");
        unitToString.put(3, "three");
        unitToString.put(4, "four");
        unitToString.put(5, "five");
        unitToString.put(6, "six");
        unitToString.put(7, "seven");
        unitToString.put(8, "eight");
        unitToString.put(9, "nine");
        unitToString.put(10, "ten");
        unitToString.put(11, "eleven");
        unitToString.put(12, "twelve");
        unitToString.put(13, "thirteen");
        unitToString.put(14, "fourteen");
        unitToString.put(15, "fifteen");
        unitToString.put(16, "sixteen");
        unitToString.put(17, "seventeen");
        unitToString.put(18, "eighteen");
        unitToString.put(19, "nineteen");
        unitToString.put(20, "twenty");
        unitToString.put(30, "thirty");
        unitToString.put(40, "forty");
        unitToString.put(50, "fifty");
        unitToString.put(60, "sixty");
        unitToString.put(70, "seventy");
        unitToString.put(80, "eighty");
        unitToString.put(90, "ninety");
        unitToString.put(100, "hundred");
    }

    public String numToWord(int num) {
        String word = "";
        if (num < 20) word = unitToString.get(num);
        else if (num < 100) {
            int tenth = (num /10) * 10;
            word += unitToString.get(tenth);
            int unit = num % 10;
            word += unitToString.get(unit);
        } else if (num < 1000) {
            int hundredth = (num/100);
            word+= unitToString.get(hundredth) + "hundred";
            if (num % 100 != 0) {
                word += "and" + numToWord(num % 100);
            }
        } else word += "onethousand";
        return word;
    }
}
