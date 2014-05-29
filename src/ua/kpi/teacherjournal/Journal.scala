package ua.kpi.teacherjournal

import scala.collection.mutable

object Journal {
  type Column = String
  type Grade = Double

  sealed abstract class Record
  case class GradeRecord(grade: Grade) extends Record
  case object AbsentRecord extends Record
  case object EmptyRecord extends Record

  case class Student(name: String, records: mutable.ArrayBuffer[Record], isBoss: Boolean = false)

  case class Sheet(name: String, columns: mutable.ArrayBuffer[Column], students: mutable.Seq[Student]) {
    require(students.forall(_.records.size == columns.size))
  }
  case class Course(name: String, sheets: Seq[Sheet])

  case class HourMinute(hour: Int, minute: Int) extends Ordered[HourMinute] {
    override def compare(that: HourMinute) = (hour - that.hour) * 60 + (minute - that.minute)
    def -(that: HourMinute) = {
      val diff = (hour - that.hour) * 60 + (minute - that.minute)
      HourMinute(diff / 60, diff % 60)
    }
  }
  case class TimePeriod(start: HourMinute, end: HourMinute)
}
