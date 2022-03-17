package com.zhidkov.viktor;

import com.github.davidmoten.bigsorter.Serializer;
import com.github.davidmoten.bigsorter.Sorter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class testTask {
    private static RandomAccessFile file;
    //*************Сортируем файл со списком аэропортов*****************************************************************
    public static void sort(int col, String fileIn, String fileOut) {
        Serializer<CSVRecord> serializer = Serializer.csv(
                CSVFormat.DEFAULT.withQuote(null), //
                StandardCharsets.UTF_8);

        Comparator<CSVRecord> comparator = (x, y) -> {
            String a = x.get(col - 1);
            String b = y.get(col - 1);
            return CharSequence.compare(a, b);
        };

        Sorter
                .serializer(serializer) //
                .comparator(comparator) //
                .input(new File(fileIn)) //
                .output(new File(fileOut)) //
                //.loggerStdOut() //
                .sort();
    }
    //******************************************************************************************************************
    //*************Алгоритм Бинарного поиска по файлу*******************************************************************
    public static ArrayList<String[]> binarySearchForFirstIndex(int col, String substrToSearch) throws IOException {
        ArrayList<String[]> resAirports = new ArrayList<>();
        try {
            file = new RandomAccessFile("resources/sortedAirport.dat", "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int firstIndex = 0;
        int lastIndex = 0;
        try {
            lastIndex = (int) file.length();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        while (lastIndex >= firstIndex) {
            int middleIndex = firstIndex + (lastIndex - firstIndex) / 2;
            file.seek(middleIndex);
            String[] row = file.readLine().replaceAll("\"", "").split(",");
            if (row.length != 14) {
                row = file.readLine().replaceAll("\"", "").split(",");
            }
            if (row[col - 1].substring(0, substrToSearch.length()).compareToIgnoreCase(substrToSearch) == 0) {
                String[] helper = row;
                int start = (int) file.getFilePointer();
                int beforePoint = Math.max(middleIndex - 150, 0);
                file.seek(beforePoint);
                row = file.readLine().replaceAll("\"", "").split(",");
                if (row.length != 14) {
                    row = file.readLine().replaceAll("\"", "").split(",");
                }
                // Здесь проверяем является ли предшествующий элемент подходящим, если нет, значит найденная строка
                // является первых вхождением, и начания с нее запускаем цикл while, который работает, пока находятся
                // удовлетворяющие нас строки
                if (middleIndex == 0 || row[col - 1].substring(0, //
                        substrToSearch.length()).compareToIgnoreCase(substrToSearch) < 0) {
                    resAirports.add(helper);
                    file.seek(start);
                    row = file.readLine().replaceAll("\"", "").split(",");
                    while (row[col - 1].substring(0, substrToSearch.length()).compareToIgnoreCase(substrToSearch) == 0) {
                        resAirports.add(row);
                        row = file.readLine().replaceAll("\"", "").split(",");
                    }
                    return resAirports;
                }
                else {
                    lastIndex = middleIndex;
                }

            }
            else if (row[col - 1].substring(0, substrToSearch.length()).compareToIgnoreCase(substrToSearch) < 0) {
                firstIndex = middleIndex + 1;
            }
            else {
                lastIndex = middleIndex - 1;
            }
        }
        return resAirports;
    }
    //******************************************************************************************************************

    public static void main(String[] args) throws IOException {
        FileInputStream fis;
        Properties property = new Properties();
        int col = 0;
        if (args.length == 0) {
            try {
                fis = new FileInputStream("resources/application.properties");
                property.load(fis);
                col = Integer.parseInt(property.getProperty("col"));

            } catch (IOException e) {
                System.err.println("ОШИБКА: Файл свойств отсуствует!");
            }
        }
        else{
            col = Integer.parseInt(args[0]);
        }
        Scanner in = new Scanner(System.in);
        System.out.print("Введите строку: ");
        String subStr = in.nextLine();
        sort(2, "resources/airports.dat",
                "resources/sortedAirport.dat");
        long start = System.currentTimeMillis();
        ArrayList<String[]> resultArray = binarySearchForFirstIndex(2, subStr);
        long finish = System.currentTimeMillis();
        if (resultArray.isEmpty()) {
            System.out.println("По вашему запросу ничего не найдено");
        }
        else {
            for (String[] strings : resultArray) {
                for (int j = 1; j < strings.length; j++) {
                    System.out.print(strings[j] + ", ");
                    if (j == strings.length - 1) {
                        System.out.print(strings[j] + ";");
                    }
                }
                System.out.println();
            }
            System.out.println("Количество найденных строк: " + resultArray.size() + " Время затраченное на поиск: " +
                    (finish - start) + " мс.");
        }
    }
}
