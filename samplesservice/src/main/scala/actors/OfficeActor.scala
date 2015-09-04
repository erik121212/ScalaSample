package actors

import actors.OfficeActor.{LocationToOfficesReceive, LocationToOfficesRequest}
import akka.actor.Actor
//import nl.ing.api.gCalendarsAPIService.actors.OfficeActor.LocationToOfficesReceive

object OfficeActor {
  case class LocationToOfficesRequest(location : String)
  case class LocationToOfficesReceive(offices : List[String])
}
class OfficeActor(private val locationsApiUri : String) extends Actor {
  def  receive = {

    case LocationToOfficesRequest(location) =>
      // Faking the Call to the LocationsAPI to get the offices based on the office
//      Thread.sleep(location match {
//        case "Amsterdam" => 16
//        case "Utrecht" => 14
//        case _ => 12
//      })
      // Result of the LocationsApi
      val offices = location match {
        case "Amsterdam" => List("Office1", "Office2", "Office3", "Office4", "Office5", "Office6")
        case "Utrecht" => List("Office7", "Office8", "Office9", "Office10")
        case _ => List("Office11", "Office12")
      }
      offices.foreach(office => {
        println(office)
      })
      sender ! LocationToOfficesReceive(offices)
      // Cleanup this actor, since we ready.
      context.system.stop(self)

    case e @ _ => //println(s"CustomerSubjectActor. Invalid message '$e'")
  }
}
