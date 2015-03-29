package nkarasch.repeatingreminder.scheduling;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Calendar;

import nkarasch.repeatingreminder.Alert;

import static org.junit.Assert.*;

public class AlertBroadcastReceiverTest {

    private AlertBroadcastReceiver receiver = new AlertBroadcastReceiver(); // the class under test

    @Test
    public void testGetNextTime() throws Exception {
        Method method = receiver.getClass().getDeclaredMethod("getNextTime", java.util.Calendar.class, nkarasch.repeatingreminder.Alert.class);
        method.setAccessible(true);

        Alert alert = createAlert(true, new boolean[]{false, true, false, true, false, true, false}, 4, 20, 15, 3);
        Calendar calendar = createCalendar(1, 1, 1, 25);

        //day before
        Calendar result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(2, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, result.get(Calendar.MINUTE));
        assertEquals(35, result.get(Calendar.SECOND));

        //earlier in day
        calendar = createCalendar(2, 1, 1, 0);
        result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(2, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, result.get(Calendar.MINUTE));
        assertEquals(10, result.get(Calendar.SECOND));

        //between start and end times
        calendar = createCalendar(2, 4, 30, 0);
        result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(2, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, result.get(Calendar.MINUTE));
        assertEquals(10, result.get(Calendar.SECOND));
    }

    @Test
    public void testGetDayToStart() throws Exception {

        Method method = receiver.getClass().getDeclaredMethod("getDayToStart", java.util.Calendar.class, nkarasch.repeatingreminder.Alert.class);
        method.setAccessible(true);

        Alert alert = createAlert(true, new boolean[]{false, true, false, true, false, true, false}, 4, 20, 15, 3);
        Calendar calendar = createCalendar(4, 18, 6, 0);

        /**
         * Alternating days
         */
        //starting wednesday
        Calendar result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(6, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, result.get(Calendar.MINUTE));

        //starting sunday
        calendar = createCalendar(1, 18, 6, 0);
        result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(2, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, result.get(Calendar.MINUTE));

        //starting monday
        calendar = createCalendar(2, 18, 6, 0);
        result = (Calendar) method.invoke(receiver, calendar, alert);

        assertEquals(4, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, result.get(Calendar.MINUTE));

        //starting friday
        calendar = createCalendar(6, 18, 6, 0);
        result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(2, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, result.get(Calendar.MINUTE));

        //starting saturday
        calendar = createCalendar(6, 18, 6, 0);
        result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(2, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, result.get(Calendar.MINUTE));

        /**
         * large span/edge cases
         */
        alert = createAlert(true, new boolean[]{false, false, false, false, false, true, false}, 4, 20, 15, 3);

        calendar = createCalendar(5, 18, 6, 0);
        result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(6, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, result.get(Calendar.MINUTE));

        calendar = createCalendar(6, 18, 6, 0);
        result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(6, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, result.get(Calendar.MINUTE));

        calendar = createCalendar(7, 18, 6, 0);
        result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(6, result.get(Calendar.DAY_OF_WEEK));
        assertEquals(4, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, result.get(Calendar.MINUTE));


        /**
         * no legal days
         */
        alert = createAlert(true, new boolean[]{false, false, false, false, false, false, false}, 4, 20, 15, 3);

        result = (Calendar) method.invoke(receiver, calendar, alert);
        assertEquals(result, null);
    }

    @Test
    public void testIsLegalTime() throws Exception {
        Method method = receiver.getClass().getDeclaredMethod("isLegalTime", java.util.Calendar.class, nkarasch.repeatingreminder.Alert.class);
        method.setAccessible(true);

        Alert alert = createAlert(true, null, 4, 20, 15, 3);

        Calendar calendar = createCalendar(1, 5, 5, 0);
        assertEquals(AlertBroadcastReceiver.DAY_POSITION.BETWEEN, method.invoke(receiver, calendar, alert));

        calendar = createCalendar(1, 4, 20, 0);
        assertEquals(AlertBroadcastReceiver.DAY_POSITION.BETWEEN, method.invoke(receiver, calendar, alert));

        calendar = createCalendar(1, 15, 3, 0);
        assertEquals(AlertBroadcastReceiver.DAY_POSITION.BETWEEN, method.invoke(receiver, calendar, alert));

        calendar = createCalendar(1, 3, 5, 0);
        assertEquals(AlertBroadcastReceiver.DAY_POSITION.BEFORE_START, method.invoke(receiver, calendar, alert));

        calendar = createCalendar(6, 15, 5, 0);
        assertEquals(AlertBroadcastReceiver.DAY_POSITION.AFTER_END, method.invoke(receiver, calendar, alert));
    }

    @Test
    public void testIsLegalDay() throws Exception {
        Method method = receiver.getClass().getDeclaredMethod("isLegalDay", java.util.Calendar.class, nkarasch.repeatingreminder.Alert.class);
        method.setAccessible(true);

        //all days off
        Alert alert = createAlert(true, new boolean[]{false, false, false, false, false, false, false}, 4, 20, 15, 3);
        for (int i = 0; i < 7; i++) {
            Calendar calendar = createCalendar(i + 1, 5, 5, 0);
            assertEquals(false, method.invoke(receiver, calendar, alert));
        }

        //all days on
        alert = createAlert(true, new boolean[]{true, true, true, true, true, true, true}, 4, 20, 15, 3);
        for (int i = 0; i < 7; i++) {
            Calendar calendar = createCalendar(i + 1, 5, 5, 0);
            assertEquals(true, method.invoke(receiver, calendar, alert));
        }

        //alternating
        alert = createAlert(true, new boolean[]{true, false, true, false, true, false, true}, 4, 20, 15, 3);
        for (int i = 0; i < 7; i++) {
            Calendar calendar = createCalendar(i + 1, 5, 5, 0);
            if (i % 2 == 0) {
                assertEquals(true, method.invoke(receiver, calendar, alert));
            } else {
                assertEquals(false, method.invoke(receiver, calendar, alert));
            }
        }

        //alternating
        alert = createAlert(true, new boolean[]{true, false, true, false, true, false, true}, 4, 20, 15, 3);
        for (int i = 0; i < 7; i++) {
            Calendar calendar = createCalendar(i + 1, 4, 19, 0);
            if (i % 2 == 0) {
                assertEquals(true, method.invoke(receiver, calendar, alert));
            } else {
                assertEquals(false, method.invoke(receiver, calendar, alert));
            }
        }
    }

    private static Alert createAlert(boolean isScheduled, boolean[] days, int startHour, int startMinute, int endHour, int endMinute) {
        Alert alert = new Alert();
        alert.setOn(true);
        if (days != null) {
            for (int i = 0; i < days.length; i++) {
                alert.setDayEnabled(days[i], i);
            }
        }
        alert.setSchedule(isScheduled);
        alert.setStartTime(startHour, startMinute);
        alert.setEndTime(endHour, endMinute);
        alert.setFrequency(10);
        return alert;
    }

    private Calendar createCalendar(int dayOfWeek, int hourOfDay, int minute, int second) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        return calendar;
    }
}