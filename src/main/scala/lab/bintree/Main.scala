package lab.bintree

import akka.actor._

object Node {
	case class Insert(n: Int)
	//case class GetDepth(n: Int = 0)
	case class Depth(n: Int)
	case object GetValue
	case class Value(n: Int)
}

class Node extends Actor with ActorLogging {
	import Node._

	override def preStart = {
		println(s"${self.path.toString}")
	}

	override def postStop = {
		println(s"zamykamy ${self.path.name}")
		if (self.path.name == "root") {
			println("No to koniec…")
			context.system.shutdown()
		}
	}

	def init: Receive = {		
		case Insert(n: Int) =>
			context become withValue(n)


	}

	def withValue(n: Int): Receive = {
		case GetValue =>
			sender() ! Value(n)
		case Insert(a: Int) =>
			if(a > n) { 
				val actor = context.actorOf(Props[Node], "right")
				actor ! Insert(a)
				context become withValueAndOneNode(n, actor)

			} else {
				val actor = context.actorOf(Props[Node], "left")
				actor ! Insert(a)
				context become withValueAndOneNode(n, actor)

			}
			
	}

	def withValueAndOneNode(n: Int, node: ActorRef): Receive = {
		case GetValue =>
			sender() ! Value(n)

		case Insert(a: Int) => 
			if(node.path.name == "left") {
				if(a < n) node ! Insert(a)
				else {
					val actor = context.actorOf(Props[Node], "right")
					actor ! Insert(a)
					context become complete(n, node, actor)
				}
			} else {
				if(a > n) node ! Insert(a)
				else {
					val actor = context.actorOf(Props[Node], "left")
					actor ! Insert(a)
					context become complete(n, actor, node)
				}
			}
	}

	def complete(n: Int, nodeLeft: ActorRef, nodeRight: ActorRef): Receive = {


		case GetValue =>
		    println("test")
			sender() ! Value(n)
		case Insert(a: Int) => 
			if (a < n) nodeLeft ! Insert(a)
			else nodeRight ! Insert(a)

	}

	def receive = init
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
			//root ! GetDepth()
			system.actorSelection("/user/root/right/right/right") ! GetValue

			become {
				case Depth(n) =>
					log.info(s"[Głębokość = $n]")
					root ! PoisonPill
				case Value(n) =>
					log.info(s"[Wartość węzła: $n]")
			}

		})
	}

}