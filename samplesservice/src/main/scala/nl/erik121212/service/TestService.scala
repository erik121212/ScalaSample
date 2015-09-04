package nl.erik121212.service

import akka.util.Timeout
import actors.CoordinatorActor
import actors.CoordinatorActor.TimeSlotsRequest
import nl.erik121212.data.TestData
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import org.joda.time.DateTime
import scala.concurrent.duration._

import nl.erik121212.service.domain.TimeSlotsPerOffice
/*
import org.joda.time.DateTime
*/


object ActorSystemRepository {

  private val system = ActorSystem("TimeslotService")

  def getActorSystem = system
}


trait TestService extends TestData {



  def getService = {
    getTimeSlots("location", "customerSubject",DateTime.now(), DateTime.now)
    getData
  }

  def getTimeSlots(location : String , customerSubject : String, utcStartDateTime : org.joda.time.DateTime, utcEndDateTime : org.joda.time.DateTime) : List[TimeSlotsPerOffice] = {


    val coordinatorActor = ActorSystemRepository.getActorSystem.actorOf(Props[CoordinatorActor])

    implicit val timeout = Timeout(3 seconds)
    val slotsFuture = coordinatorActor ? TimeSlotsRequest("location", "customerSubject", utcStartDateTime, utcEndDateTime)

    Nil
  }
}

