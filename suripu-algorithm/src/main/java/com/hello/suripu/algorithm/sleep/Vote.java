package com.hello.suripu.algorithm.sleep;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hello.suripu.algorithm.core.AmplitudeData;
import com.hello.suripu.algorithm.core.Segment;
import com.hello.suripu.algorithm.utils.DataUtils;
import com.hello.suripu.algorithm.utils.MotionFeatures;
import com.hello.suripu.algorithm.utils.NumericalUtils;
import org.apache.commons.math3.util.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HEAD;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by pangwu on 3/19/15.
 */
public class Vote {
    private final static Logger LOGGER = LoggerFactory.getLogger(Vote.class);
    private final MotionScoreAlgorithm motionScoreAlgorithmDefault;

    private final MotionCluster motionCluster;
    private final List<AmplitudeData> alignedAmplitude;
    private final Map<MotionFeatures.FeatureType, List<AmplitudeData>> aggregatedFeatures;
    private final double rawAmpMean;
    private final double rawKickOffMean;

    private final SleepPeriod sleepPeriod;

    private final boolean insertEmpty = true;
    private final boolean removeNoise = false;
    private final boolean capScoreOutOfPeriod = false;
    private final boolean defaultOverride = false;
    private final boolean newSearch = true;
    private final boolean newSearchWakeUp = true;

    public Vote(final List<AmplitudeData> rawData,
                final List<AmplitudeData> kickOffCounts,
                final List<AmplitudeData> rawSound,
                final List<DateTime> lightOutTimes,
                final Optional<DateTime> firstWaveTimeOptional){
        if(firstWaveTimeOptional.isPresent()) {
            LOGGER.debug("--------> Wave {}, millis {}", firstWaveTimeOptional.get(), firstWaveTimeOptional.get().getMillis());
        }

        this.motionScoreAlgorithmDefault = MotionScoreAlgorithm.createDefault(rawData,
                lightOutTimes,
                firstWaveTimeOptional);

        final List<AmplitudeData> noDuplicates = DataUtils.makePositive(DataUtils.dedupe(rawData));
        this.rawAmpMean = NumericalUtils.mean(noDuplicates);
        final List<AmplitudeData> noDuplicateKickOffCounts = DataUtils.dedupe(kickOffCounts);
        this.rawKickOffMean = NumericalUtils.mean(noDuplicateKickOffCounts);

        List<AmplitudeData> dataWithGapFilled = DataUtils.fillMissingValues(noDuplicates, DateTimeConstants.MILLIS_PER_MINUTE);
        this.alignedAmplitude = Lists.newArrayList(dataWithGapFilled);
        List<AmplitudeData> alignedKickOffs = DataUtils.fillMissingValues(noDuplicateKickOffCounts, DateTimeConstants.MILLIS_PER_MINUTE);
        if(insertEmpty) {
            final int insertLengthMin = 20;
            dataWithGapFilled = DataUtils.insertEmptyData(dataWithGapFilled, insertLengthMin, 1);
            alignedKickOffs = DataUtils.insertEmptyData(alignedKickOffs, insertLengthMin, 1);
        }

        this.motionCluster = MotionCluster.create(dataWithGapFilled, rawAmpMean, alignedKickOffs, rawKickOffMean, removeNoise);
        final List<Segment> motionSegments = MotionCluster.toSegments(this.motionCluster.getCopyOfClusters());
        final List<Segment> lightSegments = timeDeltaSegments(lightOutTimes,
                20 * DateTimeConstants.MILLIS_PER_MINUTE,
                rawData.get(0).offsetMillis);
        final List<Segment> waveSegments = firstWaveTimeOptional.isPresent() ?
                timeDeltaSegments(Lists.newArrayList(firstWaveTimeOptional.get()),
                5 * DateTimeConstants.MILLIS_PER_MINUTE,
                rawData.get(0).offsetMillis) : Collections.EMPTY_LIST;
        final Optional<Segment> inBedSegment = SleepPeriod.getSleepPeriod(dataWithGapFilled, motionSegments);

        this.sleepPeriod = SleepPeriod.createFromSegment(inBedSegment.get());
        this.sleepPeriod.addVotingSegments(motionSegments);
        this.sleepPeriod.addVotingSegments(SoundCluster.getClusters(rawSound));
        this.sleepPeriod.addVotingSegments(lightSegments);
        this.sleepPeriod.addVotingSegments(waveSegments);

        LOGGER.debug("data start from {} to {}", new DateTime(this.sleepPeriod.getStartTimestamp(), DateTimeZone.forOffsetMillis(this.sleepPeriod.getOffsetMillis())),
                new DateTime(this.sleepPeriod.getEndTimestamp(), DateTimeZone.forOffsetMillis(this.sleepPeriod.getOffsetMillis())));

        final Map<MotionFeatures.FeatureType, List<AmplitudeData>> motionFeatures = MotionFeatures.generateTimestampAlignedFeatures(dataWithGapFilled,
                MotionFeatures.MOTION_AGGREGATE_WINDOW_IN_MINUTES,
                MotionFeatures.WAKEUP_FEATURE_AGGREGATE_WINDOW_IN_MINUTES,
                false);
        final Map<MotionFeatures.FeatureType, List<AmplitudeData>> aggregatedFeatures = MotionFeatures.aggregateData(motionFeatures, MotionFeatures.MOTION_AGGREGATE_WINDOW_IN_MINUTES);
        LOGGER.debug("smoothed data size {}", aggregatedFeatures.get(MotionFeatures.FeatureType.MAX_AMPLITUDE).size());
        final Map<MotionFeatures.FeatureType, List<AmplitudeData>> filtered = removeNoise ?
                MotionCluster.petFiltering(motionCluster.getCopyOfClusters(),
                aggregatedFeatures,
                motionCluster.getDensityThreshold(), rawAmpMean) :
                aggregatedFeatures;
        LOGGER.debug("sleep period {}, {}",
                new DateTime(sleepPeriod.getStartTimestamp(), DateTimeZone.forOffsetMillis(sleepPeriod.getOffsetMillis())),
                new DateTime(sleepPeriod.getEndTimestamp(), DateTimeZone.forOffsetMillis(sleepPeriod.getOffsetMillis())));

        if(this.capScoreOutOfPeriod) {
            final Map<MotionFeatures.FeatureType, List<AmplitudeData>> capFeatures = capFeaturesBySleepPeriod(filtered, sleepPeriod);
            this.aggregatedFeatures = capFeatures;
        }else{
            this.aggregatedFeatures = filtered;
        }
    }




