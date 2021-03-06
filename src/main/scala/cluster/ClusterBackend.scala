package cluster

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.http.scaladsl.model.ws.TextMessage
import cluster.websocket.WSMessagePublisher
import generated.models._

import scala.collection.mutable

object ClusterBackend {
  val WSMessagePublisherRelativeActorPath = WSMessagePublisher.getClass.getSimpleName
}

class ClusterBackend(nodeId: Int) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  var workers = mutable.MutableList[Worker]()

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    createMessagePublisher()
  }

  def createMessagePublisher(): Unit = {
    context.actorOf(Props[WSMessagePublisher], ClusterBackend.WSMessagePublisherRelativeActorPath)
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {
    case AddWorkers(incomingWorkers) => handleAddWorkers(incomingWorkers)

//    case RemoveWorkers(workerCount) => handleRemoveWorkers(workerCount)
//
//    case MoveWorkers(incomingWorkers, destinationActorName, sourceActorName) =>
//      handleMoveWorkers(incomingWorkers)

    case _: MemberEvent => // ignore
  }

  def handleAddWorkers(incomingWorker: Worker): Unit = {

    println("!!!!!!!!!!!!!")
    println(incomingWorker)

//    incomingWorkers.foreach {
//      worker => workers += worker
//    }
    log.info(myWorkersMessage)
    sendMessageToPublisher(myWorkersMessage)
  }

//  def handleRemoveWorkers(workerCount: Int): Unit = {
//    workers = workers.drop(workerCount)
//    sendMessageToPublisher(myWorkersMessage)
//  }

  def myWorkersMessage = s"PI node $nodeId's workers: ${workers.size}"

  def sendMessageToPublisher(messageAsString: String): Unit = {
    val stringPublisherRef = context.actorSelection(ClusterBackend.WSMessagePublisherRelativeActorPath)
    stringPublisherRef ! TextMessage(messageAsString)
  }

//  def handleMoveWorkers(incomingWorkers: Seq[Worker]): Unit = {
//    workers = mutable.MutableList[Worker]()
//    incomingWorkers.foreach {
//      worker => workers += worker
//    }
//    sender() ! WorkersResult(workers)
//  }
}
