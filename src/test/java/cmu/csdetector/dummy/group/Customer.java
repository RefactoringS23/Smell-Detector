package cmu.csdetector.dummy.group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Customer {
    protected String name = "no name yet";
    private List<Rental> rentals = new ArrayList<>();

    public Customer(String name) {
        this.name = name;
    }

    public String statement() {
        double totalAmount = 0;
        int frequentRenterPoints = 0;
        Iterator<Rental> rentals = this.rentals.iterator();
        String result = "Rental Record for " + name + "\n";

        while (rentals.hasNext()) {
            double thisAmount = 0;
            Rental each = rentals.next();

            //determine amounts for each line
            totalAmount += getAmount(thisAmount, each);

            // add frequent renter points
            frequentRenterPoints ++;

            // add bonus for a two-day new release rental
            frequentRenterPoints = updateFrequentRenterPoints(each, frequentRenterPoints);

            //show figures for this rental
            result += "\t" + each.getTape().getMovie().getName()+ "\t" + thisAmount + "\n";

        }
        //add footer lines
        result +=  "Amount owed is " + totalAmount + "\n";
        result += "You earned " + frequentRenterPoints + " frequent renter points";

        return result;

    }

    // manually refactored
    public double getAmount(double thisAmount, Rental each) {
        switch (each.getTape().getMovie().priceCode()) {
            case Movie.REGULAR:
                thisAmount += 2;
                if (each.daysRented() > 2)
                    thisAmount += (each.daysRented() - 2) * 1.5;
                break;
            case Movie.NEW_RELEASE:
                thisAmount += each.daysRented() * 3;
                break;
            case Movie.CHILDREN:
                thisAmount += 1.5;
                if (each.daysRented() > 3)
                    thisAmount += (each.daysRented() - 3) * 1.5;
                break;

        }
        return thisAmount;
    }

    // manually refactored
    public int updateFrequentRenterPoints(Rental each, int frequentRenterPoints) {
        if ((each.getTape().getMovie().priceCode() == Movie.NEW_RELEASE) && each.daysRented() > 1) {
            frequentRenterPoints = frequentRenterPoints + 1;
        }
        return frequentRenterPoints;
    }

    public void addRental(Rental rental) {
        rentals.add(rental);
    }

    public static Customer get(String name) {
        return Registrar.getCustomer(name);
    }

    public void persist() {
        Registrar.addCustomer(this.name, this);
    }
}