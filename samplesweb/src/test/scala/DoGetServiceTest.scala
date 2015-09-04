import nl.erik121212.service.TestService
import nl.erik121212.web.TestController.DoGetService
import nl.erik121212.data.TestData

import org.scalatest.FunSuite

// Mock the service layer
trait TestServiceMock extends TestService {
  override def getService = "mock service"
}

// Mock the database layer
trait TestDatabaseMock extends TestData {
  override def getData = "mock data"
}

class DoTest extends FunSuite {
  test("Call the real service") {
    val x = DoGetService
    assert(x == "this is data")
  }

  test("Mock the service layer") {
    class MockService extends TestServiceMock

    val x = new MockService
    assert(x.getService == "mock service")
  }

  test("Mock the data layer") {
    class MockData extends TestDatabaseMock

    val x = new MockData
    assert(x.getData == "mock data")
  }

}