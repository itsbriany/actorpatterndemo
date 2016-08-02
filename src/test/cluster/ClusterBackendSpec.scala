package cluster

/**
  * Created by Brian.Yip on 7/21/2016.
  */

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import generated.models.{MoveWorkers, Worker, WorkersResult}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


// Asynchronous testing
class ClusterBackendSpec() extends TestKit(ActorSystem("ClusterBackendSpec"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "An Echo actor" must {
    "send back messages unchanged" in {
      val echo = system.actorOf(TestActors.echoActorProps)

      // The echo actor sends itself a message and should expect hello world
      echo ! "hello world"
      expectMsg("hello world")
    }
  }

  // Note that in these unit tests, actors will just be sending messages to themselves
  "A ClusterBackend" must {

    "update its workers when it receives an MoveWorkers message" in {
      val clusterBackend = system.actorOf(Props[ClusterBackend])
      val oneWorker = Seq[Worker](new Worker("Alice"))
      clusterBackend ! MoveWorkers(oneWorker)
      expectMsg(WorkersResult(oneWorker))

      val threeWorkers = Seq[Worker](new Worker("Alice"), new Worker("Bob"), new Worker("Charlie"))
      clusterBackend ! MoveWorkers(threeWorkers)
      expectMsg(WorkersResult(threeWorkers))
    }

  }
}
