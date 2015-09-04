package actors

import actors.CoordinatorActor.TimeSlotsRequest
import actors.OfficeActor.{LocationToOfficesReceive, LocationToOfficesRequest}
import akka.actor.{Actor, ActorRef, Props}
import nl.erik121212.data.Repository
import nl.erik121212.service.domain.Availability

/*import nl.ing.api.gCalendarsAPIData.Repository
import nl.ing.api.gCalendarsAPIData.domain.CustomerSubject


import nl.ing.api.gCalendarsAPIService.actors.CoordinatorActor.{TimeSlotsResponse, TimeSlotsAdvisorsReceive, TimeSlotsRequest, TimeSlotsRoomsReceive}
import nl.ing.api.gCalendarsAPIService.actors.CustomerSubjectActor.CustomerSubjectReceive
import nl.ing.api.gCalendarsAPIService.actors.OfficeActor.{LocationToOfficesReceive, LocationToOfficesRequest}
import nl.ing.api.gCalendarsAPIService.domain.Availability*/
import org.joda.time.DateTime

object CoordinatorActor {
case class TimeSlotsRequest(location : String , customerSubjectKey : String, utcStartDateTime: DateTime, utcEndDateTime: DateTime)
  case class TimeSlotsResponse(duration: Int, availabilities : List[Availability])
  case class TimeSlotsAdvisorsReceive(availabilities : List[Availability])
  case class TimeSlotsRoomsReceive(availabilities : List[Availability])
}
class CoordinatorActor extends Actor with Repository {

  //private[this] val customerSubjectActor = context.actorOf(Props[CustomerSubjectActor])
  private[this] val officeActor = context.actorOf(Props(new OfficeActor("https://xxx/api/locations"))) // :TODO: find the correct url of the locationsAPI
  private[this] var originator: ActorRef = _

  private[this] var availTimeslotsAdvisorsActors : List[ActorRef] = Nil
  private[this] var waitingOnNoOfAdvActors = -1
  private[this] var availTimeslotsRoomsActors : List[ActorRef] = Nil
  private[this] var waitingOnNoOfRoomActors = -1

  private[this] var utcStartDateTime : DateTime = _
  private[this] var utcEndDateTime : DateTime = _
  private var customerSubjectKey : String = ""
  /*private var customerSubjectRef : CustomerSubject = _

  private[this] var advAppointments : List[Availability] = Nil
  private[this] var roomAppointments : List[Availability] = Nil

  private def DeliverTimeSlots = {
    if (waitingOnNoOfAdvActors == 0 && waitingOnNoOfRoomActors == 0) {
      originator ! TimeSlotsResponse(customerSubjectRef.duration, advAppointments ++ roomAppointments)
      context.system.stop(self)
    }
  }

  private def RedirectCustomerSubject = {
    if (customerSubjectRef != null && availTimeslotsAdvisorsActors != Nil && availTimeslotsRoomsActors != Nil) {
      availTimeslotsAdvisorsActors.foreach(actor => actor ! CustomerSubjectReceive(customerSubjectRef))
      availTimeslotsRoomsActors.foreach(actor => actor ! CustomerSubjectReceive(customerSubjectRef))
    }
  }*/

  def  receive = {
      case TimeSlotsRequest(location, customerSubjKey, uctStart, utcEnd) =>
        println(s"${location} ${customerSubjKey}, ${uctStart}, ${utcEnd}")
        officeActor ! LocationToOfficesRequest(location)
      /*        println(s"CoordinatorActor.TimeSlotsRequest($location, $customerSubjectKey, $sDateTime, $eDateTime)")
              originator = sender()
              utcStartDateTime = uctStart
              utcEndDateTime = utcEnd
              customerSubjectKey = customerSubjKey
              officeActor ! LocationToOfficesRequest(location)
              customerSubjectActor ! CustomerSubjectActor.CustomerSubjectRequest(customerSubjectKey)*/

      case LocationToOfficesReceive(offices) =>
        //println(s"CoordinatorActor.LocationToOfficesReceive($offices)")
        waitingOnNoOfAdvActors = offices.length
        waitingOnNoOfRoomActors = offices.length
        offices.foreach(office => {
          val availAdvisorsActor =  context.actorOf(Props[AvailableTimeSlotsAdvisorActor])
          availTimeslotsAdvisorsActors = availAdvisorsActor :: availTimeslotsAdvisorsActors
          availAdvisorsActor ! AvailableTimeSlotsAdvisorActor.CollectSlotsRequest(office, customerSubjectKey, utcStartDateTime, utcEndDateTime)

          val availRoomsActor = context.actorOf(Props[AvailableTimeSlotsRoomActor])
          availTimeslotsRoomsActors = availRoomsActor :: availTimeslotsRoomsActors
          availRoomsActor ! AvailableTimeSlotsRoomActor.CollectSlotsRequest(office, customerSubjectKey, utcStartDateTime, utcEndDateTime)
        })
    //    RedirectCustomerSubject

      /*      case CustomerSubjectReceive(customerSubjRef) =>
              //println(s"CoordinatorActor.CustomerSubjectReceive($customerSubjRef)")
              customerSubjectRef = customerSubjRef
              RedirectCustomerSubject

            case TimeSlotsAdvisorsReceive(availabilities) =>
              //println(s"CoordinatorActor.TimeSlotsAdvisorsReceive($availabilities)")
              advAppointments = advAppointments ++ availabilities
              waitingOnNoOfAdvActors -= 1
              DeliverTimeSlots

            case TimeSlotsRoomsReceive(availabilities) =>
              //println(s"CoordinatorActor.TimeSlotsRoomsReceive($availabilities)")
              roomAppointments = roomAppointments ++ availabilities
              waitingOnNoOfRoomActors -= 1
              DeliverTimeSlots
        */
      case e @ _ => //println(s"CoordinatorActor. Invalid message '$e'")

  }
}