    public final List<Segment> getAwakes(final long fallAsleepMillis, final long wakeUpMillis, final boolean debug){
        final List<VotingSegment> allAwakesPeriods = this.sleepPeriod.getAwakePeriods(debug);
        final List<VotingSegment> awakesInTheRange = new ArrayList<>();

        for(final VotingSegment segment:allAwakesPeriods){
            if(fallAsleepMillis <= segment.getStartTimestamp() && wakeUpMillis >= segment.getEndTimestamp()){
                awakesInTheRange.add(segment);
            }
        }
        final List<VotingSegment> smoothedAwakes = smoothAwakes(awakesInTheRange, MotionCluster.toSegments(this.motionCluster.getCopyOfClusters()));
        final List<Segment> results = new ArrayList<>();
        for(final VotingSegment votingSegment:smoothedAwakes){
            results.add(votingSegment);
        }
        return results;
    }


    private List<VotingSegment> getAwakesInTimeSpanMillis(final List<VotingSegment> awakes, final long startMillis, final long endMillis){
        final List<VotingSegment> result = new ArrayList<>();
        for(final VotingSegment awake:awakes){
            if(awake.getStartTimestamp() >= startMillis && awake.getEndTimestamp() <= endMillis){
                result.add(awake);
            }
        }

        return result;
    }

    private List<VotingSegment> filterAwakeFragments(final List<VotingSegment> awakesInMotionCluster){
        final List<VotingSegment> result = new ArrayList<>();
        if(awakesInMotionCluster.size() < 2){
            return awakesInMotionCluster;
        }

        for(final VotingSegment awake:awakesInMotionCluster){
            if(awake.getDuration() < 10 * DateTimeConstants.MILLIS_PER_MINUTE){
                continue;
            }
            result.add(awake);
        }
        return result;
    }

    /*
    * Smooth out the intermediate awake fragments caused by noisy sensor readings
    * Idea: if multiple awakes is in the same motion cluster, filter out those less than
    * 20 minutes, less is more.
     */
    private List<VotingSegment> smoothAwakes(final List<VotingSegment> awakes, final List<Segment> motionClusters){
        final List<VotingSegment> smoothedAwakes = new ArrayList<>();
        for(final Segment currentCluster:motionClusters){
            final List<VotingSegment> awakesInMotionCluster = getAwakesInTimeSpanMillis(awakes,
                    currentCluster.getStartTimestamp(),
                    currentCluster.getEndTimestamp());
            smoothedAwakes.addAll(filterAwakeFragments(awakesInMotionCluster));

        }
        return smoothedAwakes;
    }


    private Map<MotionFeatures.FeatureType, List<AmplitudeData>> capFeaturesBySleepPeriod(final Map<MotionFeatures.FeatureType, List<AmplitudeData>> features,
                                                                                          final Segment sleepPeriod){
        final Set<MotionFeatures.FeatureType> featureTypes = features.keySet();
        final Map<MotionFeatures.FeatureType, List<AmplitudeData>> result = new HashMap<>();
        final int preserveEdgeMillis = 15 * DateTimeConstants.MILLIS_PER_MINUTE;

        for(final MotionFeatures.FeatureType featureType:featureTypes){
            result.put(featureType, new ArrayList<AmplitudeData>());

            if(featureType != MotionFeatures.FeatureType.DENSITY_DROP_BACKTRACK_MAX_AMPLITUDE &&
                    featureType != MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE){
                result.put(featureType, Lists.newArrayList(features.get(featureType)));
            }

            final List<AmplitudeData> originalFeature = features.get(featureType);

            for(int i = 0; i < originalFeature.size(); i++){
                final AmplitudeData item = originalFeature.get(i);
                final long timestamp = item.timestamp;
                if(timestamp >= sleepPeriod.getStartTimestamp() - preserveEdgeMillis &&
                        timestamp <= sleepPeriod.getEndTimestamp() + preserveEdgeMillis){
                    result.get(featureType).add(item);
                    continue;
                }

                result.get(featureType).add(new AmplitudeData(timestamp, 0d, item.offsetMillis));
            }
        }
        return result;
    }

