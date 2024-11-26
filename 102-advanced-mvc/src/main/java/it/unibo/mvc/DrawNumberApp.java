package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        final Configuration.Builder configurationBuilder= new Configuration.Builder();
        try (var contents = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(configFile)))) {
            for (var configLine = contents.readLine(); configLine != null; configLine = contents.readLine()) {
                final String[] lineElements = configLine.split(":");
                //contorllo nel file e quando incotro un min o max lo inserisco nel config Builder
                if (lineElements.length == 2) {
                    final int value = Integer.parseInt(lineElements[1].trim());   //salvo il valore in una strigna
                    if(lineElements[0].contains("min")){
                        configurationBuilder.setMin(value);
                    } else if (lineElements[0].contains("max")) {
                        configurationBuilder.setMax(value);
                    } else if (lineElements[0].contains("attempts")) {
                        configurationBuilder.setAttempts(value);
                    }
                } else {
                    displayError("Errore nella letterua del file di configuarazione");
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
///////////////////////////////////////////////////////////////////////////////////////////////////////////



        this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    private void displayError(final String message){
        for (final DrawNumberView view: views) {
            view.displayError(message);
        }
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
