package com.ielia.cron;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class (JUnit4) for FixedPeriodCron.
 *
 * @author ielia
 */
public class FixedPeriodCronTest {
	protected static final int DATES = 2;
	protected static final int CRONS = 2;
	protected static final int[] CHECK_FIELDS = { Calendar.DATE, Calendar.MONTH,
		Calendar.YEAR, Calendar.HOUR_OF_DAY, Calendar.MINUTE };

	FixedPeriodCron[] crons;
	Calendar[] referenceDates;
	Calendar[][] previousDates, nextDates;
	Long[][] nextMatchesInMillis, periodsInMillis;

	@Before
	public void setUp() {
		/**
		 * Crons
		 */
		this.crons = new FixedPeriodCron[CRONS];
		this.crons[0] = new FixedPeriodCron("0 0 * * 7");
		this.crons[1] = new FixedPeriodCron("10-45/15 0 */2 * *");

		/**
		 * Reference dates
		 */
		this.referenceDates = new Calendar[DATES];
		/* Wednesday, 28th of February, 2001, 23:59:00.000 */
		this.referenceDates[0] = Calendar.getInstance();
		this.referenceDates[0].set(Calendar.DATE, 28);
		this.referenceDates[0].set(Calendar.MONTH, Calendar.FEBRUARY);
		this.referenceDates[0].set(Calendar.YEAR, 2001);
		this.referenceDates[0].set(Calendar.HOUR_OF_DAY, 23);
		this.referenceDates[0].set(Calendar.MINUTE, 59);
		this.referenceDates[0].set(Calendar.SECOND, 0);
		this.referenceDates[0].set(Calendar.MILLISECOND, 0);
		/* Saturday, 1st of January, 2000, 12:00:00.000 */
		this.referenceDates[1] = Calendar.getInstance();
		this.referenceDates[1].set(Calendar.DATE, 1);
		this.referenceDates[1].set(Calendar.MONTH, Calendar.JANUARY);
		this.referenceDates[1].set(Calendar.YEAR, 2000);
		this.referenceDates[1].set(Calendar.HOUR_OF_DAY, 12);
		this.referenceDates[1].set(Calendar.MINUTE, 0);
		this.referenceDates[1].set(Calendar.SECOND, 0);
		this.referenceDates[1].set(Calendar.MILLISECOND, 0);

		/**
		 * Dates previous to the references, relative to the crons
		 */
		this.previousDates = new Calendar[CRONS][DATES];
		/* Sunday, 25th of February, 2001, 00:00:00.000 */
		this.previousDates[0][0] = Calendar.getInstance();
		this.previousDates[0][0].set(Calendar.DATE, 25);
		this.previousDates[0][0].set(Calendar.MONTH, Calendar.FEBRUARY);
		this.previousDates[0][0].set(Calendar.YEAR, 2001);
		this.previousDates[0][0].set(Calendar.HOUR_OF_DAY, 0);
		this.previousDates[0][0].set(Calendar.MINUTE, 0);
		this.previousDates[0][0].set(Calendar.SECOND, 0);
		this.previousDates[0][0].set(Calendar.MILLISECOND, 0);
		/* Sunday, 26th of December, 1999, 00:00:00.000 */
		this.previousDates[0][1] = Calendar.getInstance();
		this.previousDates[0][1].set(Calendar.DATE, 26);
		this.previousDates[0][1].set(Calendar.MONTH, Calendar.DECEMBER);
		this.previousDates[0][1].set(Calendar.YEAR, 1999);
		this.previousDates[0][1].set(Calendar.HOUR_OF_DAY, 0);
		this.previousDates[0][1].set(Calendar.MINUTE, 0);
		this.previousDates[0][1].set(Calendar.SECOND, 0);
		this.previousDates[0][1].set(Calendar.MILLISECOND, 0);
		/* Tuesday, 27th of February, 2001, 00:40:00.000 */
		this.previousDates[1][0] = Calendar.getInstance();
		this.previousDates[1][0].set(Calendar.DATE, 27);
		this.previousDates[1][0].set(Calendar.MONTH, Calendar.FEBRUARY);
		this.previousDates[1][0].set(Calendar.YEAR, 2001);
		this.previousDates[1][0].set(Calendar.HOUR_OF_DAY, 0);
		this.previousDates[1][0].set(Calendar.MINUTE, 40);
		this.previousDates[1][0].set(Calendar.SECOND, 0);
		this.previousDates[1][0].set(Calendar.MILLISECOND, 0);
		/* Saturday, 1st of January, 2000, 00:40:00.000 */
		this.previousDates[1][1] = Calendar.getInstance();
		this.previousDates[1][1].set(Calendar.DATE, 1);
		this.previousDates[1][1].set(Calendar.MONTH, Calendar.JANUARY);
		this.previousDates[1][1].set(Calendar.YEAR, 2000);
		this.previousDates[1][1].set(Calendar.HOUR_OF_DAY, 0);
		this.previousDates[1][1].set(Calendar.MINUTE, 40);
		this.previousDates[1][1].set(Calendar.SECOND, 0);
		this.previousDates[1][1].set(Calendar.MILLISECOND, 0);

		/**
		 * Dates after the references, relative to the crons
		 */
		this.nextDates = new Calendar[CRONS][DATES];
		/* Sunday, 4th of March, 2001, 00:00:00.000 */
		this.nextDates[0][0] = Calendar.getInstance();
		this.nextDates[0][0].set(Calendar.DATE, 4);
		this.nextDates[0][0].set(Calendar.MONTH, Calendar.MARCH);
		this.nextDates[0][0].set(Calendar.YEAR, 2001);
		this.nextDates[0][0].set(Calendar.HOUR_OF_DAY, 0);
		this.nextDates[0][0].set(Calendar.MINUTE, 0);
		this.nextDates[0][0].set(Calendar.SECOND, 0);
		this.nextDates[0][0].set(Calendar.MILLISECOND, 0);
		/* Sunday, 2nd of January, 2000, 00:00:00.000 */
		this.nextDates[0][1] = Calendar.getInstance();
		this.nextDates[0][1].set(Calendar.DATE, 2);
		this.nextDates[0][1].set(Calendar.MONTH, Calendar.JANUARY);
		this.nextDates[0][1].set(Calendar.YEAR, 2000);
		this.nextDates[0][1].set(Calendar.HOUR_OF_DAY, 0);
		this.nextDates[0][1].set(Calendar.MINUTE, 0);
		this.nextDates[0][1].set(Calendar.SECOND, 0);
		this.nextDates[0][1].set(Calendar.MILLISECOND, 0);
		/* Friday, 1st of March, 2001, 00:10:00.000 */
		this.nextDates[1][0] = Calendar.getInstance();
		this.nextDates[1][0].set(Calendar.DATE, 1);
		this.nextDates[1][0].set(Calendar.MONTH, Calendar.MARCH);
		this.nextDates[1][0].set(Calendar.YEAR, 2001);
		this.nextDates[1][0].set(Calendar.HOUR_OF_DAY, 0);
		this.nextDates[1][0].set(Calendar.MINUTE, 10);
		this.nextDates[1][0].set(Calendar.SECOND, 0);
		this.nextDates[1][0].set(Calendar.MILLISECOND, 0);
		/* Sunday, 3rd of January, 2000, 00:10:00.000 */
		this.nextDates[1][1] = Calendar.getInstance();
		this.nextDates[1][1].set(Calendar.DATE, 3);
		this.nextDates[1][1].set(Calendar.MONTH, Calendar.JANUARY);
		this.nextDates[1][1].set(Calendar.YEAR, 2000);
		this.nextDates[1][1].set(Calendar.HOUR_OF_DAY, 0);
		this.nextDates[1][1].set(Calendar.MINUTE, 10);
		this.nextDates[1][1].set(Calendar.SECOND, 0);
		this.nextDates[1][1].set(Calendar.MILLISECOND, 0);

		/**
		 * Next matches in milliseconds
		 */
		this.nextMatchesInMillis = new Long[CRONS][DATES];
		/* 3 days + 1 minute */
		this.nextMatchesInMillis[0][0] = (long)3*24*60*60*1000+60*1000;
		/* 12 hours */
		this.nextMatchesInMillis[0][1] = (long)12*60*60*1000;
		/* 11 minutes */
		this.nextMatchesInMillis[1][0] = (long)11*60*1000;
		/* 1 day + 12 hours + 10 minutes */
		this.nextMatchesInMillis[1][1] = (long)24*60*60*1000+12*60*60*1000+
			10*60*1000;

		/**
		 * Periods in milliseconds
		 */
		this.periodsInMillis = new Long[CRONS][DATES];
		/* 7 days */
		this.periodsInMillis[0][0] = (long)7*24*60*60*1000;
		/* 7 days */
		this.periodsInMillis[0][1] = (long)7*24*60*60*1000;
		/* 2 days - 30 minutes */
		this.periodsInMillis[1][0] = (long)2*24*60*60*1000-30*60*1000;
		/* 2 days - 30 minutes */
		this.periodsInMillis[1][1] = (long)2*24*60*60*1000-30*60*1000;
	}

