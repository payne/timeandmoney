/**
 * Copyright (c) 2004 Domain Language, Inc. (http://domainlanguage.com) This
 * free software is distributed under the "MIT" licence. See file licence.txt.
 * For more information, see http://timeandmoney.sourceforge.net.
 */

package com.domainlanguage.time;

import java.io.*;
import java.util.*;

import com.domainlanguage.base.*;

public class Duration implements Comparable, Serializable {
	public static final Duration NONE = milliseconds(0);

	private long quantity;
	private TimeUnit unit;
	
	public static Duration milliseconds(long howMany) {
		return Duration.of(howMany, TimeUnit.millisecond);
	}

	public static Duration seconds(int howMany) {
		return Duration.of(howMany, TimeUnit.second);
	}
	
	public static Duration minutes(int howMany) {
		return Duration.of(howMany, TimeUnit.minute);
	}
	
	public static Duration hours(int howMany) {
		return Duration.of(howMany, TimeUnit.hour);
	}
	
	public static Duration days(int howMany) {
		return Duration.of(howMany, TimeUnit.day);
	}
	
	public static Duration daysHoursMinutesSecondsMilliseconds(int days, int hours, int minutes, int seconds, long milliseconds) {
		Duration result = Duration.days(days);
		if (hours != 0) result = result.plus(Duration.hours(hours));
		if (minutes != 0) result = result.plus(Duration.minutes(minutes));
		if (seconds != 0) result = result.plus(Duration.seconds(seconds));
		if (milliseconds != 0) result = result.plus(Duration.milliseconds(milliseconds));
		return result;
	}
	
	public static Duration weeks(int howMany) {
		return Duration.of(howMany, TimeUnit.week);
	}
	
	public static Duration months(int howMany) {
		return Duration.of(howMany, TimeUnit.month);
	}
	
	public static Duration quarters(int howMany) {
		return Duration.of(howMany, TimeUnit.quarter);
	}
	
	public static Duration years(int howMany) {
		return Duration.of(howMany, TimeUnit.year);
	}
	
	private static Duration of(long howMany, TimeUnit unit) {
		return new Duration(howMany, unit);
	}

	Duration(long quantity, TimeUnit unit) {
		assert quantity >= 0;
		this.quantity = quantity;
		this.unit = unit;
	}
    
	long inBaseUnits() {
		return quantity * unit.getFactor();
	}
	
	//TODO: What SHOULD happen if assertion fails?
	public Duration plus(Duration other) {
		assert other.unit.isConvertibleTo(this.unit);
		long newQuantity = this.inBaseUnits() + other.inBaseUnits();
		return new Duration(newQuantity, unit.baseUnit());
	}

	//TODO: What SHOULD happen if assertion fails?
	public Duration minus(Duration other) {
		assert other.unit.isConvertibleTo(this.unit);
		assert this.compareTo(other) >= 0;
		long newQuantity = this.inBaseUnits() - other.inBaseUnits();
		return new Duration(newQuantity, unit.baseUnit());
	}
	
	public TimePoint addedTo(TimePoint point) {
        return addAmountToTimePoint(inBaseUnits(), point);
	}
	
	public TimePoint subtractedFrom(TimePoint point) {
		return addAmountToTimePoint(-1 * inBaseUnits(), point);
	}

	public CalendarDate addedTo(CalendarDate day) {
//		only valid for days and larger units
		if (unit.compareTo(TimeUnit.day) < 0) return day;
		Calendar calendar = day.asJavaCalendarUniversalZoneMidnight();
		if (unit.equals(TimeUnit.day)) 
			calendar.add(Calendar.DATE, (int) quantity);
		else 
            addAmountToCalendar(inBaseUnits(), calendar);
		return CalendarDate._from(calendar);
	}

	public CalendarDate subtractedFrom(CalendarDate day) {
//		only valid for days and larger units
		if (unit.compareTo(TimeUnit.day) < 0) return day;
		Calendar calendar = day.asJavaCalendarUniversalZoneMidnight();
		if (unit.equals(TimeUnit.day)) 
			calendar.add(Calendar.DATE, -1 * (int)quantity);
		else 
            subtractAmountFromCalendar(inBaseUnits(), calendar);
		return CalendarDate._from(calendar);
	}

	public Ratio dividedBy (Duration divisor) {
		assert unit.isConvertibleTo(divisor.unit);
		return Ratio.of(inBaseUnits(), divisor.inBaseUnits());
	}

	public boolean equals(Object object) {
		//revisit: maybe use: Reflection.equalsOverClassAndNull(this, other)
		if (!(object instanceof Duration)) return false;
		Duration other = (Duration) object;
		if (!this.unit.isConvertibleTo(other.unit)) return false;
		return this.inBaseUnits() == other.inBaseUnits();
	}
	
