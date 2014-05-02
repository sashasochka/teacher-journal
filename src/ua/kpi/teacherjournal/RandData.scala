package ua.kpi.teacherjournal

import scala.util.Random
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

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
  val courses = for (courseName <- courseNames)
    yield Course(courseName, randomSheets)

  var sheetId = 0
  var courseId = 0
  def selectedCourse = courses(courseId)
  def selectedSheet = selectedCourse.sheets(sheetId)

  private def randomRecord =
    if (Random.nextBoolean()) GradeRecord(Random.nextInt(10))
    else if (Random.nextBoolean()) AbsentRecord
    else EmptyRecord

  private def randomStudentName =
    studentSurnames(Random.nextInt(studentSurnames.size)) + " " + studentNames(Random.nextInt(studentNames.size))

  private def randomStudentSeq(nStudents: Int, nColumns: Int) =
    Vector.fill(nStudents)(Student(randomStudentName, Vector.fill(nColumns)(randomRecord))).sortBy(_.name)

  private def randomSubset[A, CC[X] <: TraversableLike[X, CC[X]]](set: CC[A])
      (implicit bf: CanBuildFrom[CC[A], A, CC[A]]): CC[A] =
    Random.shuffle(set).take(Random.nextInt(set.size - 2) + 1)

  private def randomSheets =
    (for (groupName <- randomSubset(groupNames)) yield {
      val LabRegex = raw"Lab (\d)".r
      val DateRegex = raw"(\d\d)\.(\d\d)".r
      val columns = List.fill(Random.nextInt(15) + 7)(randColumn).distinct.sortBy {
        case LabRegex(labIndex) => labIndex.toInt + 10000
        case DateRegex(day, month) => month.toInt * 31 + day.toInt
        case _ => 0
      }
      val students = randomStudentSeq(Random.nextInt(10) + 10, columns.size)
      val bossIndex = Random.nextInt(students.size)
      val studentsWithBoss = students.updated(bossIndex, students(bossIndex).copy(isBoss = true))
      Sheet(groupName, columns, studentsWithBoss)
    }).sortBy(_.name)

  private def randColumn =
    if (Random.nextBoolean()) f"${Random.nextInt(30) + 1}%02d.${Random.nextInt(12) + 1}%02d"
    else s"Lab ${Random.nextInt(10)}"
}
