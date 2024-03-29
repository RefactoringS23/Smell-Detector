package cmu.csdetector.dummy.movie;

/**
 * The tape class represents a physical tape.
 */
public class Tape {
    private String serialNumber;
    private Movie movie;

    public Tape(String serialNumber, Movie movie) {
        this.serialNumber = serialNumber;
        this.movie = movie;
    }

    public Movie getMovie() {
        return movie;
    }
}
