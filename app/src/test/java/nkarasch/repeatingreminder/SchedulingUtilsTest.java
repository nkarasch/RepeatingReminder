package nkarasch.repeatingreminder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Calendar;

import nkarasch.repeatingreminder.utils.SchedulingUtils;

import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.SATURDAY;

@RunWith(MockitoJUnitRunner.class)
public class SchedulingUtilsTest {

    @Test
    public void getNextAlertTimeTest() {
        Calendar calendar = Calendar.getInstance();
        Alert alert = new Alert();

        alert.setSchedule(true);
        alert.setStartTime(4, 5); //
        alert.setEndTime(10, 15);
        alert.setFrequency(600);
        alert.setDayEnabled(false, 0); //sunday
        alert.setDayEnabled(false, 1); //monday
        alert.setDayEnabled(true, 2); //tuesday
        alert.setDayEnabled(true, 3); //wednesday
        alert.setDayEnabled(false, 4); //thursday
        alert.setDayEnabled(false, 5); //friday
        alert.setDayEnabled(true, 6); //saturday

        //Thu Oct 13 07:30:45 EDT 2016
        calendar.set(2016, 9, 13, 7, 30, 45);

        Calendar saturdayStart = SchedulingUtils.getNextAlertTime(calendar, alert);
        assert (saturdayStart.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY);
        assert (saturdayStart.get(Calendar.HOUR_OF_DAY) == 4);
        assert (saturdayStart.get(Calendar.MINUTE) == 5);
        assert (saturdayStart.get(Calendar.SECOND) == 0);

        saturdayStart = SchedulingUtils.getNextAlertTime(saturdayStart, alert);
        assert (saturdayStart.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY);
        assert (saturdayStart.get(Calendar.HOUR_OF_DAY) == 4);
        assert (saturdayStart.get(Calendar.MINUTE) == 15);
        assert (saturdayStart.get(Calendar.SECOND) == 0);

        alert.setDayEnabled(false, 6); //saturday
        Calendar tuesdayStart = SchedulingUtils.getNextAlertTime(saturdayStart, alert);
        assert (tuesdayStart.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY);
        assert (tuesdayStart.get(Calendar.HOUR_OF_DAY) == 4);
        assert (tuesdayStart.get(Calendar.MINUTE) == 5);
        assert (tuesdayStart.get(Calendar.SECOND) == 0);

        //Thu Oct 13 10:04:45 EDT 2016
        calendar.set(2016, 9, 13, 10, 4, 45);
        alert.setDayEnabled(true, 4); //thursday
        Calendar endOfDay = SchedulingUtils.getNextAlertTime(calendar, alert);
        assert (endOfDay.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY);
        assert (endOfDay.get(Calendar.HOUR_OF_DAY) == 10);
        assert (endOfDay.get(Calendar.MINUTE) == 14);
        assert (endOfDay.get(Calendar.SECOND) == 45);

        SchedulingUtils.getNextAlertTime(endOfDay, alert);
        assert (endOfDay.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY);
        assert (endOfDay.get(Calendar.HOUR_OF_DAY) == 4);
        assert (endOfDay.get(Calendar.MINUTE) == 5);
        assert (endOfDay.get(Calendar.SECOND) == 0);

        SchedulingUtils.getNextAlertTime(endOfDay, alert);
        assert (endOfDay.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY);
        assert (endOfDay.get(Calendar.HOUR_OF_DAY) == 4);
        assert (endOfDay.get(Calendar.MINUTE) == 15);
        assert (endOfDay.get(Calendar.SECOND) == 0);

    }

