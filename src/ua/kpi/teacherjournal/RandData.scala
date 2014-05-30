package ua.kpi.teacherjournal

import scala.collection.mutable.ArrayBuffer
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.util.Random

object RandData {
  import Journal._

  val groupNames = Vector("ІС-21", "ІС-22", "ІС-23", "ІС-24", "ІП-21", "ІП-22")
  val studentNames = Vector("Віктор", "Михайло", "Антон", "Дмитро", "Олексій", "Роман",
    "Андрій", "Євгеній", "Микола", "Віталій", "Едуард", "Влад", "Юрій", "Денис",
    "Поліна", "Євгенія", "Ірина", "Олена", "Дарина", "Софія")
  val studentSurnames = Vector("Батицький", "Шмалько", "Франчук", "Хусейн", "Денькін", "Палій", "Кіндзерський",
    "Шкель", "Трощук", "Гулий", "Баранюк", "Квачук", "Лопачук", "Пащук", "Опанасенко",
    "Сочка", "Левицький", "Ленець", "Панійван", "Гук", "Місюра", "Головань", "Базелюк",
    "Калапуша", "Поляков", "Талашко", "Борисов")
  val courseNames = Vector("Об’єктно-орієнтоване програмування", "Основи програмування", "Математичний аналіз")

  def randomRecord =
    if (Random.nextBoolean()) GradeRecord(Random.nextInt(10).toDouble)
    else if (Random.nextBoolean()) AbsentRecord
    else EmptyRecord

  def randomStudentName =
    studentSurnames(Random.nextInt(studentSurnames.size)) + " " + studentNames(Random.nextInt(studentNames.size))

  def randomStudentSeq(nStudents: Int, nColumns: Int) =
    Array.fill(nStudents)(Student(randomStudentName, ArrayBuffer.fill(nColumns)(randomRecord))).sortBy(_.name)

  private def randomSubset[A, CC[X] <: TraversableLike[X, CC[X]]](set: CC[A])
      (implicit bf: CanBuildFrom[CC[A], A, CC[A]]): CC[A] =
    Random.shuffle(set).take(Random.nextInt(set.size - 2) + 1)

  def randomSheets = {
    val nRows = Random.nextInt(10) + 10
    val nCols = Random.nextInt(15) + 7
    val LabRegex = raw"Lab (\d)".r
    val DateRegex = raw"(\d\d)\.(\d\d)".r
    (for (groupName <- randomSubset(groupNames)) yield {
      val columns = ArrayBuffer.fill(nCols)(randomColumnName).distinct.sortBy {
        case LabRegex(labIndex) => labIndex.toInt + 10000
        case DateRegex(day, month) => month.toInt * 31 + day.toInt
        case _ => 0
      }
      val students = randomStudentSeq(nRows, columns.size)
      val bossIndex = Random.nextInt(students.size)
      students(bossIndex) = students(bossIndex).copy(isBoss = true)
      Sheet(groupName, columns, students)
    }).sortBy(_.name)
  }

  def randomColumnName =
    if (Random.nextBoolean()) f"${Random.nextInt(30) + 1}%02d.${Random.nextInt(12) + 1}%02d"
    else s"Lab ${Random.nextInt(10)}"
}
