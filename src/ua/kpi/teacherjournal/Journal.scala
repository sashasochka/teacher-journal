package ua.kpi.teacherjournal

object Journal {
  type Column = String
  type Grade = Int

  sealed abstract class Record(val displayString: String = "")
  case class GradeRecord(grade: Grade) extends Record(grade.toString)
  case object AbsentRecord extends Record
  case object EmptyRecord extends Record

  case class Student(name: String, records: Seq[Record])
  case class GroupSheet(name: String, columns: Seq[Column], students: Seq[Student]) {
    require(students.forall(_.records.size == columns.size))
  }
  case class Course(name: String, groups: Seq[GroupSheet])
}
