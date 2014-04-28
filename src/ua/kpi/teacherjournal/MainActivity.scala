package ua.kpi.teacherjournal

import org.scaloid.common._
import android.app.ActionBar._
import android.graphics.Color._
import scala.language.postfixOps
import android.view.Gravity
import scala.util.Random

case class Student(name: String, marks: Map[String, String])

object StudentDAO {
  def randomMark =
    if (Random.nextBoolean()) Random.nextInt(10).toString
    else ""

  val dates = Array("17.02", "24.02", "1.03", "8.03", "15.03",
    "Lab 1", "Lab 2", "Lab 3", "Lab 4", "Lab 5", "Lab 6", "Lab 7", "Lab 8", "Lab 9", "Lab 10", "Lab 11")
  val students = for (i <- 1 to 50) yield Student(s"Student $i", dates.map((_, randomMark)).toMap)
  val groups = Array("IO-25", "IO-24")
  var group_id = 0
}

class MainActivity extends SActivity { self =>

  private val headerColor = rgb(0xe7, 0xe7, 0xe7)
  private val cellColor = WHITE
  private val borderColor = rgb(0xcc, 0xcc, 0xcc)

  onCreate {

    import StudentDAO._

    val actionBar = getActionBar
    val adapter = SArrayAdapter(android.R.layout.simple_spinner_dropdown_item, groups)
    val mNavListener = new OnNavigationListener {
      def onNavigationItemSelected(position: Int, id: Long) = {
        if (group_id != position) {
          group_id = position
          self.recreate()
        }
        true
      }
    }
    actionBar.setNavigationMode(NAVIGATION_MODE_LIST)
    actionBar.setListNavigationCallbacks(adapter, mNavListener)
    actionBar.setDisplayShowTitleEnabled(false)
    actionBar.setSelectedNavigationItem(group_id)

    contentView = new SVerticalLayout {

      this += new SScrollView {
        this += new SLinearLayout {
          style {
            case t: STextView => t.textColor(BLACK).maxLines(1).padding(20 dip, 10 dip, 20 dip, 10 dip).textSize(18 dip)
          }

          this += new STableLayout {
            style {
              case t: STextView => t.backgroundColor(headerColor).<<.marginRight(1).marginBottom(1).>>
            }

            STextView(groups(group_id))

            for ((student, studentIndex) <- students.zipWithIndex)
              STextView(s"${studentIndex + 1}. ${student.name}")
          }

          this += new SHorizontalScrollView {
            this += new STableLayout {

              // Header row
              this += new STableRow {

                style {
                  case t: STextView =>
                    t.setGravity(Gravity.CENTER_HORIZONTAL)
                    t.backgroundColor(headerColor).<<.marginRight(1).marginBottom(1).>>
                }
                for (date <- dates)
                  STextView(date)
              }

              // Student rows
              for ((student, studentIndex) <- students.zipWithIndex)
                this += new STableRow {
                  style {
                    case t: STextView =>
                      t.setGravity(Gravity.CENTER_HORIZONTAL)
                      t.backgroundColor(cellColor).<<.marginRight(1).marginBottom(1).>>
                  }

                  for (date <- dates)
                    STextView(student.marks.getOrElse(date, ""))
                }
            }
          }
        }
      }
    }.backgroundColor(borderColor)
  }
}
