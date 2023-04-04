package cmu.csdetector.dummy.group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Movie represents the notion of a film. A video store might have several tapes in stock of the same movie
 */
public class Movie {
    private String name = "no name yet";
    public static final int CHILDREN = 2;
    public static final int  REGULAR = 0;
    public static final int  NEW_RELEASE = 1;

    private int priceCode;

    public Movie(String name, int priceCode) {
        this.name = name;
        this.priceCode = priceCode;
    }

    public int priceCode() {
        return priceCode;
    }

    public void persist() {
        Registrar.addMovie(name, this);
    }

    public  Movie get() {
        return Registrar.getMovie(name);
    }

    public String getName() {
        return name;
    }
//    public String statement(String m) {
//        double totalAmount = 0;
//        int frequentRenterPoints = 0;
//        List<Rental> rentalss = new ArrayList<>();
//
//        Iterator<Rental> rentals = rentalss.iterator();
//        String result = "Rental Record for " + m + "\n";
//
//        while (rentals.hasNext()) {
//            double thisAmount = 0;
//            Rental each = rentals.next();
//
//            //determine amounts for each line
//            switch (each.getTape().getMovie().priceCode()) {
//                case Movie.REGULAR:
//                    thisAmount += 2;
//                    if (each.daysRented() > 2)
//                        thisAmount += (each.daysRented() - 2) * 1.5;
//                    break;
//                case Movie.NEW_RELEASE:
//                    thisAmount += each.daysRented() * 3;
//                    break;
//                case Movie.CHILDREN:
//                    thisAmount += 1.5;
//                    if (each.daysRented() > 3)
//                        thisAmount += (each.daysRented() - 3) * 1.5;
//                    break;
//
//            }
//            totalAmount += thisAmount;
//
//            // add frequent renter points
//            frequentRenterPoints ++;
//
//            // add bonus for a two-day new release rental
//            if ((each.getTape().getMovie().priceCode() == Movie.NEW_RELEASE) && each.daysRented() > 1) frequentRenterPoints ++;
//
//            //show figures for this rental
//            result += "\t" + each.getTape().getMovie().getName()+ "\t" + thisAmount + "\n";
//
//        }
//        //add footer lines
//        result +=  "Amount owed is " + totalAmount + "\n";
//        result += "You earned " + frequentRenterPoints + " frequent renter points";
//
//        return result;
//
//    }
}

