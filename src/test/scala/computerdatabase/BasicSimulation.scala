package computerdatabase

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class BasicSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://computer-database.gatling.io")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:73.0) Gecko/20100101 Firefox/73.0")

  val headers = Map("Origin" -> "http://computer-database.gatling.io")



  val scenarioObserver = scenario("Scenario Observer").exec(Go.go, View.view)
  val scenarioCreator = scenario("Scenario Creator").exec(Go.go, Add.add)
  val scenarioDestroyer = scenario("Scenario Destroyer").exec(Go.go, ViewDelete.viewDelete)



  // Go

  object Go {
    val go = {
      http("Request").get("http://computer-database.gatling.io")
        .check(status.not(404), status.not(500))
      exec(http("Go")
        .get("/computers"))
        .pause(5 second)
    }
  }


  // View

  object View {


    val view = {

      exec(http("View")
        .get("/computers").check(regex("""<td><a href="/computers/(.*)">""").find.saveAs("numberPC")))
        .pause(5 second)
        .exec(http("View")
          .get("/computers/${numberPC}"))
        .pause(5 second)
    }
  }

  // Add
  object Add {


    val add = {
      exec(http("Add")
        .get("/computers/new"))
        .pause(5 second)
        .exec(http("Add")
          .post("/computers")
          .headers(headers)
          .formParam("name", {"RandomBook"})
          .formParam("introduced", "")
          .formParam("discontinued", "")
          .formParam("company", {"2"}))
        .pause(5 second)

    }
  }


  // Delete
  object ViewDelete {

    val viewDelete = {


      exec(http("ViewDelete")
        .get("/computers").check(regex("""<td><a href="/computers/(.*)">""").find.saveAs("numberPC")))
        .pause(5 second)
        .exec(http("ViewDelete")
          .get("/computers/${numberPC}"))
          .pause(5 second)
        .exec(http("ViewDelete")
          .post("/computers/${numberPC}/delete"))



    }
  }


//Поиск максимальной производительности

//  setUp(
//    scenarioObserver.inject(rampUsers(600) during (60 minute)),
//    scenarioCreator.inject(rampUsers(600) during (60 minute)),
//    scenarioDestroyer.inject(rampUsers(600) during (60 minute)).throttle(holdFor(20 minute))
//    ,
//    scenarioObserver.inject(rampUsers(850) during (40 minute)),
//    scenarioCreator.inject(rampUsers(850) during (40 minute)),
//    scenarioDestroyer.inject(rampUsers(850) during (40 minute)).throttle(holdFor(20 minute))
//    ,
//    scenarioObserver.inject(rampUsers(1100) during (20 minute)),
//    scenarioCreator.inject(rampUsers(1100) during (20 minute)),
//    scenarioDestroyer.inject(rampUsers(1100) during (20 minute)))
//    .protocols(httpProtocol)
//
//}


// Тестирование стабильности
  setUp(
    scenarioObserver.inject(constantConcurrentUsers(1000) during (12 hours)),
    scenarioCreator.inject(constantConcurrentUsers(1000) during (12 hours)),
    scenarioDestroyer.inject(constantConcurrentUsers(1000) during (12 hours)))
    .protocols(httpProtocol)

}










