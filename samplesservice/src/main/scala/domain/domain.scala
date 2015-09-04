package nl.erik121212.service.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.joda.time.DateTime

class Availability(val office : String, val resourceType : String, val resource : String, val duration : Int, val timeSlots : List[DateTime]) {
  //override def toString() =  s"Office:$office; Resource:$resourceType;$resource; Duration:$duration; Slots:[$timeSlots]"
}

case class TimeSlotsPerOffice(
/*                               @JsonProperty("office") val office : String,
                               @JsonProperty("duration") val durationOfSlot : Int,
                               @JsonProperty("timeSlots") val timeSlots : List[TimeSlots]){*/

   val office : String,
  val durationOfSlot : Int,
  val timeSlots : List[TimeSlots]){

  override def toString =  s"Office:$office; Duration:$durationOfSlot; Slots:[$timeSlots]"
}

case class TimeSlots(

/*                      @JsonProperty("utcStart") val utcStartTime : String,
                      @JsonProperty("advisors") val advisors : List[String],
                      @JsonProperty("rooms") val rooms : List[String]){*/

                      val utcStartTime : String,
                      val advisors : List[String],
                      val rooms : List[String]){
  override def toString =  s"Start:$utcStartTime; Advisors:$advisors; Rooms:[$rooms]"
}