    private List<Segment> timeDeltaSegments(final List<DateTime> dateTimes, final int deltaMillis, final int offsetMillis){
        final List<Segment> segments = new ArrayList<>();
        for(final DateTime dateTime:dateTimes){
            segments.add(new Segment(dateTime.getMillis() - deltaMillis, dateTime.getMillis() + deltaMillis, offsetMillis));
        }
        return segments;
    }

    public SleepEvents<Segment> getResult(final boolean debug){
        if(debug){
            LOGGER.debug("+++++++++++++ amp mean {}, kickoff mean {}", this.rawAmpMean, this.rawKickOffMean);
            MotionCluster.printClusters(this.motionCluster.getCopyOfClusters());
        }
        final SleepEvents<Segment> defaultEvents = this.motionScoreAlgorithmDefault.getSleepEvents(debug);

        final SleepEvents<Segment> events = aggregate(defaultEvents);

        LOGGER.debug("IN_BED: {}",
                new DateTime(events.goToBed.getStartTimestamp(),
                        DateTimeZone.forOffsetMillis(events.goToBed.getOffsetMillis())));
        LOGGER.debug("SLEEP: {}",
                new DateTime(events.fallAsleep.getStartTimestamp(),
                        DateTimeZone.forOffsetMillis(events.fallAsleep.getOffsetMillis())));
        LOGGER.debug("WAKE_UP: {}",
                new DateTime(events.wakeUp.getStartTimestamp(),
                        DateTimeZone.forOffsetMillis(events.wakeUp.getOffsetMillis())));
        LOGGER.debug("OUT_BED: {}",
                new DateTime(events.outOfBed.getStartTimestamp(),
                        DateTimeZone.forOffsetMillis(events.outOfBed.getOffsetMillis())));


        return events;
    }

    private static boolean isEmptyBounds(final Pair<Integer, Integer> bounds){
        if(bounds.getFirst() > -1 && bounds.getSecond() > -1){
            return false;
        }
        return true;
    }

    private SleepEvents<Segment> aggregate(final SleepEvents<Segment> defaultEvents){

        //final long wakeUpSearchTime = Math.min(defaultEvents.wakeUp.getStartTimestamp(), sleepEvents.fallAsleep.getStartTimestamp());

        final List<ClusterAmplitudeData> clusterCopy = this.motionCluster.getCopyOfClusters();

        Segment inBed = defaultEvents.goToBed;
        Segment sleep = defaultEvents.fallAsleep;

        final Pair<Long, Long> sleepTimesMillis = safeGuardPickSleep(this.sleepPeriod,
                clusterCopy,
                this.getAggregatedFeatures(),
                defaultEvents.fallAsleep.getStartTimestamp());
        long sleepTimeMillis = findNearestDataTime(this.alignedAmplitude, sleepTimesMillis.getSecond());
        long inBedTimeMillis = findNearestDataTime(this.alignedAmplitude, sleepTimesMillis.getFirst());
        inBed = new Segment(inBedTimeMillis, inBedTimeMillis + DateTimeConstants.MILLIS_PER_MINUTE, defaultEvents.goToBed.getOffsetMillis());
        sleep = new Segment(sleepTimeMillis, sleepTimeMillis + DateTimeConstants.MILLIS_PER_MINUTE, defaultEvents.fallAsleep.getOffsetMillis());
        if(inBed.getStartTimestamp() > sleep.getStartTimestamp()){
            inBed = new Segment(sleep.getStartTimestamp() - 10 * DateTimeConstants.MILLIS_PER_MINUTE,
                    sleep.getStartTimestamp() - 9 * DateTimeConstants.MILLIS_PER_MINUTE,
                    sleep.getOffsetMillis());
        }


        Segment wakeUp = defaultEvents.wakeUp;
        Segment outBed = defaultEvents.outOfBed;

        Pair<Long, Long> wakeUpTimesMillis = safeGuardPickWakeUp(clusterCopy,
                sleepPeriod,
                getAggregatedFeatures(),
                defaultEvents.wakeUp.getStartTimestamp());
        wakeUpTimesMillis = votingSafeGuardPickWakeUp(this.sleepPeriod.getAwakePeriods(false),
                clusterCopy,
                sleepPeriod,
                getAggregatedFeatures(),
                wakeUpTimesMillis,
                defaultEvents.wakeUp.getStartTimestamp());

        long wakeUpMillis = findNearestDataTime(this.alignedAmplitude, wakeUpTimesMillis.getFirst());
        long outBedMillis = findNearestDataTime(this.alignedAmplitude, wakeUpTimesMillis.getSecond());
        wakeUp = new Segment(wakeUpMillis, wakeUpMillis + DateTimeConstants.MILLIS_PER_MINUTE, defaultEvents.wakeUp.getOffsetMillis());
        outBed = new Segment(outBedMillis, outBedMillis + DateTimeConstants.MILLIS_PER_MINUTE, defaultEvents.outOfBed.getOffsetMillis());


        return SleepEvents.create(inBed, sleep, wakeUp, outBed);
    }

