package actors

import actors.AppointmentsActor.{AvailabilityReceive, DurationDeterminedReceive, AdvisorAppointmentsRequest}
import actors.CoordinatorActor.TimeSlotsAdvisorsReceive
import akka.actor.{Actor, ActorRef, Props}
//import nl.ing.api.gCalendarsAPIData.Repository
//import nl.ing.api.gCalendarsAPIData.domain.CustomerSubject
//import nl.erik121212.service
//import nl.ing.api.gCalendarsAPIService.actors.AppointmentsActor.{AdvisorAppointmentsRequest, AvailabilityReceive, DurationDeterminedReceive}
//import nl.ing.api.gCalendarsAPIService.actors.CoordinatorActor.TimeSlotsAdvisorsReceive
//import nl.ing.api.gCalendarsAPIService.actors.CustomerSubjectActor.CustomerSubjectReceive
//import nl.ing.api.gCalendarsAPIService.domain.Availability
import org.joda.time.DateTime
import nl.erik121212.data.Repository
import nl.erik121212.service.domain.Availability


object AvailableTimeSlotsAdvisorActor {
  case class CollectSlotsRequest(office : String , customerSubject : String, utcStartDateTime: DateTime, utcEndDateTime: DateTime)
}

class AvailableTimeSlotsAdvisorActor() extends Actor with Repository {

  private var originator: ActorRef = _
  private var waitingOnNoOfAppointmentActors = -1
//  private var customerSubjectRef : CustomerSubject = _

  private var appointmentActors: List[ActorRef] = Nil
  var availabilities : List[Availability] = Nil


  def  receive = {

    case AvailableTimeSlotsAdvisorActor.CollectSlotsRequest(office, customerSubject, utcStartDateTime, utcEndDateTime) =>
      //println(s"AvailableTimeSlotsAdvisorActor.CollectSlotsRequest($office, $customerSubject, $startDateTime, $endDateTime)")
      originator = sender()

      val advisors = getAdvisors(office, customerSubject, utcStartDateTime, utcEndDateTime)
      if (advisors != null && advisors.length > 0) {
        waitingOnNoOfAppointmentActors = advisors.length

        advisors.foreach(adv => {
          var actor = context.actorOf(Props[AppointmentsActor])
          appointmentActors = actor :: appointmentActors
          actor ! AdvisorAppointmentsRequest(adv, office, utcStartDateTime, utcEndDateTime)
/*          if (customerSubjectRef != null) {
            actor ! DurationDeterminedReceive(customerSubjectRef)
          }*/
        })
      } else {
        waitingOnNoOfAppointmentActors = 0
      }

    case AvailabilityReceive(availability) =>
      //println(s"AvailableTimeSlotsAdvisorActor.AvailabilityReceive($availability)")
      availabilities =  availability :: availabilities

      waitingOnNoOfAppointmentActors -= 1
      if (waitingOnNoOfAppointmentActors == 0) {
        originator ! TimeSlotsAdvisorsReceive(availabilities)
        context.system.stop(self)
      }

/*    case CustomerSubjectReceive(customerSubjRef) =>
      //println(s"AvailableTimeSlotsAdvisorActor.AvailabilityReceive($customerSubjRef)")
      //customerSubjectRef = customerSubjRef
      if (waitingOnNoOfAppointmentActors != -1) {
        if (appointmentActors != Nil) {
          appointmentActors.foreach(actor => actor ! DurationDeterminedReceive(customerSubjectRef))
        } else {
          originator ! TimeSlotsAdvisorsReceive(Nil)
          context.system.stop(self)
        }
      }*/

    case e @ _ => //println(s"AvailableTimeSlotsAdvisorActor. Invalid message '$e'")
  }

}
