package ua.kpi.teacherjournal

import org.scaloid.common._
import android.graphics.Color

case class Student(name: String, marks: Map[String, String])
object StudentDAO {
  val group = "IO-25"
  val students = for (i <- 1 to 50) yield Student(s"Student $i", Map("17.02" -> "3"))
  val dates = Array("17.02", "24.02", "1.03", "8.03", "15.03", "Lab 1", "Lab 2", "Lab 3", "Lab 4", "Lab 5", "Lab 6",
    "Lab 7", "Lab 8", "Lab 9", "Lab 10", "Lab 11")
}

class MainActivity extends SActivity {

  private val headerColor = Color.rgb(0xe7, 0xe7, 0xe7)
  private val cellColor = Color.WHITE
  private val borderColor = Color.rgb(0xcc, 0xcc, 0xcc)

  onCreate {

    import StudentDAO._

    contentView = new SVerticalLayout {

      style {
        case t: STextView => t.textColor(Color.BLACK).maxLines(1).padding(2.dip)
      }

      this += new SScrollView {
        this += new SHorizontalScrollView {
          this += new STableLayout {
            // Header row
            this += new STableRow {
              STextView(group).backgroundColor(headerColor).<<.marginRight(1).marginBottom(1).>>
              for (date <- dates)
                STextView(date).backgroundColor(headerColor).<<.marginRight(1).marginBottom(1).>>
            }
            // Student rows
            for (student <- students.zipWithIndex)
              this += new STableRow {
                STextView((student._2 + 1) + ". " + student._1.name).backgroundColor(headerColor).<<.
                  marginRight(1).marginBottom(1).>>
                for (date <- dates)
                  STextView(student._1.marks.getOrElse(date, "")).backgroundColor(cellColor).<<.marginRight(1).
                    marginBottom(1).>>
              }
          }
        }
      }

    }.backgroundColor(borderColor)
  }
}