    private long findNearestDataTime(final List<AmplitudeData> data, final long targetMillis){
        int minDiff = Integer.MAX_VALUE;
        long time = 0;
        for(int i = 0; i < data.size(); i++){
            if(data.get(i).amplitude == 0){
                continue;
            }

            final int diff = (int) Math.abs(data.get(i).timestamp - targetMillis);
            if(diff < minDiff){
                minDiff = diff;
                time = data.get(i).timestamp;
            }
        }

        if(minDiff == Integer.MAX_VALUE){
            return targetMillis;
        }

        return time;
    }


    private static Pair<Long, Long> predictionBoundsMillis(final long wakeUpMillisPredicted, final Optional<Segment> predictionSegment){
        if(!predictionSegment.isPresent()){
            return new Pair<>(wakeUpMillisPredicted, wakeUpMillisPredicted + 10 * DateTimeConstants.MILLIS_PER_MINUTE);
        }
        return new Pair<>(wakeUpMillisPredicted, predictionSegment.get().getEndTimestamp());
    }

    private static Optional<VotingSegment> getNextAwakeInSleepPeriod(final List<VotingSegment> awakesUnfiltered,
                                                                     final SleepPeriod sleepPeriod,
                                                                     final long wakeUpSafeGuarded){
        for(final VotingSegment votingSegment:awakesUnfiltered){
            if(votingSegment.getStartTimestamp() > sleepPeriod.getEndTimestamp()){
                continue;
            }

            if(votingSegment.getDuration() < 20 * DateTimeConstants.MILLIS_PER_MINUTE){
                continue;
            }

            if(votingSegment.getStartTimestamp() >= wakeUpSafeGuarded ||
                    (votingSegment.getStartTimestamp() < wakeUpSafeGuarded && votingSegment.getEndTimestamp() >= wakeUpSafeGuarded)){
                return Optional.of(votingSegment);
            }
        }

        return Optional.absent();
    }

    protected static Optional<Segment> getAwakeCluster(final List<Segment> clusters, final VotingSegment awake){
        for(final Segment cluster:clusters){
            if(cluster.getStartTimestamp() >= awake.getStartTimestamp() && cluster.getEndTimestamp() <= awake.getEndTimestamp()){
                return Optional.of(cluster);
            }
        }
        return Optional.absent();
    }

    protected static Pair<Long, Long> votingSafeGuardPickWakeUp(final List<VotingSegment> awakesUnfiltered,
                                                                final List<ClusterAmplitudeData> clusterAmplitudeData,
                                                                final SleepPeriod sleepPeriod,
                                                                final Map<MotionFeatures.FeatureType, List<AmplitudeData>> featuresNotCapped,
                                                                final Pair<Long, Long> wakeUpTimesSafeGuarded,
                                                                final long wakeUpMillisPredicted){

        final List<Segment> clusters = MotionCluster.toSegments(clusterAmplitudeData);
        if(wakeUpMillisPredicted < wakeUpTimesSafeGuarded.getFirst()){
            return wakeUpTimesSafeGuarded;
        }

        final Optional<VotingSegment> nextAwakeInSleepPeriod = getNextAwakeInSleepPeriod(awakesUnfiltered, sleepPeriod, wakeUpTimesSafeGuarded.getFirst());
        if(!nextAwakeInSleepPeriod.isPresent()){
            return wakeUpTimesSafeGuarded;
        }

        final Optional<Segment> awakeCluster = getAwakeCluster(clusters, nextAwakeInSleepPeriod.get());
        if(!awakeCluster.isPresent()){
            return wakeUpTimesSafeGuarded;
        }

        final Optional<Segment> wakeUpCluster = getClusterByTimeMillis(clusters, wakeUpTimesSafeGuarded.getFirst(), 0, 0);
        if(!wakeUpCluster.isPresent()){
            return wakeUpTimesSafeGuarded;
        }

        final Optional<AmplitudeData> maxScoreInAwake = getMaxScore(featuresNotCapped,
                MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                awakeCluster.get().getStartTimestamp(),
                awakeCluster.get().getEndTimestamp());
        final Optional<AmplitudeData> maxScoreWakeUp = getMaxScore(featuresNotCapped,
                MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                wakeUpCluster.get().getStartTimestamp(),
                wakeUpCluster.get().getEndTimestamp());
        if(maxScoreInAwake.isPresent() && maxScoreWakeUp.isPresent()){
            if(maxScoreInAwake.get().amplitude * 5 < maxScoreWakeUp.get().amplitude){
                // The max score is not in that awake
                return wakeUpTimesSafeGuarded;
            }
        }

        if(nextAwakeInSleepPeriod.get().getStartTimestamp() - wakeUpTimesSafeGuarded.getFirst() < 15 * DateTimeConstants.MILLIS_PER_MINUTE){
            LOGGER.debug("HAPPY USER3: Last wake up close to safeguarded result, last wake up {} - {}",
                    new DateTime(nextAwakeInSleepPeriod.get().getStartTimestamp(),
                            DateTimeZone.forOffsetMillis(nextAwakeInSleepPeriod.get().getOffsetMillis())),
                    new DateTime(nextAwakeInSleepPeriod.get().getEndTimestamp(),
                            DateTimeZone.forOffsetMillis(nextAwakeInSleepPeriod.get().getOffsetMillis())));
            return wakeUpTimesSafeGuarded;
        }
        final Optional<AmplitudeData> maxScore = getMaxScore(featuresNotCapped,
                MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                awakeCluster.get().getStartTimestamp(),
                awakeCluster.get().getEndTimestamp());

        LOGGER.debug("Wrong safeguarding: Last wake up far away from safeguarded result, last wake up {} - {}",
                new DateTime(nextAwakeInSleepPeriod.get().getStartTimestamp(),
                        DateTimeZone.forOffsetMillis(nextAwakeInSleepPeriod.get().getOffsetMillis())),
                new DateTime(nextAwakeInSleepPeriod.get().getEndTimestamp(),
                        DateTimeZone.forOffsetMillis(nextAwakeInSleepPeriod.get().getOffsetMillis())));

        if(!maxScore.isPresent()){
            return new Pair<>(awakeCluster.get().getStartTimestamp(), awakeCluster.get().getEndTimestamp());
        }
        return new Pair<>(maxScore.get().timestamp,
                awakeCluster.get().getEndTimestamp());
    }

