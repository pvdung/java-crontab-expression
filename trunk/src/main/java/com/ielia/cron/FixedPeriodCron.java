package com.ielia.cron;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class can parse a subset of crontab expressions and return useful
 * information such as, given a reference date, the date before, the date after,
 * the period and the next run time.
 *
 * @author ielia
 */
public class FixedPeriodCron {
	public static final int MINUTES = 0;
	public static final int HOURS = 1;
	public static final int DAYS = 2;
	public static final int MONTHS = 3;
	public static final int DAYS_OF_WEEK = 4;
	public static final int NUMBER_OF_FIELDS = 5;

	public static final int MIN_MINUTES = 0;
	public static final int MAX_MINUTES = 59;
	public static final int MIN_HOURS = 0;
	public static final int MAX_HOURS = 23;
	public static final int MIN_DAYS = 1;
	public static final int MAX_DAYS = 31;
	public static final int MIN_MONTHS = 1;
	public static final int MAX_MONTHS = 12;
	public static final int MIN_DAYS_OF_WEEK = 0;
	public static final int MAX_DAYS_OF_WEEK = 7; // Sun = 0 and 7
	public static final int[] ranges =
		{MIN_MINUTES, MAX_MINUTES,
		 MIN_HOURS, MAX_HOURS,
		 MIN_DAYS, MAX_DAYS,
		 MIN_MONTHS, MAX_MONTHS,
		 MIN_DAYS_OF_WEEK, MAX_DAYS_OF_WEEK};

	/**
	 * Type parameter for the method List.toArray(T[] a).
	 */
	private static final Integer[] typeParameter = new Integer[0];

	/**
	 * The crontab expression. See <i>crontab(5)</i> manpage.
	 */
	protected String expression;

	/**
	 * The parsed crontab expression fields
	 * ({minutes, hours, days, months, weekdays}).
	 */
	@SuppressWarnings("unchecked")
	protected SortedSet<Integer>[] crontabSpec =
		new SortedSet[NUMBER_OF_FIELDS];

	/**
	 * Creates a cron line object out of an expression.
	 *
	 * <p>REMEMBER: "&#42;&#47;2" in days means that day 1 is taken into
	 * consideration and then every 2 days, i.e. 1, 3, 5, ...<br/>
	 * This is because the "2" there is a step modifier.</p>
	 *
	 * @param crontabExpression Crontab expression
	 * (see <i>crontab(5)</i> manpage).
	 */
	public FixedPeriodCron(String crontabExpression)
			throws IllegalArgumentException {
		this.expression = crontabExpression;
		String[] crontabElements = this.expression.split(" ");
		if (crontabElements.length != NUMBER_OF_FIELDS) {
			this.throwIllegalArgumentException("Wrong number of fields.");
		}
		for (int field = 0; field < NUMBER_OF_FIELDS; ++field) {
			this.crontabSpec[field] = new TreeSet<Integer>();
			/* atoms (separated by commas) */
			String[] crontabAtoms = crontabElements[field].split(",");
			for (String crontabAtom:crontabAtoms) {
				/* steps (specified as a "division") */
				String[] crontabAtomStep = crontabAtom.split("/");
				int step = 1;
				if (crontabAtomStep.length > 2) {
					this.throwIllegalArgumentException(
							"Wrong step (division) specification.");
				} else if (crontabAtomStep.length == 2) {
					try {
						step = Integer.valueOf(crontabAtomStep[1]);
					} catch (Exception exception) {
						this.throwIllegalArgumentException(
								"Wrong step (divisor) specification.");
					}
				}
				/* ranges (hyphenated) */
				int rangeStart = 0;
				int rangeEnd = 0;
				if ("*".equals(crontabAtomStep[0])) {
					rangeStart = ranges[field*2];
					rangeEnd = ranges[field*2+1];
				} else {
					String[] crontabAtomRange = crontabAtomStep[0].split("-");
					if (crontabAtomRange.length > 2) {
						this.throwIllegalArgumentException(
								"Wrong range specification.");
					} else if (crontabAtomRange.length == 2) {
						try {
							rangeStart =
								Integer.valueOf(crontabAtomRange[0]);
							rangeEnd =
								Integer.valueOf(crontabAtomRange[1]);
						} catch (Exception exception) {
							this.throwIllegalArgumentException(
									"Wrong range specification.");
						}
					} else {
						try {
							rangeStart = Integer.valueOf(crontabAtomStep[0]);
							rangeEnd = rangeStart;
						} catch (Exception exception) {
							this.throwIllegalArgumentException(
									"Wrong number specification.");
						}
					}
				}
				/* Workaround for Sunday = 7 */
				if (field == DAYS_OF_WEEK) {
					if (rangeEnd == 7) {
						if (rangeStart == rangeEnd) {
							rangeStart = 0;
							rangeEnd = 0;
						} else {
							rangeEnd = 6;
							this.crontabSpec[field].add(0);
						}
					}
				}
				for (int i = rangeStart; i <= rangeEnd; i += step ) {
					this.crontabSpec[field].add(i);
				}
			}
		}
	}

