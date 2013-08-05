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
	public static final int MIN_MILLISECONDS = 0;
	public static final int MAX_MILLISECONDS = 999;
	public static final int MIN_SECONDS = 0;
	public static final int MAX_SECONDS = 59;
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
	public static final int[] RANGES =
			{MIN_MINUTES, MAX_MINUTES,
					MIN_HOURS, MAX_HOURS,
					MIN_DAYS, MAX_DAYS,
					MIN_MONTHS, MAX_MONTHS,
					MIN_DAYS_OF_WEEK, MAX_DAYS_OF_WEEK};
	protected static final int YEAR_REFERENCE_FIELD = MONTHS + 1;
	protected static final int YEAR_CYCLES_FOR_WEEKDAY_MATCH = 14;
	/**
	 * Type parameter for the method List.toArray(T[] a).
	 */
	protected static final Integer[] ARRAY_OF_INTEGERS = new Integer[0];
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
	 * <p/>
	 * <p>REMEMBER: "&#42;&#47;2" in days means that day 1 is taken into
	 * consideration and then every 2 days, i.e. 1, 3, 5, ...<br/>
	 * This is because the "2" there is a step modifier.</p>
	 *
	 * @param crontabExpression Crontab expression
	 *                          (see <i>crontab(5)</i> manpage).
	 */
	public FixedPeriodCron(String crontabExpression)
			throws IllegalArgumentException {
		this.expression = crontabExpression;
		String[] crontabElements = this.expression.split(" ");
		if (crontabElements.length != NUMBER_OF_FIELDS) {
			this.throwIllegalArgumentExceptionMalformedCrontab(
					"Wrong number of fields.");
		}
		for (int field = 0; field < NUMBER_OF_FIELDS; ++field) {
			this.crontabSpec[field] = new TreeSet<Integer>();
			/* atoms (separated by commas) */
			String[] crontabAtoms = crontabElements[field].split(",");
			for (String crontabAtom : crontabAtoms) {
				/* steps (specified as a "division") */
				String[] crontabAtomStep = crontabAtom.split("/");
				int step = 1;
				if (crontabAtomStep.length > 2) {
					this.throwIllegalArgumentExceptionMalformedCrontab(
							"Wrong step (division) specification.");
				} else if (crontabAtomStep.length == 2) {
					try {
						step = Integer.valueOf(crontabAtomStep[1]);
					} catch (NumberFormatException exception) {
						this.throwIllegalArgumentExceptionMalformedCrontab(
								"Wrong step (divisor) specification.");
					}
				}
				/* ranges (hyphenated) */
				int rangeStart = 0;
				int rangeEnd = 0;
				if ("*".equals(crontabAtomStep[0])) {
					rangeStart = RANGES[field * 2];
					rangeEnd = RANGES[field * 2 + 1];
				} else {
					String[] crontabAtomRange = crontabAtomStep[0].split("-");
					if (crontabAtomRange.length > 2) {
						this.throwIllegalArgumentExceptionMalformedCrontab(
								"Wrong range specification.");
					} else if (crontabAtomRange.length == 2) {
						try {
							rangeStart =
									Integer.valueOf(crontabAtomRange[0]);
							rangeEnd =
									Integer.valueOf(crontabAtomRange[1]);
						} catch (NumberFormatException exception) {
							this.throwIllegalArgumentExceptionMalformedCrontab(
									"Wrong range specification.");
						}
					} else {
						try {
							rangeStart = Integer.valueOf(crontabAtomStep[0]);
							rangeEnd = rangeStart;
						} catch (NumberFormatException exception) {
							this.throwIllegalArgumentExceptionMalformedCrontab(
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
				for (int i = rangeStart; i <= rangeEnd; i += step) {
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
	protected void throwIllegalArgumentExceptionMalformedCrontab(String cause)
			throws IllegalArgumentException {
		throw new IllegalArgumentException("Malformed crontab expression. " +
				cause + " Read crontab(5) manpage for further reference.");
	}

	/**
	 * Convenience function that checks the validity of the seconds and
	 * milliseconds passed as arguments.
	 *
	 * @param seconds Seconds.
	 * @param millis  Milliseconds.
	 */
	protected void validateSecondsAndMillis(int seconds, int millis) {
		if (seconds < MIN_SECONDS || seconds > MAX_SECONDS) {
			throw new IllegalArgumentException("Seconds spec is not valid.");
		}
		if (millis < MIN_MILLISECONDS || seconds > MAX_MILLISECONDS) {
			throw new IllegalArgumentException("Seconds spec is not valid.");
		}
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
	 * Tells whether the cron runs exactly at the reference or not (running at 0
	 * seconds, 0 milliseconds).
	 *
	 * @param reference Reference calendar (usually, "now").
	 * @return True if it matches, false if not.
	 */
	public boolean matches(Calendar reference) {
		return this.matches(reference, 0, 0);
	}

	/**
	 * Tells whether the cron runs exactly at the reference or not (running at 0
	 * seconds, 0 milliseconds).
	 *
	 * @param reference           Reference calendar (usually, "now").
	 * @param matchAllUnderMinute Indicates whether the match would take the
	 *                            fields smaller than the minute into
	 *                            consideration or not.
	 * @return True if it matches, false if not.
	 */
	public boolean matches(Calendar reference, boolean matchAllUnderMinute) {
		return this.matches(reference, 0, 0, matchAllUnderMinute);
	}

	/**
	 * Tells whether the cron runs exactly at the reference or not (running at 0
	 * seconds, 0 milliseconds).
	 *
	 * @param reference      Reference calendar (usually, "now").
	 * @param cronRunSeconds Seconds after the minute when the cron engine runs.
	 * @param cronRunMillis  Milliseconds after the second when the cron engine
	 *                       runs.
	 * @return True if it matches, false if not.
	 */
	public boolean matches(Calendar reference, int cronRunSeconds,
						   int cronRunMillis) {
		return this.matches(reference, cronRunSeconds, cronRunMillis, true);
	}

	/**
	 * Tells whether the cron runs exactly at the reference or not (running at 0
	 * seconds, 0 milliseconds).
	 *
	 * @param reference           Reference calendar (usually, "now").
	 * @param cronRunSeconds      Seconds after the minute when the cron engine
	 *                            runs.
	 * @param cronRunMillis       Milliseconds after the second when the cron
	 *                            engine runs.
	 * @param matchAllUnderMinute Indicates whether the match would take the
	 *                            fields smaller than the minute into
	 *                            consideration or not.
	 * @return True if it matches, false if not.
	 */
	public boolean matches(Calendar reference, int cronRunSeconds,
						   int cronRunMillis, boolean matchAllUnderMinute) {
		this.validateSecondsAndMillis(cronRunSeconds, cronRunMillis);

		if (matchAllUnderMinute &&
				(reference.get(Calendar.SECOND) != cronRunSeconds ||
						reference.get(Calendar.MILLISECOND) != cronRunMillis)) {
			return false;
		}

		Integer[] fields = this.getReferenceFields(reference, cronRunSeconds,
				cronRunMillis, 0);
		for (int field = 0; field < NUMBER_OF_FIELDS - 1; ++field) {
			if (!this.crontabSpec[field].contains(fields[field])) {
				return false;
			}
		}

		return this.crontabSpec[DAYS_OF_WEEK].contains(
				reference.get(Calendar.DAY_OF_WEEK) - 1);
	}

	/**
	 * Returns the closest calendar previous (or equal) to the reference, in
	 * relation to the cron expression (running at 0 seconds, 0 milliseconds).
	 *
	 * @param reference Reference calendar (usually, "now").
	 * @return The closest calendar previous (or equal) to the reference, in
	 *         relation to the cron expression.
	 */

	public Calendar getClosestDateBeforeOrSame(Calendar reference) {
		return this.getClosestDateBeforeOrSame(reference, 0, 0);
	}

	/**
	 * Returns the closest calendar previous (or equal) to the reference, in
	 * relation to the cron expression.
	 *
	 * @param reference      Reference calendar (usually, "now").
	 * @param cronRunSeconds Seconds after the minute when the cron engine runs.
	 * @param cronRunMillis  Milliseconds after the second when the cron engine
	 *                       runs.
	 * @return The closest calendar previous (or equal) to the reference, in
	 *         relation to the cron expression.
	 */
	// TODO: Re-think this method to make it shorter and faster.
	public Calendar getClosestDateBeforeOrSame(Calendar reference,
											   int cronRunSeconds,
											   int cronRunMillis) {
		this.validateSecondsAndMillis(cronRunSeconds, cronRunMillis);

		Integer[] fields = this.getReferenceFields(reference, cronRunSeconds,
				cronRunMillis, -1);

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
			specsForReferenceParent[field][0] = specs.toArray(ARRAY_OF_INTEGERS);
			/* second pass */
			specs = new ArrayList<Integer>(
					this.crontabSpec[field].tailSet(fields[field]));
			Collections.reverse(specs);
			specsForReferenceParent[field][1] = specs.toArray(ARRAY_OF_INTEGERS);
			/* general order of specs */
			specs = new ArrayList<Integer>(this.crontabSpec[field]);
			Collections.reverse(specs);
			specsInOrder[field] = specs.toArray(ARRAY_OF_INTEGERS);
		}
		List<Integer> specs = new ArrayList<Integer>(
				this.crontabSpec[MONTHS].headSet(fields[MONTHS] + 1));
		Collections.reverse(specs);
		List<Integer> specsTail = new ArrayList<Integer>(
				this.crontabSpec[MONTHS].tailSet(fields[MONTHS]));
		Collections.reverse(specsTail);
		specs.addAll(specsTail);
		specsInOrder[MONTHS] = specs.toArray(ARRAY_OF_INTEGERS);

		int cycles = 0;
		Calendar beforeOrSame = null;
		while (beforeOrSame == null && cycles < YEAR_CYCLES_FOR_WEEKDAY_MATCH) {
			beforeOrSame = this.findDate(-1, fields, specsInOrder,
					specsForReferenceParent, cronRunSeconds, cronRunMillis);
			--fields[YEAR_REFERENCE_FIELD];
			++cycles;
		}
		return beforeOrSame;
	}

	/**
	 * Returns the closest calendar after the reference, in relation to the cron
	 * expression (running at 0 seconds, 0 milliseconds).
	 *
	 * @param reference Reference calendar (usually, "now").
	 * @return The closest calendar after the reference, in relation to the cron
	 *         expression.
	 */
	public Calendar getClosestDateAfter(Calendar reference) {
		return this.getClosestDateAfter(reference, 0, 0);
	}

	/**
	 * Returns the closest calendar after the reference, in relation to the cron
	 * expression.
	 *
	 * @param reference      Reference calendar (usually, "now").
	 * @param cronRunSeconds Seconds after the minute when the cron engine runs.
	 * @param cronRunMillis  Milliseconds after the second when the cron engine
	 *                       runs.
	 * @return The closest calendar after the reference, in relation to the cron
	 *         expression.
	 */
	// TODO: Re-think this method to make it shorter and faster.
	public Calendar getClosestDateAfter(Calendar reference, int cronRunSeconds,
										int cronRunMillis) {
		this.validateSecondsAndMillis(cronRunSeconds, cronRunMillis);

		Integer[] fields = this.getReferenceFields(reference, cronRunSeconds,
				cronRunMillis, 1);

		Integer[][][] specsForReferenceParent =
				new Integer[NUMBER_OF_FIELDS - 2][][];
		Integer[][] specsInOrder =
				new Integer[NUMBER_OF_FIELDS - 1][];
		for (int field = 0; field < NUMBER_OF_FIELDS - 2; ++field) {
			specsForReferenceParent[field] = new Integer[2][];
			/* first pass */
			List<Integer> specs = new ArrayList<Integer>(
					this.crontabSpec[field].tailSet(fields[field]));
			specsForReferenceParent[field][0] = specs.toArray(ARRAY_OF_INTEGERS);
			/* second pass */
			specs = new ArrayList<Integer>(
					this.crontabSpec[field].headSet(fields[field] + 1));
			specsForReferenceParent[field][1] = specs.toArray(ARRAY_OF_INTEGERS);
			/* general order of specs */
			specs = new ArrayList<Integer>(this.crontabSpec[field]);
			specsInOrder[field] = specs.toArray(ARRAY_OF_INTEGERS);
		}
		List<Integer> specs = new ArrayList<Integer>(
				this.crontabSpec[MONTHS].tailSet(fields[MONTHS]));
		List<Integer> specsTail = new ArrayList<Integer>(
				this.crontabSpec[MONTHS].headSet(fields[MONTHS] + 1));
		specs.addAll(specsTail);
		specsInOrder[MONTHS] = specs.toArray(ARRAY_OF_INTEGERS);

		int cycles = 0;
		Calendar beforeOrSame = null;
		while (beforeOrSame == null && cycles < YEAR_CYCLES_FOR_WEEKDAY_MATCH) {
			beforeOrSame = this.findDate(1, fields, specsInOrder,
					specsForReferenceParent, cronRunSeconds, cronRunMillis);
			++fields[YEAR_REFERENCE_FIELD];
			++cycles;
		}
		return beforeOrSame;
	}

	/**
	 * Returns the number of milliseconds to the next match/run, relative to a
	 * calendar (running at 0 seconds, 0 milliseconds).
	 *
	 * @param reference Reference calendar (usually, "now").
	 * @return Number of milliseconds to the next match/run (or null).
	 */
	public Long nextMatchInMillis(Calendar reference) {
		return this.nextMatchInMillis(reference, 0, 0);
	}

	/**
	 * Returns the number of milliseconds to the next match/run, relative to a
	 * calendar.
	 *
	 * @param reference      Reference calendar (usually, "now").
	 * @param cronRunSeconds Seconds after the minute when the cron engine runs.
	 * @param cronRunMillis  Milliseconds after the second when the cron engine
	 *                       runs.
	 * @return Number of milliseconds to the next match/run (or null).
	 */
	public Long nextMatchInMillis(Calendar reference, int cronRunSeconds,
								  int cronRunMillis) {
		this.validateSecondsAndMillis(cronRunSeconds, cronRunMillis);
		Long millis = null;
		Calendar next = this.getClosestDateAfter(reference, cronRunSeconds,
				cronRunMillis);
		if (next != null) {
			millis = next.getTimeInMillis() - reference.getTimeInMillis();
		}
		return millis;
	}

	/**
	 * Returns the period span in milliseconds given a reference date. Takes the
	 * period where the date of the calendar is in (running at 0 seconds,
	 * 0 milliseconds).
	 *
	 * @param reference Reference calendar (usually, "now").
	 * @return Period span in milliseconds (or null).
	 */
	public Long periodInMillis(Calendar reference) {
		return this.periodInMillis(reference, 0, 0);
	}

	/**
	 * Returns the period span in milliseconds given a reference date. Takes the
	 * period where the date of the calendar is in.
	 *
	 * @param reference      Reference calendar (usually, "now").
	 * @param cronRunSeconds Seconds after the minute when the cron engine runs.
	 * @param cronRunMillis  Milliseconds after the second when the cron engine
	 *                       runs.
	 * @return Period span in milliseconds (or null).
	 */
	public Long periodInMillis(Calendar reference, int cronRunSeconds,
							   int cronRunMillis) {
		this.validateSecondsAndMillis(cronRunSeconds, cronRunMillis);
		Long millis = null;
		Calendar last = this.getClosestDateBeforeOrSame(reference,
				cronRunSeconds, cronRunMillis);
		if (last != null) {
			Calendar next = this.getClosestDateAfter(reference, cronRunSeconds,
					cronRunMillis);
			if (next != null) {
				millis = next.getTimeInMillis() - last.getTimeInMillis();
			}
		}
		return millis;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.expression;
	}

	/**
	 * Returns the fields of a reference calendar plus the year: {minutes, hour,
	 * day, month, year}. It shifts the calendar one minute if necessary.
	 *
	 * @param reference      A calendar.
	 * @param cronRunSeconds Seconds after the minute when the cron engine runs.
	 * @param cronRunMillis  Milliseconds after the second when the cron engine
	 *                       runs.
	 * @param direction      Indicates whether a difference in seconds and/or
	 *                       milliseconds increments or decrements minutes in
	 *                       1.
	 * @return The fields of the calendar.
	 */
	// TODO: Refactor.
	protected Integer[] getReferenceFields(Calendar reference,
										   int cronRunSeconds,
										   int cronRunMillis, int direction) {
		Calendar calendar = reference;
		if (direction > 0) {
			int seconds = reference.get(Calendar.SECOND);
			int millis = reference.get(Calendar.MILLISECOND);
			if (seconds > cronRunSeconds ||
					(seconds == cronRunSeconds && millis >= cronRunMillis)) {
				calendar = (Calendar) reference.clone();
				calendar.add(Calendar.MINUTE, 1);
			}
		} else if (direction < 0) {
			int seconds = reference.get(Calendar.SECOND);
			int millis = reference.get(Calendar.MILLISECOND);
			if (seconds < cronRunSeconds ||
					(seconds == cronRunSeconds && millis < cronRunMillis)) {
				calendar = (Calendar) reference.clone();
				calendar.add(Calendar.MINUTE, -1);
			}
		}
		int minute = calendar.get(Calendar.MINUTE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int day = calendar.get(Calendar.DATE);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		return new Integer[]{minute, hour, day, month, year};
	}

	/**
	 * Searches for a cron matching date, forwards or backwards in time, from a
	 * given reference.
	 *
	 * @param direction               1 or -1, indicating forwards or backwards,
	 *                                respectively.
	 * @param fields                  Fields of the reference date {minutes,
	 *                                hour, day, month, year}.
	 * @param specsInOrder            Values coming from the crontab evaluation,
	 *                                sorted to
	 *                                be matched in order.
	 * @param specsForReferenceParent Values coming from the crontab evaluation,
	 *                                sorted to be matched in order, given that
	 *                                the parent value matches the reference
	 *                                date field. Each containing two lists for
	 *                                the first and second passes (i.e. same
	 *                                year and the year before/after--depending
	 *                                on the direction).
	 * @param cronRunSeconds          Seconds after the minute when the cron
	 *                                engine runs.
	 * @param cronRunMillis           Milliseconds after the second when the
	 *                                cron engine runs.
	 * @return The date looked for.
	 */
	// TODO: Re-think this method to make it shorter and faster.
	protected Calendar findDate(int direction, Integer[] fields,
								Integer[][] specsInOrder,
								Integer[][][] specsForReferenceParent,
								int cronRunSeconds, int cronRunMillis) {
		int lastMonth;
		if (direction > 0) {
			lastMonth = MIN_MONTHS - 1;
		} else {
			lastMonth = MAX_MONTHS + 1;
		}
		int pass = 0;
		Calendar calendarHelper = Calendar.getInstance();
		calendarHelper.set(Calendar.YEAR, fields[YEAR_REFERENCE_FIELD]);
		for (Integer aMonth : specsInOrder[MONTHS]) {
			if (aMonth.compareTo(lastMonth) != direction) {
				calendarHelper.add(Calendar.YEAR, direction);
			}
			Integer[] specsForCurrentMonth;
			if (aMonth.equals(fields[MONTHS])) {
				if (aMonth.compareTo(lastMonth) != direction) {
					calendarHelper.add(Calendar.YEAR, direction);
					++pass;
				}
				specsForCurrentMonth = specsForReferenceParent[DAYS][pass];
			} else {
				specsForCurrentMonth = specsInOrder[DAYS];
			}
			lastMonth = aMonth;
			for (Integer aDay : specsForCurrentMonth) {
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
						calendarHelper.set(Calendar.MONTH, aMonth - 1);
						calendarHelper.set(Calendar.DATE, aDay);
						calendarHelper.set(Calendar.HOUR_OF_DAY, specsForCurrentDay[0]);
						calendarHelper.set(Calendar.MINUTE, specsForCurrentHour[0]);
						calendarHelper.set(Calendar.SECOND, cronRunSeconds);
						calendarHelper.set(Calendar.MILLISECOND, cronRunMillis);
						return calendarHelper;
					}
				}
			}
		}
		return null;
	}
}
