package com.hello.suripu.algorithm.sleep.scores;

import com.google.common.collect.Ordering;
import com.hello.suripu.algorithm.core.ScoringFunction;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pangwu on 12/16/14.
 */
public class WakeUpTimeScoringFunction implements ScoringFunction<Long, Double> {

    private final double cutPercentage;
    public WakeUpTimeScoringFunction(final double cutPercentage){
        this.cutPercentage = cutPercentage;
    }
    @Override
    public Map<Long, Double> getPDF(final Collection<Long> data) {
        List<Long> sortedCopy = Ordering.natural().immutableSortedCopy(data);

        final LinkedHashMap<Long, Double> rankingPositions = new LinkedHashMap<>();
        final double cutBound = data.size() * this.cutPercentage;
        final int dataSize = data.size();
        for(int i = 0; i < sortedCopy.size(); i++){
            double score = 0;
            if(i >= cutBound){
                score = Double.valueOf(i - cutBound) / (dataSize - cutBound);
            }

            final Long value = sortedCopy.get(i);
            if(rankingPositions.containsKey(value)){
                continue;
            }
            rankingPositions.put(value, score);
        }
        return rankingPositions;
    }

    @Override
    public Double getScore(final Long data, final Map<Long, Double> pdf) {
        if(pdf.containsKey(data)){
            return pdf.get(data);
        }
        return 0d;
    }
}