	/**
	 * Convenience function that builds a common format exception to be thrown
	 * when the arguments passed are illegal.
	 *
	 * @param cause The exception to be thrown.
	 */
	protected void throwIllegalArgumentException(String cause)
			throws IllegalArgumentException{
		throw new IllegalArgumentException("Malformed crontab expression. " +
				cause + " Read crontab(5) manpage for further reference.");
	}

	/**
	 * The minutes field of the parsed crontab expression.
	 *
	 * @return Minutes field.
	 */
	public SortedSet<Integer> getMinutes() {
		return this.crontabSpec[MINUTES];
	}

	/**
	 * The hours field of the parsed crontab expression.
	 *
	 * @return Hours field.
	 */
	public SortedSet<Integer> getHours() {
		return this.crontabSpec[HOURS];
	}

	/**
	 * The days field of the parsed crontab expression.
	 *
	 * @return Days field.
	 */
	public SortedSet<Integer> getDays() {
		return this.crontabSpec[DAYS];
	}

	/**
	 * The months field of the parsed crontab expression.
	 *
	 * @return Months field.
	 */
	public SortedSet<Integer> getMonths() {
		return this.crontabSpec[MONTHS];
	}

	/**
	 * The days of the week field of the parsed crontab expression.
	 *
	 * @return Days of the week field.
	 */
	public SortedSet<Integer> getDaysOfWeek() {
		return this.crontabSpec[DAYS_OF_WEEK];
	}

	/**
	 * Returns the closest calendar previous (or equal) to the reference, in
	 * relation to the cron expression.
	 *
	 * <p>TODO: Re-think this method to make it shorter and faster.</p>
	 *
	 * @param reference Reference calendar (usually, "now").
	 * @return The closest calendar previous (or equal) to the reference, in
	 * relation to the cron expression.
	 */
	public Calendar getClosestDateBeforeOrSame(Calendar reference) {
		Integer[] fields = this.getFields(reference);

		Integer[][][] specsForReferenceParent =
			new Integer[NUMBER_OF_FIELDS - 2][][];
		Integer[][] specsInOrder =
			new Integer[NUMBER_OF_FIELDS - 1][];
		for (int field = 0; field < NUMBER_OF_FIELDS - 2; ++field) {
			specsForReferenceParent[field] = new Integer[2][];
			/* first pass */
			List<Integer> specs = new ArrayList<Integer>(
					this.crontabSpec[field].headSet(fields[field] + 1));
			Collections.reverse(specs);
			specsForReferenceParent[field][0] = specs.toArray(typeParameter);
			/* second pass */
			specs = new ArrayList<Integer>(
					this.crontabSpec[field].tailSet(fields[field]));
			Collections.reverse(specs);
			specsForReferenceParent[field][1] = specs.toArray(typeParameter);
			/* general order of specs */
			specs = new ArrayList<Integer>(this.crontabSpec[field]);
			Collections.reverse(specs);
			specsInOrder[field] = specs.toArray(typeParameter);
		}
		List<Integer> specs = new ArrayList<Integer>(
				this.crontabSpec[MONTHS].headSet(fields[MONTHS] + 1));
		Collections.reverse(specs);
		List<Integer> specsTail = new ArrayList<Integer>(
				this.crontabSpec[MONTHS].tailSet(fields[MONTHS]));
		Collections.reverse(specsTail);
		specs.addAll(specsTail);
		specsInOrder[MONTHS] = specs.toArray(typeParameter);

		return this.findDate(-1, fields, reference.get(Calendar.YEAR),
				specsInOrder, specsForReferenceParent);
	}

