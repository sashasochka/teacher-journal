package ua.kpi.teacherjournal

import org.scaloid.common._
import android.app.ActionBar._
import android.content.Intent
import android.graphics.Color._
import android.view.{Menu, Gravity}
import android.widget.ShareActionProvider
import scala.language.postfixOps

class MainActivity extends SActivity { self =>
  private val headerColor = rgb(0xe7, 0xe7, 0xe7)
  private val cellColor = WHITE
  private val borderColor = rgb(0xcc, 0xcc, 0xcc)

  override def onCreateOptionsMenu(menu: Menu) = {
    // setup menu
    getMenuInflater.inflate(R.menu.main_activity_actions, menu)

    // setup share button
    val shareProvider = menu.findItem(R.id.action_share).getActionProvider.asInstanceOf[ShareActionProvider]
    shareProvider.setShareIntent(new Intent(Intent.ACTION_SEND).setType("text/*"))

    super.onCreateOptionsMenu(menu)
  }

  onCreate {

    import RandData._

    val actionBar = getActionBar
    val adapter = SArrayAdapter(android.R.layout.simple_spinner_dropdown_item, courses.map(_.name).toArray)
    val mNavListener = new OnNavigationListener {
      def onNavigationItemSelected(position: Int, id: Long) = {
        if (courseId != position) {
          courseId = position
          groupId = 0
          self.recreate()
        }
        true
      }
    }
    actionBar.setNavigationMode(NAVIGATION_MODE_LIST)
    actionBar.setListNavigationCallbacks(adapter, mNavListener)
    actionBar.setDisplayShowTitleEnabled(false)
    actionBar.setSelectedNavigationItem(courseId)

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

            STextView(selectedGroup.name)

            for ((student, studentIndex) <- selectedGroup.students.zipWithIndex)
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
                for (column <- selectedGroup.columns)
                  STextView(column)
              }

              // Student rows
              for ((student, studentIndex) <- selectedGroup.students.zipWithIndex)
                this += new STableRow {
                  style {
                    case t: STextView =>
                      t.setGravity(Gravity.CENTER_HORIZONTAL)
                      t.backgroundColor(cellColor).<<.marginRight(1).marginBottom(1).>>
                  }

                  for (record <- student.records)
                    STextView(record.displayString)
                }
            }
          }
        }
      }
    }.backgroundColor(borderColor)
  }
}
