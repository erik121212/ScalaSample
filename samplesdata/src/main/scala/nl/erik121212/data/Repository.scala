package nl.erik121212.data

import java.util.TimeZone

import nl.erik121212.data.domain._
//import nl.erik121212.data.Repository
/*import nl.ing.api.gCalendarsAPIData.domain._
import nl.ing.supernova.driver.cassandra.cql.exception.CqlException
import nl.ing.supernova.driver.cassandra.repository.CassandraRepository*/
import org.joda.time.{DateTime, DateTimeZone}

/*object CassandraRepository //extends CassandraRepositoryConfig {
 /* val cassandraRepository = new CassandraRepository(config)

  @throws(classOf[CqlException])
  private def createPreparedStatement(cql: String): PreparedStatement = {    return cassandraRepository.getSession.prepare(cql)   }*/
  val getRooms = createPreparedStatement("select * from cal_availability where customerquestion = ? and office = ? and resourcetype='R'")

  def getRooms(office : String , customerSubject : String, utcStartDateTime: DateTime, utcEndDateTime: DateTime) : List[Room] = {

    case class RawData(val room : String, val timezone : String, val weekday: Int, val starttime: Int, val endtime: Int) {}

    val binding : Array[AnyRef] = Array(customerSubject, office)//,utcStartDateTime.getMillis.asInstanceOf[AnyRef], utcEndDateTime.getMillis.asInstanceOf[AnyRef])

    val rows : java.util.List[Row] = cassandraRepository.execute(getRooms, ConsistencyLevel.LOCAL_QUORUM, binding)
    var itRows = rows.iterator()
    var rawDatas : List[RawData] = Nil
    while (itRows.hasNext){
      val row = itRows.next()
      rawDatas = RawData(row.getString("resource"), row.getString("timezone"), row.getInt("weekday"), row.getInt("starttime"), row.getInt("endtime")) :: rawDatas
    }
    val rooms = for {
      (rK, rV) <- rawDatas.groupBy(r => r.room)
    } yield new Room(rK, office, for {wd <- rV} yield new WorkingDay(TimeZone.getTimeZone(wd.timezone), wd.weekday, wd.starttime, wd.endtime))
    rooms.toList
  }
}*/

trait Repository {

  def getRooms(office : String , customerSubject : String, utcStartDateTime: DateTime, utcEndDateTime: DateTime) : List[Room] = {
   // CassandraRepository.getRooms(office, customerSubject , utcStartDateTime, utcEndDateTime)
    Nil
  }

  def getAdvisors(office : String , customerSubject : String, utcStartDateTime: DateTime, utcEndDateTime: DateTime) : List[Advisor] = {
    val r = scala.util.Random
    val listOfAdvisors = customerSubject match {
      case "Wonen_6" => List.tabulate(6)(n => new Advisor(s"advisor${n+1}", office, List.tabulate(3+r.nextInt(3))(n => new WorkingDay(TimeZone.getTimeZone("CET"),n,9+r.nextInt(3),13+r.nextInt(6)))))
      case "Wonen_40" => List.tabulate(40)(n => new Advisor(s"advisor${n+1}", office, List.tabulate(3+r.nextInt(3))(n => new WorkingDay(TimeZone.getTimeZone("CET"),n,9+r.nextInt(3),13+r.nextInt(6)))))
      case _  => List.tabulate(2)(n => new Advisor(s"advisor${n+1}", office, List.tabulate(3+r.nextInt(3))(n => new WorkingDay(TimeZone.getTimeZone("CET"),n,9+r.nextInt(3),13+r.nextInt(6)))))
    }

    listOfAdvisors
    //List (new Advisor ("AA11CC"), new Advisor ("BB11CC"), new Advisor ("CC11CC"), new Advisor ("DD11CC") )
  }
  def getCustomerSubject(customerSubject : String) : CustomerSubject = {

    new CustomerSubject(customerSubject, 45) //default duration of 45 minutes
  }
  def getRoomAppointments(room : String, utcStartDateTime: DateTime, utcEndDateTime: DateTime) : List[Appointment] = {
    List(
      new Appointment("subj 1", new DateTime(2015,10,5,8,0,0, DateTimeZone.UTC), new DateTime(2015,10,5,9,0,0, DateTimeZone.UTC)), // 9:00 - 10:00 ETC ( 10:00 - 11:00 ETC with DST)
      new Appointment("subj 2", new DateTime(2015,10,5,11,0,0, DateTimeZone.UTC), new DateTime(2015,10,5,12,0,0, DateTimeZone.UTC)),// 12:00 - 13:00 ETC ( 13:00 - 14:00 ETC with DST)
      new Appointment("subj 3", new DateTime(2015,10,6,14,0,0, DateTimeZone.UTC), new DateTime(2015,10,5,15,0,0, DateTimeZone.UTC)),// 15:00 - 16:00 ETC ( 16:00 - 17:00 ETC with DST)
      new Appointment("subj 4", new DateTime(2015,10,7,19,0,0, DateTimeZone.UTC), new DateTime(2015,10,7,20,0,0, DateTimeZone.UTC)) // 20:00 - 21:00 ETC ( 21:00 - 22:00 ETC with DST)
    )
  }
  def getAdvisorAppointments(advisor : String, office : String, utcStartDateTime: DateTime, utcEndDateTime: DateTime) : List[Appointment] = {
    List(
      new Appointment("subj 1", new DateTime(2015,10,5,8,0,0, DateTimeZone.UTC), new DateTime(2015,10,5,9,0,0, DateTimeZone.UTC)), // 9:00 - 10:00 ETC ( 10:00 - 11:00 ETC with DST)
      new Appointment("subj 2", new DateTime(2015,10,5,11,0,0, DateTimeZone.UTC), new DateTime(2015,10,5,12,0,0, DateTimeZone.UTC)),// 12:00 - 13:00 ETC ( 13:00 - 14:00 ETC with DST)
      new Appointment("subj 3", new DateTime(2015,10,6,14,0,0, DateTimeZone.UTC), new DateTime(2015,10,5,15,0,0, DateTimeZone.UTC)),// 15:00 - 16:00 ETC ( 16:00 - 17:00 ETC with DST)
      new Appointment("subj 4", new DateTime(2015,10,7,19,0,0, DateTimeZone.UTC), new DateTime(2015,10,7,20,0,0, DateTimeZone.UTC)) // 20:00 - 21:00 ETC ( 21:00 - 22:00 ETC with DST)
    )
  }

}