	/**
	 * Returns the closest calendar after the reference, in relation to the cron
	 * expression.
	 *
	 * <p>TODO: Re-think this method to make it shorter and faster.</p>
	 *
	 * @param reference Reference calendar (usually, "now").
	 * @return The closest calendar after the reference, in relation to the cron
	 * expression.
	 */
	public Calendar getClosestDateAfter(Calendar reference) {
		Integer[] fields = this.getFields(reference);

		Integer[][][] specsForReferenceParent =
			new Integer[NUMBER_OF_FIELDS - 2][][];
		Integer[][] specsInOrder =
			new Integer[NUMBER_OF_FIELDS - 1][];
		for (int field = 0; field < NUMBER_OF_FIELDS - 2; ++field) {
			specsForReferenceParent[field] = new Integer[2][];
			/* first pass */
			List<Integer> specs = new ArrayList<Integer>(
					this.crontabSpec[field].tailSet(fields[field]));
			specsForReferenceParent[field][0] = specs.toArray(typeParameter);
			/* second pass */
			specs = new ArrayList<Integer>(
					this.crontabSpec[field].headSet(fields[field] + 1));
			specsForReferenceParent[field][1] = specs.toArray(typeParameter);
			/* general order of specs */
			specs = new ArrayList<Integer>(this.crontabSpec[field]);
			specsInOrder[field] = specs.toArray(typeParameter);
		}
		List<Integer> specs = new ArrayList<Integer>(
				this.crontabSpec[MONTHS].tailSet(fields[MONTHS]));
		List<Integer> specsTail = new ArrayList<Integer>(
				this.crontabSpec[MONTHS].headSet(fields[MONTHS] + 1));
		specs.addAll(specsTail);
		specsInOrder[MONTHS] = specs.toArray(typeParameter);

		return this.findDate(1, fields, reference.get(Calendar.YEAR),
				specsInOrder, specsForReferenceParent);
	}

	/**
	 * Returns the number of milliseconds to the next match/run, relative to a
	 * calendar.
	 *
	 * @param reference Reference calendar (usually, "now").
	 * @return Number of milliseconds to the next match/run.
	 */
	public long nextMatchInMillis(Calendar reference) {
		Calendar next = this.getClosestDateAfter(reference);
		return next.getTimeInMillis() - reference.getTimeInMillis();
	}

	/**
	 * Returns the period span in milliseconds given a reference date. Takes the
	 * period where the date of the calendar is in.
	 *
	 * @param reference Reference calendar (usually, "now").
	 * @return Period span in milliseconds.
	 */
	public long periodInMillis(Calendar reference) {
		Calendar last = this.getClosestDateBeforeOrSame(reference);
		Calendar next = this.getClosestDateAfter(reference);
		return next.getTimeInMillis() - last.getTimeInMillis();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.expression;
	}

