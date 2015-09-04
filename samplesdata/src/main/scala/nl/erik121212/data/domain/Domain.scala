package nl.erik121212.data.domain

import java.util.TimeZone

import org.joda.time.DateTime

/**
 * Created by m07h817 on 17/Aug/2015.
 */
class WorkingDay(val timeZone : TimeZone, val weekday : Int, val startHour : Int, val endHour : Int) {
  override def toString() =  s"$timeZone $weekday $startHour:00 - $endHour:00"
}
abstract class Resource(val office : String, val workingDays : List[WorkingDay]) {
  def key : String
  def `type` : String

}
class Advisor( val corporateKey:String, office : String, workingDays : List[WorkingDay]) extends Resource(office, workingDays) {
  override def key = corporateKey
  override def `type` = "A"
  override def toString() =  s"$corporateKey ($workingDays)"
}

class Room(val mailbox:String, office : String, workingDays : List[WorkingDay]) extends Resource(office, workingDays) {
  override def key = mailbox
  override def `type` = "R"
  override def toString() =  s"$mailbox ($workingDays)"
}

class Appointment(val subject: String, val utcStartTime : DateTime, val utcEndTime : DateTime) {
  override def toString() =  s"'$subject' at $utcStartTime/$utcEndTime"
}

class CustomerSubject(val subject: String, val duration:Int) {
  override def toString() = s"'$subject' takes ($duration) minutes"
}