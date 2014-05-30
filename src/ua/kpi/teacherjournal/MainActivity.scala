package ua.kpi.teacherjournal

import android.app.ActionBar._
import android.content.Intent
import android.view.{Menu, MenuItem}
import android.widget.ShareActionProvider
import org.scaloid.common._
import scala.language.postfixOps
import Journal.{AbsentRecord, EmptyRecord}
import TableFragment.Coord

class MainActivity extends SActivity { self =>
  import Data._

  var tableFragment: TableFragment = _

  override def onCreateOptionsMenu(menu: Menu) = {
    // setup menu
    getMenuInflater.inflate(R.menu.main_activity_actions, menu)

    // setup share button
    val shareProvider = menu.findItem(R.id.action_share).getActionProvider.asInstanceOf[ShareActionProvider]
    shareProvider.setShareIntent(new Intent(Intent.ACTION_SEND).setType("text/*"))

    super.onCreateOptionsMenu(menu)
  }

  def setupActionBar() = {
    val actionBar = getActionBar
    val adapter = SArrayAdapter(android.R.layout.simple_spinner_dropdown_item, courses.map(_.name).toArray)
    val mNavListener = new OnNavigationListener {
      def onNavigationItemSelected(position: Int, id: Long) = {
        if (courseId != position) {
          courseId = position
          sheetId = 0
          tableFragment = TableFragment.update(getFragmentManager, selectedSheet)
        }
        true
      }
    }
    actionBar.setNavigationMode(NAVIGATION_MODE_LIST)
    actionBar.setListNavigationCallbacks(adapter, mNavListener)
    actionBar.setDisplayShowTitleEnabled(false)
    actionBar.setSelectedNavigationItem(courseId)
  }

  /**
   * @param item Not used but required argument
   */
  def startCallOver(item: MenuItem = null) = {
    tableFragment.selectedCellCoord match {
      case Some(Coord(x, _)) =>
        CallOverDialogFragment.show(getFragmentManager, selectedSheet.columns(x),
          selectedSheet.students.map(_.name)) onFinish { answers =>
          for ((answer, rowIndex) <- answers.zipWithIndex) {
            val oldRecord = selectedSheet.students(rowIndex).records(x)
            val newRecord = if (answer) EmptyRecord else AbsentRecord
            if (oldRecord != newRecord && (oldRecord == AbsentRecord || !answer))
              tableFragment.updateCellRecord(Coord(x = x, y = rowIndex), newRecord)
          }
        }
      case None => toast(R.string.choose_column_first)
    }
  }

  /**
   * Exit activity
   * @param item Not used but required argument
   */
  def exit(item: MenuItem = null) = {
    finish()
  }

  onCreate {
    setupActionBar()
    setContentView(R.layout.main_activity)
    tableFragment = TableFragment.update(getFragmentManager, selectedSheet)
    BottomBar.setup(getFragmentManager)
  }
}