    @Test
    public void setCalendarToStartDayTest() {
        //Thu Oct 13 07:30:45 EDT 2016
        Calendar calendar = Calendar.getInstance();

        Alert alert = new Alert();
        alert.setDayEnabled(false, 0); //sunday
        alert.setDayEnabled(false, 1); //monday
        alert.setDayEnabled(true, 2); //tuesday
        alert.setDayEnabled(true, 3); //wednesday
        alert.setDayEnabled(false, 4); //thursday
        alert.setDayEnabled(false, 5); //friday
        alert.setDayEnabled(true, 6); //saturday
        calendar.set(2016, 9, 13, 7, 30, 45);
        assert (SchedulingUtils.setCalendarToStartDay(calendar, alert).get(DAY_OF_WEEK) == SATURDAY);

        alert.setDayEnabled(false, 0); //sunday
        alert.setDayEnabled(false, 1); //monday
        alert.setDayEnabled(false, 2); //tuesday
        alert.setDayEnabled(false, 3); //wednesday
        alert.setDayEnabled(true, 4); //thursday
        alert.setDayEnabled(false, 5); //friday
        alert.setDayEnabled(false, 6); //saturday

        calendar.set(2016, 9, 13, 7, 30, 45);
        assert (SchedulingUtils.setCalendarToStartDay(calendar, alert).get(DAY_OF_WEEK) == Calendar.THURSDAY);

        alert.setDayEnabled(true, 0); //sunday
        alert.setDayEnabled(false, 1); //monday
        alert.setDayEnabled(false, 2); //tuesday
        alert.setDayEnabled(true, 3); //wednesday
        alert.setDayEnabled(true, 4); //thursday
        alert.setDayEnabled(false, 5); //friday
        alert.setDayEnabled(false, 6); //saturday

        calendar.set(2016, 9, 13, 7, 30, 45);
        assert (SchedulingUtils.setCalendarToStartDay(calendar, alert).get(DAY_OF_WEEK) == Calendar.SUNDAY);
    }

    @Test
    public void getTimePositionTest() throws Exception {

        //Thu Oct 13 07:30:45 EDT 2016
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, 9, 13, 7, 30, 45);

        Alert alert = new Alert();

        // BETWEEN
        // 01:18:00AM - 09:32:00AM
        alert.setStartTime(1, 18);
        alert.setEndTime(9, 32);
        assert (SchedulingUtils.getTimePosition(calendar, alert) == SchedulingUtils.DAY_POSITION.BETWEEN);

        // BEFORE START
        // 08:18:00AM - 09:32:00AM
        alert.setStartTime(8, 18);
        alert.setEndTime(9, 32);
        assert (SchedulingUtils.getTimePosition(calendar, alert) == SchedulingUtils.DAY_POSITION.BEFORE_START);

        // AFTER END
        // 01:18:00AM - 03:32:00AM
        alert.setStartTime(1, 18);
        alert.setEndTime(3, 32);
        assert (SchedulingUtils.getTimePosition(calendar, alert) == SchedulingUtils.DAY_POSITION.AFTER_END);
    }

    /*
    @Test
    public void printNext(){
        Calendar calendar = Calendar.getInstance();
        Alert alert = new Alert();

        alert.setSchedule(true);
        alert.setStartTime(4, 5); //
        alert.setEndTime(10, 15);
        alert.setFrequency(601);
        alert.setDayEnabled(false, 0); //sunday
        alert.setDayEnabled(false, 1); //monday
        alert.setDayEnabled(true, 2); //tuesday
        alert.setDayEnabled(true, 3); //wednesday
        alert.setDayEnabled(false, 4); //thursday
        alert.setDayEnabled(false, 5); //friday
        alert.setDayEnabled(true, 6); //saturday

        //Thu Oct 13 07:30:45 EDT 2016
        calendar.set(2016, 9, 13, 7, 30, 45);

        int day = Calendar.SATURDAY;
        int hour = 4;
        int minute = 5;
        int second = 0;
        boolean saturdayJumped = false;
        boolean tuesdayJumped = false;

        for(int i = 0; i < 10000; ++i){
            SchedulingUtils.getNextAlertTime(calendar, alert);

            if(!saturdayJumped && (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)){
             //   day = Calendar.TUESDAY;
              //  saturdayJumped = true;
               // hour = 4;
              //  minute = 5;
            }
           // assert (calendar.get(Calendar.DAY_OF_WEEK) == day);
           // assert (calendar.get(Calendar.HOUR_OF_DAY) == hour);
           // assert (calendar.get(Calendar.MINUTE) == minute);
           // assert (calendar.get(Calendar.SECOND) == second);
           // minute += 10;
           // if(minute > 60){
            //    hour++;
           //     minute -= 60;
           // }

            System.out.println(calendar.getTime());
        }
    }*/
}