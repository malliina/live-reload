package tests

import akka.actor.ActorSystem
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Source}
import org.scalatest.FunSuite

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class StreamTests extends FunSuite {
  ignore("combo") {
    implicit val as = ActorSystem("test")
    val (sink, source) =
      MergeHub
        .source[String](perProducerBufferSize = 16)
        .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
        .run()
    val job = source.take(2).runForeach(println)
    val job2 = source.take(2).runForeach(println)
    Source.single("hello").to(sink).run()
    Source.single("hello2").to(sink).run()
    await(job)
    await(job2)
    await(as.terminate())
  }

  def await[T](f: Future[T]): T = Await.result(f, 10.seconds)
}
