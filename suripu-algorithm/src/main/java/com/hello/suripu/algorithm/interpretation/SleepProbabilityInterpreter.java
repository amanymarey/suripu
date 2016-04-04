package com.hello.suripu.algorithm.interpretation;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.hello.suripu.algorithm.hmm.BetaPdf;
import com.hello.suripu.algorithm.hmm.GaussianPdf;
import com.hello.suripu.algorithm.hmm.HiddenMarkovModelFactory;
import com.hello.suripu.algorithm.hmm.HiddenMarkovModelInterface;
import com.hello.suripu.algorithm.hmm.HmmDecodedResult;
import com.hello.suripu.algorithm.hmm.HmmPdfInterface;
import com.hello.suripu.algorithm.hmm.PdfCompositeBuilder;
import com.hello.suripu.algorithm.hmm.PoissonPdf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by benjo on 3/24/16.
 */
public class SleepProbabilityInterpreter {

    final static Logger LOGGER = LoggerFactory.getLogger(SleepProbabilityInterpreter.class);
    final static double MIN_GAUSSIAN_LOG_PDF_EVAL = -10000;
    final static HmmPdfInterface[] obsModelsMain = {new BetaPdf(2.0,10.0,0),new BetaPdf(6.0,6.0,0),new BetaPdf(10.0,2.0,0)};
    final static HmmPdfInterface[] obsModelsDiff = {new GaussianPdf(-0.02,0.02,1,MIN_GAUSSIAN_LOG_PDF_EVAL),new GaussianPdf(0.00,0.02,1,MIN_GAUSSIAN_LOG_PDF_EVAL),new GaussianPdf(0.02,0.02,1,MIN_GAUSSIAN_LOG_PDF_EVAL)};

    final static double MIN_HMM_PDF_EVAL = 1e-320;
    final static int MAX_ON_BED_SEARCH_WINDOW = 30; //minutes
    final static int MAX_OFF_BED_SEARCH_WINDOW = 30; //minutes

    final protected static int DEFAULT_SPACING_OF_OUT_OF_BED_AFTER_WAKE = 1;
    final protected static int DEFAULT_SPACING_OF_IN_BED_BEFORE_SLEEP = 5;

    final static double POISSON_MEAN_FOR_A_LITTLE_MOTION = 0.1;
    final static double POISSON_MEAN_FOR_MOTION = 5.0;
    final static double POISSON_MEAN_FOR_NO_MOTION = 0.1;

    final static double MIN_SLEEP_PROB = 0.001;

    final static int MIN_SLEEP_DURATION = 60; //insanity check

    public static class EventIndices {
        public final int iInBed;
        public final int iSleep;
        public final int iWake;
        public final int iOutOfBed;

        public EventIndices(int iInBed, int iSleep, int iWake, int iOutOfBed) {
            this.iInBed = iInBed;
            this.iSleep = iSleep;
            this.iWake = iWake;
            this.iOutOfBed = iOutOfBed;
        }
    }

