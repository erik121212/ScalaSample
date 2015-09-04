package actors

import actors.AppointmentsActor.{DurationDeterminedReceive, AvailabilityReceive, RoomAppointmentsRequest}
import actors.CoordinatorActor.TimeSlotsRoomsReceive
import akka.actor.{Props, ActorRef, Actor}
import nl.erik121212.data.Repository
import nl.erik121212.service.domain.Availability
/*import nl.ing.api.gCalendarsAPIData.Repository
import nl.ing.api.gCalendarsAPIData.domain.CustomerSubject
import nl.ing.api.gCalendarsAPIService.actors.AppointmentsActor.{RoomAppointmentsRequest, AvailabilityReceive, DurationDeterminedReceive}
import nl.ing.api.gCalendarsAPIService.actors.CoordinatorActor.TimeSlotsRoomsReceive
import nl.ing.api.gCalendarsAPIService.actors.CustomerSubjectActor.CustomerSubjectReceive
import nl.ing.api.gCalendarsAPIService.domain.Availability*/
import org.joda.time.DateTime

object AvailableTimeSlotsRoomActor {
  case class CollectSlotsRequest(location : String , customerSubject : String, utcStartDateTime: DateTime, utcEndDateTime: DateTime)
}

class AvailableTimeSlotsRoomActor () extends Actor with Repository {

  private var originator: ActorRef = _
  private var waitingOnNoOfAppointmentActors = -1
 // private var customerSubjectRef : CustomerSubject = _

  private var appointmentActors: List[ActorRef] = Nil
  var availabilities : List[Availability] = Nil

  def  receive = {

    case AvailableTimeSlotsRoomActor.CollectSlotsRequest(office, customerSubject, utcStartDateTime, utcEndDateTime) =>
      //println(s"AvailableTimeSlotsRoomActor.CollectSlotsRequest($office, $customerSubject, $utcStartDateTime, $utcEndDateTime)")
      originator = sender()

      val rooms = getRooms(office, customerSubject, utcStartDateTime, utcEndDateTime)
      if (rooms != null && rooms.length > 0) {
        waitingOnNoOfAppointmentActors = rooms.length

        rooms.foreach(room => {
          var actor = context.actorOf(Props[AppointmentsActor])
          appointmentActors = actor :: appointmentActors
          actor ! RoomAppointmentsRequest(room, office, utcStartDateTime, utcEndDateTime)
/*          if (customerSubjectRef != null) {
            actor ! DurationDeterminedReceive(customerSubjectRef)
          }*/
        })
      } else {
        waitingOnNoOfAppointmentActors = 0
      }

    case AvailabilityReceive(availability) =>
      //println(s"AvailableTimeSlotsRoomActor.AvailabilityReceive($availability)")
      availabilities =  availability :: availabilities

      waitingOnNoOfAppointmentActors -= 1
      if (waitingOnNoOfAppointmentActors == 0) {
        originator ! TimeSlotsRoomsReceive(availabilities)
        context.system.stop(self)
      }


/*    case CustomerSubjectReceive(customerSubjRef) =>
      //println(s"AvailableTimeSlotsRoomActor.CustomerSubjectReceive($customerSubjRef)")
      customerSubjectRef = customerSubjRef
      if (waitingOnNoOfAppointmentActors != -1) {
        if (appointmentActors != Nil) {
          appointmentActors.foreach(actor => actor ! DurationDeterminedReceive(customerSubjectRef))
        } else {
          originator ! TimeSlotsRoomsReceive(Nil)
          context.system.stop(self)
        }
      }*/

    case e @ _ => //println(s"AvailableTimeSlotsRoomActor. Invalid message '$e'")
  }

}