	public String toString() {
		return toNormalizedString(unit.descendingUnitsForDisplay());
	}

	public String toNormalizedString() {
		return toNormalizedString(unit.descendingUnits());
	}

	private String toNormalizedString(TimeUnit[] units) {
		StringBuffer buffer = new StringBuffer();
		long remainder = inBaseUnits();
		boolean first = true;		
		for (int i = 0; i < units.length; i++) {
			TimeUnit aUnit = units[i];
			long portion = remainder / aUnit.getFactor();			
			if (portion > 0) {
				if (!first)
					buffer.append(", ");
				else
					first = false;
				buffer.append(aUnit.toString(portion));
			}
			remainder = remainder % aUnit.getFactor();
		}
		return buffer.toString();
	}
	
	public TimeUnit normalizedUnit() {
		TimeUnit[] units = unit.descendingUnits();
		long baseAmount = inBaseUnits();
		for (int i = 0; i < units.length; i++) {
			TimeUnit aUnit = units[i];
			long remainder = baseAmount % aUnit.getFactor();
			if (remainder == 0) return aUnit;
		}
		return null;
		
	}
	
	public int hashCode() {
		return (int) quantity;
	}
	
	//TODO: What should happen if units are not convertible?
	public int compareTo(Object arg) {
		Duration other = (Duration) arg;
		assert this.unit.isConvertibleTo(other.unit);
		long difference = this.inBaseUnits() - other.inBaseUnits();
		if (difference > 0) return 1;
		if (difference < 0) return -1;
		return 0;
	}
	
	public TimeInterval startingFrom(TimePoint start) {
		return TimeInterval.startingFrom(start, this);
	}
	
	public CalendarInterval startingFrom(CalendarDate start) {
		return CalendarInterval.startingFrom(start, this);
	}

	public TimeInterval preceding(TimePoint end) {
		return TimeInterval.preceding(end, this);
	}

	public boolean isGreaterThan(Duration other) {
		assert this.unit.isConvertibleTo(other.unit);
		return this.inBaseUnits() > other.inBaseUnits();
	}

	public boolean isLessThan(Duration other) {
		assert this.unit.isConvertibleTo(other.unit);
		return this.inBaseUnits() < other.inBaseUnits();
	}

	public boolean isGreaterThanOrEqualTo(Duration other) {
		assert this.unit.isConvertibleTo(other.unit);
		return this.inBaseUnits() >= other.inBaseUnits();
	}

	public boolean isLessThanOrEqualTo(Duration other) {
		assert this.unit.isConvertibleTo(other.unit);
		return this.inBaseUnits() <= other.inBaseUnits();
	}

    TimePoint addAmountToTimePoint(long amount, TimePoint point) {
        if (unit.isConvertibleToMilliseconds()) {
            return TimePoint.from(amount + point.millisecondsFromEpoc);
        } else {
            Calendar calendar = point.asJavaCalendar();
            addAmountToCalendar(amount, calendar);
            return TimePoint.from(calendar);
        }
    }
    void addAmountToCalendar(long amount, Calendar calendar) {
        if (unit.isConvertibleToMilliseconds()) {
            calendar.setTimeInMillis(calendar.getTimeInMillis() + amount);
        } else {
            assert (amount >= Integer.MIN_VALUE && amount <= Integer.MAX_VALUE);
            calendar.add(unit.javaCalendarConstantForBaseType(), (int) amount);
        }
    }
    void subtractAmountFromCalendar(long amount, Calendar calendar) {
        addAmountToCalendar(-1 * amount, calendar);
    }

    //Only for use by persistence mapping frameworks
    //<rant>These methods break encapsulation and we put them in here begrudgingly</rant>
    Duration() {
    }
    //Only for use by persistence mapping frameworks
    //<rant>These methods break encapsulation and we put them in here begrudgingly</rant>
    private long getForPersistentMapping_Quantity() {
        return quantity;
    }
    //Only for use by persistence mapping frameworks
    //<rant>These methods break encapsulation and we put them in here begrudgingly</rant>
    private void setForPersistentMapping_Quantity(long quantity) {
        this.quantity = quantity;
    }
    //Only for use by persistence mapping frameworks
    //<rant>These methods break encapsulation and we put them in here begrudgingly</rant>
    private TimeUnit getForPersistentMapping_Unit() {
        return unit;
    }
    //Only for use by persistence mapping frameworks
    //<rant>These methods break encapsulation and we put them in here begrudgingly</rant>
    private void setForPersistentMapping_Unit(TimeUnit unit) {
        this.unit = unit;
    }
}