    /*
     *  This takes the output of a neural network that outputs p(sleep), and a motion signal (on duration seconds) from the pill
     *  and will output when it thinks you got in bed, fell asleep, woke up, and got out of bed.
     *
     *
     *
     *  There are two steps:
     *  Step 1) Segment sleep with a hidden Markov model.  The general notion is that p(sleep) can jump around a bit, so we want to segment
     *  this into states.
     *  State 0 - pre sleep (awake)
     *  State 1 - entering sleep
     *  State 2 - unsure if sleeping
     *  State 3 - definitely sleeping
     *  State 4 - waking rapidly
     *  State 5 - waking / unsure if sleeping
     *  State 6 - post sleep (awake)
     *
     *  The general idea is that when you first get to State 3, you're asleep.
     *  The last time you leave state 3 is when you woke up.
     *
     *  So, you can go between states 2 and 3 all night, and we still consider you asleep.
     *  As soon as you get to states 4 or 5 from 2 or 3, you're considered awake.
     *
     *  Sensor data for this HMM is p(sleep) and diff[p(sleep)], and the obs model is
     *  a composite obs models is  a beta distribution for p(sleep) and a gaussian for
     *  the differential p(sleep) signal
     *
     *  Initial state is always state 0, and final state is always state 6.
     *
     *  There has got to be a better way of doing this.  Oh well.
     */
    public static Optional<EventIndices> getEventIndices(final double [] sleepProbabilities, final double [] myMotionDurations) {

        int iSleep = -1;
        int iWake = -1;
        int iInBed = -1;
        int iOutOfBed = -1;

        if (sleepProbabilities.length <= 1 || myMotionDurations.length <= 1) {
            return Optional.absent();
        }



        final double [] sleep = sleepProbabilities.clone();

        //a sleep prob of 0.0 can screw up the decode b/c of a negative inf likelihood
        for (int t = 0; t < sleep.length; t++) {
            if (sleep[t] < MIN_SLEEP_PROB) {
                sleep[t] = MIN_SLEEP_PROB;
            }
        }


        final double [] dsleep = new double[sleep.length];

        for (int i = 1; i < dsleep.length; i++) {
            dsleep[i] = sleep[i] - sleep[i-1];
        }

        final double [][] sleepProbsWithDeltaProb = {sleep,dsleep};

        {
            //iterate through all possible combinations
            final HmmPdfInterface s0 = PdfCompositeBuilder.newBuilder().withPdf(obsModelsMain[0]).withPdf(obsModelsDiff[1]).build();
            final HmmPdfInterface s1 = PdfCompositeBuilder.newBuilder().withPdf(obsModelsMain[1]).withPdf(obsModelsDiff[2]).build();
            final HmmPdfInterface s2 = PdfCompositeBuilder.newBuilder().withPdf(obsModelsMain[1]).withPdf(obsModelsDiff[1]).build();
            final HmmPdfInterface s3 = PdfCompositeBuilder.newBuilder().withPdf(obsModelsMain[2]).withPdf(obsModelsDiff[1]).build();
            final HmmPdfInterface s4 = PdfCompositeBuilder.newBuilder().withPdf(obsModelsMain[1]).withPdf(obsModelsDiff[0]).build();
            final HmmPdfInterface s5 = PdfCompositeBuilder.newBuilder().withPdf(obsModelsMain[1]).withPdf(obsModelsDiff[1]).build();
            final HmmPdfInterface s6 = PdfCompositeBuilder.newBuilder().withPdf(obsModelsMain[0]).withPdf(obsModelsDiff[1]).build();


            final HmmPdfInterface[] obsModels = {s0, s1, s2, s3, s4, s5, s6};

            final double[][] A = new double[obsModels.length][obsModels.length];
            A[0][0] = 0.99; A[0][1] = 0.01;
            A[1][1] = 0.98; A[1][2] = 0.01; A[1][3] = 0.01;
            A[2][2] = 0.98; A[2][3] = 0.01; A[2][4] = 0.01;
            A[3][3] = 0.99; A[3][2] = 0.001;                A[3][4] = 0.01;
            A[4][4] = 0.99; A[4][5] = 0.01;                                A[4][6] = 0.01;
            A[5][5] = 0.99; A[5][4] = 0.01;
            A[6][6] = 1.0;



            final double[] pi = new double[obsModels.length];
            pi[0] = 1.0;

            //segment this shit
            final HiddenMarkovModelInterface hmm = HiddenMarkovModelFactory.create(HiddenMarkovModelFactory.HmmType.LOGMATH, obsModels.length, A, pi, obsModels, 0);

            final HmmDecodedResult res = hmm.decode(sleepProbsWithDeltaProb, new Integer[]{obsModels.length - 1}, MIN_HMM_PDF_EVAL);


            if (res.bestPath.size() <= 1) {
                LOGGER.info("action=return_invalid_indices reason=path_size_less_than_one");
                return Optional.absent();
            }
/*
        float [] f = new float[sleep.length];
        for (int i = 0; i < sleep.length; i++) {
            f[i] = (float)sleep[i];
        }
        LOGGER.info("\n{}\n{}",f,res.bestPath);
*/



            boolean foundSleep = false;
            Integer prevState = res.bestPath.get(0);
            for (int i = 1; i < res.bestPath.size(); i++) {
                final Integer state = res.bestPath.get(i);

                if (!state.equals(prevState)) {
                    LOGGER.info("action=hmm_decode from_state={} to_state={} index={}", prevState, state, i);
                }

                if (state.equals(3) && !prevState.equals(3) && !foundSleep) {
                    foundSleep = true;
                    iSleep = i;
                } else if (!state.equals(3) && prevState.equals(3)) {
                    iWake = i;
                }

                prevState = state;

            }

            if (iSleep == -1 || iWake == -1) {
                return Optional.absent();
            }

            if (iWake - iSleep < MIN_SLEEP_DURATION) {
                return Optional.absent();
            }

            iInBed = iSleep - DEFAULT_SPACING_OF_IN_BED_BEFORE_SLEEP;
            iOutOfBed = iWake + DEFAULT_SPACING_OF_OUT_OF_BED_AFTER_WAKE;
        }


        /*
         *  Step 2:  Figure out when you got into and out of bed based off of motion and sleep.
         *
         *  The general idea is that you got into bed before you went to sleep, and you got out of bed after you woke up.
         *  So for sleep, you go backwards from when we think you fell asleep, and find the beginning of the previous motion segment.
         *  For wake, you go forwards from wake, and find the end of the next motion segment.
         *
         *
         *  Motion data is sporadic, meaning that you'll get one minute with five seconds of motion, and then ten minutes later
         *  you'll get another minute with six seconds of motion, and then maybe two hours of no motion.  The idea is that these two minutes
         *  of motion, spaced ten minutes apart constitute a "segment" of motion.
         *
         *  Motion segments are determined by an HMM, again.
         *  State 0: No motion for long duration
         *  State 1: moderate amount of motion for short duration
         *  State 2: no motion for short duration.
         *
         *  States 1 and 2 are considered in a motion segment.
         *
         *  So for the above example, it'd be  [.... 0 0 0 0 1 2 2 2 2 2 2 2 2 2 2 1 0 0 0 0 0 ....]
         *
         */

        {
            final double[][] x = {myMotionDurations};
            final double[][] A = {
                    {0.998, 1e-3, 1e-3},
                    {1e-5, 0.90, 0.10},
                    {0.0, 0.10, 0.90}
            };
            final double[] pi = {0.5, 0.5,0.5};
            final HmmPdfInterface[] obsModels = {new PoissonPdf(POISSON_MEAN_FOR_NO_MOTION, 0), new PoissonPdf(POISSON_MEAN_FOR_MOTION, 0),new PoissonPdf(POISSON_MEAN_FOR_A_LITTLE_MOTION, 0)};

            final HiddenMarkovModelInterface hmm = HiddenMarkovModelFactory.create(HiddenMarkovModelFactory.HmmType.LOGMATH, 3, A, pi, obsModels, 0);


            final HmmDecodedResult result = hmm.decode(x, new Integer[]{0, 1,2}, MIN_HMM_PDF_EVAL);

            final List<Integer> motionInts = Lists.newArrayList();

            for (final double d : myMotionDurations) {
                motionInts.add(Integer.valueOf((int)d));
            }

            //LOGGER.debug("\nclusterpath={};\nmotion={};\n",result.bestPath,motionInts);

            boolean foundCluster = false;

            //go backwards from sleep and find beginning of next motion cluster encountered
            for (int i = iSleep; i >= 0; i--) {
                final Integer state = result.bestPath.get(i);

                if (!state.equals(0)) {
                    //if motion cluster start was found too far before sleep, then stop search and use default
                    if (iSleep - i > MAX_ON_BED_SEARCH_WINDOW && !foundCluster) {
                        LOGGER.warn("action=return_default_in_bed reason=motion_cluster_too_far_out");
                        break;
                    }

                    foundCluster = true;
                    continue;
                }

                if (state.equals(0) && foundCluster) {
                    iInBed = i;
                    break;
                }
            }

            foundCluster = false;
            for (int i = iWake; i < myMotionDurations.length; i++) {
                final Integer state = result.bestPath.get(i);

                if (!state.equals(0)) {
                    //if motion cluster start was found too far after wake, then stop search and use default
                    if (i - iWake > MAX_OFF_BED_SEARCH_WINDOW && !foundCluster) {
                        LOGGER.warn("action=return_default_out_of_bed reason=motion_cluster_too_far_out");
                        break;
                    }
                    foundCluster = true;
                }

                if (state.equals(0) && foundCluster) {
                    iOutOfBed = i - 1;
                    break;
                }
            }
        }

        //sanity checks, make sure event times are not the same
        final int outOfBedBounds = iWake + DEFAULT_SPACING_OF_OUT_OF_BED_AFTER_WAKE;

        if (iOutOfBed < outOfBedBounds) {
            LOGGER.info("action=moving_out_of_bed reason=default_bounds_violation change={}",outOfBedBounds - iOutOfBed );
            iOutOfBed = outOfBedBounds;
        }

        final int inBedBounds = iSleep - DEFAULT_SPACING_OF_IN_BED_BEFORE_SLEEP;

        if (iInBed > inBedBounds) {
            LOGGER.info("action=moving_in_bed reason=default_bounds_violation change={}",inBedBounds - iInBed );
            iInBed = inBedBounds;
        }

        LOGGER.info("timeline_event=IN_BED idx={} psleep={}",iInBed,sleep[iInBed]);
        LOGGER.info("timeline_event=SLEEP  idx={} psleep={}",iSleep,sleep[iSleep]);
        LOGGER.info("timeline_event=WAKE_UP idx={} psleep={}",iWake,sleep[iWake]);
        LOGGER.info("timeline_event=OUT_OF_BED idx={} psleep={}",iOutOfBed,sleep[iOutOfBed]);

        return Optional.of(new EventIndices(iInBed,iSleep,iWake,iOutOfBed));

    }




}