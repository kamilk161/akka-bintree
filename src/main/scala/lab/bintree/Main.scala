package lab.bintree

import akka.actor._

object Node {
	case class Insert(n: Int)
	case class GetDepth(n: Int = 0)
	case class Depth(n: Int)
	case object GetValue
}

class Node extends Actor with ActorLogging {
	import Node._

	override def postStop = {
		println(s"zamykamy ${self.path.name}")
		if (self.path.name == "root") {
			println("No to koniec…")
			context.system.shutdown()
		}
	}

	def receive = ???
}

object Main {
	import akka.actor.PoisonPill

	def main(args: Array[String]): Unit = {
		val system = ActorSystem("system")

		import akka.actor.ActorDSL._
		actor(system, "main")(new Act {
			import Node._
			import system.log
			val root = system.actorOf(Props[Node], "root")
			for (n <- List(3, -5, 2, 4, -7, 0, -4, 12, 9, -1, 20)) {
				root ! Insert(n)
			}
			system.actorSelection("/user/root/right/right/right") ! GetValue
			root ! GetDepth()

			become {
				case Depth(n) =>
					log.info(s"[Głębokość = $n]")
					root ! PoisonPill
				case n: Int =>
					log.info(s"[Wartość węzła: $n]")
			}
		})
	}

}