    protected static Pair<Long, Long> safeGuardPickWakeUp(final List<ClusterAmplitudeData> clusters,
                                                          final SleepPeriod sleepPeriod,
                                                          final Map<MotionFeatures.FeatureType, List<AmplitudeData>> featuresNotCapped,
                                                          final long wakeUpMillisPredicted){

        final List<Segment> clusterSegments = MotionCluster.toSegments(clusters);

        final Segment lastSegment = clusterSegments.get(clusterSegments.size() - 1);
        final Segment lastSegmentInSleepPeriod = getLastClusterInSleep(clusterSegments, sleepPeriod);

        final Optional<Segment> predictionSegment = getClusterByTimeMillis(clusterSegments, wakeUpMillisPredicted,
                10 * DateTimeConstants.MILLIS_PER_MINUTE,
                10 * DateTimeConstants.MILLIS_PER_MINUTE);

        // predict in last segment of sleep period.
        if(wakeUpMillisPredicted >= lastSegmentInSleepPeriod.getStartTimestamp() &&
                wakeUpMillisPredicted <= lastSegmentInSleepPeriod.getEndTimestamp() + 15 * DateTimeConstants.MILLIS_PER_MINUTE){
            LOGGER.debug("HAPPY USER, wake up cluster is last cluster.");
            return new Pair<>(wakeUpMillisPredicted, lastSegmentInSleepPeriod.getEndTimestamp());
        }

        // predict < last cluster
        if(wakeUpMillisPredicted < lastSegmentInSleepPeriod.getStartTimestamp()) {
            // predict << last segment of sleep period, this user toss and turn a lot during sleep
            // The prediction can landed on one of the heavy toss-and-turn cluster.
            if (lastSegmentInSleepPeriod.getStartTimestamp() - wakeUpMillisPredicted > 40 * DateTimeConstants.MILLIS_PER_MINUTE) {
                // prediction way too off, fallback to the cluster with max score.
                LOGGER.debug("Predicted too far way from end, predicted {}",
                        new DateTime(wakeUpMillisPredicted, DateTimeZone.forOffsetMillis(lastSegment.getOffsetMillis())));
                final Optional<AmplitudeData> maxWakeUpScoreOptional = getMaxScore(featuresNotCapped,
                        MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                        lastSegmentInSleepPeriod.getEndTimestamp() - 120 * DateTimeConstants.MILLIS_PER_MINUTE,
                        lastSegmentInSleepPeriod.getEndTimestamp() + 20 * DateTimeConstants.MILLIS_PER_MINUTE);

                final Optional<AmplitudeData> maxSleepScoreOptional = getMaxScore(featuresNotCapped,
                        MotionFeatures.FeatureType.DENSITY_DROP_BACKTRACK_MAX_AMPLITUDE,
                        wakeUpMillisPredicted,
                        lastSegmentInSleepPeriod.getEndTimestamp());

                if(maxSleepScoreOptional.isPresent()){  // deal with edge case, noo significant motion.
                    // decide if we should safe guard this result to the max score end
                    if(wakeUpMillisPredicted <= maxSleepScoreOptional.get().timestamp &&
                            maxSleepScoreOptional.get().timestamp < lastSegmentInSleepPeriod.getStartTimestamp()){
                        LOGGER.debug("Max drop between prediction and last segment detected, prediction is likely right.");
                        return predictionBoundsMillis(wakeUpMillisPredicted, predictionSegment);
                    }
                }

                // Deal with no obvious motion, edge case
                if (!maxWakeUpScoreOptional.isPresent()) {
                    return new Pair<>(lastSegmentInSleepPeriod.getStartTimestamp(), lastSegmentInSleepPeriod.getEndTimestamp());
                }

                final Pair<Integer, Integer> maxScoreCluster = MotionCluster.getClusterByTime(clusters, maxWakeUpScoreOptional.get().timestamp);
                // Deal with no obvious motion, edge case
                if(isEmptyBounds(maxScoreCluster)) {
                    return new Pair<>(maxWakeUpScoreOptional.get().timestamp, lastSegmentInSleepPeriod.getEndTimestamp());
                }

//                if(lastSegmentInSleepPeriod.getStartTimestamp() - maxWakeUpScoreOptional.get().timestamp > 40 * DateTimeConstants.MILLIS_PER_MINUTE){
//                    // cluster with max score still too far from end
//                    LOGGER.debug("Max score cluster still too far way to end, fallback to the last cluster.");
//                    return new Pair<>(lastSegmentInSleepPeriod.getEndTimestamp() - 10 * DateTimeConstants.MILLIS_PER_MINUTE,
//                            lastSegment.getEndTimestamp());
//                }
                return new Pair<>(maxWakeUpScoreOptional.get().timestamp, clusters.get(maxScoreCluster.getSecond()).timestamp);
            }else {
                // Prediction is nearby the last cluster, but still not the last
                // fallback to the moment has max wake up score.
                LOGGER.debug("OK USER: Predict not too far from end");
                final Optional<AmplitudeData> maxWakeUpScoreOptional = getMaxScore(featuresNotCapped,
                        MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                        wakeUpMillisPredicted - 20 * DateTimeConstants.MILLIS_PER_MINUTE,
                        lastSegmentInSleepPeriod.getEndTimestamp() + 20 * DateTimeConstants.MILLIS_PER_MINUTE);
                // No obvious motion, edge case
                if (!maxWakeUpScoreOptional.isPresent()) {
                    return new Pair<>(wakeUpMillisPredicted, wakeUpMillisPredicted + 10 * DateTimeConstants.MILLIS_PER_MINUTE);
                }

                final Pair<Integer, Integer> maxScoreCluster = MotionCluster.getClusterByTime(clusters, maxWakeUpScoreOptional.get().timestamp);
                // No obvious motion, edge case
                if(isEmptyBounds(maxScoreCluster)) {
                    return predictionBoundsMillis(wakeUpMillisPredicted, predictionSegment);
                }


                return new Pair<>(maxWakeUpScoreOptional.get().timestamp, clusters.get(maxScoreCluster.getSecond()).timestamp);
            }
        }

        // predict > last segment in sleep period, possible caused by maid or wrong sleep period detection
        if(lastSegment.getStartTimestamp() == lastSegmentInSleepPeriod.getStartTimestamp() &&
                lastSegment.getEndTimestamp() == lastSegmentInSleepPeriod.getEndTimestamp()){

            // No motion cluster after end of sleep period. error not caused by maid motion
            LOGGER.debug("-------------* No maid found, last motion cluster {} - {}",
                    new DateTime(lastSegment.getStartTimestamp(), DateTimeZone.forOffsetMillis(lastSegment.getOffsetMillis())),
                    new DateTime(lastSegment.getEndTimestamp(), DateTimeZone.forOffsetMillis(lastSegment.getOffsetMillis())));

            LOGGER.debug("Sleep period detection wrong.");
            final List<AmplitudeData> amps = featuresNotCapped.get(MotionFeatures.FeatureType.MAX_AMPLITUDE);
            final long lastMotionMillis = amps.get(amps.size() - 1).timestamp;
            if(lastMotionMillis - wakeUpMillisPredicted > DateTimeConstants.MILLIS_PER_HOUR){
                final Optional<AmplitudeData> maxWakeUpScoreOptional = getMaxScore(featuresNotCapped,
                        MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                        lastMotionMillis - 60 * DateTimeConstants.MILLIS_PER_MINUTE,
                        lastMotionMillis);
                if(maxWakeUpScoreOptional.isPresent()){
                    return new Pair<>(maxWakeUpScoreOptional.get().timestamp, lastMotionMillis);
                }
                // No obvious motion, edge case
                return new Pair<>(lastMotionMillis - 10 * DateTimeConstants.MILLIS_PER_MINUTE, lastMotionMillis);
            }
            return new Pair<>(wakeUpMillisPredicted, lastMotionMillis);

        }else {
            // Maid or partner motion found, maid might cause multiple motion clusters
            // last segment in sleep period < last segment && predict > last segment in sleep period
            LOGGER.debug("-------------* Maid found, last motion cluster in sleep period {} - {}",
                    new DateTime(lastSegmentInSleepPeriod.getStartTimestamp(), DateTimeZone.forOffsetMillis(lastSegmentInSleepPeriod.getOffsetMillis())),
                    new DateTime(lastSegmentInSleepPeriod.getEndTimestamp(), DateTimeZone.forOffsetMillis(lastSegmentInSleepPeriod.getOffsetMillis())));

            if(wakeUpMillisPredicted < lastSegment.getStartTimestamp() && wakeUpMillisPredicted > sleepPeriod.getEndTimestamp()){
                LOGGER.debug("Sleep period {} - {} wrong, might due to data quality issue, keep predicted result",
                        new DateTime(sleepPeriod.getStartTimestamp(), DateTimeZone.forOffsetMillis(sleepPeriod.getOffsetMillis())),
                        new DateTime(sleepPeriod.getEndTimestamp(), DateTimeZone.forOffsetMillis(sleepPeriod.getOffsetMillis())));
                return predictionBoundsMillis(wakeUpMillisPredicted, predictionSegment);
            }

            // Fallback to the last segment inside sleep period
            final Optional<AmplitudeData> maxWakeUpScoreOptional = getMaxScore(featuresNotCapped,
                    MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                    lastSegmentInSleepPeriod.getEndTimestamp() - 60 * DateTimeConstants.MILLIS_PER_MINUTE,
                    lastSegmentInSleepPeriod.getEndTimestamp());
            // No obvious motion, edge case
            if(!maxWakeUpScoreOptional.isPresent()){
                return new Pair<>(lastSegmentInSleepPeriod.getStartTimestamp(), lastSegmentInSleepPeriod.getEndTimestamp());
            }

            final Pair<Integer, Integer> maxScoreCluster = MotionCluster.getClusterByTime(clusters, maxWakeUpScoreOptional.get().timestamp);
            // No obvious motion, edge case
            if(isEmptyBounds(maxScoreCluster)) {
                return new Pair<>(maxWakeUpScoreOptional.get().timestamp, lastSegmentInSleepPeriod.getEndTimestamp());
            }
            return new Pair<>(maxWakeUpScoreOptional.get().timestamp, clusters.get(maxScoreCluster.getSecond()).timestamp);

        }

        //return new Pair<>(wakeUpMillisPredicted, lastSegmentInSleepPeriod.getEndTimestamp());

    }