	/**
	 * Test method for {@link com.barcelo.utils.FixedPeriodCron#getClosestDateBeforeOrSame(java.util.Calendar)}.
	 */
	@Test
	public void testGetClosestDateBeforeOrSame() {
		for (int i = 0; i < CRONS; ++i) {
			for (int j = 0; j < DATES; ++j) {
				Calendar result = this.crons[i].getClosestDateBeforeOrSame(
						this.referenceDates[j]);
				/**
				 * No comparo fechas directamente para que el assert me diga qué
				 * campo está mal.
				 */
				for (int field:CHECK_FIELDS) {
					assertEquals(this.previousDates[i][j].get(field),
							result.get(field));
				}
			}
		}
	}

	/**
	 * Test method for {@link com.barcelo.utils.FixedPeriodCron#getClosestDateAfter(java.util.Calendar)}.
	 */
	@Test
	public void testGetClosestDateAfter() {
		for (int i = 0; i < CRONS; ++i) {
			for (int j = 0; j < DATES; ++j) {
				Calendar result = this.crons[i].getClosestDateAfter(
						this.referenceDates[j]);
				/**
				 * No comparo fechas directamente para que el assert me diga qué
				 * campo está mal.
				 */
				for (int field:CHECK_FIELDS) {
					assertEquals(this.nextDates[i][j].get(field),
							result.get(field));
				}
			}
		}
	}

	/**
	 * Test method for {@link com.barcelo.utils.FixedPeriodCron#nextMatchInMillis(java.util.Calendar)}.
	 */
	@Test
	public void testNextMatchInMillis() {
		for (int i = 0; i < CRONS; ++i) {
			for (int j = 0; j < DATES; ++j) {
				Long result = this.crons[i].nextMatchInMillis(
						this.referenceDates[j]);
				assertEquals(this.nextMatchesInMillis[i][j], result);
			}
		}
	}

	/**
	 * Test method for {@link com.barcelo.utils.FixedPeriodCron#periodInMillis(java.util.Calendar)}.
	 */
	@Test
	public void testPeriodInMillis() {
		for (int i = 0; i < CRONS; ++i) {
			for (int j = 0; j < DATES; ++j) {
				Long result = this.crons[i].periodInMillis(
						this.referenceDates[j]);
				assertEquals(this.periodsInMillis[i][j], result);
			}
		}
	}
}
