package se.emilsjolander;

import be.ac.ulg.montefiore.run.distributions.MultiGaussianDistribution;
import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.draw.GenericHmmDrawerDot;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationSequencesReader;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationVectorReader;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchScaledLearner;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    static public void main(String[] argv) throws java.io.IOException {
        Hmm<ObservationVector> nohmm = buildHmm();
        System.out.println("Initial no HMM:\n" + nohmm);
        Hmm<ObservationVector> yeshmm = buildHmm();
        System.out.println("Initial yes HMM:\n" + yeshmm);

        List<List<ObservationVector>> noSeq;
        List<List<ObservationVector>> yesSeq;

        try {
            noSeq = getSequence("no");
            yesSeq = getSequence("yes");
            Collections.shuffle(noSeq);
            Collections.shuffle(yesSeq);
        } catch (FileFormatException e) {
            e.printStackTrace();
            return;
        }

        List<List<ObservationVector>> trainSequencesNo = noSeq.subList(0, (int) (noSeq.size() * 0.75));
        List<List<ObservationVector>> trainSequencesYes = yesSeq.subList(0, (int) (yesSeq.size() * 0.75));
        List<List<ObservationVector>> testSequencesNo = noSeq.subList((int) (noSeq.size() * 0.75), noSeq.size());
        List<List<ObservationVector>> testSequencesYes = yesSeq.subList((int) (yesSeq.size() * 0.75), yesSeq.size());

        // Incrementally improve the solution
        BaumWelchScaledLearner nobwl = new BaumWelchScaledLearner();
        for (int i = 0; i < 10; i++) {
            nohmm = nobwl.iterate(nohmm, trainSequencesNo);
        }
        BaumWelchScaledLearner yesbwl = new BaumWelchScaledLearner();
        for (int i = 0; i < 10; i++) {
            yeshmm = yesbwl.iterate(yeshmm, trainSequencesYes);
        }
        System.out.println("Resulting no HMM:\n" + nohmm);
        printCovarianceMatrices(nohmm);
        System.out.println("Resulting yes HMM:\n" + yeshmm);
        printCovarianceMatrices(yeshmm);

        for (List<ObservationVector> seq : testSequencesNo) {
            System.out.println("No probability ratio: " + yeshmm.probability(seq)/nohmm.probability(seq));
        }
        for (List<ObservationVector> seq : testSequencesYes) {
            System.out.println("Yes probability ratio: " + yeshmm.probability(seq) / nohmm.probability(seq));
        }

        float noCorrect = 0;
        for (List<ObservationVector> seq : testSequencesNo) {
            double ratio = yeshmm.probability(seq) / nohmm.probability(seq);
            if (ratio < 1.0E-30) {
                noCorrect++;
            }
        }

        float yesCorrect = 0;
        for (List<ObservationVector> seq : testSequencesYes) {
            double ratio = yeshmm.probability(seq) / nohmm.probability(seq);
            if (ratio > 1.0E30) {
                yesCorrect++;
            }
        }

        noCorrect /= testSequencesNo.size();
        yesCorrect /= testSequencesYes.size();

        System.out.println("No correct clasification: " + noCorrect);
        System.out.println("Yes correct clasification: " + yesCorrect);

        (new GenericHmmDrawerDot()).write(nohmm, "nohmm.dot");
        (new GenericHmmDrawerDot()).write(yeshmm, "yeshmm.dot");
    }

    private static void printCovarianceMatrices(Hmm<ObservationVector> hmm) {
        try {
            for (int i = 0; i < hmm.nbStates(); i++) {
                Field distField = OpdfMultiGaussian.class.getDeclaredField("distribution");
                Field covarianceField = MultiGaussianDistribution.class.getDeclaredField("covariance");
                distField.setAccessible(true);
                covarianceField.setAccessible(true);
                MultiGaussianDistribution dist = (MultiGaussianDistribution) distField.get(hmm.getOpdf(i));
                double[][] covariance = (double[][]) covarianceField.get(dist);
                System.out.println(Arrays.deepToString(covariance));
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static List<List<ObservationVector>> getSequence(String name) throws IOException, FileFormatException {
        Reader reader = new FileReader(name + ".seq");
        List<List<ObservationVector>> v = ObservationSequencesReader.
                readSequences(new ObservationVectorReader(2), reader);
        reader.close();
        return v;
    }

    static Hmm<ObservationVector> buildHmm()
    {
        Hmm<ObservationVector> hmm = new Hmm<>(4, new OpdfMultiGaussianFactory(2));

        hmm.setPi(0, 0.25);
        hmm.setPi(1, 0.25);
        hmm.setPi(2, 0.25);
        hmm.setPi(3, 0.25);

        hmm.setOpdf(0, new OpdfMultiGaussian(
                new double[]{0,0},
                new double[][] {{0.1,0},
                                {0,0.1}}));

        hmm.setOpdf(1, new OpdfMultiGaussian(
                new double[]{0,0},
                new double[][] {{0.1,0},
                                {0,0.1}}));

        hmm.setOpdf(2, new OpdfMultiGaussian(
                new double[]{0,0},
                new double[][] {{0.1,0},
                                {0,0.1}}));

        hmm.setOpdf(3, new OpdfMultiGaussian(
                new double[]{0,0},
                new double[][] {{0.1,0},
                                {0,0.1}}));

        hmm.setAij(0, 0, 0.24);
        hmm.setAij(0, 1, 0.26);
        hmm.setAij(0, 2, 0.22);
        hmm.setAij(0, 3, 0.28);

        hmm.setAij(1, 0, 0.24);
        hmm.setAij(1, 1, 0.26);
        hmm.setAij(1, 2, 0.22);
        hmm.setAij(1, 3, 0.28);

        hmm.setAij(2, 0, 0.24);
        hmm.setAij(2, 1, 0.26);
        hmm.setAij(2, 2, 0.22);
        hmm.setAij(2, 3, 0.28);

        hmm.setAij(3, 0, 0.24);
        hmm.setAij(3, 1, 0.26);
        hmm.setAij(3, 2, 0.22);
        hmm.setAij(3, 3, 0.28);

        return hmm;
    }

}