    private static Optional<Segment> getClusterByTimeMillis(final List<Segment> clusterSegments,
                                                            final long millis,
                                                            final int deltaLeftMillis,
                                                            final int deltaRightMillis){
        for(final Segment cluster:clusterSegments){
            if(millis <= cluster.getEndTimestamp() + deltaRightMillis &&
                    millis >= cluster.getStartTimestamp() - deltaLeftMillis){
                return Optional.of(cluster);
            }
        }
        return Optional.absent();
    }

    private static Segment getFirstClusterInSleep(final List<Segment> clusterSegments, final SleepPeriod sleepPeriod){
        for(final Segment cluster:clusterSegments){
            if(cluster.getEndTimestamp() >= sleepPeriod.getStartTimestamp()){
                return cluster;
            }
        }

        return clusterSegments.get(0);
    }

    private static Segment getLastClusterInSleep(final List<Segment> clusterSegments, final SleepPeriod sleepPeriod){
        Segment last = clusterSegments.get(clusterSegments.size() - 1);
        for(final Segment segment:clusterSegments){
            if(segment.getStartTimestamp() <= sleepPeriod.getEndTimestamp() + 20 * DateTimeConstants.MILLIS_PER_MINUTE) {
                last = segment;
            }
        }

        return last;
    }

    private static boolean isPredictInFirstCluster(final Segment firstCluster, final long predictMillis){
        return firstCluster.getStartTimestamp() <= predictMillis &&
                firstCluster.getEndTimestamp() + 15 * DateTimeConstants.MILLIS_PER_MINUTE >= predictMillis;
    }

