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
      exec(http("Go")
        .get("/computers"))
        .pause(3)
    }
  }


  // View

  object View {

    val feederNum = csv("numbers.csv").eager.random


    val view = {
      feed(feederNum)
      exec(http("View")
        .get(s"/computers/${feederNum}"))
        .pause(3)
        .exec(http("View")
          .get("/computers"))
        .pause(3)
    }
  }

  // Add
  object Add {

//    val feederPC = csv("computers.csv").eager.random

    val add = {
      exec(http("Add")
        .get("/computers/new"))
        .pause(3)
//      feed(feederPC)
        .exec(http("Add")
          .post("/computers")
          .headers(headers)
          .formParam("name", {"RandomBook"})
          .formParam("introduced", "")
          .formParam("discontinued", "")
          .formParam("company", {"2"}))
        .pause(3)

    }
  }

  // Delete
  object Delete {

    val delete = repeat(5, "n"){

      exec(http("Delete")
        .get("/computers/?p=${n}"))
        .pause(3)
        .exec(http("Delete")
          .post("/computers/?p={n}/delete")
          .headers(headers))

    }
  }

  // Delete
  object ViewDelete {

    val viewDelete = repeat(5, "n"){

      exec(http("ViewDelete")
        .get("/computers/?p=${n}"))
        .pause(3)
      exec(http("ViewDelete")
        .get("/computers/?p=${n}"))
        .pause(3)
        .exec(http("ViewDelete")
          .post("/computers/?p={n}/delete")
          .headers(headers))

    }
  }




  setUp(
    scenarioObserver.inject(constantConcurrentUsers(500) during (30 minute)),
    scenarioCreator.inject(constantConcurrentUsers(500) during (30 minute)),
    scenarioDestroyer.inject(constantConcurrentUsers(10) during (30 minute)))
      .protocols(httpProtocol)
}










