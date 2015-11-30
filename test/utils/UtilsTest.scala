package utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Date, Calendar, UUID, GregorianCalendar}

import models.{Clazz, Recurrence, ClazzDefinition}
import org.scalatest.{GivenWhenThen, FeatureSpec}
import play.api.libs.json._


/**
 * Created by alex on 27/09/15.
 */
class UtilsTest extends FeatureSpec with GivenWhenThen {

  info("As a Partner")
  info("I want to defince Clazzes with starting tim and stop time and active From and activeTill")
  info("So I can define the clazz once")
  info("And do the recurrent clazzes created automatically")

  feature("Create clazz definition with different recurrences, active times and start times") {

    val format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
    val startFrom = new GregorianCalendar
    val endAt = new GregorianCalendar
    val activeFrom = new GregorianCalendar
    val activeTill = new GregorianCalendar
    val seeInAdvanceDays = 16

    // weekly
    val clazzDef = ClazzDefinition(Some(UUID.randomUUID()), startFrom, endAt, activeFrom, activeTill, Recurrence.weekly, "Test Gym", 5, "http://avatar", "Super gym description", Some("Crossfit"), UUID.randomUUID())
    // onetime
    val clazzDefOnce = ClazzDefinition(Some(UUID.randomUUID()), startFrom, endAt, activeFrom, activeTill, Recurrence.onetime, "Test Gym", 5, "http://avatar", "Super gym description", Some("Crossfit"), UUID.randomUUID())

    scenario("Partner creates a clazz definition weekly currently active") {

      Given("A Util that calculates the recurrence")

      When("This is a weekly recurrent class with 4 years active time +2/-2 years from now, starts from Tuesday 4. Aug 17:00 - 17:45")
      // Tuesday 4. Aug 17:00 - 17:45
      startFrom.set(2015, Calendar.AUGUST, 4, 17, 0)
      endAt.set(2015, Calendar.AUGUST, 4, 17, 45)
      activeFrom.add(Calendar.YEAR, -2)
      activeTill.add(Calendar.YEAR, 2)
      
      info("startFrom:" + startFrom.getTime)
      info("endAt:" + endAt.getTime)
      info("activeFrom:" + activeFrom.getTime)
      info("activeTill:" + activeTill.getTime)
      

      val clazzes = Utils.calculateNextClazzes(clazzDef, seeInAdvanceDays)

      Then("the date must be in future and Tuesday 17:00 - 17:45")
      val format2 = new SimpleDateFormat("EEE HH:mm")
      val expectedSize: Int = seeInAdvanceDays / 7
      assert(clazzes.size === expectedSize)
      Then("the date must be in future and Tuesday 17:00 - 17:45")
      val compare = (x: Clazz, y: Clazz) => {
        info(format.format(x.startFrom.getTime))
        info(format.format(x.endAt.getTime))
        info(format.format(y.startFrom.getTime))
        info(format.format(y.endAt.getTime))
        assert(format2.format(x.startFrom.getTime).matches("Tue 17:00"))
        assert(format2.format(x.endAt.getTime).matches("Tue 17:45"))
        Then("The weekly classes mus deffer by exactly 7 days")
        assert(y.startFrom.get(Calendar.DAY_OF_YEAR) - x.startFrom.get(Calendar.DAY_OF_YEAR) === 7)
        assert(y.endAt.get(Calendar.DAY_OF_YEAR) - x.endAt.get(Calendar.DAY_OF_YEAR) === 7)
        y
      }
      clazzes.reduceLeft(compare)
      activeFrom.add(Calendar.YEAR, +2)
      activeTill.add(Calendar.YEAR, -2)
    }
    scenario("Partner creates a clazz definition weekly not active outdated") {

      Given("A Util that calculates the recurrence")

      When("This is a weekly recurrent class which with 1 year active time -1 year from now, starts from Tuesday 4. Aug 17:00 - 17:45")
      // Tuesday 4. Aug 17:00 - 17:45
      startFrom.set(2015, Calendar.AUGUST, 4, 17, 0)
      endAt.set(2015, Calendar.AUGUST, 4, 17, 45)
      activeFrom.add(Calendar.YEAR, -2)
      activeTill.add(Calendar.YEAR, -1)
      
      info("startFrom:" + startFrom.getTime)
      info("endAt:" + endAt.getTime)
      info("activeFrom:" + activeFrom.getTime)
      info("activeTill:" + activeTill.getTime)
      
      val clazzes2 = Utils.calculateNextClazzes(clazzDef, seeInAdvanceDays)

      Then("The clazzes list must be empty")
      assert(clazzes2.size === 0)
      // revert the state
      activeFrom.add(Calendar.YEAR, +2)
      activeTill.add(Calendar.YEAR, +1)
    }

    scenario("Partner creates a clazz definition weekly active in future") {

      Given("A Util that calculates the recurrence")


      When("the next clazz appointment is calculated")
      startFrom.set(2015, Calendar.AUGUST, 4, 17, 0)
      endAt.set(2015, Calendar.AUGUST, 4, 17, 45)
      activeFrom.add(Calendar.YEAR, +1)
      activeTill.add(Calendar.YEAR, +2)

      info("This is a weekly recurrent class with 1 year active time +1 year from now, starts from Tuesday 4. Aug 17:00 - 17:45")
      info("startFrom:" + startFrom.getTime)
      info("endAt:" + endAt.getTime)
      info("activeFrom:" + activeFrom.getTime)
      info("activeTill:" + activeTill.getTime)

      val clazzes3 = Utils.calculateNextClazzes(clazzDef, seeInAdvanceDays).map { x =>
        info(format.format(x.startFrom.getTime))
        info(format.format(x.endAt.getTime))
      }

      Then("The clazzes list must be empty, cause the are in future")
      assert(clazzes3.size === 0)
      // revert the state
      activeFrom.add(Calendar.YEAR, -1)
      activeTill.add(Calendar.YEAR, -2)

    }

    scenario("Partner creates a clazz definition weekly partially active") {

      Given("A Util that calculates the recurrence")


      When("the next clazz appointment is calculated")
      startFrom.set(2015, Calendar.AUGUST, 4, 17, 0)
      endAt.set(2015, Calendar.AUGUST, 4, 17, 45)
      activeFrom.add(Calendar.WEEK_OF_YEAR, +1)
      activeTill.add(Calendar.YEAR, +2)

      info("This is a weekly recurrent class with 1 year active time +1 week from now, starts from Tuesday 4. Aug 17:00 - 17:45")
      info("startFrom:" + startFrom.getTime)
      info("endAt:" + endAt.getTime)
      info("activeFrom:" + activeFrom.getTime)
      info("activeTill:" + activeTill.getTime)

      val clazzes4 = Utils.calculateNextClazzes(clazzDef, seeInAdvanceDays).map { x =>
        info(format.format(x.startFrom.getTime))
        info(format.format(x.endAt.getTime))
      }
      val expectedSize: Int = seeInAdvanceDays / 7
      Then("The clazzes list must give one less clazz, cause the are in future")
      assert(clazzes4.size === (expectedSize - 1))
      // revert the state
      activeFrom.add(Calendar.WEEK_OF_YEAR, -1)
      activeTill.add(Calendar.YEAR, -2)

    }

    scenario("Partner creates a clazz definition onetime active") {

      When("the next clazz appointment is calculated")
      startFrom.setTime(new Date())
      startFrom.add(Calendar.DAY_OF_WEEK, +1)
      endAt.setTime(new Date())
      endAt.add(Calendar.DAY_OF_WEEK, +1)
      activeFrom.add(Calendar.WEEK_OF_YEAR, +1)
      activeTill.add(Calendar.YEAR, +2)

      info("This is a onetime class")
      info("startFrom:" + startFrom.getTime)
      info("endAt:" + endAt.getTime)
      info("activeFrom:" + activeFrom.getTime)
      info("activeTill:" + activeTill.getTime)

      val clazzes = Utils.calculateNextClazzes(clazzDefOnce, 7).map { x =>
        info(format.format(x.startFrom.getTime))
        info(format.format(x.endAt.getTime))
      }
      Then("The clazzes list must give one less clazz, cause the are in future")
      assert(clazzes.size === 1)
      // revert the state
      activeFrom.add(Calendar.WEEK_OF_YEAR, -1)
      activeTill.add(Calendar.YEAR, -2)
    }

    scenario("Partner creates a clazz definition onetime in past") {

      When("the next clazz appointment is calculated")
      startFrom.setTime(new Date())
      startFrom.add(Calendar.DAY_OF_WEEK, -1)
      endAt.setTime(new Date())
      endAt.add(Calendar.DAY_OF_WEEK, -1)
      activeFrom.add(Calendar.WEEK_OF_YEAR, +1)
      activeTill.add(Calendar.YEAR, +2)

      info("This is a onetime class")
      info("startFrom:" + startFrom.getTime)
      info("endAt:" + endAt.getTime)
      info("activeFrom:" + activeFrom.getTime)
      info("activeTill:" + activeTill.getTime)

      val clazzes = Utils.calculateNextClazzes(clazzDefOnce, 7).map { x =>
        info(format.format(x.startFrom.getTime))
        info(format.format(x.endAt.getTime))
      }
      Then("The clazzes list must give one less clazz, cause the are in future")
      assert(clazzes.size === 0)
      // revert the state
      activeFrom.add(Calendar.WEEK_OF_YEAR, -1)
      activeTill.add(Calendar.YEAR, -2)
    }

    scenario("Partner creates a clazz definition onetime in future to far") {

      When("the next clazz appointment is calculated")
      startFrom.setTime(new Date())
      startFrom.add(Calendar.YEAR, +2)
      endAt.setTime(new Date())
      endAt.add(Calendar.YEAR, +2)
      activeFrom.add(Calendar.WEEK_OF_YEAR, +1)
      activeTill.add(Calendar.YEAR, +2)

      info("This is a onetime class")
      info("startFrom:" + startFrom.getTime)
      info("endAt:" + endAt.getTime)
      info("activeFrom:" + activeFrom.getTime)
      info("activeTill:" + activeTill.getTime)

      val clazzes = Utils.calculateNextClazzes(clazzDefOnce, 7).map { x =>
        info(format.format(x.startFrom.getTime))
        info(format.format(x.endAt.getTime))
      }
      Then("The clazzes list must give one less clazz, cause the are in future")
      assert(clazzes.size === 0)
      // revert the state
      activeFrom.add(Calendar.WEEK_OF_YEAR, -1)
      activeTill.add(Calendar.YEAR, -2)
    }
  }


  feature("calculate subscription periods with different start time") {

    val createdOn = new GregorianCalendar

    scenario("The subscription has more than one past periods") {
      Given("A Util that calculates the subscription periods until now")
      When("This is the suscription start time from 29.02.2012")
      createdOn.set(2012,Calendar.FEBRUARY,29)
      info("createdOn:" + createdOn.getTime)
      val periods = Utils.calculatePeriods(createdOn)
      Then("the list of periods must be more than 10")
      periods.map(x => println("### start: "+x.start.getTime+", end: "+x.end.getTime))
      assert(periods.size >= 10)

      Then("Check the some elements")
      val df = new SimpleDateFormat("EEE, MMM dd yyyy")
      assert(df.format(periods(0).start.getTime).equals("Wed, Feb 29 2012"))
      assert(df.format(periods(0).end.getTime).equals("Thu, Mar 29 2012"))
      assert(df.format(periods(11).start.getTime).equals("Tue, Jan 29 2013"))
      assert(df.format(periods(11).end.getTime).equals("Thu, Feb 28 2013"))

      Then("The last date must be in future")
      assert(periods.last.end.after(new GregorianCalendar()))

    }
  }

}