    private static boolean isClusterAgreeWithMaxScore(final Segment predictedSegment, final AmplitudeData maxScore){
        return maxScore.timestamp >= predictedSegment.getStartTimestamp() &&
                maxScore.timestamp <= predictedSegment.getEndTimestamp() + 15 * DateTimeConstants.MILLIS_PER_MINUTE;
    }

    private static Optional<AmplitudeData> getMaxWakeUpScoreInPredictionSegment(final Map<MotionFeatures.FeatureType, List<AmplitudeData>> features,
                                                                                final Optional<Segment> predictedSegmentOptional,
                                                                                final long originalSleepMillis){
        if(predictedSegmentOptional.isPresent()) {
            return getMaxScore(features,
                    MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                    predictedSegmentOptional.get().getStartTimestamp(),
                    predictedSegmentOptional.get().getEndTimestamp());
        }

        return getMaxScore(features,
                MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                originalSleepMillis - 20 * DateTimeConstants.MILLIS_PER_MINUTE,
                originalSleepMillis + 20 * DateTimeConstants.MILLIS_PER_MINUTE);

    }

    protected static Pair<Long, Long> safeGuardPickSleep(final SleepPeriod sleepPeriod,
                                                         final List<ClusterAmplitudeData> clusters,
                                                         final Map<MotionFeatures.FeatureType, List<AmplitudeData>> features,
                                                         final long originalSleepMillis){


        final List<Segment> clusterSegments = MotionCluster.toSegments(clusters);
        final Segment firstCluster = getFirstClusterInSleep(clusterSegments, sleepPeriod);
        LOGGER.debug("First motion cluster in sleep period {} - {}",
                new DateTime(firstCluster.getStartTimestamp(), DateTimeZone.forOffsetMillis(firstCluster.getOffsetMillis())),
                new DateTime(firstCluster.getEndTimestamp(), DateTimeZone.forOffsetMillis(firstCluster.getOffsetMillis())));

        final Optional<Segment> predictedSegmentOptional = getClusterByTimeMillis(clusterSegments, originalSleepMillis,
                // The delta is important because features are in 10 min's chunk
                // need to give a chance for mis-align
                0,
                15 * DateTimeConstants.MILLIS_PER_MINUTE);


        final Optional<AmplitudeData> predictedMaxScoreOptional = getMaxWakeUpScoreInPredictionSegment(features, predictedSegmentOptional, originalSleepMillis);

        final Optional<AmplitudeData> firstMaxScoreItemOptional = getMaxScore(features,
                MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                firstCluster.getStartTimestamp(),
                firstCluster.getEndTimestamp() + 20 * DateTimeConstants.MILLIS_PER_MINUTE);
        final Optional<AmplitudeData> maxScoreSinceSleep = getMaxScore(features,
                MotionFeatures.FeatureType.DENSITY_BACKWARD_AVERAGE_AMPLITUDE,
                sleepPeriod.getStartTimestamp(),
                originalSleepMillis + 20 * DateTimeConstants.MILLIS_PER_MINUTE);

        if(isPredictInFirstCluster(firstCluster, originalSleepMillis)){
            LOGGER.debug("HAPPY USER: Predicted sleep in first cluster. predicted sleep {}",
                    new DateTime(originalSleepMillis, DateTimeZone.forOffsetMillis(firstCluster.getOffsetMillis())));

            /*if(predictedMaxScoreOptional.isPresent() &&
                    predictedMaxScoreOptional.get().timestamp - originalSleepMillis > 40 * DateTimeConstants.MILLIS_PER_MINUTE){
                return new Pair<>(firstCluster.getStartTimestamp(), predictedMaxScoreOptional.get().timestamp);
            }*/
            return new Pair<>(firstCluster.getStartTimestamp(), originalSleepMillis);
        }


        if(maxScoreSinceSleep.isPresent() && predictedSegmentOptional.isPresent()){
            if(isClusterAgreeWithMaxScore(predictedSegmentOptional.get(), maxScoreSinceSleep.get())){
                LOGGER.debug("HAPPY USER2: Max score in the same predicted cluster. predicted sleep {}",
                        new DateTime(originalSleepMillis, DateTimeZone.forOffsetMillis(predictedSegmentOptional.get().getOffsetMillis())));
                return new Pair<>(predictedSegmentOptional.get().getStartTimestamp(),
                        Math.max(maxScoreSinceSleep.get().timestamp, originalSleepMillis));
            }
        }




        if(predictedMaxScoreOptional.isPresent() && firstMaxScoreItemOptional.isPresent()){
            if(predictedMaxScoreOptional.get().amplitude / 5d > firstMaxScoreItemOptional.get().amplitude){
                final Optional<Segment> maxScoreCluster = getClusterByTimeMillis(clusterSegments,
                        predictedMaxScoreOptional.get().timestamp,
                        0, 0);
                if(maxScoreCluster.isPresent()){
                    final Segment cluster = maxScoreCluster.get();
                    LOGGER.debug("NOISY SLEEP PERIOD: predicted sleep in cluster {} - {}",
                            new DateTime(cluster.getStartTimestamp(), DateTimeZone.forOffsetMillis(cluster.getOffsetMillis())),
                            new DateTime(cluster.getEndTimestamp(), DateTimeZone.forOffsetMillis(cluster.getOffsetMillis())));

                    return new Pair<>(cluster.getStartTimestamp(), originalSleepMillis);
                }

            }
        }

        LOGGER.debug("COMPLETELY OFF: Move prediction to first cluster {} - {}",
                new DateTime(firstCluster.getStartTimestamp(), DateTimeZone.forOffsetMillis(firstCluster.getOffsetMillis())),
                new DateTime(firstCluster.getEndTimestamp(), DateTimeZone.forOffsetMillis(firstCluster.getOffsetMillis())));
        final long inBedMillis = firstCluster.getStartTimestamp();

        LOGGER.debug("first {}, predicted {}",
                new DateTime(firstMaxScoreItemOptional.get().timestamp, DateTimeZone.forOffsetMillis(firstMaxScoreItemOptional.get().offsetMillis)),
                new DateTime(originalSleepMillis, DateTimeZone.forOffsetMillis(firstMaxScoreItemOptional.get().offsetMillis)));

        final Optional<AmplitudeData> maxDrop = getMaxScore(features,
                MotionFeatures.FeatureType.DENSITY_DROP_BACKTRACK_MAX_AMPLITUDE,
                firstCluster.getStartTimestamp() - 10 * DateTimeConstants.MILLIS_PER_MINUTE,
                firstCluster.getEndTimestamp() + 20 * DateTimeConstants.MILLIS_PER_MINUTE);
        if(!maxDrop.isPresent()){
            return new Pair<>(inBedMillis, inBedMillis + 10 * DateTimeConstants.MILLIS_PER_MINUTE);
        }

        return new Pair<>(inBedMillis, maxDrop.get().timestamp);
    }

    public Map<MotionFeatures.FeatureType, List<AmplitudeData>> getAggregatedFeatures(){
        return ImmutableMap.copyOf(this.aggregatedFeatures);
    }

    public static Optional<AmplitudeData> getMaxScore(final Map<MotionFeatures.FeatureType, List<AmplitudeData>> features,
                                                        final MotionFeatures.FeatureType featureType,
                                                      final long startSearchMillis, final long endSearchMillis){
        final List<AmplitudeData> feature = features.get(featureType);
        if(feature == null || feature.size() == 0){
            return Optional.absent();
        }

        Optional<AmplitudeData> maxScore = Optional.absent();
        for(final AmplitudeData datum:feature){
            if(datum.timestamp >= startSearchMillis && datum.timestamp <= endSearchMillis){
                if(!maxScore.isPresent()){
                    maxScore = Optional.of(datum);
                    continue;
                }

                if(maxScore.get().amplitude <= datum.amplitude){
                    maxScore = Optional.of(datum);
                }
            }
        }

        return maxScore;
    }

}
