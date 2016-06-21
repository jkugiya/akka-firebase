akka-firebase
=========================
A Firebase extension for Akka.

# Usage
You can register the extension in application.conf:

```bash
akka {
    extensions = ["jkugiya.akka.extension.firebase.Firebase"]
}
```

Get database reference and set value:

```scala
import akka.actor._
import com.typesafe.config.ConfigFactory

val config = ConfigFactory.parseString("""
akka {
    extensions = ["jkugiya.akka.extension.firebase.Firebase"]
}
ext-firebase {
  database-url = ""
  service-account = ""
}"""

val system = ActorSystem()
val db = Firebase(system).database
val ref = db.getReference("/example")

ref.setValue("Hello World!")

```

Create database command handler:

```scala
import akka.actor.{ Actor, ActorRef, Props }
import com.google.firebase.tasks.{ OnFailureListener => OriginalOnFailureListener }
import jkugiya.akka.extension.firebase.Firebase

object DatabaseCommandHandler {

  trait Command

  final case class SetValue[A](value: A, replyTo: ActorRef) extends Command

  final case class RemoveValue(replyTo: ActorRef) extends Command

  final case class UpdateChildren(children: Map[String, AnyRef], replyTo: ActorRef) extends Command

  sealed trait Event

  final case class SuccessEvent[A](value: A) extends Event

  final case class FailureEvent(t: Throwable) extends Event

  def props(path: String, appName: Option[String]): Props = Props(classOf[DatabaseCommandHandler], path, appName)
}

class DatabaseCommandHandler(path: String, appName: Option[String]) extends Actor {
  import DatabaseCommandHandler._

  private val databaseReference = appName.map { appName =>
    Firebase(context.system).databaseForAppName(appName).getReference(path)
  } getOrElse {
    Firebase(context.system).database.getReference(path)
  }

 def receive: Receive = {
    case SetValue(value, replyTo) =>
      val task = databaseReference.setValue(value)
      addOnFailureListener(task, replyTo)
    case RemoveValue(replyTo) =>
      val task = databaseReference.removeValue()
      addOnFailureListener(task, replyTo)
    case UpdateChildren(children, replyTo) =>
      import collection.JavaConverters._
      val task = databaseReference.updateChildren(children.asJava)
      addOnFailureListener(task, replyTo)
  }

  private[this] def addOnFailureListener(task: Task[_], replyTo: ActorRef): Unit = {
    task.addOnFailureListener(new OnFailureListener(replyTo))
  }
}

class OnFailureListener(replyTo: ActorRef) extends OriginalOnFailureListener {
  import DatabaseCommandHandler._
  override def onFailure(e: Exception): Unit = replyTo ! FailureEvent(e)
}

import akka.actor._
import DatabaseCommandHandler._
val system = ActorSystem()
system.actorOf(Props(classOf[DatabaseCommandHandler], "example", None)) ! SetValue("Hello World", Actor.noSender)
```

