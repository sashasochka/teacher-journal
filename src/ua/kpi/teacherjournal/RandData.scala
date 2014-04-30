package ua.kpi.teacherjournal

import scala.util.Random
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

object RandData {
  import Journal._

  val groupNames = Vector("ІС-21", "ІС-22", "ІС-23", "ІС-24", "ІП-21", "ІП-22")
  val courseNames = Vector("Об’єктно-орієнтоване програмування", "Основи програмування", "Математичний аналіз")
  val courses = for (courseName <- courseNames)
    yield Course(courseName, randGroupJournals)

  var groupId = 0
  var courseId = 0
  def selectedCourse = courses(courseId)
  def selectedGroup = selectedCourse.groups(groupId)

  private def randomRecord =
    if (Random.nextBoolean()) GradeRecord(Random.nextInt(10))
    else AbsentRecord

  private def randStudentName =
    Random.alphanumeric.filter(_.isLetter).take(Random.nextInt(8) + 5).mkString.toLowerCase.capitalize

  private def randStudentSeq(nStudents: Int, nColumns: Int) =
    Vector.fill(nStudents)(Student(s"$randStudentName $randStudentName", Vector.fill(nColumns)(randomRecord)))

  private def randSubset[A, CC[X] <: TraversableLike[X, CC[X]]](set: CC[A])
      (implicit bf: CanBuildFrom[CC[A], A, CC[A]]): CC[A] =
    Random.shuffle(set).take(Random.nextInt(set.size - 2) + 1)

  private def randGroupJournals =
    for (groupName <- randSubset(groupNames)) yield {
      val LabRegex = raw"Lab (\d)".r
      val DateRegex = raw"(\d\d)\.(\d\d)".r
      val columns = List.fill(Random.nextInt(15) + 7)(randColumn).distinct.sortBy {
        case LabRegex(labIndex) => labIndex.toInt + 10000
        case DateRegex(day, month) => month.toInt * 31 + day.toInt
        case _ => 0
      }
      GroupSheet(groupName, columns, randStudentSeq(Random.nextInt(10) + 10, columns.size))
    }

  private def randColumn =
    if (Random.nextBoolean()) f"${Random.nextInt(30) + 1}%02d.${Random.nextInt(12) + 1}%02d"
    else s"Lab ${Random.nextInt(10)}"
}