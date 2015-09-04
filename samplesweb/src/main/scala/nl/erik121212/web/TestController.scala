package nl.erik121212.web

import com.typesafe.config.ConfigFactory
import nl.erik121212.service.TestService
import com.typesafe.config


object TestController extends TestService {


  def main(args: Array[String]) {
    println("In Main: " + DoGetService)

    val config = ConfigFactory.load("test.properties")
     val level = config.getString("level")
    println("level is: " + level)
  }

  def DoGetService = getService
}