	/**
	 * Returns the fields of a calendar: {minutes, hour, day, month}.
	 *
	 * @param reference A calendar.
	 * @return The fields of the calendar.
	 */
	protected Integer[] getFields(Calendar reference) {
		int minute = reference.get(Calendar.MINUTE);
		int hour = reference.get(Calendar.HOUR_OF_DAY);
		int day = reference.get(Calendar.DATE);
		int month = reference.get(Calendar.MONTH) + 1;
		return new Integer[]{minute, hour, day, month};
	}

	/**
	 * Searches for a cron matching date, forwards or backwards in time, from a
	 * given reference.
	 *
	 * <p>TODO: Re-think this method to make it shorter and faster.</p>
	 *
	 * @param direction 1 or -1, indicating forwards or backwards, respectively.
	 * @param fields Fields of the reference date {minutes, hour, day, month}.
	 * @param specsInOrder Values coming from the crontab evaluation, sorted to
	 * be matched in order.
	 * @param year The year of the reference date (not in the fields array to
	 * avoid confusion).
	 * @param specsForReferenceParent Values coming from the crontab evaluation,
	 * sorted to be matched in order, given that the parent value matches the
	 * reference date field. Each containing two lists for the first and second
	 * passes (i.e. same year and the year before/after--depending on the
	 * direction).
	 * @return The date looked for.
	 */
	protected Calendar findDate(int direction, Integer[] fields, int year,
			Integer[][] specsInOrder, Integer[][][] specsForReferenceParent) {
		int lastMonth;
		if (direction > 0) {
			lastMonth = MIN_MONTHS - 1;
		} else {
			lastMonth = MAX_MONTHS + 1;
		}
		int pass = 0;
		Calendar calendarHelper = Calendar.getInstance();
		calendarHelper.set(Calendar.YEAR, year);
		for (Integer aMonth:specsInOrder[MONTHS]) {
			if (aMonth.compareTo(lastMonth) != direction) {
				calendarHelper.add(Calendar.YEAR, direction);
			}
			Integer[] specsForCurrentMonth;
			if (aMonth.equals(fields[MONTHS])) {
				if (aMonth.compareTo(lastMonth) != direction) {
					++pass;
				}
				specsForCurrentMonth = specsForReferenceParent[DAYS][pass];
			} else {
				specsForCurrentMonth = specsInOrder[DAYS];
			}
			lastMonth = aMonth;
			for (Integer aDay:specsForCurrentMonth) {
				/* Check month validity and matching day of the week */
				calendarHelper.set(Calendar.MONTH, aMonth - 1);
				calendarHelper.set(Calendar.DATE, aDay);
				if (!aMonth.equals(calendarHelper.get(Calendar.MONTH) + 1) ||
					!this.crontabSpec[DAYS_OF_WEEK].contains(
						calendarHelper.get(Calendar.DAY_OF_WEEK) - 1)) {
					continue;
				}
				Integer[] specsForCurrentDay;
				if (aDay.equals(fields[DAYS])) {
					specsForCurrentDay =
						specsForReferenceParent[HOURS][pass];
				} else {
					specsForCurrentDay = specsInOrder[HOURS];
				}
				if (specsForCurrentDay.length > 0) {
					Integer[] specsForCurrentHour;
					if (specsForCurrentDay[0].equals(fields[HOURS])) {
						specsForCurrentHour =
							specsForReferenceParent[MINUTES][pass];
					} else {
						specsForCurrentHour = specsInOrder[MINUTES];
					}
					if (specsForCurrentHour.length > 0) {
						Calendar match = calendarHelper;
						match.add(Calendar.YEAR, direction * pass);
						match.set(Calendar.MONTH, aMonth - 1);
						match.set(Calendar.DATE, aDay);
						match.set(Calendar.HOUR_OF_DAY, specsForCurrentDay[0]);
						match.set(Calendar.MINUTE, specsForCurrentHour[0]);
						match.set(Calendar.SECOND, 0);
						match.set(Calendar.MILLISECOND, 0);
						return match;
					}
				}
			}
		}
		return null;
	}
}
