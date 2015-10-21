package strata.data

import java.io.File
import java.lang.management.ManagementFactory
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import com.sun.xml.internal.messaging.saaj.util.Base64
import strata.GlobalOptions
import strata.tasks.{InitialSearchSuccess, InitialSearchTask, TaskResult, Task}
import strata.util.{TimingInfo, IO}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json4s
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.reflect.Reflector
import scala.sys.process._


/**
 * Helper methods to deal with log entries.
 */
object Log {

  case object DateTimeSerializer extends CustomSerializer[DateTime](format => ({
    case JString(s) => new DateTime(s)
    case JNull => null
  }, {
    case d: DateTime => JString(format.dateFormat.format(d.toDate))
  }))

  def serializeMessage(logMessage: LogMessage): String = {
    implicit val formats = Serialization.formats(FullTypeHints(List(
      classOf[Task],
      classOf[TaskResult],
      classOf[LogMessage]
    ))) ++ Vector(DateTimeSerializer)
    write(logMessage)
  }

  def deserializeMessage(s: String): LogMessage = {
    implicit val formats = org.json4s.DefaultFormats ++
      Vector(
        new CustomSerializer[Task](serializerImpl),
        new CustomSerializer[TaskResult](serializerImpl),
        new CustomSerializer[LogMessage](serializerImpl),
        DateTimeSerializer
      )
    parse(s).extract[LogMessage]
  }

  def serializerImpl[A]: (Formats) => (PartialFunction[json4s.JValue, A], PartialFunction[Any, JsonAST.JString]) = {
    format => ({
      case JObject(JField("jsonClass", JString(className)) :: l) =>
        implicit val formats = format
        Extraction.extract(JObject(l), Reflector.scalaTypeOf(className).get).asInstanceOf[A]
    }, {
      case _ if false => JString("")
    })
  }

  def test() = {
//    val task = InitialSearchTask(GlobalOptions(), Instruction("xorb_r8_r8"), 100)
//    val res = InitialSearchSuccess(task, TimingInfo(Map("a" -> 2)))
//    serializeMessage(LogTaskEnd(task, Some(res)))
  }
}

trait Top
case class A(i: Int) extends Top
case class B(globalOptions: GlobalOptions, instruction: Instruction, budget: Long) extends Top

trait Top2
case class A2(i: Int) extends Top2
case class B2(s: String) extends Top2

/**
 * Context information
 *
 * @param hostname hostname
 * @param pid process id
 * @param tid thread id
 */
case class ThreadContext(hostname: String, pid: Long, tid: Long) {
  override def toString = {
    val hostMinLength = "pinkman01".length + 3
    val host = hostname + (" " * (hostMinLength - hostname.length))
    f"$host / $pid%6d / $tid%6d"
  }

  def fileNameSafe: String = {
    s"$hostname-$pid-$tid"
  }
}

object ThreadContext {
  private var host: Option[String] = None

  def self: ThreadContext = {
    host match {
      case None =>
        host = Some("hostname".!!.stripLineEnd)
      case _ =>
    }
    val pid: Int = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    val tid = Thread.currentThread().getId
    ThreadContext(host.get, pid, tid)
  }
}

sealed trait LogMessage {
  /** The date the log message was generated. */
  def time: DateTime

  /** Where was this message generated. */
  def context: ThreadContext

  override def toString = {
    s"[ ${IO.formatTime(time)} / $context ]"
  }
}

trait LogEnd extends LogMessage

case class LogEntryPoint(arguments: Seq[String], gitHash: String = IO.getGitHash,
                         time: DateTime = DateTime.now(), context: ThreadContext = ThreadContext.self) extends LogMessage {
  override def toString = {
    super.toString + s": entry point: denali ${arguments.mkString(" ")}"
  }
}

case class LogInitEnd(timing: TimingInfo, time: DateTime = DateTime.now(), context: ThreadContext = ThreadContext.self) extends LogEnd {
  override def toString = {
    super.toString + ": initialize end"
  }
}

case class LogTaskEnd(task: Task, res: Option[TaskResult], pseudoTimeEnd: Int, time: DateTime = DateTime.now(), context: ThreadContext = ThreadContext.self) extends LogEnd {
  override def toString = {
    res match {
      case None => super.toString + s": task end: '$task' without result"
      case Some(r) => super.toString + s": task end: '$task' with result '$r'"
    }
  }
}

case class LogError(msg: String, time: DateTime = DateTime.now(), context: ThreadContext = ThreadContext.self) extends LogMessage {
  override def toString = {
    super.toString + s": error: $msg"
  }
}

case class LogVerifyResult(instr: Instruction,
                           formal: Boolean,
                           verifyResult: StokeVerifyOutput,
                           program1: String,
                           program2: String,
                           time: DateTime = DateTime.now(),
                           context: ThreadContext = ThreadContext.self) extends LogMessage {
  override def toString = {
    assert(!verifyResult.hasError)
    (verifyResult.verified, verifyResult.counter_examples_available) match {
      case (true, true) => assert(false); ""
      case (true, false) => super.toString + s": verification for $instr: verified"
      case (false, false) => super.toString + s": verification for $instr: unknown"
      case (false, true) => super.toString + s": verification for $instr: counterexample"
    }
  }
}