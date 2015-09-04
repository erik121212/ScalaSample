package actors

import actors.AppointmentsActor.{RoomAppointmentsRequest, DurationDeterminedReceive, AvailabilityReceive, AdvisorAppointmentsRequest}
import akka.actor.Actor
import nl.erik121212.data.Repository
import nl.erik121212.data.domain._
import nl.erik121212.service.domain.Availability
//import nl.ing.api.gCalendarsAPIData.Repository
//import nl.ing.api.gCalendarsAPIData.domain._
//import nl.ing.api.gCalendarsAPIService.actors.AppointmentsActor.{AdvisorAppointmentsRequest, AvailabilityReceive, DurationDeterminedReceive, RoomAppointmentsRequest}
//import nl.ing.api.gCalendarsAPIService.domain.Availability
import org.joda.time.{DateTime, DateTimeZone}

object AppointmentsActor {
  case class AdvisorAppointmentsRequest(advisor : Advisor, office : String, utcStartDateTime: DateTime, utcEndDateTime: DateTime)
  case class RoomAppointmentsRequest(room : Room, office : String, utcStartDateTime: DateTime, utcEndDateTime: DateTime)
  case class AvailabilityReceive(availability: Availability)
  case class DurationDeterminedReceive(subject : CustomerSubject)
}
class AppointmentsActor() extends Actor with Repository {
  private[this] var appointments : List[Appointment] = Nil
  private[this] var office : String = _
  private[this] var duration : Int = 0
  private[this] var resource : Resource = _
  private[this] var utcStartDateTime : DateTime = _
  private[this] var utcEndDateTime : DateTime = _

  private def InvertAppointmentsToTimeSlots() : List[DateTime] = {//: List[TimeSlot] = {
    val millisPerMinute = 60*1000
    val millisPerQuarter = 15*millisPerMinute
    val millisPerHour = 60*millisPerMinute
    val millisPerDay = 24*millisPerHour

    def EpochToWeekDay(epoch : Long): Int = (((epoch+(3*millisPerDay))%7)+1).asInstanceOf[Int]

    def TimeRangesForResource(utcStartDateTime : DateTime, utcEndDateTime : DateTime, duration : Int, resource : Resource) : List[(DateTime, DateTime)] = {
      val wdOnOffice = resource.workingDays//.filter(wd => wd.office == this.office)
      val sDate = utcStartDateTime.getMillis
      val eDate = utcEndDateTime.getMillis

      val timeranges = for {
        wd <- wdOnOffice
        dayEpoch <- sDate to eDate by millisPerDay
        dateTimeZone = DateTimeZone.forTimeZone(wd.timeZone)
        dateTimeUTC = new DateTime(dayEpoch, DateTimeZone.UTC)
        dateTimeInTimeZone = dateTimeUTC.withZone(dateTimeZone)
        if (dateTimeInTimeZone.getDayOfWeek == wd.weekday) // week day matches day in TimeZone
      } yield (
        new DateTime(dateTimeInTimeZone.getYear, dateTimeInTimeZone.getMonthOfYear, dateTimeInTimeZone.getDayOfMonth, wd.startHour/60, wd.startHour%60, dateTimeZone),
        new DateTime(dateTimeInTimeZone.getYear, dateTimeInTimeZone.getMonthOfYear, dateTimeInTimeZone.getDayOfMonth, wd.endHour/60, wd.endHour%60, dateTimeZone))
      timeranges.sortBy(tr => tr._1.getMillis)
    }

    def NoAppointmentOnTimeSlot(utcStartSlot : DateTime, utcEndSlot : DateTime) = {

      val res = if (appointments == Nil || appointments == null) true
      else appointments.forall(app => (utcEndSlot.getMillis <= app.utcStartTime.getMillis || utcStartSlot.getMillis >= app.utcEndTime.getMillis))
//      appointments.foreach(app => println(s"  AppCheck\t${utcStartSlot}\t${utcEndSlot}\t${app.utcStartTime}\t${app.utcEndTime}\t$res\t${utcEndSlot.getMillis <= app.utcStartTime.getMillis || utcStartSlot.getMillis >= app.utcEndTime.getMillis}"))
      res
    }

    val timeslots = for {
      tr <- TimeRangesForResource(utcStartDateTime, utcEndDateTime, duration, resource)
      startSlotEpoch <- tr._1.getMillis to tr._2.getMillis-duration*millisPerMinute by millisPerQuarter // for all quarters in an time range
      endSlotEpoch = startSlotEpoch+duration*millisPerMinute
      utcStartSlot = new DateTime(startSlotEpoch, DateTimeZone.UTC)
      utcEndSlot = new DateTime(endSlotEpoch, DateTimeZone.UTC)
      if NoAppointmentOnTimeSlot(utcStartSlot, utcEndSlot) // all appointments outside this time slot
    } yield utcStartSlot
//    timeslots.toList.foreach(ts => println(s"TimeSlot\t${ts}"))
    timeslots.toList
  }

  private def CalculateTimeSlots = {

    if (appointments != Nil && duration != 0) {
      val availability : Availability = resource match {
        case adv: Advisor =>
          new Availability(office, "A", adv.corporateKey, duration, InvertAppointmentsToTimeSlots)
        case rm: Room =>
          new Availability(office, "R", rm.mailbox, duration, InvertAppointmentsToTimeSlots)
        //case None =>
      }

      sender ! AvailabilityReceive(availability)
      context.system.stop(self)
    }
  }

  def  receive = {

    case AdvisorAppointmentsRequest(advisor, office, utcStartDateTime, utcEndDateTime) =>
      //println(s"AppointmentsActor.AdvisorAppointmentsRequest($advisor, $startDateTime, $endDateTime)")
      this.resource = advisor
      this.office = office
      this.utcStartDateTime = utcStartDateTime
      this.utcEndDateTime = utcEndDateTime

      appointments = getAdvisorAppointments(advisor.corporateKey, office, utcStartDateTime, utcEndDateTime)
      CalculateTimeSlots

    case RoomAppointmentsRequest(room, office, utcStartDateTime, utcEndDateTime) =>
      //println(s"AppointmentsActor.RoomAppointmentsRequest($room, $startDateTime, $endDateTime)")
      this.resource = room
      this.office = office
      this.utcStartDateTime = utcStartDateTime
      this.utcEndDateTime = utcEndDateTime

      appointments = getRoomAppointments(room.mailbox, utcStartDateTime, utcEndDateTime)
      CalculateTimeSlots

    case DurationDeterminedReceive(subject) =>
      //println(s"AppointmentsActor.DurationDeterminedReceive($subject)")
      duration = subject.duration
      CalculateTimeSlots

    case e @ _ => //println(s"AppointmentsActor. Invalid message '$e'")
  }
}
