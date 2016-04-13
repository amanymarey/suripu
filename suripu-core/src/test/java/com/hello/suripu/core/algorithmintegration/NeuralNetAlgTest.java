package com.hello.suripu.core.algorithmintegration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hello.suripu.core.models.AllSensorSampleList;
import com.hello.suripu.core.models.TimelineFeedback;
import com.hello.suripu.core.models.TrackerMotion;
import com.hello.suripu.core.processors.OnlineHmmTest;
import com.hello.suripu.core.util.DateTimeUtil;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Created by benjo on 4/11/16.
 *  Misnomer, because we are just testing the ability to put sensor data for the neural net in the proper form
 *
 */
public class NeuralNetAlgTest extends NeuralNetAlgorithm {

    public NeuralNetAlgTest() {
        super(null);
    }



    @Test
    //spot sanity check, just make sure it produces a matrix without any exceptions
    public void testGetSensorData() throws Exception {
        DateTime date = DateTimeUtil.ymdStringToDateTime("2015-09-01");
        DateTime startTime = date.withHourOfDay(18);
        DateTime endTime = startTime.plusHours(16);


        AllSensorSampleList senseData = OnlineHmmTest.getTypicalDayOfSense(startTime,endTime,0);
        ImmutableList<TrackerMotion> pillData = OnlineHmmTest.getTypicalDayOfPill(startTime.minusHours(4),endTime.plusHours(4),0);
        final ImmutableList<TimelineFeedback> emptyFeedback = ImmutableList.copyOf(Lists.<TimelineFeedback>newArrayList());
        final OneDaysSensorData oneDaysSensorData = new OneDaysSensorData(senseData,pillData,pillData,emptyFeedback,pillData,pillData,date,startTime,endTime,endTime,0);

        final double[][] neuralNetInput = getSensorData(oneDaysSensorData);

        TestCase.assertEquals(SensorIndices.MAX_NUM_INDICES,neuralNetInput.length);
        TestCase.assertEquals(961,neuralNetInput[0].length);


    }
}